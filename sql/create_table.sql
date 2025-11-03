-- 数据库名：cms
-- 模块：学科竞赛管理系统
-- 作者：miji
-- 日期：2025-10-09

CREATE DATABASE IF NOT EXISTS cms
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE cms;

-- ----------------------------
-- 1. 用户表 user
-- ----------------------------
CREATE TABLE cms.user
(
    id            BIGINT AUTO_INCREMENT COMMENT 'id'
        PRIMARY KEY,
    userName      VARCHAR(256)                       NULL COMMENT '用户昵称',
    userAccount   VARCHAR(256)                       NOT NULL COMMENT '用户账号',
    userPassword  VARCHAR(512)                       NOT NULL COMMENT '用户密码',
    userUrl       VARCHAR(1024)                      NULL COMMENT '用户头像',
    gender        TINYINT                            NULL COMMENT '性别 0-女 1-男',
    phone         VARCHAR(256)                       NULL COMMENT '电话',
    email         VARCHAR(512)                       NULL COMMENT '邮箱',
    tags          VARCHAR(1024)                      NULL COMMENT '个人简介',
    userRole      TINYINT  DEFAULT 0                 NULL COMMENT '用户角色 0-普通用户，1-管理员',
    createTime    DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    updateTime    DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete      TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除'
)
    COMMENT '用户表';


-- ----------------------------
-- 2. 竞赛表 competition
-- ----------------------------
CREATE TABLE cms.competition
(
    id            BIGINT AUTO_INCREMENT COMMENT '竞赛ID'
        PRIMARY KEY,
    name          VARCHAR(255)                       NOT NULL COMMENT '竞赛名称',
    summary       VARCHAR(1024)                      NULL COMMENT '竞赛简要介绍',
    content       LONGTEXT                           NULL COMMENT '竞赛详情（富文本HTML内容）',
    coverUrl      VARCHAR(512)                       NULL COMMENT '封面图片URL',
    organizer     VARCHAR(255)                       NULL COMMENT '主办方',
    creatorId     BIGINT                             NOT NULL COMMENT '创建者ID',
    maxMembers    INT                                NOT NULL COMMENT '最大人数',
    startTime     DATETIME                           NULL COMMENT '开始时间',
    endTime       DATETIME                           NULL COMMENT '结束时间',
    createTime    DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    updateTime    DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete      TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除'
)
    COMMENT '竞赛信息表';


-- ----------------------------
-- 3. 队伍表 team
-- ----------------------------
CREATE TABLE cms.team (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '队伍ID',
    competitionId   BIGINT              NOT NULL COMMENT '所属竞赛ID',
    name            VARCHAR(256)        NOT NULL COMMENT '队伍名称',
    description     VARCHAR(1024)       NULL COMMENT '队伍简介',
    maxNum          INT                 NOT NULL COMMENT '队伍最大人数（默认等于竞赛人数上限）',
    currentNum      INT                 DEFAULT 1 NOT NULL COMMENT '当前队伍人数（含队长）',
    status          TINYINT DEFAULT 0 NOT NULL COMMENT '队伍状态：0-正常，1-已满员，2-已报名，3-解散',
    expireTime      DATETIME NULL COMMENT '队伍过期时间（可用于自动解散）',
    userId          BIGINT NOT NULL COMMENT '队长ID（用户ID）',
    createTime      DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    updateTime      DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete        TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    CONSTRAINT fk_team_competition FOREIGN KEY (competitionId) REFERENCES cms.competition (id),
    CONSTRAINT fk_team_leader FOREIGN KEY (userId) REFERENCES cms.user (id),
    UNIQUE KEY uq_competition_team_name (competitionId, name)
) COMMENT '队伍表';



-- ----------------------------
-- 4. 用户-队伍关系表 team_member
-- ----------------------------
CREATE TABLE cms.team_member
(
    id         BIGINT AUTO_INCREMENT COMMENT '主键'
        PRIMARY KEY,
    userId     BIGINT                             NOT NULL COMMENT '用户ID',
    teamId     BIGINT                             NOT NULL COMMENT '队伍ID',
    role       TINYINT  DEFAULT 0                          COMMENT '成员角色：0-成员，1-队长',
    joinTime   DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '加入时间',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete   TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    CONSTRAINT fk_user_team_user FOREIGN KEY (userId) REFERENCES cms.user (id),
    CONSTRAINT fk_user_team_team FOREIGN KEY (teamId) REFERENCES cms.team (id)
)
    COMMENT '用户-队伍关系表';

-- ----------------------------
-- 5. 报名数据表 competition_registration
-- ----------------------------
CREATE TABLE cms.competition_registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报名ID',
    competitionId BIGINT NOT NULL COMMENT '竞赛ID',
    userId BIGINT NULL COMMENT '报名用户ID（个人赛）',
    teamId BIGINT NULL COMMENT '报名队伍ID（团队赛）',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-拒绝',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    isDelete TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    CONSTRAINT fk_registration_competition FOREIGN KEY (competitionId) REFERENCES cms.competition (id),
    CONSTRAINT fk_registration_user FOREIGN KEY (userId) REFERENCES cms.user (id),
    CONSTRAINT fk_registration_team FOREIGN KEY (teamId) REFERENCES cms.team (id)
) COMMENT '竞赛报名表';
