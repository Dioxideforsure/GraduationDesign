package com.kuopan.Entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("file_info")
public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId("file_id")
    private String fileId;

    @TableField("user_id")
    private String userId;

    /** 秒传、去重预留（可选） */
    @TableField("file_md5")
    private String fileMd5;

    /** 目录树父级，根目录可为 null；秒传/分片业务流程预留 */
    @TableField("file_pid")
    private String filePid;

    @TableField("file_size")
    private Long fileSize;

    @TableField("file_name")
    private String fileName;

    /** 缩略图/封面预留 */
    @TableField("file_cover")
    private String fileCover;

    /** 相对 project.folder 的存储路径，满足库字段长度 */
    @TableField("file_path")
    private String filePath;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("last_update_time")
    private LocalDateTime lastUpdateTime;

    /** 0: 文件 1: 目录 */
    @TableField("folder_type")
    private Integer folderType;

    @TableField("file_category")
    private Integer fileCategory;

    @TableField("file_type")
    private Integer fileType;

    /** 转码状态；普通上传可直接置成功，视频处理预留 */
    @TableField("status")
    private Integer status;

    @TableField("recovery_time")
    private LocalDateTime recoveryTime;

    /** 0: 删除 1: 回收站 2: 正常 */
    @TableField("del_flag")
    private Integer delFlag;
}
