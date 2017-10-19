CREATE TABLE `crmcloud`.`crm_twilio_config` (
  `twilio_id` INT NOT NULL AUTO_INCREMENT,
  `company_id` INT NOT NULL,
  `phone_number` VARCHAR(20) NOT NULL,
  `application_sid` VARCHAR(45) NOT NULL,
  `api_sid` VARCHAR(45) NOT NULL,
  `api_secret` VARCHAR(45) NOT NULL,
  `account_sid` VARCHAR(45) NOT NULL,
  `account_auth_token` VARCHAR(45) NOT NULL,
  `creator_id` VARCHAR(45) NOT NULL,
  `created_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_id` VARCHAR(45) NULL,
  `updated_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`twilio_id`),
  INDEX `fk_crm_twilio_config_company_idx` (`company_id` ASC),
  UNIQUE INDEX `phone_number_UNIQUE` (`phone_number` ASC),
  CONSTRAINT `fk_crm_twilio_config_company`
    FOREIGN KEY (`company_id`)
    REFERENCES `crmcloud`.`crm_company` (`company_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
