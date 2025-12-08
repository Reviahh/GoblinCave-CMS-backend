-- 数据库升级脚本：添加公开聊天功能
-- 适用于已有数据库的情况
-- 执行前请备份数据库！

USE cms;

-- ==================== 第一步：修改 team_recruitment 表 ====================

-- 添加缺失字段
ALTER TABLE `team_recruitment`
  ADD COLUMN `maxMembers` INT DEFAULT 2 COMMENT '招募人数' AFTER `isTeam`,
  ADD COLUMN `status` TINYINT DEFAULT 0 COMMENT '状态：0-招募中，1-已满员，2-已关闭' AFTER `maxMembers`;

-- 添加索引
ALTER TABLE `team_recruitment`
  ADD INDEX `idx_competitionId` (`competitionId`),
  ADD INDEX `idx_userId` (`userId`),
  ADD INDEX `idx_teamId` (`teamId`);

-- ==================== 第二步：删除旧的聊天表（如果存在）====================

-- 删除旧表（如果不需要保留私聊数据）
DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `chat_session`;

-- ==================== 第三步：创建新的公开留言表 ====================

-- 创建简化的公开留言表
CREATE TABLE `chat_message` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
  `recruitmentId` BIGINT NOT NULL COMMENT '招募帖ID（公开留言）',
  `senderId` BIGINT NOT NULL COMMENT '发送者用户ID',
  `senderName` VARCHAR(256) NULL COMMENT '发送者用户名（冗余字段）',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `isDelete` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
  INDEX `idx_recruitmentId` (`recruitmentId`),
  INDEX `idx_senderId` (`senderId`),
  INDEX `idx_createTime` (`createTime`),
  CONSTRAINT `fk_message_recruitment` 
    FOREIGN KEY (`recruitmentId`) REFERENCES `team_recruitment`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_message_sender` 
    FOREIGN KEY (`senderId`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招募帖公开留言表';

-- ==================== 第四步：为其他表添加索引（性能优化）====================

-- competition 表
ALTER TABLE `competition`
  ADD INDEX `idx_creatorId` (`creatorId`);

-- team 表
ALTER TABLE `team`
  ADD INDEX `idx_competitionId` (`competitionId`),
  ADD INDEX `idx_userId` (`userId`);

-- team_member 表
ALTER TABLE `team_member`
  ADD INDEX `idx_userId` (`userId`),
  ADD INDEX `idx_teamId` (`teamId`);

-- competition_registration 表
ALTER TABLE `competition_registration`
  ADD INDEX `idx_competitionId` (`competitionId`),
  ADD INDEX `idx_userId` (`userId`),
  ADD INDEX `idx_teamId` (`teamId`);

-- competition_submission 表
ALTER TABLE `competition_submission`
  ADD INDEX `idx_competitionId` (`competitionId`),
  ADD INDEX `idx_userId` (`userId`),
  ADD INDEX `idx_teamId` (`teamId`);

-- ==================== 完成 ====================

-- 验证表结构
SHOW TABLES;
DESCRIBE chat_message;
DESCRIBE team_recruitment;

SELECT 'Database upgrade completed successfully!' as Status;
