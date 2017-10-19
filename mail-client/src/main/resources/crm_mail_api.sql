-- ----------------------------
-- Table structure for `crm_company`
-- ----------------------------
DROP TABLE IF EXISTS `crm_company`;
CREATE TABLE `crm_company` (
  `company_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '会社ID',
  `company_name` varchar(80) NOT NULL DEFAULT '' COMMENT '名前',
  `company_post` varchar(8) DEFAULT NULL COMMENT '郵便番号',
  `company_city` int(11) DEFAULT NULL COMMENT '都道府県',
  `company_address` varchar(150) DEFAULT NULL COMMENT '住所',
  `company_address_kana` varchar(200) DEFAULT NULL COMMENT '住所カナ',
  `company_logo` varchar(150) DEFAULT NULL COMMENT 'ロゴ',
  `company_home_page` varchar(150) DEFAULT NULL COMMENT 'ホームページ',
  `company_copy_right` varchar(150) DEFAULT NULL COMMENT 'Copyright',
  `company_layout` varchar(30) DEFAULT NULL COMMENT 'システムレイアウト',
  `company_global_ip` varchar(120) DEFAULT NULL COMMENT '会社グロバールIP',
  `company_global_group_flag` tinyint(4) DEFAULT '0' COMMENT '会社グルーブフラグ、１は有効',
  `company_union_key` varchar(100) DEFAULT '0' COMMENT 'グルーブ会社IDキー',
  `company_business_flag` tinyint(4) NOT NULL DEFAULT '1' COMMENT '業界フラグ「１は相談室、２店舗、３保険、。。。」',
  `company_memo` varchar(500) DEFAULT NULL COMMENT 'メモ',
  `company_global_locale` tinyint(4) DEFAULT NULL COMMENT '１は他言語使用可能',
  `company_table_distinction` tinyint(4) NOT NULL DEFAULT '0',
  `company_basic_login_id` varchar(30) NOT NULL COMMENT 'Basic認証ID',
  `company_basic_password` varchar(70) NOT NULL COMMENT 'Basic認証パスワード',
  `company_seperate_data` tinyint(4) DEFAULT '0' COMMENT '1: 別のたープルに保存する\n0: 一緒にたープルに保存する',
  `company_deleted` tinyint(4) DEFAULT '0' COMMENT '存在フラグ、１は削除',
  `creator_id` int(11) DEFAULT NULL COMMENT '作成者',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日',
  `updated_id` int(11) DEFAULT NULL COMMENT '更新者',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日',
  `company_two_factor` varchar(45) DEFAULT '0',
  PRIMARY KEY (`company_id`),
  UNIQUE KEY `company_basic_login_id_UNIQUE` (`company_basic_login_id`),
  KEY `company_global_ip` (`company_global_ip`),
  KEY `crm_company_delete_idx` (`company_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='会社情報';

-- ----------------------------
-- Table structure for `crm_group`
-- ----------------------------
DROP TABLE IF EXISTS `crm_group`;
CREATE TABLE `crm_group` (
  `group_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'グループID',
  `group_name` varchar(45) NOT NULL DEFAULT '' COMMENT '名前',
  `group_parent_id` int(11) DEFAULT NULL COMMENT 'グループ親ID',
  `group_tree_id` varchar(500) DEFAULT NULL COMMENT 'グループツリー',
  `group_order` int(11) DEFAULT NULL COMMENT '表示順',
  `group_memo` varchar(500) DEFAULT NULL COMMENT 'メモ',
  `group_deleted` tinyint(4) DEFAULT '0' COMMENT '存在フラグ、１は削除',
  `company_id` int(11) NOT NULL DEFAULT '0' COMMENT '会社ID',
  `creator_id` int(11) DEFAULT NULL COMMENT '作成者',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日',
  `updated_id` int(11) DEFAULT NULL COMMENT '更新者',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日',
  PRIMARY KEY (`group_id`),
  KEY `cons_company` (`company_id`) USING BTREE,
  CONSTRAINT `FK_crm_group_company_id` FOREIGN KEY (`company_id`) REFERENCES `crm_company` (`company_id`),
  CONSTRAINT `cons_company` FOREIGN KEY (`company_id`) REFERENCES `crm_company` (`company_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='グループ情報';

-- ----------------------------
-- Table structure for `crm_member`
-- ----------------------------
DROP TABLE IF EXISTS `crm_member`;
CREATE TABLE `crm_member` (
  `member_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'メンバーID',
  `member_login_id` varchar(30) DEFAULT NULL COMMENT 'ログインID',
  `member_code` varchar(30) DEFAULT NULL COMMENT 'メンバーコード',
  `member_password` varchar(60) DEFAULT NULL COMMENT 'パスワード',
  `member_name_first` varchar(70) NOT NULL DEFAULT '' COMMENT '名前１',
  `member_name_last` varchar(70) NOT NULL DEFAULT '' COMMENT '名前２',
  `member_kana_first` varchar(70) DEFAULT NULL COMMENT 'カナ１',
  `member_kana_last` varchar(70) DEFAULT NULL COMMENT 'カナ２',
  `member_post` varchar(20) DEFAULT '' COMMENT '郵便番号',
  `member_city` int(11) DEFAULT NULL COMMENT '都道府県',
  `member_address` varchar(150) DEFAULT NULL COMMENT '住所',
  `member_address_kana` varchar(200) DEFAULT NULL COMMENT '住所カナ',
  `member_image` varchar(150) DEFAULT NULL COMMENT 'メンバー写真',
  `member_layout` varchar(20) DEFAULT NULL COMMENT 'システムレイアウト',
  `member_firewall` tinyint(4) DEFAULT '0' COMMENT 'FW許可、会社情報にGrobalIP設定した場合、１は外から接続NG',
  `member_global_flag` tinyint(4) DEFAULT '0' COMMENT 'グールブ会社データ使用フラグ',
  `member_manager_flag` tinyint(4) DEFAULT '0' COMMENT '１は管理者、０は一般メンバー',
  `member_global_locale` tinyint(4) DEFAULT NULL COMMENT '１は他言語使用可能',
  `member_memo` varchar(500) DEFAULT NULL COMMENT 'メモ',
  `member_order` int(11) DEFAULT NULL COMMENT '表示順',
  `member_deleted` tinyint(4) DEFAULT '0' COMMENT '存在フラグ、１は削除',
  `group_id` int(11) NOT NULL DEFAULT '0' COMMENT 'グルーブID',
  `creator_id` int(11) DEFAULT NULL COMMENT '作成者',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日',
  `updated_id` int(11) DEFAULT NULL COMMENT '更新者',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日',
  `member_using_twofa` tinyint(4) DEFAULT '0',
  `member_secret` varchar(45) DEFAULT NULL,
  `member_last_login_time` timestamp NULL DEFAULT NULL,
  `member_last_logout_time` timestamp NULL DEFAULT NULL,
  `member_reset_pwd_datetime` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `member_login_id_UNIQUE` (`member_login_id`),
  KEY `crm_member_group_idx` (`group_id`) USING BTREE,
  CONSTRAINT `FK_crm_member_group_id` FOREIGN KEY (`group_id`) REFERENCES `crm_group` (`group_id`),
  CONSTRAINT `cons_group_user` FOREIGN KEY (`group_id`) REFERENCES `crm_group` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COMMENT='メンバー情報';

-- ----------------------------
-- Table structure for `crm_mail_server`
-- ----------------------------
DROP TABLE IF EXISTS `crm_mail_server`;
CREATE TABLE `crm_mail_server` (
  `server_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'サーバID',
  `server_name` varchar(100) DEFAULT NULL COMMENT 'サーバ名',
  `server_host` varchar(100) DEFAULT NULL COMMENT 'ドメイン名',
  `server_port` int(11) DEFAULT '25' COMMENT 'ポート',
  `server_smtp` varchar(100) DEFAULT NULL COMMENT 'SMTPドメイン名',
  `server_smtp_port` int(6) DEFAULT NULL COMMENT 'SMTPポート',
  `server_auth` enum('true','false') DEFAULT 'false' COMMENT 'SMTP認証',
  `server_ssl` enum('true','false') DEFAULT 'false' COMMENT 'SSL認証',
  `server_charset` varchar(255) DEFAULT 'utf-8' COMMENT '文字コード',
  `server_header` varchar(255) DEFAULT '7bit' COMMENT 'Header文字コード',
  `server_format` varchar(255) DEFAULT 'text' COMMENT 'フォマット',
  `server_same_receive_mail` tinyint(1) DEFAULT '0' COMMENT 'サーバID',
  `server_type` varchar(6) DEFAULT NULL COMMENT 'サーバタイプ「SMTP、IMAP、POP3。。。」',
  `server_memo` varchar(500) DEFAULT NULL COMMENT 'メモ',
  `server_deleted` tinyint(4) DEFAULT '0' COMMENT '存在フラグ、１は削除',
  `company_id` int(11) NOT NULL DEFAULT '0' COMMENT '会社ID',
  `creator_id` int(11) DEFAULT NULL COMMENT '作成者',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日',
  `updated_id` int(11) DEFAULT NULL COMMENT '更新者',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日',
  PRIMARY KEY (`server_id`),
  KEY `fk_crm_mail_server_company_idx` (`company_id`),
  CONSTRAINT `fk_crm_mail_server_company` FOREIGN KEY (`company_id`) REFERENCES `crm_company` (`company_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='メールサーバテーブル情報';

-- ----------------------------
-- Table structure for `crm_mail_account`
-- ----------------------------
DROP TABLE IF EXISTS `crm_mail_account`;
CREATE TABLE `crm_mail_account` (
  `account_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'アカウントID',
  `account_name` varchar(45) NOT NULL DEFAULT '' COMMENT 'アカウント表示名',
  `account_send_flag` tinyint(4) DEFAULT '0' COMMENT '１は送信可能',
  `account_receive_flag` tinyint(4) DEFAULT '0' COMMENT '１は受信可能',
  `acount_support` tinyint(4) DEFAULT '0' COMMENT '依頼',
  `acount_request` tinyint(4) DEFAULT '0' COMMENT '対応',
  `account_mail_address` varchar(70) NOT NULL DEFAULT '' COMMENT 'メールアドレス',
  `account_user_name` varchar(70) NOT NULL DEFAULT '' COMMENT 'アカウント名',
  `account_password` varchar(30) NOT NULL DEFAULT '' COMMENT 'アカウントパスワード',
  `account_delete_received_days` int(11) DEFAULT '0' COMMENT '受信BOXに設定した日数自動削除',
  `account_order` int(11) DEFAULT '0' COMMENT '表示順',
  `acount_memo` varchar(500) DEFAULT NULL COMMENT 'メモ',
  `account_is_deleted` tinyint(4) DEFAULT '0' COMMENT '存在フラグ、１は削除',
  `server_id` int(11) NOT NULL DEFAULT '0' COMMENT 'サーバID',
  `company_id` int(11) NOT NULL DEFAULT '0' COMMENT '会社ID',
  `creator_id` int(11) DEFAULT NULL COMMENT '作成者',
  `created_time` timestamp NULL DEFAULT NULL COMMENT '作成日',
  `updated_id` int(11) DEFAULT NULL COMMENT '更新者',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '更新日',
  PRIMARY KEY (`account_id`),
  KEY `cons_server_account` (`server_id`),
  KEY `cons_company_account` (`company_id`),
  CONSTRAINT `cons_company_account` FOREIGN KEY (`company_id`) REFERENCES `crm_company` (`company_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `cons_server_account` FOREIGN KEY (`server_id`) REFERENCES `crm_mail_server` (`server_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='メールアカウント情報';

-- ----------------------------
-- Table structure for `crm_server`
-- ----------------------------
DROP TABLE IF EXISTS `crm_server`;
CREATE TABLE `crm_server` (
  `server_id` int(11) NOT NULL AUTO_INCREMENT,
  `server_name` varchar(150) NOT NULL,
  `server_type` varchar(150) DEFAULT NULL,
  `server_flag` int(11) DEFAULT NULL,
  `server_folder` varchar(150) DEFAULT NULL,
  `server_host` varchar(150) DEFAULT NULL,
  `server_port` int(11) DEFAULT NULL,
  `server_username` varchar(150) DEFAULT NULL,
  `server_password` varchar(150) DEFAULT NULL,
  `server_ssl` tinyint(4) DEFAULT NULL,
  `server_protocol` varchar(150) DEFAULT NULL,
  `server_deleted` tinyint(4) NOT NULL,
  `company_id` int(11) NOT NULL,
  `creator_id` int(11) NOT NULL,
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_id` int(11) DEFAULT NULL,
  `updated_time` timestamp NULL DEFAULT NULL,
  `server_gnext` tinyint(4) DEFAULT NULL,
  `server_memo` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`server_id`),
  KEY `fk_crm_server_company` (`company_id`),
  CONSTRAINT `constraint_server_company` FOREIGN KEY (`company_id`) REFERENCES `crm_company` (`company_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `crm_attachment`
-- ----------------------------
DROP TABLE IF EXISTS `crm_attachment`;
CREATE TABLE `crm_attachment` (
  `attachment_id` int(11) NOT NULL AUTO_INCREMENT,
  `attachment_name` varchar(150) NOT NULL,
  `attachment_hash_name` varchar(100) DEFAULT NULL,
  `attachment_extension` varchar(10) DEFAULT NULL,
  `attachment_mime_type` varchar(100) DEFAULT NULL,
  `attachment_path` varchar(254) DEFAULT NULL,
  `attachment_file_size` varchar(16) DEFAULT NULL,
  `attachment_deleted` tinyint(4) NOT NULL,
  `attachment_target_type` int(11) NOT NULL,
  `attachment_target_id` int(11) NOT NULL,
  `server_id` int(11) NOT NULL,
  `company_id` int(11) NOT NULL,
  `creator_id` int(11) NOT NULL,
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_id` int(11) DEFAULT NULL,
  `updated_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`attachment_id`),
  KEY `fk_crm_attachment_company` (`company_id`),
  KEY `fk_crm_attachment_server` (`server_id`),
  CONSTRAINT `constraint_attachment_company` FOREIGN KEY (`company_id`) REFERENCES `crm_company` (`company_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `constraint_attachment_server` FOREIGN KEY (`server_id`) REFERENCES `crm_server` (`server_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `crm_mail_data`
-- ----------------------------
DROP TABLE IF EXISTS `crm_mail_data`;
CREATE TABLE `crm_mail_data` (
  `mail_data_id` int(10) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `mail_data_account_id` int(10) NOT NULL COMMENT 'ID Account mail',
  `mail_data_account_name` varchar(200) DEFAULT NULL COMMENT 'Tên account mail',
  `mail_data_mail_server` varchar(200) DEFAULT NULL COMMENT 'Server mail',
  `mail_data_unique_id` varchar(500) NOT NULL COMMENT 'ID unique',
  `mail_data_header` longtext COMMENT 'header',
  `mail_data_subject` varchar(1000) NOT NULL COMMENT 'Subject',
  `mail_data_from` mediumtext NOT NULL COMMENT 'Người gửi',
  `mail_data_to` mediumtext NOT NULL COMMENT 'Người nhận',
  `mail_data_cc` mediumtext COMMENT 'CC',
  `mail_data_bcc` mediumtext COMMENT 'BCC',
  `mail_data_datetime` datetime DEFAULT NULL COMMENT 'Ngày giờ gửi',
  `mail_data_size` int(10) NOT NULL COMMENT 'size',
  `mail_data_priority` int(10) DEFAULT NULL COMMENT 'Độ khái quát',
  `mail_data_reply_to_address` mediumtext COMMENT 'Địa chỉ nhận mail reply',
  `mail_data_reply_sender_address` mediumtext COMMENT 'Address của người gửi dùng để reply mail',
  `mail_data_reply_return_path` longtext COMMENT 'Path reply',
  `mail_data_body` longtext NOT NULL COMMENT 'Nội dung',
  `mail_data_attach_display` longtext COMMENT 'Tên file đính kèm (Dùng search)',
  `mail_data_attachreal_file` varchar(500) DEFAULT NULL COMMENT 'Tên file đính kèm（Dùng save）',
  `mail_data_attach_file_type` varchar(500) DEFAULT NULL COMMENT 'Phân loại file đính kèm',
  `mail_data_person_id` varchar(20) DEFAULT NULL COMMENT 'Code người phụ trách',
  `mail_data_folder_code` varchar(50) DEFAULT NULL COMMENT 'Code Folder',
  `mail_data_folder_name` varchar(250) DEFAULT NULL COMMENT 'Tên folder',
  `mail_data_issue_relation_flag` int(1) DEFAULT NULL COMMENT 'Flag issue hóa',
  `mail_data_issue_id` varchar(30) DEFAULT NULL COMMENT 'Mã issue',
  `mail_data_delete_flag` int(1) DEFAULT '0' COMMENT 'Flag delete',
  `mail_data_from_standard` varchar(100) DEFAULT NULL,
  `mail_data_is_history` tinyint(1) DEFAULT '0',
  `mail_data_is_read` tinyint(1) DEFAULT '0' COMMENT '1 : 既読 , 0 : 未読',
  `mail_data_mail_explode_id` int(10) DEFAULT NULL,
  `mail_data_mail_sent` tinyint(1) DEFAULT '0',
  `company_id` int(10) NOT NULL COMMENT '会社ID',
  `creator_id` int(10) NOT NULL COMMENT 'Id người tạo',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày giờ tạo',
  `updated_id` int(10) DEFAULT NULL COMMENT 'Id người update',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày giờ update',
  PRIMARY KEY (`mail_data_id`),
  KEY `fk_crm_mail_data_company_idx` (`company_id`),
  KEY `fk_crm_mail_data_account_idx` (`mail_data_account_id`),
  FULLTEXT KEY `fk_crm_mail_data_search_idx` (`mail_data_subject`,`mail_data_body`),
  CONSTRAINT `fk_crm_mail_data_account` FOREIGN KEY (`mail_data_account_id`) REFERENCES `crm_mail_account` (`account_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_crm_mail_data_company` FOREIGN KEY (`company_id`) REFERENCES `crm_company` (`company_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4;

ALTER TABLE `crm_attachment` DROP FOREIGN KEY `constraint_attachment_server`;
ALTER TABLE `crm_attachment` CHANGE COLUMN `server_id` `server_id` INT(11) NULL ;
ALTER TABLE `crmcloud`.`crm_attachment` ADD CONSTRAINT `constraint_attachment_server` FOREIGN KEY (`server_id`) REFERENCES `crmcloud`.`crm_server` (`server_id`) ON DELETE CASCADE ON UPDATE NO ACTION;

INSERT INTO `crm_company` VALUES ('1', '株式会社ジーネクスト（G-NEXT）', null, null, 'dsfgdsfgsdfg', 'dfgsdfgdsfgdsfg', '1-20151108_164055577.jpg', null, '&copy;2016 GNEXT | All rights reserved.', 'skin-blue', null, '1', '20160913170000', '1', 'asdfasdfasdf', null, '0', 'admin1', '$2a$10$8K5MiDkCGUJBBQEXEyYuIO3wACU7vZh8vlMdaWDcsfQBA6Yh3bGRu', '0', '0', '1', '2016-10-03 11:50:07', '1', '2017-02-16 17:01:41', '0');
INSERT INTO `crm_group` VALUES ('1', '開発部 1', null, null, null, 'メモ', '0', '1', '1', '2016-10-03 11:50:08', '1', '2017-01-05 16:22:51');
INSERT INTO `crm_member` VALUES ('1', 'GN001admin', null, '$2a$10$/ZYUgIHOwWR/fDYV2m7CaOK44SOuQvkoQ3GTaNqmU4pKDtjnj/Teq', 'ジーネクスト', '株式会社', null, null, null, null, null, null, '1-background.jpg', 'skin-blue', '0', '1', '0', null, null, null, '0', '1', '1', '2016-10-03 11:50:08', '1', '2017-02-28 14:59:42', '0', null, '2016-12-16 18:28:44', '2017-02-28 14:59:42', null);
INSERT INTO `crm_mail_server` VALUES ('1', 'GMAIL', 'imap.gmail.com', '993', 'smtp.gmail.com', '465', 'true', 'true', null, null, null, null, 'IMAP', null, '0', '1', '1', '2017-01-12 18:12:20', '1', '2017-01-12 18:28:43');
INSERT INTO `crm_mail_account` VALUES ('1', 'kienvnext', '1', '1', '1', '1', 'kienvnext@gmail.com', 'kienvnext', 'Daithukien', null, null, null, '0', '1', '1', '1', '2017-01-12 18:13:55', '1', '2017-01-17 17:31:32');
INSERT INTO `crm_server` VALUES ('1', 'VNEXT_SERVER_001', 'FTP', '3', '/home/ftpuser/', '192.168.1.165', '21', 'ftpuser', 'Kien123456.', '0', 'SSL', '0', '1', '1', '2017-02-15 16:50:58', null, null, '1', null);
