package com.kuopan.Controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kuopan.Annotation.GlobalInterceptor;
import com.kuopan.DAO.FileInfoMapper;
import com.kuopan.Entity.FileInfo;
import com.kuopan.Entity.dto.SessionWebUserDto;
import com.kuopan.Service.impl.FileInfoServiceImpl;
import com.kuopan.vo.ResponseVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recycle")
public class RecycleController extends BaseController {

    @Resource
    private FileInfoMapper fileInfoMapper;

    // 引入服务层，为了调用归还空间的方法
    @Resource
    private FileInfoServiceImpl fileInfoService;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 加载回收站列表
     */
    @RequestMapping("/loadRecycleList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadRecycleList(HttpSession session) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        LambdaQueryWrapper<FileInfo> query = new LambdaQueryWrapper<>();
        query.eq(FileInfo::getUserId, userDto.getUserId());
        query.eq(FileInfo::getDelFlag, 1); // 1为回收站
        query.orderByDesc(FileInfo::getRecoveryTime);

        List<FileInfo> list = fileInfoMapper.selectList(query);

        List<Map<String, Object>> outList = new ArrayList<>();
        for (FileInfo f : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("fileId", f.getFileId());
            map.put("fileName", f.getFileName());
            map.put("fileSize", f.getFileSize());
            map.put("folderType", f.getFolderType() == null ? 0 : f.getFolderType());
            map.put("recoveryTime", f.getRecoveryTime() != null ? TIME_FMT.format(f.getRecoveryTime()) : "");

            map.put("fileCover", f.getFileCover());
            map.put("fileCategory", f.getFileCategory());
            map.put("filePath", f.getFilePath());

            outList.add(map);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", outList);
        return ResponseVO.success(data);
    }

    /**
     * 还原文件
     */
    @RequestMapping("/recoverFile")
    @GlobalInterceptor(checkLogin = true)
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO recoverFile(HttpSession session, String fileIds) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        String[] idArray = fileIds.split(",");

        LambdaUpdateWrapper<FileInfo> update = new LambdaUpdateWrapper<>();
        update.in(FileInfo::getFileId, Arrays.asList(idArray))
                .eq(FileInfo::getUserId, userDto.getUserId())
                .set(FileInfo::getDelFlag, 2); // 恢复正常状态 2

        fileInfoMapper.update(null, update);
        return ResponseVO.success(null);
    }

    /**
     * 彻底删除文件（调用 service 层方法，同步释放磁盘配额）
     */
    @RequestMapping("/delFile")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delFile(HttpSession session, String fileIds) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        // 👇 调用核心的扣除空间的方法
        fileInfoService.thoroughDeleteFile(userDto.getUserId(), fileIds);
        return ResponseVO.success(null);
    }
}