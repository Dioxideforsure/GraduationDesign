package com.kuopan.Entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

@TableName("file_share")
public class FileShare implements Serializable {
    @TableId
    private String shareId;
    private String fileId;
    private String userId;
    private Integer validType;
    private Date expireTime;
    private Date shareTime;
    private String code;
    private Integer showCount;
    public String getShareId() { return shareId; }
    public void setShareId(String shareId) { this.shareId = shareId; }
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Integer getValidType() { return validType; }
    public void setValidType(Integer validType) { this.validType = validType; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    public Date getShareTime() { return shareTime; }
    public void setShareTime(Date shareTime) { this.shareTime = shareTime; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getShowCount() { return showCount; }
    public void setShowCount(Integer showCount) { this.showCount = showCount; }
}