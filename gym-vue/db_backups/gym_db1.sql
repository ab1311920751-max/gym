/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : gym_db1

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 07/06/2026 20:36:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id` bigint(20) NOT NULL COMMENT '会话ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色：user / assistant',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 73 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI客服消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_chat_message
-- ----------------------------
INSERT INTO `ai_chat_message` VALUES (1, 1, 'user', '最近有什么课推荐？', '2026-05-20 21:54:33');
INSERT INTO `ai_chat_message` VALUES (2, 1, 'assistant', '嘿，admin！你可是年卡VIP，以下课程都能免费上哦！推荐几个热门的：\n- **燃脂搏击操**（6月1日18:00，杰克教练，剩8个名额）\n- **舒缓瑜伽**（6月2日10:00，Lisa老师，剩9个名额）\n- **力量举重**（6月3日15:00，施瓦辛格，剩9个名额）\n\n想约哪个？去「预约课程」页面自己操作就行！', '2026-05-20 21:54:35');
INSERT INTO `ai_chat_message` VALUES (3, 1, 'user', '你好你是什么模型', '2026-06-02 11:19:58');
INSERT INTO `ai_chat_message` VALUES (4, 1, 'assistant', '我是您的专属 AI 健身客服。您可以问我：\n• 课程推荐（说「推荐课程」）\n• 特定课程详情（输入课程名称）\n• 教练课程（说「XX教练有什么课」）\n• 时间查询（说「明天有什么课」）\n• 名额查询（说「还有名额吗」）\n• 余额/VIP 查询', '2026-06-02 11:19:59');
INSERT INTO `ai_chat_message` VALUES (5, 1, 'user', '你是什么模型', '2026-06-02 11:21:24');
INSERT INTO `ai_chat_message` VALUES (6, 1, 'assistant', '我是您的专属 AI 健身客服。您可以问我：\n• 课程推荐（说「推荐课程」）\n• 特定课程详情（输入课程名称）\n• 教练课程（说「XX教练有什么课」）\n• 时间查询（说「明天有什么课」）\n• 名额查询（说「还有名额吗」）\n• 余额/VIP 查询', '2026-06-02 11:21:24');
INSERT INTO `ai_chat_message` VALUES (7, 1, 'user', '课程推荐', '2026-06-02 11:21:55');
INSERT INTO `ai_chat_message` VALUES (8, 1, 'assistant', '根据您的余额 (1200.00元) 和 VIP 权益，我为您分析了最近的课程：\n\n- 力量举重 | 教练:施瓦辛格 | 价格:60元 | 剩余名额:9 | 时间:06-03 15:00 | 简介:\n\n(Tips: 结合您的余额，建议优先选择性价比高的课程)', '2026-06-02 11:21:55');
INSERT INTO `ai_chat_message` VALUES (9, 1, 'user', '你是什么模型', '2026-06-02 11:23:43');
INSERT INTO `ai_chat_message` VALUES (10, 1, 'assistant', '我是您的专属 AI 健身客服。您可以问我：\n• 课程推荐（说「推荐课程」）\n• 特定课程详情（输入课程名称）\n• 教练课程（说「XX教练有什么课」）\n• 时间查询（说「明天有什么课」）\n• 名额查询（说「还有名额吗」）\n• 余额/VIP 查询', '2026-06-02 11:23:43');
INSERT INTO `ai_chat_message` VALUES (11, 1, 'user', '你是什么模型', '2026-06-02 11:29:09');
INSERT INTO `ai_chat_message` VALUES (12, 1, 'assistant', '我是您的专属 AI 健身客服。您可以问我：\n• 课程推荐（说「推荐课程」）\n• 特定课程详情（输入课程名称）\n• 教练课程（说「XX教练有什么课」）\n• 时间查询（说「明天有什么课」）\n• 名额查询（说「还有名额吗」）\n• 余额/VIP 查询', '2026-06-02 11:29:10');
INSERT INTO `ai_chat_message` VALUES (13, 1, 'user', '你是什么模型', '2026-06-02 11:29:21');
INSERT INTO `ai_chat_message` VALUES (14, 1, 'assistant', '我是您的专属 AI 健身客服。您可以问我：\n• 课程推荐（说「推荐课程」）\n• 特定课程详情（输入课程名称）\n• 教练课程（说「XX教练有什么课」）\n• 时间查询（说「明天有什么课」）\n• 名额查询（说「还有名额吗」）\n• 余额/VIP 查询', '2026-06-02 11:29:21');
INSERT INTO `ai_chat_message` VALUES (15, 1, 'user', '1', '2026-06-02 11:29:29');
INSERT INTO `ai_chat_message` VALUES (16, 1, 'assistant', '我是您的专属 AI 健身客服。您可以问我：\n• 课程推荐（说「推荐课程」）\n• 特定课程详情（输入课程名称）\n• 教练课程（说「XX教练有什么课」）\n• 时间查询（说「明天有什么课」）\n• 名额查询（说「还有名额吗」）\n• 余额/VIP 查询', '2026-06-02 11:29:29');
INSERT INTO `ai_chat_message` VALUES (17, 1, 'user', '你是什么模型', '2026-06-02 11:32:20');
INSERT INTO `ai_chat_message` VALUES (18, 1, 'assistant', '我是您的专属 AI 健身客服。您可以问我：\n• 课程推荐（说「推荐课程」）\n• 特定课程详情（输入课程名称）\n• 教练课程（说「XX教练有什么课」）\n• 时间查询（说「明天有什么课」）\n• 名额查询（说「还有名额吗」）\n• 余额/VIP 查询', '2026-06-02 11:32:21');
INSERT INTO `ai_chat_message` VALUES (19, 1, 'user', '你是什么模型', '2026-06-02 11:34:50');
INSERT INTO `ai_chat_message` VALUES (20, 1, 'assistant', '嘿，admin！你可是年卡VIP，以下课程都能免费上哦！推荐几个热门的：\n- **燃脂搏击操**（6月1日18:00，杰克教练，剩8个名额）\n- **舒缓瑜伽**（6月2日10:00，Lisa老师，剩9个名额）\n- **力量举重**（6月3日15:00，施瓦辛格，剩9个名额）\n\n想约哪个？去「预约课程」页面自己操作就行！', '2026-06-02 11:34:52');
INSERT INTO `ai_chat_message` VALUES (21, 1, 'user', '这个月有什么课程', '2026-06-02 11:39:04');
INSERT INTO `ai_chat_message` VALUES (22, 1, 'assistant', '我是您的专属 AI 健身客服。您可以问我：\n• 课程推荐（说「推荐课程」）\n• 特定课程详情（输入课程名称）\n• 教练课程（说「XX教练有什么课」）\n• 时间查询（说「明天有什么课」）\n• 名额查询（说「还有名额吗」）\n• 余额/VIP 查询', '2026-06-02 11:39:06');
INSERT INTO `ai_chat_message` VALUES (23, 2, 'user', '最近有什么课推荐？', '2026-06-02 11:40:27');
INSERT INTO `ai_chat_message` VALUES (24, 2, 'assistant', '嘿！最近推荐你去试试「力量举重」课，教练是施瓦辛格，价格60元，还剩9个名额，时间是6月3号下午3点，正好适合你年卡VIP，性价比超高！', '2026-06-02 11:40:29');
INSERT INTO `ai_chat_message` VALUES (25, 2, 'user', '你是谁', '2026-06-02 11:40:40');
INSERT INTO `ai_chat_message` VALUES (26, 2, 'assistant', '我是健身房预约系统的AI智能客服，你可以叫我小健！有啥课程问题或者想了解啥，尽管问我～', '2026-06-02 11:40:42');
INSERT INTO `ai_chat_message` VALUES (27, 2, 'user', '最近有什么课程', '2026-06-02 11:40:56');
INSERT INTO `ai_chat_message` VALUES (28, 2, 'assistant', '嘿，最近推荐你试试「力量举重」课！教练是施瓦辛格，价格60元，6月3日下午3点开课，还剩9个名额。你是年卡VIP，正好划算又带劲，感兴趣不？', '2026-06-02 11:40:57');
INSERT INTO `ai_chat_message` VALUES (29, 3, 'user', '最近有什么课推荐？', '2026-06-02 13:04:44');
INSERT INTO `ai_chat_message` VALUES (30, 3, 'assistant', '嗨！admin，最近有不少热门课程哦！我帮你挑几个适合你的：\n\n- **动感单车**（06-03 19:00，30元，剩25个名额）—— 超嗨的骑行课，适合释放压力\n- **普拉提**（06-04 09:30，60元，剩15个名额）—— 久坐办公党必试，练核心超棒\n- **街舞入门**（06-04 18:30，40元，剩20个名额）—— 轻松有趣，还能学成品舞\n\n想了解哪个课的详细信息？或者想看看其他时间段的课？😊', '2026-06-02 13:04:46');
INSERT INTO `ai_chat_message` VALUES (31, 3, 'user', '我想踩单车， 你推荐我什么课程', '2026-06-02 13:05:23');
INSERT INTO `ai_chat_message` VALUES (32, 3, 'assistant', '你说想踩单车，那必须推荐 **动感单车** 啊！Michael教练带你45分钟高强度骑行，配合音乐灯光，超带劲！只要30元，名额还有25个，时间在06-03 19:00。\n\n想了解具体内容吗？还是说直接去「预约课程」页面抢个位？ 😎', '2026-06-02 13:05:24');
INSERT INTO `ai_chat_message` VALUES (33, 3, 'user', '你是谁', '2026-06-02 13:05:54');
INSERT INTO `ai_chat_message` VALUES (34, 3, 'assistant', '哈哈，刚才你不是说想踩单车嘛，那必须首推 **动感单车** 呀！Michael教练带课，45分钟高强度骑行，配合音乐灯光超级燃，30元一节，名额还有25个，时间在06-03 19:00。\n\n其他热门课也有，比如**普拉提**（06-04 09:30，60元）适合练核心，**街舞入门**（06-04 18:30，40元）轻松出汗。不过感觉动感单车最对你胃口！\n\n想了解具体哪个？还是直接去「预约课程」页面安排上？😎', '2026-06-02 13:05:55');
INSERT INTO `ai_chat_message` VALUES (35, 3, 'user', '你这里我可以咨询什么', '2026-06-02 13:09:16');
INSERT INTO `ai_chat_message` VALUES (36, 3, 'assistant', '哈哈，可以呀！我这边主要能帮你：\n\n1. **查课程信息** —— 比如时间、教练、价格、剩余名额\n2. **推荐课程** —— 根据你的喜好（像你刚才想踩单车）给你推荐合适的课\n\n不过要注意，我不能帮你预约、下单或支付哦。如果想约课，得自己去「预约课程」页面操作。\n\n你想先了解哪个？比如刚才的动感单车，或者看看其他课？😊', '2026-06-02 13:09:18');
INSERT INTO `ai_chat_message` VALUES (37, 3, 'user', '我想知道有哪些热门教练', '2026-06-02 13:09:34');
INSERT INTO `ai_chat_message` VALUES (38, 3, 'assistant', '我是你的健身房AI助手，专门帮你查课程信息、推荐热门课。比如你想踩单车，我就推荐Michael教练的动感单车；想了解其他课，我也可以告诉你时间、价格、名额这些。\n\n不过我不能帮你预约或下单，想约课得去「预约课程」页面自己操作哦！有什么想问的随时找我～😊', '2026-06-02 13:09:35');
INSERT INTO `ai_chat_message` VALUES (39, 4, 'user', '明天有哪些课？', '2026-06-02 13:34:40');
INSERT INTO `ai_chat_message` VALUES (40, 4, 'assistant', '明天（06-04）有两节很不错的课哦：\n\n1. **普拉提**（09:30）—— 王雅婷教练，60元，适合久坐人群，强化核心力量\n2. **街舞入门**（18:30）—— Alex老师，40元，轻松有趣还能跳出汗\n\n你是年卡VIP，所有课程都享8折优惠！想了解哪个课程的更多细节？或者需要我帮你规划一下适合你的组合？', '2026-06-02 13:34:42');
INSERT INTO `ai_chat_message` VALUES (41, 4, 'user', '你是谁', '2026-06-02 13:36:36');
INSERT INTO `ai_chat_message` VALUES (42, 4, 'assistant', '我是小健呀，FitLife 健身房的智能客服助手！专门帮你解答课程、教练、时间、价格和VIP权益等问题，还可以根据你的目标和偏好推荐课程组合。有什么健身方面的需要，尽管问我吧！😊', '2026-06-02 13:36:37');
INSERT INTO `ai_chat_message` VALUES (43, 4, 'user', '我想增肌', '2026-06-02 13:38:34');
INSERT INTO `ai_chat_message` VALUES (44, 4, 'assistant', '增肌是个很棒的目标！结合你的VIP折扣和近期课程，我推荐你尝试以下组合：\n\n1. **TRX悬挂训练**（06-05 12:00）—— 刘教练，原价80元，VIP折后64元，能有效激活核心和全身肌群\n2. **拳击体能**（06-05 17:00）—— 张教练，原价70元，VIP折后56元，提升心肺和上肢力量\n\n建议每周安排2-3次力量训练，配合适量有氧。如果你想更系统，也可以搭配普拉提（06-04 09:30）来强化深层肌群。\n\n需要我帮你看看其他日期的课程安排吗？预约的话记得在「预约课程」页面自行操作哦～', '2026-06-02 13:38:37');
INSERT INTO `ai_chat_message` VALUES (47, 6, 'user', 'VIP 有什么优惠？', '2026-06-03 19:49:41');
INSERT INTO `ai_chat_message` VALUES (48, 6, 'assistant', '目前普通会员暂无折扣哦~ VIP会员通常可以享受课程价格优惠、优先预约等权益。建议您联系前台咨询具体升级方式和优惠内容，比如是否有充值送积分或折扣套餐。如果升级后，我可以帮您算算哪些课更划算！', '2026-06-03 19:49:43');
INSERT INTO `ai_chat_message` VALUES (49, 4, 'user', '我想身体柔韧性更好', '2026-06-03 19:50:39');
INSERT INTO `ai_chat_message` VALUES (50, 4, 'assistant', '想提升柔韧性，**普拉提**（06-04 09:30）是最适合你的选择！\n\n- 教练：王雅婷\n- 价格：60元（VIP折后仅**48元**）\n- 特点：垫上普拉提，精准控制动作强化深层核心肌群，特别适合久坐办公人群\n\n另外，你也可以搭配**街舞入门**（06-04 18:30）作为辅助，舞蹈动作能顺便拉伸和提升身体协调性。\n\n需要我推荐更多柔韧性相关的课程吗？预约的话记得在「预约课程」页面自行操作哦～', '2026-06-03 19:50:40');
INSERT INTO `ai_chat_message` VALUES (51, 4, 'user', '我BMI有点高，我想有力量感的身材我后天上午有时间', '2026-06-05 15:00:15');
INSERT INTO `ai_chat_message` VALUES (52, 4, 'assistant', '后天（06-07）上午**08:00**的**核心训练**（陈教练，原价50元，VIP折后**40元**）非常适合你！30分钟集中雕刻腹部、侧腰和下背部，高效打造紧致核心，配合力量训练能帮你逐步练出有力量感的身材。\n\n另外，建议你搭配**有氧燃脂操**（06-07 18:00，免费课程）来降低体脂，让肌肉线条更明显。核心训练打好基础后，也可以关注后续的**拳击体能**或**TRX悬挂训练**，全面提升力量感。\n\n预约的话，记得在「预约课程」页面自行操作哦～需要我帮你看看其他日期的课程吗？', '2026-06-05 15:00:18');
INSERT INTO `ai_chat_message` VALUES (53, 7, 'user', '明天有哪些课？', '2026-06-06 19:58:47');
INSERT INTO `ai_chat_message` VALUES (54, 7, 'assistant', '明天（06-08）有两节很棒的课，推荐给你：\n- **HIIT暴汗**（12:30，Jack教练，100元）—— 短时高效燃脂，适合想快速提升体能的朋友\n- **全身拉伸**（20:00，Lisa老师，免费）—— 适合训练后放松或日常久坐恢复\n\n你想了解哪节课的详情？或者我帮你根据目标推荐一下？', '2026-06-06 19:58:49');
INSERT INTO `ai_chat_message` VALUES (55, 7, 'user', '我还有多少钱', '2026-06-06 22:22:29');
INSERT INTO `ai_chat_message` VALUES (56, 7, 'assistant', '你的账户余额是 **570.10元**。作为普通会员，目前没有VIP折扣。有什么课程想了解吗？我可以帮你算算够不够用！', '2026-06-06 22:22:31');
INSERT INTO `ai_chat_message` VALUES (57, 7, 'user', '我账号还有多少余额', '2026-06-06 22:23:07');
INSERT INTO `ai_chat_message` VALUES (58, 7, 'assistant', '你的账户当前余额是 **540.10元**。另外，你是月卡VIP会员，购买课程可享9折优惠哦！有什么想了解的课程或健身计划吗？我帮你算算最划算的组合～', '2026-06-06 22:23:09');
INSERT INTO `ai_chat_message` VALUES (59, 7, 'user', '最近的系统公告有哪些', '2026-06-06 22:24:20');
INSERT INTO `ai_chat_message` VALUES (60, 7, 'assistant', '抱歉，我暂时无法查看系统公告。如果你需要了解最新的通知或活动，建议直接查看健身房前台、公告栏或App内的公告页面。有其他课程或健身计划的问题，随时问我！', '2026-06-06 22:24:22');
INSERT INTO `ai_chat_message` VALUES (61, 7, 'user', 'VIP有哪些优惠', '2026-06-06 22:28:59');
INSERT INTO `ai_chat_message` VALUES (62, 7, 'assistant', '作为月卡VIP，你主要享受以下优惠：\n- **课程9折**：所有付费课程（如HIIT暴汗100元，你只需90元）\n- **优先预约**：部分热门课程可提前锁定名额\n- **专属活动**：偶尔会有VIP专享的免费体验或折扣\n\n另外，你的账户余额还有540.10元，搭配折扣选课更划算！想看看哪节课适合你？', '2026-06-06 22:29:01');
INSERT INTO `ai_chat_message` VALUES (63, 8, 'user', 'VIP 有什么优惠？', '2026-06-07 09:46:10');
INSERT INTO `ai_chat_message` VALUES (64, 8, 'assistant', '亲，您已经是我们的年卡VIP啦！VIP权益很实在：所有付费课程都能享受**8折优惠**，比如原价100元的HIIT暴汗课，您只需付80元。另外，部分免费课程（如全身拉伸、有氧燃脂操）也能优先预约。简单说就是省钱又省心！要不要看看最近的热门课程？比如东king教练的游泳课，名额快满了哦～', '2026-06-07 09:46:12');
INSERT INTO `ai_chat_message` VALUES (65, 8, 'user', '你好，你是谁', '2026-06-07 09:47:14');
INSERT INTO `ai_chat_message` VALUES (66, 8, 'assistant', '嗨，我是小健，FitLife健身房的AI智能客服，也是你的专属健身小助手！有啥关于课程、教练、VIP优惠或者健身计划的问题，随时问我，帮你安排得明明白白～ 比如最近有东king的游泳课和免费的有氧操，感兴趣吗？😊', '2026-06-07 09:47:16');
INSERT INTO `ai_chat_message` VALUES (67, 8, 'user', '我想了解健身', '2026-06-07 09:47:33');
INSERT INTO `ai_chat_message` VALUES (68, 8, 'assistant', '好的，没问题！健身是个超棒的选择～ 您是想达到什么目标呢？比如**减脂、塑形、增肌**，还是**放松拉伸**？我可以根据您的需求推荐适合的课程组合。\n\n比如，最近有：\n- **HIIT暴汗**（100元，燃脂超高效）\n- **杠铃操**（60元，塑形线条）\n- **全身拉伸**（免费，适合放松恢复）\n\n您作为VIP还能打8折，超划算！告诉我您的目标，我帮您规划一下~', '2026-06-07 09:47:35');
INSERT INTO `ai_chat_message` VALUES (69, 8, 'user', '那个100元暴汗的课程是几点，教练是谁', '2026-06-07 10:14:42');
INSERT INTO `ai_chat_message` VALUES (70, 8, 'assistant', 'HIIT暴汗课程安排在 **06-08（周六）12:30**，由 **Jack教练** 授课。他带课超有激情，动作指导也很细致，适合想高效燃脂的你！VIP打完8折只要80元，还剩14个名额，要抓紧哦～如果感兴趣，记得在「预约课程」页面自行操作预约哈！', '2026-06-07 10:14:43');
INSERT INTO `ai_chat_message` VALUES (71, 8, 'user', '有什么课程推荐么', '2026-06-07 12:22:56');
INSERT INTO `ai_chat_message` VALUES (72, 8, 'assistant', '好嘞！根据您的VIP折扣和近期目标，我推荐几个搭配组合：\n\n**减脂燃脂**：HIIT暴汗（100元，打8折80元）+ 全身拉伸（免费）\n→ 高效燃脂后放松，适合周六中午+晚上\n\n**塑形入门**：杠铃操（60元，打8折48元）+ 有氧燃脂操（免费）\n→ 力量+有氧结合，周日+周五晚都能安排\n\n**综合体验**：游泳课（90元，打8折72元）+ 全身拉伸（免费）\n→ 游泳后拉伸，适合周五下午放松\n\n您更偏向哪种目标？或者直接告诉我想练哪方面，我帮您细调～', '2026-06-07 12:22:59');

-- ----------------------------
-- Table structure for ai_chat_session
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_session`;
CREATE TABLE `ai_chat_session`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '新对话' COMMENT '会话标题',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI客服会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_chat_session
-- ----------------------------
INSERT INTO `ai_chat_session` VALUES (1, 1, '最近有什么课推荐？', '2026-05-20 21:54:33', '2026-06-02 11:39:06');
INSERT INTO `ai_chat_session` VALUES (2, 1, '最近有什么课推荐？', '2026-06-02 11:40:27', '2026-06-02 11:40:57');
INSERT INTO `ai_chat_session` VALUES (3, 1, '最近有什么课推荐？', '2026-06-02 13:04:44', '2026-06-02 13:09:35');
INSERT INTO `ai_chat_session` VALUES (4, 1, '明天有哪些课？', '2026-06-02 13:34:40', '2026-06-05 15:00:18');
INSERT INTO `ai_chat_session` VALUES (6, 3, 'VIP 有什么优惠？', '2026-06-03 19:49:41', '2026-06-03 19:49:43');
INSERT INTO `ai_chat_session` VALUES (7, 3, '明天有哪些课？', '2026-06-06 19:58:47', '2026-06-06 22:29:01');
INSERT INTO `ai_chat_session` VALUES (8, 1, 'VIP 有什么优惠？', '2026-06-07 09:46:10', '2026-06-07 12:22:59');

