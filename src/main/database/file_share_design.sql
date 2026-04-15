DROP TABLE IF EXISTS `file_share`;
CREATE TABLE `file_share` (
                              `share_id` varchar(20) NOT NULL COMMENT '分享ID',
                              `file_id` varchar(36) NOT NULL COMMENT '文件ID',
                              `user_id` varchar(36) NOT NULL COMMENT '用户ID',
                              `valid_type` tinyint(1) NOT NULL COMMENT '有效期(0:1天 1:7天 2:30天 3:永久)',
                              `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
                              `share_time` datetime NOT NULL COMMENT '分享时间',
                              `code` varchar(5) NOT NULL COMMENT '提取码',
                              `show_count` int(11) DEFAULT 0 COMMENT '浏览次数',
                              PRIMARY KEY (`share_id`),
                              KEY `idx_file_id` (`file_id`),
                              KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件分享表';