package com.kuopan.Service.impl;

import com.kuopan.Entity.UserInfo;
import com.kuopan.DAO.UserInfoMapper;
import com.kuopan.Service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * To put in User Infomation 服务实现类
 * </p>
 *
 * @author Kuo
 * @since 2026-01-13
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    @Override
    public boolean changeOccuSpace(String id, Long space) {
        return baseMapper.updateRemainSpaceByGUID(id, space) > 0;
    }
}