-- ----------------------------
-- Table structure for banner_info
-- ----------------------------
DROP TABLE IF EXISTS `banner_info`;
CREATE TABLE `banner_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `subtitle` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '副标题',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序（升序）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banner_info
-- ----------------------------
INSERT INTO `banner_info` VALUES (1, '专业教练团队', '持证上岗，一对一私教指导', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=1400&q=80', 1, 1, '2026-05-28 14:35:14');
INSERT INTO `banner_info` VALUES (2, '灵活预约系统', '随时随地，想练就练', 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=1400&q=80', 2, 1, '2026-05-28 14:35:14');
INSERT INTO `banner_info` VALUES (3, 'VIP 专属折扣', '月卡 9 折 · 年卡 8 折', 'https://images.unsplash.com/photo-1549060279-7e168fcee0c2?w=1400&q=80', 3, 1, '2026-05-28 14:35:14');

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
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_booking
-- ----------------------------
INSERT INTO `course_booking` VALUES (1, 1, 1, 2, '2026-02-06 23:53:39', '', NULL);
INSERT INTO `course_booking` VALUES (2, 2, 1, 0, '2026-02-07 10:24:38', '', NULL);
INSERT INTO `course_booking` VALUES (3, 2, 2, 0, '2026-02-07 10:24:39', '', NULL);
INSERT INTO `course_booking` VALUES (4, 2, 3, 0, '2026-02-07 10:24:39', '', NULL);
INSERT INTO `course_booking` VALUES (6, 4, 1, 1, '2026-02-09 22:40:19', '2020870379571523584', 0.00);
INSERT INTO `course_booking` VALUES (7, 4, 3, 1, '2026-02-09 22:40:30', '2020870423578161152', 0.00);
INSERT INTO `course_booking` VALUES (9, 1, 2, 2, '2026-02-10 00:25:48', '2020896923941654528', 0.00);
INSERT INTO `course_booking` VALUES (18, 1, 1, 2, '2026-02-10 17:06:23', '2021148729074515968', 0.00);
INSERT INTO `course_booking` VALUES (19, 1, 1, 2, '2026-02-10 17:07:34', '2021149025733443584', 0.00);
INSERT INTO `course_booking` VALUES (21, 3, 2, 2, '2026-05-20 20:51:44', '2057081841335742464', 0.00);
INSERT INTO `course_booking` VALUES (22, 8, 1, 0, '2026-05-24 13:12:22', '2058415788187480064', 0.00);
INSERT INTO `course_booking` VALUES (23, 1, 1, 2, '2026-05-28 13:52:18', '2059875390108241920', 96.00);
INSERT INTO `course_booking` VALUES (24, 1, 3, 2, '2026-05-28 13:54:09', '2059875852660281344', 48.00);
INSERT INTO `course_booking` VALUES (25, 1, 1, 1, '2026-06-02 12:43:03', '2061669900618723328', 96.00);
INSERT INTO `course_booking` VALUES (26, 1, 11, 1, '2026-06-04 11:12:22', '2062371855833980928', 63.92);
INSERT INTO `course_booking` VALUES (27, 3, 17, 2, '2026-06-06 19:59:22', '2063229253410750464', 99.90);
INSERT INTO `course_booking` VALUES (28, 3, 17, 2, '2026-06-06 19:59:48', '2063229365448998912', 99.90);
INSERT INTO `course_booking` VALUES (29, 3, 14, 1, '2026-06-06 22:05:29', '2063260993353285632', 49.90);
INSERT INTO `course_booking` VALUES (30, 3, 37, 2, '2026-06-06 22:20:19', '2063264724790734848', 59.90);
INSERT INTO `course_booking` VALUES (31, 3, 17, 1, '2026-06-06 22:21:42', '2063265072460787712', 99.90);
INSERT INTO `course_booking` VALUES (32, 3, 23, 1, '2026-06-07 07:54:24', '2063409198825148416', 35.91);
INSERT INTO `course_booking` VALUES (33, 1, 23, 1, '2026-06-07 09:05:28', '2063427084486643712', 31.92);

-- ----------------------------
-- Table structure for course_comment
-- ----------------------------
DROP TABLE IF EXISTS `course_comment`;
CREATE TABLE `course_comment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `user_id` bigint(20) NOT NULL COMMENT '评论者ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint(20) NULL DEFAULT NULL COMMENT '父评论ID，NULL 表示顶级评论',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_comment
-- ----------------------------
INSERT INTO `course_comment` VALUES (1, 1, 3, '强度真的很高，第一次上完腿软了两天，但效果明显！', NULL, '2026-06-01 10:00:00');
INSERT INTO `course_comment` VALUES (2, 1, 4, '杰克教练讲解很细致，动作纠正也很到位，推荐！', NULL, '2026-06-01 14:30:00');
INSERT INTO `course_comment` VALUES (3, 1, 2, '上了三次之后感觉心肺提升了很多，超值！', NULL, '2026-06-02 09:00:00');
INSERT INTO `course_comment` VALUES (4, 2, 8, 'Lisa老师声音超温柔，整节课都很放松，睡前上最合适', NULL, '2026-06-02 20:00:00');
INSERT INTO `course_comment` VALUES (5, 2, 9, '第一次练瑜伽有点跟不上，但老师会照顾新人节奏的', NULL, '2026-06-03 08:30:00');
INSERT INTO `course_comment` VALUES (6, 2, 4, '深度拉伸之后身体轻盈了很多，久坐党必上！', NULL, '2026-06-03 11:00:00');
INSERT INTO `course_comment` VALUES (7, 3, 4, '施瓦辛格教练真的专业，硬拉姿势一直没纠正好，这次搞明白了', NULL, '2026-06-03 16:00:00');
INSERT INTO `course_comment` VALUES (8, 3, 7, '容量只有10人，教练能照顾到每个人，私教级体验', NULL, '2026-06-03 17:30:00');
INSERT INTO `course_comment` VALUES (9, 7, 2, '音乐节奏超带劲，45分钟感觉飞速过去了！', NULL, '2026-06-02 19:30:00');
INSERT INTO `course_comment` VALUES (10, 7, 7, '灯光加音乐完全是演唱会的感觉哈哈，太爽了', NULL, '2026-06-02 20:00:00');
INSERT INTO `course_comment` VALUES (11, 7, 8, '骑完之后腿软但是很爽，已经约了下周的课', NULL, '2026-06-03 10:00:00');
INSERT INTO `course_comment` VALUES (12, 15, 2, '免费课还这么用心，真的太良心了，适合我这种健身小白', NULL, '2026-06-04 18:30:00');
INSERT INTO `course_comment` VALUES (13, 15, 9, '动作讲解很详细，一节课下来出了很多汗，入门必选', NULL, '2026-06-04 19:00:00');
INSERT INTO `course_comment` VALUES (14, 17, 3, '真的20分钟就汗流浃背，Jack教练激励方式很有效', NULL, '2026-06-04 13:00:00');
INSERT INTO `course_comment` VALUES (15, 17, 4, '强度比预期高很多，但成就感爆棚，下次还来！', NULL, '2026-06-04 14:00:00');
INSERT INTO `course_comment` VALUES (16, 10, 8, '打完沙袋压力全消，这门课真的解压神器', NULL, '2026-06-03 17:30:00');
INSERT INTO `course_comment` VALUES (17, 10, 3, '张教练很严格，但都是为我们好，技术进步很快', NULL, '2026-06-03 18:00:00');
INSERT INTO `course_comment` VALUES (18, 1, 4, '同感！我第二天全身酸，感觉每块肌肉都练到了', 1, '2026-06-01 15:00:00');
INSERT INTO `course_comment` VALUES (19, 1, 7, '我也觉得！杰克教练会根据每个人水平调整难度', 2, '2026-06-01 16:30:00');
INSERT INTO `course_comment` VALUES (20, 2, 9, '对！我也是睡前上的，回家倒头就睡，质量特别好', 4, '2026-06-02 21:00:00');
INSERT INTO `course_comment` VALUES (21, 2, 3, '多上几节就好了，Lisa老师会记住你的薄弱点的！', 5, '2026-06-03 09:00:00');
INSERT INTO `course_comment` VALUES (22, 3, 3, '价格还这么亲民，和私教比简直是白菜价', 7, '2026-06-03 18:00:00');
INSERT INTO `course_comment` VALUES (23, 7, 1, '这门课一直很受欢迎，建议早点预约', 9, '2026-06-03 11:00:00');
INSERT INTO `course_comment` VALUES (24, 17, 1, '建议新手先上有氧燃脂操打基础再来挑战这个', 15, '2026-06-04 14:30:00');
INSERT INTO `course_comment` VALUES (25, 17, 9, '我也是！上完之后一直在喘，但是太有成就感了', 14, '2026-06-04 13:45:00');
INSERT INTO `course_comment` VALUES (26, 10, 4, '哈哈同感，打完沙袋感觉什么烦恼都没了', 16, '2026-06-03 18:30:00');
INSERT INTO `course_comment` VALUES (27, 7, 1, '欢迎您的下次参与', 10, '2026-06-05 16:19:24');
INSERT INTO `course_comment` VALUES (28, 7, 1, '下次一起来练，哈哈', 9, '2026-06-07 10:16:09');

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
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程分类',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健身课程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gym_course
-- ----------------------------
INSERT INTO `gym_course` VALUES (1, '燃脂搏击操', '杰克教练', '高强度有氧搏击，融合拳击与舞蹈元素，快速燃烧卡路里', '2026-06-10 18:00:00', 20, 120.00, '2026-02-06 23:39:49', 14, 0, '60分钟燃脂搏击课，全程高强度有氧，帮助塑形并提升心肺功能', '格斗搏击');
INSERT INTO `gym_course` VALUES (2, '舒缓瑜伽', 'Lisa老师', '经典流瑜伽，深度拉伸肌肉，平衡身心，适合所有程度', '2026-06-11 10:00:00', 15, 80.00, '2026-02-06 23:39:49', 13, 0, '60分钟全流程瑜伽练习，涵盖热身、体式序列与冥想收式，改善柔韧性与专注力', '瑜伽冥想');
INSERT INTO `gym_course` VALUES (3, '力量举重', '施瓦辛格', '系统性力量训练，深蹲、硬拉、卧推三大项，增肌增力', '2026-06-12 15:00:00', 10, 60.00, '2026-02-06 23:39:49', 8, 0, '90分钟力量举重专项课，教练全程指导发力技巧与保护方法，适合有基础的学员', '力量训练');
INSERT INTO `gym_course` VALUES (7, '动感单车', 'Michael教练', '跟随音乐节奏骑行，高效燃脂，释放压力', '2026-06-03 19:00:00', 25, 29.90, '2026-06-02 11:55:15', 25, 0, '45分钟高强度骑行，配合音乐灯光，让你在汗水中释放激情', '有氧训练');
INSERT INTO `gym_course` VALUES (8, '普拉提', '王雅婷', '核心肌群精准训练，改善体态与柔韧性', '2026-06-04 09:30:00', 15, 59.90, '2026-06-02 11:55:15', 15, 0, '垫上普拉提，通过精准控制动作强化深层核心肌群，适合久坐办公人群', '功能性训练');
INSERT INTO `gym_course` VALUES (9, '街舞入门', 'Alex老师', '零基础也能跟上，快乐燃脂不枯燥', '2026-06-04 18:30:00', 20, 39.90, '2026-06-02 11:55:15', 20, 0, '流行街舞元素编排，每节课教一段成品舞，轻松有趣又能出汗', '舞蹈健身');
INSERT INTO `gym_course` VALUES (10, '拳击体能', '张教练', '拳法组合+体能训练，释放压力增强爆发力', '2026-06-05 17:00:00', 16, 69.90, '2026-06-02 11:55:15', 16, 0, '包含基础拳法、沙袋训练和体能循环，全面提升心肺与上肢力量', '格斗搏击');
INSERT INTO `gym_course` VALUES (11, 'TRX悬挂训练', '刘教练', '利用自重进行全身功能性训练，雕刻肌肉线条', '2026-06-05 12:00:00', 12, 79.90, '2026-06-02 11:55:15', 11, 0, 'TRX悬挂带训练，通过不稳定平面激活核心，适合有一定基础的健身爱好者', '功能性训练');
INSERT INTO `gym_course` VALUES (12, '战绳', '赵教练', '高强度间歇训练，极速燃脂塑形', '2026-06-06 10:00:00', 10, 89.90, '2026-06-02 11:55:15', 10, 0, '战绳波浪+HIIT组合，30分钟让你体验极限燃脂的快感', '功能性训练');
INSERT INTO `gym_course` VALUES (13, '尊巴', 'Cindy老师', '拉丁舞风有氧操，边跳边瘦快乐加倍', '2026-06-06 16:00:00', 30, 29.90, '2026-06-02 11:55:15', 30, 0, '融合恰恰、桑巴、梅伦格等拉丁舞步，无需舞蹈基础，跟着音乐尽情摇摆', '舞蹈健身');
INSERT INTO `gym_course` VALUES (14, '核心训练', '陈教练', '腹部+下背部专项训练，打造马甲线', '2026-06-07 08:00:00', 20, 49.90, '2026-06-02 11:55:15', 19, 0, '30分钟集中雕刻腹部、侧腰和下背部，高效打造紧致核心', '功能性训练');
INSERT INTO `gym_course` VALUES (15, '有氧燃脂操', '李教练', '中低强度有氧，适合减脂新手', '2026-06-07 18:00:00', 25, 0.00, '2026-06-02 11:55:15', 25, 0, '入门级有氧操，动作简单易学，循序渐进提升心肺功能，免费课程', '有氧训练');
INSERT INTO `gym_course` VALUES (16, '全身拉伸', 'Lisa老师', '深度拉伸放松，缓解肌肉紧张与酸痛', '2026-06-08 20:00:00', 20, 0.00, '2026-06-02 11:55:15', 20, 0, '针对全身各大肌群的系统拉伸，适合训练后放松和日常久坐恢复', '瑜伽冥想');
INSERT INTO `gym_course` VALUES (17, 'HIIT暴汗', 'Jack教练', '高强度间歇训练，20分钟=跑步1小时', '2026-06-08 12:30:00', 15, 99.90, '2026-06-02 11:55:15', 14, 0, '短时高效的极限训练，包含跳跃、深蹲、波比等复合动作，燃脂持续到课后', '有氧训练');
INSERT INTO `gym_course` VALUES (18, '杠铃操', '周教练', '小重量多组数，塑形不粗腿', '2026-06-09 19:30:00', 18, 59.90, '2026-06-02 11:55:15', 18, 0, 'BodyPump风格杠铃操，针对胸、背、臀、腿各部位，小重量高次数塑造紧致线条', '力量训练');
INSERT INTO `gym_course` VALUES (19, '有氧燃脂操', '李雪梅', '中低强度有氧，动作简单，适合减脂新手入门', '2026-06-13 18:00:00', 25, 0.00, '2026-06-02 12:20:34', 25, 0, '入门级有氧操，动作简单易学，循序渐进提升心肺功能，免费体验课', '有氧训练');
INSERT INTO `gym_course` VALUES (20, '全身拉伸', 'Lisa老师', '深度拉伸放松，缓解肌肉紧张，提升关节灵活度', '2026-06-14 20:00:00', 20, 0.00, '2026-06-02 12:20:34', 20, 0, '针对全身各大肌群的系统拉伸，适合训练后放松恢复，久坐族日常调理', '瑜伽冥想');
INSERT INTO `gym_course` VALUES (21, '动感单车', 'Michael教练', '跟随音乐节奏骑行，高效燃脂，充分释放压力', '2026-06-14 19:00:00', 25, 29.90, '2026-06-02 12:20:34', 25, 0, '45分钟高强度骑行，配合专属音乐灯光，让你在汗水中彻底释放激情', '有氧训练');
INSERT INTO `gym_course` VALUES (22, '尊巴', 'Cindy老师', '拉丁舞风有氧操，边跳边瘦，快乐加倍', '2026-06-17 16:00:00', 30, 29.90, '2026-06-02 12:20:34', 30, 0, '融合恰恰、桑巴、梅伦格等拉丁舞步，无需基础，跟着音乐尽情摇摆', '舞蹈健身');
INSERT INTO `gym_course` VALUES (23, '街舞入门', 'Alex老师', '零基础友好，流行元素编排，快乐燃脂不枯燥', '2026-06-18 18:30:00', 20, 39.90, '2026-06-02 12:20:34', 18, 0, '每节课教一段成品舞，融入Hip-Hop与流行舞蹈元素，轻松有趣又出汗', '舞蹈健身');
INSERT INTO `gym_course` VALUES (24, '核心训练', '陈力', '腹部与下背部专项强化，打造马甲线', '2026-06-19 08:00:00', 20, 49.90, '2026-06-02 12:20:34', 20, 0, '30分钟集中雕刻腹部、侧腰和下背部，高效打造紧致核心，无需器械', '功能性训练');
INSERT INTO `gym_course` VALUES (25, '普拉提', '王雅婷', '核心肌群精准训练，改善体态与柔韧性', '2026-06-20 09:30:00', 15, 59.90, '2026-06-02 12:20:34', 15, 0, '垫上普拉提，通过精准控制动作强化深层核心肌群，特别适合久坐办公人群', '功能性训练');
INSERT INTO `gym_course` VALUES (26, '杠铃操', '周昊', '小重量多组数，塑形紧致不粗腿', '2026-06-21 19:30:00', 18, 59.90, '2026-06-02 12:20:34', 18, 0, 'BodyPump风格杠铃操，针对胸、背、臀、腿各部位，小重量高次数塑造紧致线条', '力量训练');
INSERT INTO `gym_course` VALUES (27, '拳击体能', '张磊', '拳法组合与体能训练结合，释放压力同时增强爆发力', '2026-06-24 17:00:00', 16, 69.90, '2026-06-02 12:20:34', 16, 0, '包含基础拳法、沙袋训练和体能循环，全面提升心肺耐力与上肢力量', '格斗搏击');
INSERT INTO `gym_course` VALUES (28, 'TRX悬挂训练', '刘宇轩', '利用自重进行全身功能性训练，雕刻肌肉线条', '2026-06-25 12:00:00', 12, 79.90, '2026-06-02 12:20:34', 12, 0, 'TRX悬挂带训练，通过不稳定平面激活核心，适合有一定基础的健身爱好者', '功能性训练');
INSERT INTO `gym_course` VALUES (29, '战绳', '赵刚', '高强度战绳间歇训练，极速燃脂塑形', '2026-06-26 10:00:00', 10, 89.90, '2026-06-02 12:20:34', 10, 0, '战绳波浪结合HIIT循环，30分钟让你体验极限燃脂的快感，上肢爆发力显著提升', '功能性训练');
INSERT INTO `gym_course` VALUES (30, 'HIIT暴汗', 'Jack教练', '20分钟极限间歇，燃脂效果媲美慢跑1小时', '2026-06-28 12:30:00', 15, 99.90, '2026-06-02 12:20:34', 15, 0, '短时高效极限训练，包含跳跃、深蹲、波比等复合动作，运动后燃脂持续数小时', '有氧训练');
INSERT INTO `gym_course` VALUES (32, '全身拉伸', 'Lisa老师', '深度拉伸放松，缓解肌肉紧张，提升关节灵活度', '2026-06-14 20:00:00', 20, 0.00, '2026-06-02 12:22:06', 20, 0, '针对全身各大肌群的系统拉伸，适合训练后放松恢复，久坐族日常调理', '瑜伽冥想');
INSERT INTO `gym_course` VALUES (33, '动感单车', 'Michael教练', '跟随音乐节奏骑行，高效燃脂，充分释放压力', '2026-06-14 19:00:00', 25, 29.90, '2026-06-02 12:22:06', 25, 0, '45分钟高强度骑行，配合专属音乐灯光，让你在汗水中彻底释放激情', '有氧训练');
INSERT INTO `gym_course` VALUES (34, '尊巴', 'Cindy老师', '拉丁舞风有氧操，边跳边瘦，快乐加倍', '2026-06-17 16:00:00', 30, 29.90, '2026-06-02 12:22:06', 30, 0, '融合恰恰、桑巴、梅伦格等拉丁舞步，无需基础，跟着音乐尽情摇摆', '舞蹈健身');
INSERT INTO `gym_course` VALUES (35, '街舞入门', 'Alex老师', '零基础友好，流行元素编排，快乐燃脂不枯燥', '2026-06-18 18:30:00', 20, 39.90, '2026-06-02 12:22:06', 20, 0, '每节课教一段成品舞，融入Hip-Hop与流行舞蹈元素，轻松有趣又出汗', '舞蹈健身');
INSERT INTO `gym_course` VALUES (36, '核心训练', '陈力', '腹部与下背部专项强化，打造马甲线', '2026-06-19 08:00:00', 20, 49.90, '2026-06-02 12:22:06', 20, 0, '30分钟集中雕刻腹部、侧腰和下背部，高效打造紧致核心，无需器械', '功能性训练');
INSERT INTO `gym_course` VALUES (37, '普拉提', '王雅婷', '核心肌群精准训练，改善体态与柔韧性', '2026-06-20 09:30:00', 15, 59.90, '2026-06-02 12:22:06', 15, 0, '垫上普拉提，通过精准控制动作强化深层核心肌群，特别适合久坐办公人群', '功能性训练');
INSERT INTO `gym_course` VALUES (38, '杠铃操', '周昊', '小重量多组数，塑形紧致不粗腿', '2026-06-21 19:30:00', 18, 59.90, '2026-06-02 12:22:06', 18, 0, 'BodyPump风格杠铃操，针对胸、背、臀、腿各部位，小重量高次数塑造紧致线条', '力量训练');
INSERT INTO `gym_course` VALUES (39, '拳击体能', '张磊', '拳法组合与体能训练结合，释放压力同时增强爆发力', '2026-06-24 17:00:00', 16, 69.90, '2026-06-02 12:22:06', 16, 0, '包含基础拳法、沙袋训练和体能循环，全面提升心肺耐力与上肢力量', '格斗搏击');
INSERT INTO `gym_course` VALUES (40, 'TRX悬挂训练', '刘宇轩', '利用自重进行全身功能性训练，雕刻肌肉线条', '2026-06-25 12:00:00', 12, 79.90, '2026-06-02 12:22:06', 12, 0, 'TRX悬挂带训练，通过不稳定平面激活核心，适合有一定基础的健身爱好者', '功能性训练');
INSERT INTO `gym_course` VALUES (41, '战绳', '赵刚', '高强度战绳间歇训练，极速燃脂塑形', '2026-06-26 10:00:00', 10, 89.90, '2026-06-02 12:22:06', 10, 0, '战绳波浪结合HIIT循环，30分钟让你体验极限燃脂的快感，上肢爆发力显著提升', '功能性训练');
INSERT INTO `gym_course` VALUES (42, 'HIIT暴汗', 'Jack教练', '20分钟极限间歇，燃脂效果媲美慢跑1小时', '2026-06-28 12:30:00', 15, 99.90, '2026-06-02 12:22:06', 15, 0, '短时高效极限训练，包含跳跃、深蹲、波比等复合动作，运动后燃脂持续数小时', '有氧训练');
INSERT INTO `gym_course` VALUES (43, '有氧燃脂操', '刘翔', NULL, '2026-06-22 00:00:00', 20, 50.00, '2026-06-04 12:46:28', 20, NULL, '帅哥刘翔教你在线燃脂编程大帅哥', '有氧训练');
INSERT INTO `gym_course` VALUES (44, '游泳', '东king', NULL, '2026-06-07 14:05:00', 19, 90.00, '2026-06-06 23:05:36', 19, NULL, '东king的游泳课热门必不可缺席', '有氧训练');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(BCrypt哈希加密)',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'user' COMMENT '角色: admin/user',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `balance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '账户余额',
  `vip_type` int(11) NULL DEFAULT 0 COMMENT '会员类型: 0-普通, 1-月卡, 2-年卡',
  `vip_expire_time` datetime NULL DEFAULT NULL COMMENT 'VIP到期时间',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话号码',
  `gender` tinyint(1) NULL DEFAULT NULL COMMENT '性别:       \r\n 0-未知, 1-男, 2-女',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=正常',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$zKq5C5qSWsb4wmdL3EWRjOW0NOgu1HWshHt7obe4rzDtX75E2PDaq', '系统管理员', 'admin', '2026-02-04 16:37:03', 1008.16, 2, '2027-05-19 16:17:36', '18039478806', 1, NULL, 1);
