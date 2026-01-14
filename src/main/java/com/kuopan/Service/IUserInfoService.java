package com.kuopan.Service;

import com.kuopan.Entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * To put in User Infomation 服务类
 * </p>
 *
 * @author Kuo
 * @since 2026-01-13
 */
public interface IUserInfoService extends IService<UserInfo> {
    // To imply the new mapper function.
    boolean changeOccuSpace(String id, Long space);
}
