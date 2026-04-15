package com.kuopan.Controller;

import com.kuopan.DAO.FileShareMapper;
import com.kuopan.DAO.UserInfoMapper;
import com.kuopan.Entity.FileShare;
import com.kuopan.Entity.UserInfo;
import com.kuopan.Exception.BusinessException;
import com.kuopan.vo.ResponseVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/showShare")
public class WebShareController extends BaseController {

    @Resource
    private FileShareMapper fileShareMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 获取分享人信息（用于在提取码页面展示头像是谁分享的）
     */
    @RequestMapping("/getShareInfo")
    public ResponseVO getShareInfo(String shareId) {
        FileShare share = fileShareMapper.selectById(shareId);
        if (share == null) {
            throw new BusinessException("分享链接不存在或已被取消");
        }
        if (share.getExpireTime() != null && new Date().after(share.getExpireTime())) {
            throw new BusinessException("分享链接已过期");
        }

        UserInfo user = userInfoMapper.selectById(share.getUserId());
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("shareId", shareId);
        resultMap.put("nickName", user != null ? user.getNickName() : "未知用户");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        resultMap.put("shareTime", share.getShareTime() != null ? sdf.format(share.getShareTime()) : "");

        return ResponseVO.success(resultMap);
    }

    /**
     * 校验提取码
     */
    @RequestMapping("/checkShareCode")
    public ResponseVO checkShareCode(HttpSession session, String shareId, String code) {
        FileShare share = fileShareMapper.selectById(shareId);
        if (share == null || (share.getExpireTime() != null && new Date().after(share.getExpireTime()))) {
            throw new BusinessException("分享链接不存在或已过期");
        }
        if (!share.getCode().equals(code)) {
            throw new BusinessException("提取码错误，请重新输入");
        }

        // 校验成功，把解锁状态存入 session（后续获取文件列表时需要用到）
        session.setAttribute("share_check_" + shareId, true);
        return ResponseVO.success(null);
    }
}