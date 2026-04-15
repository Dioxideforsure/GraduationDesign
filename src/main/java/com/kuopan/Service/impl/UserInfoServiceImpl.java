package com.kuopan.Service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kuopan.Component.RedisComponent;
import com.kuopan.Config.AppConfig;
import com.kuopan.Entity.UserInfo;
import com.kuopan.DAO.UserInfoMapper;
import com.kuopan.Entity.dto.SessionWebUserDto;
import com.kuopan.Entity.dto.UserSpaceDto;
import com.kuopan.Exception.BusinessException;
import com.kuopan.Service.EmailCodeService;
import com.kuopan.Service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuopan.Util.SHAUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * User Information 服务实现类
 * </p>
 *
 * @author Kuo
 * @since 2026-01-13
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Autowired
    private EmailCodeService emailCodeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionWebUserDto login(String email, String password) {
        LambdaQueryWrapper<UserInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserInfo::getEmail, email);
        UserInfo user = userInfoMapper.selectOne(lambdaQueryWrapper);

        if (user == null || !user.getPassword().equals(password)) {
            throw new BusinessException("账号或密码错误");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
        sessionWebUserDto.setUserId(user.getUserId());

        sessionWebUserDto.setNickName(user.getNickName());

        sessionWebUserDto.setIsAdmin(false);
        if (ArrayUtils.contains(appConfig.getAdminEmail().split(","), email)) {
            sessionWebUserDto.setIsAdmin(true);
        }

        redisComponent.getUserUsedSpace(user.getUserId());

        return sessionWebUserDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String email, String password, String emailCode) {
        LambdaQueryWrapper<UserInfo> query = new LambdaQueryWrapper<>();
        query.eq(UserInfo::getEmail, email);
        UserInfo user = userInfoMapper.selectOne(query);

        if (user == null) {
            throw new BusinessException("邮箱账号不存在");
        }
        emailCodeService.checkCode(email, emailCode);

        UserInfo updateUser = new UserInfo();
        updateUser.setUserId(user.getUserId());
        updateUser.setPassword(SHAUtil.SHA256Encrypt(password));

        int rows = userInfoMapper.updateById(updateUser);

        if (rows == 0) {
            throw new BusinessException("重置密码失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(SessionWebUserDto userDto, String oldPassword, String password) {
        if (userDto == null) {
            throw new BusinessException("重置密码出现错误，登录可能过期");
        }
        String userID = userDto.getUserId();

        LambdaQueryWrapper<UserInfo> query = new LambdaQueryWrapper<>();
        query.eq(UserInfo::getUserId, userID);
        UserInfo userInfo = userInfoMapper.selectOne(query);

        if (!userInfo.getPassword().equals(oldPassword)) {
            throw new BusinessException(403, "旧密码错误");
        }

        UserInfo updateUser = new UserInfo();
        updateUser.setUserId(userID);
        updateUser.setPassword(password);

        int rows = userInfoMapper.updateById(updateUser);
        if (rows == 0) {
            throw new BusinessException("修改密码失败，请稍后重试");
        }
    }
}