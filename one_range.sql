-- ==========================================
-- One 安全靶场 - 数据库初始化脚本
-- Database: one_range
-- ==========================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户表结构
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `perms` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `role` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 12 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 用户表数据
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', 'admin', 'manage', 'administrator');
INSERT INTO `users` VALUES (2, 'test', 'test', 'manage', 'administrator');

-- ----------------------------
-- XSS存储型漏洞表结构
-- ----------------------------
DROP TABLE IF EXISTS `xss`;
CREATE TABLE `xss`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 22 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- XSS存储型漏洞数据
-- ----------------------------

-- ----------------------------
-- 学习进度表结构
-- ----------------------------
DROP TABLE IF EXISTS `learning_progress`;
CREATE TABLE `learning_progress`  (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` int(11) UNSIGNED NOT NULL COMMENT '用户ID',
  `vuln_module` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '漏洞模块标识',
  `completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否完成：0-未完成，1-已完成',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_module`(`user_id`, `vuln_module`) USING BTREE COMMENT '用户+模块唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '学习进度表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
