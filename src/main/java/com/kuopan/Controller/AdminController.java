package com.kuopan.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kuopan.Annotation.GlobalInterceptor;
import com.kuopan.Component.RedisComponent;
import com.kuopan.DAO.FileInfoMapper;
import com.kuopan.DAO.UserInfoMapper;
import com.kuopan.Entity.FileInfo;
import com.kuopan.Entity.UserInfo;
import com.kuopan.Entity.constants.Constants;
import com.kuopan.Entity.dto.SysSettingsDto;
import com.kuopan.Entity.dto.UserSpaceDto;
import com.kuopan.Exception.BusinessException;
import com.kuopan.Service.impl.FileInfoServiceImpl;
import com.kuopan.Util.SHAUtil;
import com.kuopan.vo.ResponseVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Date;

@RestController
@RequestMapping("/admin")
public class AdminController extends BaseController {

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private FileInfoMapper fileInfoMapper;
    @Resource
    private FileInfoServiceImpl fileInfoService;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @RequestMapping("/getSysSettings")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getSysSettings() {
        return ResponseVO.success(redisComponent.getSysSettingDto());
    }

    @RequestMapping("/saveSysSettings")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO saveSysSettings(String registerEmailTitle, String registerEmailContent, Integer userInitUseSpace) {
        SysSettingsDto dto = new SysSettingsDto();
        dto.setEmailTitle(registerEmailTitle);
        dto.setEmailContent(registerEmailContent);
        dto.setUserInitUseSpace(userInitUseSpace);
        redisComponent.saveSysSettingsDto(dto);
        return ResponseVO.success(null);
    }

    @RequestMapping("/loadUserList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadUserList(String nickNameFuzzy) {
        QueryWrapper<UserInfo> query = new QueryWrapper<>();
        if (StringUtils.isNotBlank(nickNameFuzzy)) {
            query.like("user_name", nickNameFuzzy);
        }
        query.orderByDesc("reg_time");

        List<UserInfo> list = userInfoMapper.selectList(query);
        for(UserInfo u : list) {
            u.setUseSpace(u.getOccuSpace() == null ? 0L : u.getOccuSpace());
        }
        return ResponseVO.success(list);
    }

    // 👇 新增：后台手动添加用户
    @RequestMapping("/addUser")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO addUser(String nickName, String email, String password, Integer role) {
        QueryWrapper<UserInfo> query = new QueryWrapper<>();
        query.eq("email", email);
        if (userInfoMapper.selectCount(query) > 0) {
            throw new BusinessException("该邮箱已被注册！");
        }

        UserInfo user = new UserInfo();
        user.setUserId(UUID.randomUUID().toString()); // 生成唯一ID
        user.setNickName(nickName);
        user.setEmail(email);
        user.setPassword(SHAUtil.SHA256Encrypt(password)); // 密码加密入库
        user.setJoinTime(new Date());
        user.setStatus(1); // 默认启用
        user.setRole(role != null ? role : 2); // 默认分配为学生

        long initSpaceBytes = (long) redisComponent.getSysSettingDto().getUserInitUseSpace() * Constants.MB;
        user.setTotalSpace(initSpaceBytes);
        user.setOccuSpace(0L);

        userInfoMapper.insert(user);
        return ResponseVO.success(null);
    }


    @RequestMapping("/delUser")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delUser(String userId) {
        UserInfo user = userInfoMapper.selectById(userId);
        if (user != null && user.getRole() != null && user.getRole() == 0) {
            throw new BusinessException("越权操作：禁止删除管理员账号！");
        }
        userInfoMapper.deleteById(userId);
        return ResponseVO.success(null);
    }

    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO updateUserStatus(String userId, Integer status) {
        UserInfo dbUser = userInfoMapper.selectById(userId);
        if (dbUser != null && dbUser.getRole() != null && dbUser.getRole() == 0) {
            throw new BusinessException("越权操作：禁止修改管理员的登录状态！");
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setStatus(status);
        userInfoMapper.updateById(userInfo);
        return ResponseVO.success(null);
    }

    @RequestMapping("/updateUserSpace")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO updateUserSpace(String userId, Integer changeSpace) {
        UserInfo dbUser = userInfoMapper.selectById(userId);
        if (dbUser != null && dbUser.getRole() != null && dbUser.getRole() == 0) {
            throw new BusinessException("越权操作：禁止修改管理员的网盘空间！");
        }

        Long newTotalSpace = changeSpace * Constants.MB;
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setTotalSpace(newTotalSpace);
        userInfoMapper.updateById(userInfo);

        UserSpaceDto spaceDto = redisComponent.getUserUsedSpace(userId);
        spaceDto.setTotalSpace(newTotalSpace);
        redisComponent.saveUserUsedSpace(userId, spaceDto);

        return ResponseVO.success(null);
    }

    // ... 下方 File 管理相关代码保持不变
    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadFileList(String fileNameFuzzy) {
        QueryWrapper<FileInfo> query = new QueryWrapper<>();
        if (StringUtils.isNotBlank(fileNameFuzzy)) {
            query.like("file_name", fileNameFuzzy);
        }
        query.ne("del_flag", 0);
        query.orderByDesc("last_update_time");

        List<FileInfo> list = fileInfoMapper.selectList(query);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (FileInfo f : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("fileId", f.getFileId());
            map.put("userId", f.getUserId());
            map.put("fileName", f.getFileName());
            map.put("fileSize", f.getFileSize());
            map.put("folderType", f.getFolderType());
            map.put("updateTime", f.getLastUpdateTime() != null ? TIME_FMT.format(f.getLastUpdateTime()) : "");
            map.put("fileCover", f.getFileCover());
            map.put("filePath", f.getFilePath());
            map.put("fileCategory", f.getFileCategory());

            UserInfo u = userInfoMapper.selectById(f.getUserId());
            map.put("nickName", u != null ? u.getNickName() : "未知用户");
            resultList.add(map);
        }
        return ResponseVO.success(resultList);
    }

    @RequestMapping("/delFile")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delFile(String fileId, String userId) {
        fileInfoService.thoroughDeleteFile(userId, fileId);
        return ResponseVO.success(null);
    }
}