INSERT INTO `sys_user` VALUES (2, 'laixu', '123456', '健身达人', 'user', '2026-02-07 10:24:30', 300.00, 2, NULL, '18039478806', NULL, NULL, 1);
INSERT INTO `sys_user` VALUES (3, 'cd', '$2a$10$vFxQGioM0JUA2e9l0c.sC.YmeugukaGzHimKCKxIMVpeyf/2hAlJK', NULL, 'user', '2026-02-08 21:31:12', 604.19, 1, '2026-07-06 22:22:54', NULL, 1, NULL, 1);
INSERT INTO `sys_user` VALUES (4, '陈东', '$2a$10$V19vrG.UUMePOF4JtE5lle.lCkaC4dA4G6..3sXn4Ac/SOAyORqiy', NULL, 'user', '2026-02-08 21:38:30', 10010.00, 2, NULL, NULL, NULL, NULL, 1);
INSERT INTO `sys_user` VALUES (7, '李子昱', '$2a$10$RDr7rwD2EHC4GIiU7jELse5QcIIeIcIZSzyBCZMKGCeA4uH4hMDGW', NULL, 'user', '2026-05-20 19:27:40', 0.00, 0, NULL, '18039478806', 1, '1311920751@qq.com', 1);
INSERT INTO `sys_user` VALUES (8, '付明斌', '$2a$10$mdQmYmE6BTivPvXr3jNJDuNKOomRg0BUIkMo1lcd3c5Fni.tAZW3y', NULL, 'user', '2026-05-24 13:11:25', 270.00, 1, '2026-06-28 13:51:05', '18039478806', 1, '1311920751@qq.com', 1);
INSERT INTO `sys_user` VALUES (9, '吴锦程', '$2a$10$tURbGsmFMCuYxN3CSwu6b.YAs5rUsZx9HpXmA9yFIozBSWvx54JDS', NULL, 'user', '2026-06-04 22:13:12', 100.00, 0, NULL, '18039478806', NULL, NULL, 1);
INSERT INTO `sys_user` VALUES (10, '吴家辉', '$2a$10$6dmP8eyLAIdSTUfjBaqB9eJ/Jfakltr/iaoTwNbXOF81Yw4hDAU5O', NULL, 'user', '2026-06-05 16:25:34', 200.00, 0, NULL, NULL, NULL, NULL, 1);
INSERT INTO `sys_user` VALUES (11, '赖煦', '$2a$10$cKxQMPNH.IWEnWVSsedmYeCUBBmbPWrqI9UpsbI7CGBWl9vJP7Cl6', NULL, 'user', '2026-06-05 16:32:21', 1000.00, 0, NULL, NULL, NULL, NULL, 1);

