package com.kuopan.Aspect;

import com.kuopan.Annotation.GlobalInterceptor;
import com.kuopan.Annotation.VerifyParams;
import com.kuopan.Entity.constants.Constants;
import com.kuopan.Entity.dto.SessionWebUserDto;
import com.kuopan.Entity.enums.ResponseCodeEnum;
import com.kuopan.Exception.BusinessException;
import com.kuopan.Util.StringUtil;
import com.kuopan.Util.VerifyUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component("GlobalOperationAspect")
public class GlobalOperationAspect {

    private static final String TYPE_STRING = "java.lang.String";
    private static final String TYPE_INTEGER = "java.lang.Integer";
    private static final String TYPE_LONG = "java.lang.Long";


    @Pointcut("@annotation(com.kuopan.Annotation.GlobalInterceptor)")
    private void requestInterceptor() {

    }

    @Before("requestInterceptor()")
    public void interceptorDo(JoinPoint jp) throws BusinessException {
        try {
            Object target = jp.getTarget();
            Object[] args = jp.getArgs();
            String methodName = jp.getSignature().getName();
            Class<?>[] paramTypes = ((MethodSignature) jp.getSignature()).getMethod().getParameterTypes();
            Method method = target.getClass().getMethod(methodName, paramTypes);
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);

            if (interceptor == null) {
                return;
            }
            // Validate Login and Admin Identity
            if (interceptor.checkLogin() || interceptor.checkAdmin()) {
                checkLogin(interceptor.checkAdmin());
            }

            // Validate Parameters
            if (interceptor.checkParams()) {
                validateParams(method, args);
            }
        } catch (BusinessException e) {
            log.error("全局拦截器异常：", e);
            throw e;
        } catch (Exception e) {
            log.error("全局拦截器异常：", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        } catch (Throwable throwable) {
            log.error("全局拦截器异常：", throwable);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    private void validateParams(Method m, Object[] args) throws BusinessException{
        Parameter[] parameters = m.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object value = args[i];
            VerifyParams verifyParams = parameter.getAnnotation(VerifyParams.class);
            if (verifyParams == null) {
                continue;
            }
            // Basic data type.
            if (TYPE_STRING.equals(parameter.getParameterizedType().getTypeName()) || TYPE_LONG.equals(parameter.getParameterizedType().getTypeName()) || TYPE_INTEGER.equals(parameter.getParameterizedType().getTypeName())) {
                checkValue(value, verifyParams);
            } else {
                checkObjectValue(parameter, value);
            }
        }
    }

    private void checkObjectValue(Parameter p, Object v) {
        try {
            String typeName = p.getParameterizedType().getTypeName();
            Class c = Class.forName(typeName);
            Field[] f = c.getDeclaredFields();
            for (int i = 0; i < f.length; i++) {
                VerifyParams fV = f[i].getAnnotation(VerifyParams.class);
                if (fV == null) {
                    continue;
                }
                f[i].setAccessible(true);
                Object r = f[i].get(v);
                checkValue(r, fV);
            }

        } catch (BusinessException e) {
            log.error("参数校验失败：", e);
            throw e;
        } catch (Exception e) {
            log.error("参数校验失败：", e);
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    private void checkValue(Object v, VerifyParams vp) throws BusinessException {
        Boolean isEmpty = v == null || StringUtil.isEmpty(v.toString());
        Integer l = v == null ? 0 : v.toString().length();

        /**
         * Validate empty
         */
        if (isEmpty && vp.required()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        /*
        * Validate length
        */
        if (!isEmpty && (
                (vp.max() != -1 && l > vp.max()) ||
                        (vp.min() != -1 && l < vp.min())
        )) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        /**
         * Validate Regex
         */
        if (!isEmpty && !StringUtil.isEmpty(vp.regex().getRegex()) && !VerifyUtil.verify(vp.regex(), String.valueOf(v))) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    private void checkLogin(Boolean checkAdmin) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        SessionWebUserDto userDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);

        if (userDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }

        if (checkAdmin && !userDto.getIsAdmin()) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }
}
