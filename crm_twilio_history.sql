CREATE TABLE `crmcloud`.`crm_twilio_history` (
  `twilio_history_id` INT NOT NULL AUTO_INCREMENT,
  `twilio_conference_id` VARCHAR(45) NULL,
  `twilio_agent_id` VARCHAR(45) NULL,
  `twilio_call_start_time` VARCHAR(45) NULL,
  `twilio_call_finish_time` VARCHAR(45) NULL,
  `twilio_status` VARCHAR(45) NULL,
  `twilio_company_id` VARCHAR(45) NULL,
  PRIMARY KEY (`twilio_history_id`));
  
ALTER TABLE `crmcloud`.`crm_twilio` 
ADD COLUMN `twilio_from` VARCHAR(20) NOT NULL AFTER `twilio_conference_id`;
