-- ==========================================
-- One Security Range - Database Init Script
-- Database: one_range
-- ==========================================

CREATE DATABASE IF NOT EXISTS `one_range`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `one_range`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- users table
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `perms` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `role` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=Dynamic;

INSERT INTO `users` VALUES (1, 'admin', 'admin', 'manage', 'administrator');
INSERT INTO `users` VALUES (2, 'test', 'test', 'manage', 'administrator');

-- xss table
DROP TABLE IF EXISTS `xss`;
CREATE TABLE `xss` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=Dynamic;

-- learning progress table
DROP TABLE IF EXISTS `learning_progress`;
CREATE TABLE `learning_progress` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` int(11) UNSIGNED NOT NULL COMMENT 'user id',
  `vuln_module` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'vulnerability module key',
  `completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 means incomplete, 1 means complete',
  `completed_at` datetime DEFAULT NULL COMMENT 'completed time',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_module` (`user_id`, `vuln_module`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci COMMENT='learning progress table' ROW_FORMAT=Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
