DROP VIEW IF EXISTS `view_system_basic_auth`;
CREATE VIEW `view_system_basic_auth` AS
    SELECT 
        `company_basic_login_id` AS `basic_login_id`,
        `company_basic_password` AS `basic_group_password`,
        `company_id` AS `company_id`
    FROM
        `crm_company`;