package com.kuopan.Component;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kuopan.Config.AppConfig;
import com.kuopan.DAO.FileInfoMapper;
import com.kuopan.Entity.FileInfo;
import com.kuopan.Util.FfmpegUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

@Component
public class FileProcessTask {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessTask.class);

    @Resource
    private AppConfig appConfig;
    @Resource
    private FileInfoMapper fileInfoMapper;

    @Async // 标注为异步任务
    public void processFile(FileInfo fileInfo) {
        String fileId = fileInfo.getFileId();
        String originalFilePath = appConfig.getProjectFolder() + fileInfo.getFilePath();

        try {
            // 视频文件分类为 1 (根据你的 FileCategoryUtil 判断)
            if (fileInfo.getFileCategory() != null && fileInfo.getFileCategory() == 1) {
                logger.info("开始处理视频文件: {}", fileInfo.getFileName());

                // 1. 准备封面路径: file/userId/date/uuid.png
                String coverRelativePath = fileInfo.getFilePath().replace("." + FilenameUtils.getExtension(fileInfo.getFilePath()), ".png");
                String coverAbsolutePath = appConfig.getProjectFolder() + coverRelativePath;

                // 生成封面
                FfmpegUtils.createCover(originalFilePath, coverAbsolutePath);

                // 2. 准备切片目录: file/userId/date/uuid/
                String tsFolderRelative = fileInfo.getFilePath().substring(0, fileInfo.getFilePath().lastIndexOf(".")) + "/";
                String tsFolderAbsolute = appConfig.getProjectFolder() + tsFolderRelative;
                File tsFolder = new File(tsFolderAbsolute);
                if (!tsFolder.exists()) {
                    tsFolder.mkdirs();
                }

                // 视频切片
                FfmpegUtils.transferToM3u8(originalFilePath, tsFolderAbsolute);

                // 3. 处理完成，更新数据库状态为成功 (status = 2)
                LambdaUpdateWrapper<FileInfo> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(FileInfo::getFileId, fileId)
                        .set(FileInfo::getFileCover, coverRelativePath)
                        .set(FileInfo::getStatus, 2);
                fileInfoMapper.update(null, updateWrapper);

                logger.info("视频文件处理成功: {}", fileInfo.getFileName());
            }
            // 如果是图片 (fileCategory == 3)，也可以在这里用 Thumbnailator 生成缩略图
        } catch (Exception e) {
            logger.error("文件处理失败", e);
            // 处理失败，更新状态 (status = 1)
            LambdaUpdateWrapper<FileInfo> failWrapper = new LambdaUpdateWrapper<>();
            failWrapper.eq(FileInfo::getFileId, fileId).set(FileInfo::getStatus, 1);
            fileInfoMapper.update(null, failWrapper);
        }
    }
}