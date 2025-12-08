-- ============================================================
-- 数据库名：cms
-- 模块：学科竞赛管理系统（全新版本）
-- 作者：miji
-- 日期：2025-12-08
-- 说明：包含招募帖公开留言功能，无私聊功能
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS cms
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE cms;

-- ----------------------------
-- 1. 用户表 user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `userName` VARCHAR(256) NULL COMMENT '用户昵称',
    `userAccount` VARCHAR(256) NOT NULL COMMENT '用户账号',
    `userPassword` VARCHAR(512) NOT NULL COMMENT '用户密码',
    `userUrl` VARCHAR(1024) NULL COMMENT '用户头像',
    `gender` TINYINT NULL COMMENT '性别 0-女 1-男',
    `phone` VARCHAR(256) NULL COMMENT '电话',
    `email` VARCHAR(512) NULL COMMENT '邮箱',
    `tags` VARCHAR(1024) NULL COMMENT '个人简介',
    `userRole` TINYINT DEFAULT 0 NULL COMMENT '用户角色 0-普通用户(学生)，1-管理员(教师)',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    UNIQUE KEY `uk_userAccount` (`userAccount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 竞赛表 competition
-- ----------------------------
DROP TABLE IF EXISTS `competition`;
CREATE TABLE `competition` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '竞赛ID',
    `name` VARCHAR(255) NOT NULL COMMENT '竞赛名称',
    `summary` VARCHAR(1024) NULL COMMENT '竞赛简要介绍',
    `content` LONGTEXT NULL COMMENT '竞赛详情（富文本HTML内容）',
    `coverUrl` VARCHAR(512) NULL COMMENT '封面图片URL',
    `organizer` VARCHAR(255) NULL COMMENT '主办方',
    `creatorId` BIGINT NOT NULL COMMENT '创建者ID',
    `maxMembers` INT NOT NULL COMMENT '最大人数',
    `startTime` DATETIME NULL COMMENT '开始时间',
    `endTime` DATETIME NULL COMMENT '结束时间',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    INDEX `idx_creatorId` (`creatorId`),
    INDEX `idx_startTime` (`startTime`),
    CONSTRAINT `fk_competition_creator` FOREIGN KEY (`creatorId`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛信息表';

-- ----------------------------
-- 3. 队伍表 team
-- ----------------------------
DROP TABLE IF EXISTS `team`;
CREATE TABLE `team` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '队伍ID',
    `competitionId` BIGINT NOT NULL COMMENT '所属竞赛ID',
    `name` VARCHAR(256) NOT NULL COMMENT '队伍名称',
    `description` VARCHAR(1024) NULL COMMENT '队伍简介',
    `maxNum` INT NOT NULL COMMENT '队伍最大人数',
    `currentNum` INT DEFAULT 1 NOT NULL COMMENT '当前队伍人数（含队长）',
    `status` TINYINT DEFAULT 0 NOT NULL COMMENT '队伍状态：0-正常，1-已满员，2-已报名，3-解散',
    `expireTime` DATETIME NULL COMMENT '队伍过期时间',
    `userId` BIGINT NOT NULL COMMENT '队长ID',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    INDEX `idx_competitionId` (`competitionId`),
    INDEX `idx_userId` (`userId`),
    UNIQUE KEY `uq_competition_team_name` (`competitionId`, `name`),
    CONSTRAINT `fk_team_competition` FOREIGN KEY (`competitionId`) REFERENCES `competition`(`id`),
    CONSTRAINT `fk_team_leader` FOREIGN KEY (`userId`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队伍表';

-- ----------------------------
-- 4. 用户-队伍关系表 team_member
-- ----------------------------
DROP TABLE IF EXISTS `team_member`;
CREATE TABLE `team_member` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `userId` BIGINT NOT NULL COMMENT '用户ID',
    `teamId` BIGINT NOT NULL COMMENT '队伍ID',
    `role` TINYINT DEFAULT 0 COMMENT '成员角色：0-成员，1-队长',
    `joinTime` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '加入时间',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    INDEX `idx_userId` (`userId`),
    INDEX `idx_teamId` (`teamId`),
    UNIQUE KEY `uq_user_team` (`userId`, `teamId`),
    CONSTRAINT `fk_member_user` FOREIGN KEY (`userId`) REFERENCES `user`(`id`),
    CONSTRAINT `fk_member_team` FOREIGN KEY (`teamId`) REFERENCES `team`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-队伍关系表';

-- ----------------------------
-- 5. 报名数据表 competition_registration
-- ----------------------------
DROP TABLE IF EXISTS `competition_registration`;
CREATE TABLE `competition_registration` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报名ID',
    `competitionId` BIGINT NOT NULL COMMENT '竞赛ID',
    `userId` BIGINT NULL COMMENT '报名用户ID（个人赛）',
    `teamId` BIGINT NULL COMMENT '报名队伍ID（团队赛）',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-拒绝',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    INDEX `idx_competitionId` (`competitionId`),
    INDEX `idx_userId` (`userId`),
    INDEX `idx_teamId` (`teamId`),
    CONSTRAINT `fk_registration_competition` FOREIGN KEY (`competitionId`) REFERENCES `competition`(`id`),
    CONSTRAINT `fk_registration_user` FOREIGN KEY (`userId`) REFERENCES `user`(`id`),
    CONSTRAINT `fk_registration_team` FOREIGN KEY (`teamId`) REFERENCES `team`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛报名表';

-- ----------------------------
-- 6. 提交表 competition_submission
-- ----------------------------
DROP TABLE IF EXISTS `competition_submission`;
CREATE TABLE `competition_submission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '提交ID',
    `competitionId` BIGINT NOT NULL COMMENT '竞赛ID',
    `registrationId` BIGINT NOT NULL COMMENT '报名记录ID',
    `userId` BIGINT NULL COMMENT '提交用户ID（个人提交）',
    `teamId` BIGINT NULL COMMENT '提交队伍ID（团队提交）',
    `fileUrl` VARCHAR(1024) NOT NULL COMMENT '作品文件访问URL',
    `description` TEXT NULL COMMENT '作品描述（富文本）',
    `score` INT NULL COMMENT '评分（管理员评审）',
    `reviewerId` BIGINT NULL COMMENT '评分管理员ID',
    `status` TINYINT DEFAULT 0 NOT NULL COMMENT '状态：0-已提交待评审，1-已评分',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    INDEX `idx_competitionId` (`competitionId`),
    INDEX `idx_userId` (`userId`),
    INDEX `idx_teamId` (`teamId`),
    INDEX `idx_registrationId` (`registrationId`),
    CONSTRAINT `fk_submission_competition` FOREIGN KEY (`competitionId`) REFERENCES `competition`(`id`),
    CONSTRAINT `fk_submission_registration` FOREIGN KEY (`registrationId`) REFERENCES `competition_registration`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛提交作品表';

-- ----------------------------
-- 7. 队友招募表 team_recruitment
-- ----------------------------
DROP TABLE IF EXISTS `team_recruitment`;
CREATE TABLE `team_recruitment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '招募ID',
    `userId` BIGINT NOT NULL COMMENT '发布者用户ID',
    `competitionId` BIGINT NOT NULL COMMENT '所属竞赛ID',
    `teamId` BIGINT NULL COMMENT '相关队伍ID（可为空，表示个人）',
    `isTeam` TINYINT DEFAULT 0 NOT NULL COMMENT '是否代表队伍发布 0-个人 1-队伍',
    `maxMembers` INT DEFAULT 2 COMMENT '招募人数',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-招募中，1-已满员，2-已关闭',
    `title` VARCHAR(255) NOT NULL COMMENT '招募标题',
    `description` TEXT NULL COMMENT '招募描述',
    `contact` VARCHAR(255) NULL COMMENT '联系方式',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    INDEX `idx_userId` (`userId`),
    INDEX `idx_competitionId` (`competitionId`),
    INDEX `idx_teamId` (`teamId`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_recruitment_user` FOREIGN KEY (`userId`) REFERENCES `user`(`id`),
    CONSTRAINT `fk_recruitment_competition` FOREIGN KEY (`competitionId`) REFERENCES `competition`(`id`),
    CONSTRAINT `fk_recruitment_team` FOREIGN KEY (`teamId`) REFERENCES `team`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队友招募表';

-- ----------------------------
-- 8. 招募帖公开留言表 chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    `recruitmentId` BIGINT NOT NULL COMMENT '招募帖ID',
    `senderId` BIGINT NOT NULL COMMENT '发送者用户ID',
    `senderName` VARCHAR(256) NULL COMMENT '发送者用户名（冗余字段）',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `isDelete` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    INDEX `idx_recruitmentId` (`recruitmentId`),
    INDEX `idx_senderId` (`senderId`),
    INDEX `idx_createTime` (`createTime`),
    CONSTRAINT `fk_message_recruitment` FOREIGN KEY (`recruitmentId`) REFERENCES `team_recruitment`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_message_sender` FOREIGN KEY (`senderId`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招募帖公开留言表';

-- ============================================================
-- 初始化数据（可选）
-- ============================================================

-- 插入管理员账号（密码需要加密后插入）
INSERT INTO `user` (`userName`, `userAccount`, `userPassword`, `userRole`) VALUES
('管理员', 'admin', '加密后的密码', 1);

-- ============================================================
-- 验证脚本
-- ============================================================
SHOW TABLES;
SELECT 'Database created successfully!' AS Status;
