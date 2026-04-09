package com.kuopan.Component;

import com.kuopan.DAO.UserInfoMapper;
import com.kuopan.Entity.UserInfo;
import com.kuopan.Entity.constants.Constants;
import com.kuopan.Entity.dto.SysSettingsDto;
import com.kuopan.Entity.dto.UserSpaceDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component("redisComponent")
public class RedisComponent {
    @Resource
    private RedisTemplate<String, Object> searchAndUseInRedis;

    @Resource
    private UserInfoMapper userInfoMapper;

    public SysSettingsDto getSysSettingDto() {
        SysSettingsDto sysSettingsDto = (SysSettingsDto) searchAndUseInRedis.opsForValue().get(Constants.REDIS_KEY_SYS_SETTINGS);
        if (sysSettingsDto == null) {
            sysSettingsDto = new SysSettingsDto();
            searchAndUseInRedis.opsForValue().set(Constants.REDIS_KEY_SYS_SETTINGS, sysSettingsDto);
        }
        return sysSettingsDto;
    }

    public void saveUserUsedSpace(String userId, UserSpaceDto userSpaceDto) {
        searchAndUseInRedis.opsForValue().set(Constants.REDIS_KEY_USER_SPACE_USE + userId, userSpaceDto, Constants.SEVEN, TimeUnit.DAYS);
    }

    public UserSpaceDto getUserUsedSpace(String userId) {
        UserSpaceDto userSpaceDto = (UserSpaceDto) searchAndUseInRedis.opsForValue().get(Constants.REDIS_KEY_USER_SPACE_USE + userId);
        if (userSpaceDto == null || userSpaceDto.getTotalSpace() == null || userSpaceDto.getTotalSpace() <= 0) {
            userSpaceDto = new UserSpaceDto();

            // 优先从数据库读取（与 user_info.total_space/occu_space 保持一致）
            UserInfo dbUser = userInfoMapper.selectUserSpaceByUserId(userId);
            if (dbUser != null && dbUser.getTotalSpace() != null && dbUser.getTotalSpace() > 0) {
                userSpaceDto.setUseSpace(dbUser.getOccuSpace() == null ? 0L : dbUser.getOccuSpace());
                userSpaceDto.setTotalSpace(dbUser.getTotalSpace());
            } else {
                // 兜底：使用系统默认初始空间
                userSpaceDto.setUseSpace(0L);
                userSpaceDto.setTotalSpace(getSysSettingDto().getUserInitialSpace() * Constants.MB);
            }
            saveUserUsedSpace(userId, userSpaceDto);
        }
        return userSpaceDto;
    }

}
