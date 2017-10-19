ALTER TABLE `crmcloud`.`crm_mail_attachment` 
CHANGE COLUMN `attachment_mime_type` `attachment_mime_type` VARCHAR(300) NULL DEFAULT '' COMMENT 'Mimeタイプ' ;
