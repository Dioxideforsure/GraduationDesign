package com.kuopan.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kuopan.Annotation.GlobalInterceptor;
import com.kuopan.DAO.FileInfoMapper;
import com.kuopan.DAO.FileShareMapper;
import com.kuopan.Entity.FileInfo;
import com.kuopan.Entity.FileShare;
import com.kuopan.Entity.dto.SessionWebUserDto;
import com.kuopan.vo.ResponseVO;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/share")
public class ShareController extends BaseController {

    @Resource
    private FileShareMapper fileShareMapper;

    @Resource
    private FileInfoMapper fileInfoMapper;

    // 1. 创建分享链接
    @RequestMapping("/shareFile")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO shareFile(HttpSession session, String fileId, Integer validType, String code) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);

        FileShare share = new FileShare();
        share.setShareId(RandomStringUtils.randomAlphanumeric(15));
        share.setFileId(fileId);
        share.setUserId(userDto.getUserId());
        share.setValidType(validType);
        // 如果没有填提取码，自动生成5位随机数
        share.setCode(StringUtils.isBlank(code) ? RandomStringUtils.randomAlphanumeric(5) : code);
        share.setShareTime(new Date());
        share.setShowCount(0);

        // 计算过期时间
        if (validType != 3) {
            Calendar calendar = Calendar.getInstance();
            if (validType == 0) calendar.add(Calendar.DAY_OF_MONTH, 1);
            else if (validType == 1) calendar.add(Calendar.DAY_OF_MONTH, 7);
            else if (validType == 2) calendar.add(Calendar.DAY_OF_MONTH, 30);
            share.setExpireTime(calendar.getTime());
        }

        fileShareMapper.insert(share);
        return ResponseVO.success(share);
    }

    // 2. 加载真实的分享列表
    @RequestMapping("/loadShareList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadShareList(HttpSession session) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);

        QueryWrapper<FileShare> query = new QueryWrapper<>();
        query.eq("user_id", userDto.getUserId());
        query.orderByDesc("share_time");
        List<FileShare> shareList = fileShareMapper.selectList(query);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> result = new ArrayList<>();

        for (FileShare s : shareList) {
            Map<String, Object> map = new HashMap<>();
            map.put("shareId", s.getShareId());
            map.put("shareTime", s.getShareTime() != null ? sdf.format(s.getShareTime()) : "");
            map.put("expireTime", s.getExpireTime() != null ? sdf.format(s.getExpireTime()) : "永久有效");
            map.put("code", s.getCode());
            map.put("browseCount", s.getShowCount());

            // 联表查出对应的文件信息（名字、缩略图等）
            FileInfo f = fileInfoMapper.selectById(s.getFileId());
            if (f != null) {
                map.put("fileName", f.getFileName());
                map.put("folderType", f.getFolderType());
                map.put("fileCategory", f.getFileCategory());
                map.put("fileType", f.getFileType());
                map.put("fileCover", f.getFileCover());
                map.put("filePath", f.getFilePath());
            } else {
                map.put("fileName", "该文件已被删除");
            }
            result.add(map);
        }
        return ResponseVO.success(result);
    }

    // 3. 批量/单个取消分享
    @RequestMapping("/cancelShare")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO cancelShare(HttpSession session, String shareIds) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        String[] ids = shareIds.split(",");

        QueryWrapper<FileShare> query = new QueryWrapper<>();
        query.eq("user_id", userDto.getUserId());
        query.in("share_id", Arrays.asList(ids));

        fileShareMapper.delete(query);
        return ResponseVO.success(null);
    }
}