-- ----------------------------
-- Table structure for system_notice
-- ----------------------------
DROP TABLE IF EXISTS `system_notice`;
CREATE TABLE `system_notice`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '0=草稿 1=已发布',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_notice
-- ----------------------------
INSERT INTO `system_notice` VALUES (1, '夏季营业时间调整通知', '尊敬的会员，自6月起健身房营业时间调整为：周一至周五 06:00-22:40，周六至周日 07:00-22:00，请合理安排锻炼时间。', 1, '2026-06-05 15:16:39', '2026-06-05 15:16:39');
INSERT INTO `system_notice` VALUES (2, '新增课程：搏击操开班通知', '本月新增搏击操课程，由专业教练授课，每周三、周六下午15:00开课，每期限15人。有意向的会员请尽快在\"预约课程\"中报名！', 1, '2026-06-05 15:16:39', '2026-06-05 15:16:39');
INSERT INTO `system_notice` VALUES (3, '健身房设备维护公告', '定于本周六（6月7日）上午8:00-12:00对部分有氧器械进行例行维护，维护期间相关设备暂停使用，带来不便敬请谅解。', 1, '2026-06-05 15:16:39', '2026-06-05 15:16:39');
INSERT INTO `system_notice` VALUES (4, '端午节活动：会员充值优惠', '端午节期间（6月7日-6月9日），充值满500元赠50元，充值满1000元赠150元，优惠仅限节日期间，欢迎到前台或在线充值。', 1, '2026-06-05 15:16:39', '2026-06-05 15:16:39');
INSERT INTO `system_notice` VALUES (5, '游泳池清洁消毒通知', '为保证水质达标，游泳池将于每月第一个周一进行全面清洁消毒，届时游泳池暂停开放一天，感谢您的理解与配合。', 1, '2026-06-05 15:16:39', '2026-06-05 15:16:39');

SET FOREIGN_KEY_CHECKS = 1;
