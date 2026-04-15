package com.kuopan.Entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

@TableName("user_info")
public class UserInfo implements Serializable {

    @TableId
    private String userId;

    @TableField("user_name")
    private String nickName;

    private String email;

    @TableField(exist = false)
    private String qqOpenId;

    @TableField(exist = false)
    private String qqAvatar;

    private String password;

    @TableField("reg_time")
    private Date joinTime;

    @TableField(exist = false)
    private Date lastLoginTime;

    private Integer status;

    // 👇 新增：角色映射字段 (0:管理员, 1:老师, 2:学生, 3:访客)
    private Integer role;

    @TableField(exist = false)
    private Long useSpace;

    private Long totalSpace;
    private Long occuSpace;


    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getQqOpenId() { return qqOpenId; }
    public void setQqOpenId(String qqOpenId) { this.qqOpenId = qqOpenId; }

    public String getQqAvatar() { return qqAvatar; }
    public void setQqAvatar(String qqAvatar) { this.qqAvatar = qqAvatar; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Date getJoinTime() { return joinTime; }
    public void setJoinTime(Date joinTime) { this.joinTime = joinTime; }

    public Date getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(Date lastLoginTime) { this.lastLoginTime = lastLoginTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }

    public Long getUseSpace() { return useSpace; }
    public void setUseSpace(Long useSpace) { this.useSpace = useSpace; }

    public Long getTotalSpace() { return totalSpace; }
    public void setTotalSpace(Long totalSpace) { this.totalSpace = totalSpace; }

    public Long getOccuSpace() { return occuSpace; }
    public void setOccuSpace(Long occuSpace) { this.occuSpace = occuSpace; }
}