/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : gym_db

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 15/02/2026 11:08:04
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for course_booking
-- ----------------------------
DROP TABLE IF EXISTS `course_booking`;
CREATE TABLE `course_booking`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `status` int(11) NULL DEFAULT 0 COMMENT '状态: 0-已预约, 1-已取消, 2-已签到',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预约时间',
  `booking_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '唯一订单号',
  `real_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '实际支付金额',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_booking
-- ----------------------------
INSERT INTO `course_booking` VALUES (1, 1, 1, 2, '2026-02-06 23:53:39', '', NULL);
INSERT INTO `course_booking` VALUES (2, 2, 1, 0, '2026-02-07 10:24:38', '', NULL);
INSERT INTO `course_booking` VALUES (3, 2, 2, 0, '2026-02-07 10:24:39', '', NULL);
INSERT INTO `course_booking` VALUES (4, 2, 3, 0, '2026-02-07 10:24:39', '', NULL);
INSERT INTO `course_booking` VALUES (5, 4, 4, 2, '2026-02-09 22:39:53', '2020870271924711424', 10.00);
INSERT INTO `course_booking` VALUES (6, 4, 1, 1, '2026-02-09 22:40:19', '2020870379571523584', 0.00);
INSERT INTO `course_booking` VALUES (7, 4, 3, 1, '2026-02-09 22:40:30', '2020870423578161152', 0.00);
INSERT INTO `course_booking` VALUES (8, 4, 5, 1, '2026-02-09 22:51:15', '2020873130024132608', 80.00);
INSERT INTO `course_booking` VALUES (9, 1, 2, 2, '2026-02-10 00:25:48', '2020896923941654528', 0.00);
INSERT INTO `course_booking` VALUES (10, 1, 5, 2, '2026-02-10 00:34:35', '2020899135745011712', 100.00);
INSERT INTO `course_booking` VALUES (11, 5, 1, 0, '2026-02-10 14:45:46', '2021113343275200512', 0.00);
INSERT INTO `course_booking` VALUES (12, 5, 4, 2, '2026-02-10 14:45:55', '2021113379543347200', 8.00);
INSERT INTO `course_booking` VALUES (13, 6, 5, 1, '2026-02-10 14:51:36', '2021114811566813184', 100.00);
INSERT INTO `course_booking` VALUES (14, 3, 6, 0, '2026-02-10 15:00:38', '2021117084854411264', 100.00);
INSERT INTO `course_booking` VALUES (15, 3, 5, 0, '2026-02-10 15:07:13', '2021118738844672000', 100.00);
INSERT INTO `course_booking` VALUES (16, 1, 4, 2, '2026-02-10 15:10:46', '2021119633569406976', 10.00);
INSERT INTO `course_booking` VALUES (17, 1, 6, 2, '2026-02-10 15:43:03', '2021127757533560832', 100.00);
INSERT INTO `course_booking` VALUES (18, 1, 1, 2, '2026-02-10 17:06:23', '2021148729074515968', 0.00);
INSERT INTO `course_booking` VALUES (19, 1, 1, 2, '2026-02-10 17:07:34', '2021149025733443584', 0.00);
INSERT INTO `course_booking` VALUES (20, 3, 4, 0, '2026-02-10 17:17:31', '2021151532954206208', 10.00);

-- ----------------------------
-- Table structure for gym_course
-- ----------------------------
DROP TABLE IF EXISTS `gym_course`;
CREATE TABLE `gym_course`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程名称',
  `coach` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教练姓名',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程介绍',
  `start_time` datetime NOT NULL COMMENT '上课时间',
  `capacity` int(11) NULL DEFAULT 20 COMMENT '最大人数',
  `price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '价格',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `stock` int(11) NULL DEFAULT 10 COMMENT '剩余名额',
  `version` int(11) NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程简介',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健身课程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gym_course
-- ----------------------------
INSERT INTO `gym_course` VALUES (1, '燃脂搏击操', '杰克教练', '高强度有氧，快速燃烧卡路里', '2026-06-01 18:00:00', 20, 0.00, '2026-02-06 23:39:49', 8, 0, NULL);
INSERT INTO `gym_course` VALUES (2, '舒缓瑜伽', 'Lisa老师', '拉伸肌肉，放松身心', '2026-06-02 10:00:00', 15, 0.00, '2026-02-06 23:39:49', 10, 0, NULL);
INSERT INTO `gym_course` VALUES (3, '力量举重', '施瓦辛格', '核心力量训练，增肌必备', '2026-06-03 15:00:00', 10, 0.00, '2026-02-06 23:39:49', 9, 0, NULL);
INSERT INTO `gym_course` VALUES (4, '测试数据3', '测试数据3', NULL, '2026-02-10 00:00:00', 20, 10.00, '2026-02-08 21:54:28', 9, 0, '测试3');
INSERT INTO `gym_course` VALUES (5, '测试数据4', '测试数据4', NULL, '2026-02-11 00:00:00', 20, 100.00, '2026-02-09 22:50:55', 2, 0, '测试');
INSERT INTO `gym_course` VALUES (6, '测试数据5', '陈东', NULL, '2026-02-11 00:00:00', 20, 100.00, '2026-02-10 14:48:56', 9, 0, '陈东的课');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(明文存储为了简单，答辩说哈希加密即可)',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'user' COMMENT '角色: admin/user',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `balance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '账户余额',
  `vip_type` int(11) NULL DEFAULT 0 COMMENT '会员类型: 0-普通, 1-月卡, 2-年卡',
  `vip_expire_time` datetime NULL DEFAULT NULL COMMENT 'VIP到期时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '123456', '系统管理员', 'admin', '2026-02-04 16:37:03', 900.00, 0, NULL);
INSERT INTO `sys_user` VALUES (2, 'laixu', '123456', '健身达人', 'user', '2026-02-07 10:24:30', 0.00, 0, NULL);
INSERT INTO `sys_user` VALUES (3, 'cd', '123456', NULL, 'user', '2026-02-08 21:31:12', 170.00, 1, '2026-03-15 10:58:40');
INSERT INTO `sys_user` VALUES (4, '陈东', '123456', NULL, 'user', '2026-02-08 21:38:30', 10010.00, 2, NULL);
INSERT INTO `sys_user` VALUES (5, 'test001', '123456', NULL, 'admin', '2026-02-10 14:44:30', 800.00, 2, NULL);
INSERT INTO `sys_user` VALUES (6, 'poor_guy', '123456', NULL, 'user', '2026-02-10 14:51:33', 0.00, 0, NULL);

SET FOREIGN_KEY_CHECKS = 1;
