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

    /**
     * 👇 修复：方法名恢复为 getSysSettingDto (不带 s)
     */
    public SysSettingsDto getSysSettingDto() {
        SysSettingsDto sysSettingsDto = (SysSettingsDto) searchAndUseInRedis.opsForValue().get(Constants.REDIS_KEY_SYS_SETTING);
        if (sysSettingsDto == null) {
            sysSettingsDto = new SysSettingsDto();
            searchAndUseInRedis.opsForValue().set(Constants.REDIS_KEY_SYS_SETTING, sysSettingsDto);
        }
        return sysSettingsDto;
    }

    public void saveSysSettingsDto(SysSettingsDto sysSettingsDto) {
        searchAndUseInRedis.opsForValue().set(Constants.REDIS_KEY_SYS_SETTING, sysSettingsDto);
    }

    public void saveUserUsedSpace(String userId, UserSpaceDto userSpaceDto) {
        searchAndUseInRedis.opsForValue().set(Constants.REDIS_KEY_USER_SPACE_USE + userId, userSpaceDto, Constants.SEVEN, TimeUnit.DAYS);
    }

    public UserSpaceDto getUserUsedSpace(String userId) {
        UserSpaceDto userSpaceDto = (UserSpaceDto) searchAndUseInRedis.opsForValue().get(Constants.REDIS_KEY_USER_SPACE_USE + userId);
        if (userSpaceDto == null || userSpaceDto.getTotalSpace() == null || userSpaceDto.getTotalSpace() <= 0) {
            userSpaceDto = new UserSpaceDto();
            UserInfo dbUser = userInfoMapper.selectUserSpaceByUserId(userId);
            if (dbUser != null && dbUser.getTotalSpace() != null && dbUser.getTotalSpace() > 0) {
                userSpaceDto.setUseSpace(dbUser.getOccuSpace() == null ? 0L : dbUser.getOccuSpace());
                userSpaceDto.setTotalSpace(dbUser.getTotalSpace());
            } else {
                userSpaceDto.setUseSpace(0L);
                // 调用修复后的方法名
                long initSpaceBytes = (long) getSysSettingDto().getUserInitUseSpace() * Constants.MB;
                userSpaceDto.setTotalSpace(initSpaceBytes);
            }
            saveUserUsedSpace(userId, userSpaceDto);
        }
        return userSpaceDto;
    }
}