CREATE TABLE `crmcloud`.`crm_project_cust_column_width` (
  `column_id` VARCHAR(45) NOT NULL,
  `column_width` INT UNSIGNED NOT NULL DEFAULT 0,
  `company_id` INT NOT NULL,
  `updated_id` INT NOT NULL,
  `updated_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`column_id`));
