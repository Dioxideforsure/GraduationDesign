package com.kuopan.Controller;

import com.kuopan.Controller.ResultImpl.Result;
import com.kuopan.Entity.UserInfo;
import com.kuopan.Service.IUserInfoService;
import com.kuopan.Service.impl.UserInfoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * To put in User Infomation 前端控制器
 * </p>
 *
 * @author Kuo
 * @since 2026-01-13
 */
@RestController
@RequestMapping("/userInfo")
public class UserInfoController {

    @Autowired
    private UserInfoServiceImpl userInfoService;

    /*
     * Add users.
     */

}
