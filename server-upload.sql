CREATE TABLE IF NOT EXISTS `crm_command` (
  `command_id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
   command_type int(11) NOT NULL COMMENT 'コマンドタイプ',
   command_name VARCHAR(100) NOT NULL COMMENT '名前',
   command_value VARCHAR(2000) NOT NULL COMMENT '値',
   command_json VARCHAR(2000) COMMENT '値JSON',
   command_memo VARCHAR(500) COMMENT 'メモ',
  `command_deleted` tinyint DEFAULT 0 COMMENT '削除',
  `company_id` int(11) NOT NULL DEFAULT 0 COMMENT '会社ID',
  `creator_id` int(11) NOT NULL DEFAULT 0 COMMENT '作成者',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
  `updated_id` int(11) DEFAULT NULL COMMENT '更新者',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
  PRIMARY KEY (`command_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT 'バッチコマンド情報';

ALTER TABLE `crmcloud`.`crm_mail_data` 
CHANGE COLUMN `mail_data_issue_code` `mail_data_issue_id` VARCHAR(30) NULL DEFAULT NULL COMMENT 'Mã issue' ;

ALTER TABLE `crmcloud`.`crm_mail_data` 
DROP COLUMN `mail_data_person_name`,
CHANGE COLUMN `mail_data_person_code` `mail_data_person_id` VARCHAR(20) NULL DEFAULT NULL COMMENT 'Code người phụ trách' ;

CREATE TABLE IF NOT EXISTS `crm_mail_person` (
  `mail_person_id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `mail_person_in_charge_id` int NOT NULL DEFAULT 0 COMMENT '依頼者ID',
  `mail_person_order` int DEFAULT NULL COMMENT '表示順',
  `mail_person_deleted` tinyint DEFAULT 0 COMMENT '存在フラグ、１は削除',
  `company_id` int(11) NOT NULL DEFAULT 0 COMMENT '会社ID',
  `creator_id` int(11) NOT NULL DEFAULT 0 COMMENT '作成者',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日',
  `updated_id` int(11) DEFAULT NULL COMMENT '更新者',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日',
  PRIMARY KEY (`mail_person_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT '依頼者情報';

CREATE TABLE crm_batch (
            batch_id int(11) NOT NULL AUTO_INCREMENT ,
            batch_name varchar(150) NOT NULL ,
            batch_group varchar(150) NOT NULL ,
	    batch_command varchar(150) NOT NULL,
            batch_cron varchar(150) NOT NULL,
            batch_flag tinyint(4) ,
            batch_status int(11) ,
            batch_memo VARCHAR(500),
            batch_deleted tinyint(4) NOT NULL ,
            company_id int(11) NOT NULL ,
            creator_id int(11) NOT NULL ,
            created_time timestamp NOT NULL ,
            updated_id int(11) ,
            updated_time timestamp NULL ,
start_date timestamp NULL ,
pause_date timestamp NULL ,
            PRIMARY KEY (batch_id),
            KEY fk_crm_batch_company (company_id),
	    CONSTRAINT constraint_batch_company FOREIGN KEY (company_id) REFERENCES crm_company (company_id) ON DELETE CASCADE ON UPDATE NO ACTION
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*
    server_flag:
        1 - COMMON (LOGO, MEMBER, MAIL, ISSUE, SOUND(SOFTPHONE)) /home/crmcloud/upload/
        2 - SOUND /home/crmcloud/upload/sound
        3 - MAIL  /home/crmcloud/upload/mail
*/
DROP TABLE IF EXISTS crm_server;
CREATE TABLE crm_server (
            server_id int(11) NOT NULL AUTO_INCREMENT ,
            server_name varchar(150) NOT NULL ,
	    server_type varchar(150),
            server_flag int(11),/* chuyen sang kieu integer */
            server_folder varchar(150),
	    server_host varchar(150),
	    server_port int(11),
	    server_username varchar(150),
	    server_password varchar(150),
	    server_ssl tinyint(4),
	    server_protocol varchar(150),
            server_deleted tinyint(4) NOT NULL ,
            company_id int(11) NOT NULL ,
            creator_id int(11) NOT NULL ,
            created_time timestamp NOT NULL ,
            updated_id int(11) ,
            updated_time timestamp NULL ,
            PRIMARY KEY (server_id),
            KEY fk_crm_server_company (company_id),
	    CONSTRAINT constraint_server_company FOREIGN KEY (company_id) REFERENCES crm_company (company_id) ON DELETE CASCADE ON UPDATE NO ACTION
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
ALTER TABLE `crmcloud`.`crm_server`  ADD COLUMN `server_gnext` TINYINT(4) NULL AFTER `updated_time`;
insert into crm_server(server_name,server_type,server_flag,server_folder,server_host,server_port,server_username,server_password,server_ssl,server_protocol,server_deleted,company_id,creator_id,server_gnext)
 values('GNEXT_SERVER','FTP',1,'/home/crmcloud/upload/','192.168.1.165',21,'crmcloud','Kien123456.',1,"SSL",0,1,1,1);
insert into crm_server(server_name,server_type,server_flag,server_folder,server_host,server_port,server_username,server_password,server_ssl,server_protocol,server_deleted,company_id,creator_id,server_gnext)
 values('GNEXT_SERVER','FTP',1,'/home/crmcloud/upload/','192.168.1.165',21,'ftpuser','Kien123456.',0,null,0,1,1,1);

DROP TABLE IF EXISTS crm_attachment;
CREATE TABLE crm_attachment (
            attachment_id int(11) NOT NULL AUTO_INCREMENT ,
            attachment_name varchar(150) NOT NULL ,
            attachment_hash_name varchar(100) ,
            attachment_extension varchar(10) ,
            attachment_mime_type varchar(100) ,
            attachment_path varchar(254) ,
            attachment_file_size varchar(16) ,
            attachment_deleted tinyint(4) NOT NULL ,
            attachment_target_type int(11) NOT NULL,
            attachment_target_id int(11) NOT NULL,
	    server_id int(11) NOT NULL,
            company_id int(11) NOT NULL ,
            creator_id int(11) NOT NULL ,
            created_time timestamp NOT NULL ,
            updated_id int(11) ,
            updated_time timestamp NULL ,
            PRIMARY KEY (attachment_id),
            KEY fk_crm_attachment_company (company_id),
	    CONSTRAINT constraint_attachment_company FOREIGN KEY (company_id) REFERENCES crm_company (company_id) ON DELETE CASCADE ON UPDATE NO ACTION,
	    KEY fk_crm_attachment_server (server_id),
	    CONSTRAINT constraint_attachment_server FOREIGN KEY (server_id) REFERENCES crm_server (server_id) ON DELETE CASCADE ON UPDATE NO ACTION
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;