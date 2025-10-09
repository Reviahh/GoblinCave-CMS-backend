-- 数据库名：cms
-- 模块：学科竞赛管理系统
-- 作者：cms
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
    tags          VARCHAR(1024)                      NULL COMMENT '标签列表',
    userRole      TINYINT  DEFAULT 0                 NULL COMMENT '用户角色 0-普通用户，1-教师，2-管理员',
    createTime    DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    updateTime    DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete      TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    profile       VARCHAR(512)                       NULL COMMENT '个人简介'
)
    COMMENT '用户表';


-- ----------------------------
-- 2. 竞赛表 competition
-- ----------------------------
CREATE TABLE cms.competition
(
    id            BIGINT AUTO_INCREMENT COMMENT 'id'
        PRIMARY KEY,
    compName      VARCHAR(256)                       NOT NULL COMMENT '竞赛名称',
    category      VARCHAR(128)                       NULL COMMENT '学科类别',
    organizer     VARCHAR(256)                       NULL COMMENT '主办方',
    description   VARCHAR(2048)                      NULL COMMENT '竞赛简介',
    startTime     DATETIME                           NULL COMMENT '开始时间',
    endTime       DATETIME                           NULL COMMENT '结束时间',
    isTeam        TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否团队赛 0-否 1-是',
    maxTeamSize   INT      DEFAULT 1                 NOT NULL COMMENT '最大队伍人数',
    createUserId  BIGINT                             NULL COMMENT '创建者id（教师或管理员）',
    createTime    DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    updateTime    DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete      TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除'
)
    COMMENT '竞赛表';


-- ----------------------------
-- 3. 队伍表 team
-- ----------------------------
CREATE TABLE cms.team
(
    id           BIGINT AUTO_INCREMENT COMMENT 'id'
        PRIMARY KEY,
    compId       BIGINT                             NOT NULL COMMENT '所属竞赛id',
    name         VARCHAR(256)                       NOT NULL COMMENT '队伍名称',
    description  VARCHAR(1024)                      NULL COMMENT '描述',
    maxNum       INT      DEFAULT 1                 NOT NULL COMMENT '最大人数',
    expireTime   DATETIME                           NULL COMMENT '报名截止时间',
    userId       BIGINT                             NULL COMMENT '队长id（用户id）',
    status       INT      DEFAULT 0                 NOT NULL COMMENT '0 - 公开，1 - 私有，2 - 加密',
    password     VARCHAR(512)                       NULL COMMENT '密码',
    createTime   DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    updateTime   DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete     TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    CONSTRAINT fk_team_competition FOREIGN KEY (compId) REFERENCES cms.competition (id)
)
    COMMENT '队伍表';


-- ----------------------------
-- 4. 用户-队伍关系表 user_team
-- ----------------------------
CREATE TABLE cms.user_team
(
    id          BIGINT AUTO_INCREMENT COMMENT 'id'
        PRIMARY KEY,
    userId      BIGINT                             NOT NULL COMMENT '用户id',
    teamId      BIGINT                             NOT NULL COMMENT '队伍id',
    joinTime    DATETIME                           NULL COMMENT '加入时间',
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete    TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    CONSTRAINT fk_userteam_user FOREIGN KEY (userId) REFERENCES cms.user (id),
    CONSTRAINT fk_userteam_team FOREIGN KEY (teamId) REFERENCES cms.team (id)
)
    COMMENT '用户-队伍关系表';


-- ----------------------------
-- 5. 报名表 registration
-- ----------------------------
CREATE TABLE cms.registration
(
    id           BIGINT AUTO_INCREMENT COMMENT 'id'
        PRIMARY KEY,
    compId       BIGINT                             NOT NULL COMMENT '竞赛id',
    userId       BIGINT                             NULL COMMENT '报名用户id',
    teamId       BIGINT                             NULL COMMENT '报名队伍id（团队赛使用）',
    status       INT      DEFAULT 0                 NOT NULL COMMENT '报名状态 0-待审核 1-已通过 2-驳回',
    reviewUserId BIGINT                             NULL COMMENT '审核人id',
    submitTime   DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '提交时间',
    reviewTime   DATETIME                           NULL COMMENT '审核时间',
    createTime   DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    updateTime   DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete     TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    CONSTRAINT fk_reg_comp FOREIGN KEY (compId) REFERENCES cms.competition (id),
    CONSTRAINT fk_reg_user FOREIGN KEY (userId) REFERENCES cms.user (id),
    CONSTRAINT fk_reg_team FOREIGN KEY (teamId) REFERENCES cms.team (id)
)
    COMMENT '报名表';


-- ----------------------------
-- 6. 成绩表 result
-- ----------------------------
CREATE TABLE cms.result
(
    id           BIGINT AUTO_INCREMENT COMMENT 'id'
        PRIMARY KEY,
    compId       BIGINT                             NOT NULL COMMENT '竞赛id',
    userId       BIGINT                             NULL COMMENT '个人赛用户id',
    teamId       BIGINT                             NULL COMMENT '团队赛队伍id',
    score        DECIMAL(5,2) DEFAULT 0.00          NOT NULL COMMENT '成绩分数',
    rankNum      INT                                NULL COMMENT '名次',
    award        VARCHAR(128)                       NULL COMMENT '奖项（一等奖、二等奖等）',
    recordUserId BIGINT                             NULL COMMENT '录入人id（教师）',
    createTime   DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    updateTime   DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete     TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    CONSTRAINT fk_result_comp FOREIGN KEY (compId) REFERENCES cms.competition (id)
)
    COMMENT '成绩表';


-- ----------------------------
-- 7. 公告表 announcement
-- ----------------------------
CREATE TABLE cms.announcement
(
    id          BIGINT AUTO_INCREMENT COMMENT 'id'
        PRIMARY KEY,
    compId      BIGINT                             NULL COMMENT '所属竞赛id（可为空）',
    title       VARCHAR(256)                       NOT NULL COMMENT '公告标题',
    content     VARCHAR(4096)                      NOT NULL COMMENT '公告内容',
    userId      BIGINT                             NULL COMMENT '发布人id',
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete    TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    CONSTRAINT fk_announcement_comp FOREIGN KEY (compId) REFERENCES cms.competition (id)
)
    COMMENT '公告表';


-- ----------------------------
-- 8. 文件上传表 file_upload
-- ----------------------------
CREATE TABLE cms.file_upload
(
    id          BIGINT AUTO_INCREMENT COMMENT 'id'
        PRIMARY KEY,
    compId      BIGINT                             NOT NULL COMMENT '所属竞赛id',
    userId      BIGINT                             NOT NULL COMMENT '上传人id',
    teamId      BIGINT                             NULL COMMENT '队伍id（如为团队赛）',
    filePath    VARCHAR(1024)                      NOT NULL COMMENT '文件路径',
    fileType    VARCHAR(128)                       NULL COMMENT '文件类型（报告、作品等）',
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete    TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    CONSTRAINT fk_file_comp FOREIGN KEY (compId) REFERENCES cms.competition (id),
    CONSTRAINT fk_file_user FOREIGN KEY (userId) REFERENCES cms.user (id)
)
    COMMENT '文件上传表';
