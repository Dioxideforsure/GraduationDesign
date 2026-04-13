package com.kuopan.Service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kuopan.Component.FileProcessTask;
import com.kuopan.Component.RedisComponent;
import com.kuopan.Config.AppConfig;
import com.kuopan.DAO.FileInfoMapper;
import com.kuopan.DAO.UserInfoMapper;
import com.kuopan.Entity.FileInfo;
import com.kuopan.Entity.dto.UserSpaceDto;
import com.kuopan.Exception.BusinessException;
import com.kuopan.Util.FileCategoryUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileInfoServiceImpl {

    private static final int DB_FILE_PATH_MAX = 100;
    private static final int DEL_FLAG_NORMAL = 2;
    private static final int STATUS_OK = 2;
    private static final int STATUS_TRANSFERRING = 0;
    private static final int FOLDER_TYPE_FILE = 0;

    @Resource
    private FileInfoMapper fileInfoMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @Resource
    private FileProcessTask fileProcessTask;

    private static final DateTimeFormatter LIST_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadFile(String userId, String filePid, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("上传文件不能为空");
        if (StringUtils.isBlank(filePid)) filePid = "0";

        Long fileSize = file.getSize();
        UserSpaceDto userSpaceDto = redisComponent.getUserUsedSpace(userId);
        Long useSpace = userSpaceDto.getUseSpace() == null ? 0L : userSpaceDto.getUseSpace();
        Long totalSpace = userSpaceDto.getTotalSpace() == null ? 0L : userSpaceDto.getTotalSpace();

        if (useSpace + fileSize > totalSpace) throw new BusinessException("用户空间不足，请清理后重试");

        String originFileName = file.getOriginalFilename();
        if (originFileName == null) originFileName = "unknown";
        if (originFileName.length() > 200) originFileName = originFileName.substring(0, 200);

        String suffix = FilenameUtils.getExtension(originFileName);
        String fileMd5 = calculateFileMd5(file);

        FileInfo fastHit = findFastUploadCandidate(fileMd5, fileSize);
        if (fastHit != null) {
            return saveByFastUpload(userId, filePid, originFileName, fileSize, suffix, fileMd5, fastHit.getFilePath(), fastHit.getFileCover(), userSpaceDto, useSpace);
        }

        String fileId = RandomStringUtils.randomAlphanumeric(10);
        String localName = StringUtils.isBlank(suffix) ? fileId : fileId + "." + suffix;
        String relativePath = "file/" + userId + "/" + LocalDate.now() + "/" + localName;

        File dest = new File(appConfig.getProjectFolder() + relativePath);
        File parent = dest.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) throw new BusinessException("创建上传目录失败");

        try { file.transferTo(dest); } catch (IOException e) { throw new BusinessException("文件保存失败，请稍后重试"); }

        try {
            LocalDateTime now = LocalDateTime.now();
            int[] catType = FileCategoryUtil.resolveFromExtension(suffix);
            int status = (catType[0] == 1) ? STATUS_TRANSFERRING : STATUS_OK;

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileId(fileId);
            fileInfo.setUserId(userId);
            fileInfo.setFileMd5(fileMd5);
            fileInfo.setFilePid(filePid);
            fileInfo.setFileName(originFileName);
            fileInfo.setFilePath(relativePath);
            fileInfo.setFileCover(null);
            fileInfo.setFileSize(fileSize);
            fileInfo.setFolderType(FOLDER_TYPE_FILE);
            fileInfo.setFileCategory(catType[0]);
            fileInfo.setFileType(catType[1]);
            fileInfo.setStatus(status);
            fileInfo.setRecoveryTime(null);
            fileInfo.setDelFlag(DEL_FLAG_NORMAL);
            fileInfo.setCreateTime(now);
            fileInfo.setLastUpdateTime(now);

            fileInfoMapper.insert(fileInfo);
            userInfoMapper.updateRemainSpaceByGUID(userId, fileSize);

            userSpaceDto.setUseSpace(useSpace + fileSize);
            redisComponent.saveUserUsedSpace(userId, userSpaceDto);

            if (status == STATUS_TRANSFERRING) fileProcessTask.processFile(fileInfo);

            Map<String, Object> result = new HashMap<>();
            result.put("fileId", fileId);
            result.put("fileName", originFileName);
            result.put("fileSize", fileSize);
            result.put("filePath", relativePath);
            result.put("fileCategory", catType[0]);
            result.put("fileType", catType[1]);
            return result;
        } catch (Exception e) {
            if (dest.exists()) dest.delete();
            throw new BusinessException("上传失败，请稍后重试");
        }
    }

    public List<Map<String, Object>> listUserFiles(String userId, String filePid, String fileNameFuzzy, String categoryKey) {
        LambdaQueryWrapper<FileInfo> w = new LambdaQueryWrapper<>();
        w.eq(FileInfo::getUserId, userId);
        w.eq(FileInfo::getDelFlag, DEL_FLAG_NORMAL);

        if (StringUtils.isBlank(filePid)) filePid = "0";
        w.eq(FileInfo::getFilePid, filePid);

        if (StringUtils.isNotBlank(fileNameFuzzy)) w.like(FileInfo::getFileName, fileNameFuzzy);
        Integer cat = resolveCategoryFilter(categoryKey);
        if (cat != null) w.eq(FileInfo::getFileCategory, cat);
        w.orderByDesc(FileInfo::getFolderType).orderByDesc(FileInfo::getLastUpdateTime);

        List<FileInfo> rows = fileInfoMapper.selectList(w);
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (FileInfo f : rows) {
            Map<String, Object> row = new HashMap<>();
            row.put("fileId", f.getFileId());
            row.put("fileName", f.getFileName());
            row.put("fileSize", f.getFileSize());
            row.put("folderType", f.getFolderType() == null ? FOLDER_TYPE_FILE : f.getFolderType());
            row.put("status", f.getStatus());
            row.put("filePath", f.getFilePath());
            row.put("fileCategory", f.getFileCategory());
            row.put("fileCover", f.getFileCover());
            row.put("filePid", f.getFilePid());

            LocalDateTime t = f.getLastUpdateTime() != null ? f.getLastUpdateTime() : f.getCreateTime();
            row.put("updateTime", t == null ? "" : LIST_TIME_FMT.format(t));
            out.add(row);
        }
        return out;
    }

    public Map<String, Object> uploadChunk(String userId, String fileMd5, Integer chunkIndex, Integer chunks, MultipartFile chunk) {
        String tmpRelativeDir = "file/chunks/" + userId + "/" + fileMd5 + "/";
        File dir = new File(appConfig.getProjectFolder() + tmpRelativeDir);
        if (!dir.exists() && !dir.mkdirs()) throw new BusinessException("创建分片目录失败");
        File part = new File(dir, chunkIndex + ".part");
        try { chunk.transferTo(part); } catch (IOException e) { throw new BusinessException("保存分片失败"); }
        Map<String, Object> r = new HashMap<>();
        r.put("chunkIndex", chunkIndex);
        r.put("chunks", chunks);
        r.put("uploaded", true);
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> mergeChunks(String userId, String filePid, String fileMd5, String fileName, Long fileSize, Integer chunks) {
        if (StringUtils.isBlank(filePid)) filePid = "0";
        FileInfo fastHit = findFastUploadCandidate(fileMd5, fileSize);
        if (fastHit != null) {
            return saveByFastUpload(userId, filePid, fileName, fileSize, FilenameUtils.getExtension(fileName), fileMd5, fastHit.getFilePath(), fastHit.getFileCover(), redisComponent.getUserUsedSpace(userId), redisComponent.getUserUsedSpace(userId).getUseSpace());
        }

        UserSpaceDto userSpaceDto = redisComponent.getUserUsedSpace(userId);
        Long useSpace = userSpaceDto.getUseSpace() == null ? 0L : userSpaceDto.getUseSpace();
        Long totalSpace = userSpaceDto.getTotalSpace() == null ? 0L : userSpaceDto.getTotalSpace();
        if (useSpace + fileSize > totalSpace) throw new BusinessException("用户空间不足，请清理后重试");

        String suffix = FilenameUtils.getExtension(fileName);
        String fileId = RandomStringUtils.randomAlphanumeric(10);
        String localName = StringUtils.isBlank(suffix) ? fileId : fileId + "." + suffix;
        String relativePath = "file/" + userId + "/" + LocalDate.now() + "/" + localName;

        String tmpRelativeDir = "file/chunks/" + userId + "/" + fileMd5 + "/";
        File dir = new File(appConfig.getProjectFolder() + tmpRelativeDir);
        File dest = new File(appConfig.getProjectFolder() + relativePath);
        File parent = dest.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) throw new BusinessException("创建上传目录失败");

        try (java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(dest))) {
            for (int i = 0; i < chunks; i++) {
                File part = new File(dir, i + ".part");
                try (java.io.InputStream in = new java.io.BufferedInputStream(new java.io.FileInputStream(part))) {
                    byte[] buf = new byte[1024 * 1024]; int len;
                    while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
                }
                part.delete();
            }
            dir.delete();
        } catch (Exception e) {
            if (dest.exists()) dest.delete();
            throw new BusinessException("合并失败，请稍后重试");
        }

        LocalDateTime now = LocalDateTime.now();
        int[] catType = FileCategoryUtil.resolveFromExtension(suffix);
        int status = (catType[0] == 1) ? STATUS_TRANSFERRING : STATUS_OK;

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(fileId);
        fileInfo.setUserId(userId);
        fileInfo.setFileMd5(fileMd5);
        fileInfo.setFilePid(filePid);
        fileInfo.setFileName(fileName);
        fileInfo.setFilePath(relativePath);
        fileInfo.setFileSize(fileSize);
        fileInfo.setFolderType(FOLDER_TYPE_FILE);
        fileInfo.setFileCategory(catType[0]);
        fileInfo.setFileType(catType[1]);
        fileInfo.setStatus(status);
        fileInfo.setDelFlag(DEL_FLAG_NORMAL);
        fileInfo.setCreateTime(now);
        fileInfo.setLastUpdateTime(now);

        fileInfoMapper.insert(fileInfo);
        userInfoMapper.updateRemainSpaceByGUID(userId, fileSize);
        userSpaceDto.setUseSpace(useSpace + fileSize);
        redisComponent.saveUserUsedSpace(userId, userSpaceDto);

        if (status == STATUS_TRANSFERRING) fileProcessTask.processFile(fileInfo);

        Map<String, Object> r = new HashMap<>();
        r.put("fileId", fileId);
        r.put("merged", true);
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> newFolder(String userId, String filePid, String folderName) {
        if (StringUtils.isBlank(folderName)) throw new BusinessException("文件夹名称不能为空");
        if (StringUtils.isBlank(filePid)) filePid = "0";

        Long count = fileInfoMapper.selectCount(new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getFilePid, filePid)
                .eq(FileInfo::getUserId, userId)
                .eq(FileInfo::getFileName, folderName)
                .eq(FileInfo::getFolderType, 1)
                .eq(FileInfo::getDelFlag, DEL_FLAG_NORMAL));
        if (count > 0) throw new BusinessException("该目录下已存在同名文件夹");

        LocalDateTime now = LocalDateTime.now();
        FileInfo folder = new FileInfo();

        folder.setFileId(RandomStringUtils.randomAlphanumeric(10));
        folder.setUserId(userId);
        folder.setFilePid(filePid);
        folder.setFileName(folderName);
        folder.setFolderType(1); // 文件夹类型为1
        folder.setStatus(STATUS_OK);
        folder.setDelFlag(DEL_FLAG_NORMAL);
        folder.setCreateTime(now);
        folder.setLastUpdateTime(now);

        fileInfoMapper.insert(folder);

        Map<String, Object> res = new HashMap<>();
        res.put("fileId", folder.getFileId());
        return res;
    }

    // 将文件移入回收站
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(String userId, String fileIds) {
        if (StringUtils.isBlank(fileIds)) throw new BusinessException("请选择文件");
        String[] idArray = fileIds.split(",");
        LambdaUpdateWrapper<FileInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(FileInfo::getFileId, java.util.Arrays.asList(idArray))
                .eq(FileInfo::getUserId, userId)
                .set(FileInfo::getDelFlag, 1) // 移入回收站状态为1
                .set(FileInfo::getRecoveryTime, LocalDateTime.now());
        fileInfoMapper.update(null, updateWrapper);
    }

    // 彻底删除文件，并返还用户网盘空间

    @Transactional(rollbackFor = Exception.class)
    public void thoroughDeleteFile(String userId, String fileIds) {
        if (StringUtils.isBlank(fileIds)) throw new BusinessException("请选择要彻底删除的文件");
        String[] idArray = fileIds.split(",");

        // 1. 查询要彻底删除的文件的总大小（只计算真实文件，因为文件夹大小为0）
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getUserId, userId)
                .in(FileInfo::getFileId, java.util.Arrays.asList(idArray))
                .eq(FileInfo::getFolderType, FOLDER_TYPE_FILE);

        List<FileInfo> fileList = fileInfoMapper.selectList(queryWrapper);
        long totalSize = 0L;
        for (FileInfo file : fileList) {
            totalSize += file.getFileSize() == null ? 0L : file.getFileSize();
        }

        // 2. 将数据库中的文件标记为彻底删除 (delFlag = 0)
        LambdaUpdateWrapper<FileInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(FileInfo::getFileId, java.util.Arrays.asList(idArray))
                .eq(FileInfo::getUserId, userId)
                .set(FileInfo::getDelFlag, 0);
        fileInfoMapper.update(null, updateWrapper);

        // 3. 归还空间：扣除 MySQL 中的占用的空间，并同步到 Redis
        if (totalSize > 0) {
            userInfoMapper.decreaseOccuSpaceByUserId(userId, totalSize);
            UserSpaceDto userSpaceDto = redisComponent.getUserUsedSpace(userId);
            Long useSpace = userSpaceDto.getUseSpace() == null ? 0L : userSpaceDto.getUseSpace();
            // 防止扣成负数，最小为0
            userSpaceDto.setUseSpace(Math.max(useSpace - totalSize, 0L));
            redisComponent.saveUserUsedSpace(userId, userSpaceDto);
        }
    }
    // ==========================================

    private Integer resolveCategoryFilter(String category) {
        if (StringUtils.isBlank(category) || "all".equalsIgnoreCase(category.trim())) return null;
        switch (category.trim().toLowerCase()) {
            case "video": return 1;
            case "music": return 2;
            case "image": return 3;
            case "doc": return 4;
            case "others": return 5;
            default: return null;
        }
    }

    private String calculateFileMd5(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtils.md5Hex(inputStream);
        } catch (Exception e) { throw new BusinessException("读取摘要失败"); }
    }

    private FileInfo findFastUploadCandidate(String fileMd5, Long fileSize) {
        LambdaQueryWrapper<FileInfo> query = new LambdaQueryWrapper<>();
        query.eq(FileInfo::getFileMd5, fileMd5).eq(FileInfo::getFileSize, fileSize)
                .eq(FileInfo::getFolderType, FOLDER_TYPE_FILE).eq(FileInfo::getDelFlag, DEL_FLAG_NORMAL)
                .eq(FileInfo::getStatus, STATUS_OK).last("limit 1");
        FileInfo exist = fileInfoMapper.selectOne(query);
        if (exist == null || StringUtils.isBlank(exist.getFilePath())) return null;
        return new File(appConfig.getProjectFolder() + exist.getFilePath()).exists() ? exist : null;
    }

    private Map<String, Object> saveByFastUpload(String userId, String filePid, String originFileName, Long fileSize, String suffix, String fileMd5, String relativePath, String fileCover, UserSpaceDto userSpaceDto, Long useSpace) {
        if (StringUtils.isBlank(filePid)) filePid = "0";
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(RandomStringUtils.randomAlphanumeric(10));
        fileInfo.setUserId(userId);
        fileInfo.setFileMd5(fileMd5);
        fileInfo.setFilePid(filePid);
        fileInfo.setFileName(originFileName);
        fileInfo.setFilePath(relativePath);
        fileInfo.setFileCover(fileCover);
        fileInfo.setFileSize(fileSize);
        fileInfo.setFolderType(FOLDER_TYPE_FILE);
        int[] catType = FileCategoryUtil.resolveFromExtension(suffix);
        fileInfo.setFileCategory(catType[0]);
        fileInfo.setFileType(catType[1]);
        fileInfo.setStatus(STATUS_OK);
        fileInfo.setDelFlag(DEL_FLAG_NORMAL);
        fileInfo.setCreateTime(LocalDateTime.now());
        fileInfo.setLastUpdateTime(LocalDateTime.now());
        fileInfoMapper.insert(fileInfo);

        userInfoMapper.updateRemainSpaceByGUID(userId, fileSize);
        userSpaceDto.setUseSpace(useSpace + fileSize);
        redisComponent.saveUserUsedSpace(userId, userSpaceDto);

        Map<String, Object> result = new HashMap<>();
        result.put("fileId", fileInfo.getFileId());
        result.put("instantUpload", true);
        return result;
    }

    public boolean checkFileExists(String fileMd5, Long fileSize) {
        return findFastUploadCandidate(fileMd5, fileSize) != null;
    }
}