-- Refs
SELECT * FROM `crm_system_use_auth_rel`;

SELECT * FROM `crm_company`;

SELECT  member_id, member_login_id, member_manager_flag, member_global_flag, group_id FROM crm_member where member_login_id like '%tk003';

SELECT IFNULL(b.company_id, a.company_id) company_id,  a.company_union_key FROM crm_company a LEFT JOIN crm_union_company_rel b on a.company_union_key = b.company_union_key;

select a.* 
from crm_system_use_auth_rel a
	join crm_company b on a.company_id = b.company_id
where b.company_id = 2 and group_member_flag = 1 and group_member_id = 4
;

select * from crm_role b order by b.role_id desc;
select * from crm_role_page_method_rel where role_id = 65;

SELECT DISTINCT a.*
-- c.group_member_flag, c.group_member_id
FROM `crm_union_company_rel` a 
	LEFT JOIN `crm_role` b ON a.`company_id` = b.`company_id` 
	LEFT JOIN `crm_system_use_auth_rel` c  ON b.`role_id` = c.`role_id`
WHERE b.`role_flag` = 0 
	AND c.`group_member_flag` = 1 
	AND c.group_member_id = 62
;

select * from crm_union_company_rel;

select a.* , e.role_flag
from crm_system_use_auth_rel a
	left join crm_union_company_rel b on a.company_id = b.company_id
	left join crm_company c on b.company_union_key = c.company_union_key
	join crm_company d on a.company_id = d.company_id
	join crm_role e on a.role_id = e.role_id
where a.company_id = 2  and a.group_member_id = 4
;

SELECT a.role_id from crm_role a
	inner join crm_system_use_auth_rel b on a.role_id = b.role_id
WHERE a.role_flag = 1  and b.group_member_flag = 1 and b.company_id = 2 and b.group_member_id = 62;

select member_id, member_login_id from crm_member where group_id = 36;


-- ###################################################################################
-- Main SQL
SELECT a.* FROM
(
	SELECT DISTINCT
		IFNULL(
			`mbr`.`member_id`,
			`mbr1`.`member_id`
		) AS `member_id`,
		IF(EXISTS( --  Kiem tra member trong cong ty khong co role an
			SELECT a.`role_id` FROM `crm_role` a
				INNER JOIN `crm_system_use_auth_rel` b on a.`role_id` = b.`role_id`
			WHERE a.`role_flag` = 1  AND b.`group_member_flag` = 1 AND b.`company_id` = `com`.`company_id` 
				AND b.`group_member_id` = IFNULL(`mbr`.`member_id`, `mbr1`.`member_id`)
		), `main`.`company_id`, `com`.`company_id`) `company_id`,
		`crm_role`.`role_flag` AS `role_flag`,
		`mdl`.`module_name` AS `module_name`,
		`pag`.`page_name` AS `page_name`,
		`mtd`.`method_name` AS `method_name`
	FROM
		`crm_system_use_auth_rel` `main`
		
		-- Xu ly cho truong hop quyen cua member
		LEFT JOIN `crm_member` `mbr` ON `mbr`.`member_id` = `main`.`group_member_id`
			AND `main`.`group_member_flag` = 1
			AND `mbr`.`member_deleted` = 0

		-- Xu ly cho truong hop quyen cua group
		LEFT JOIN `crm_group` `grp` ON `grp`.`group_id` = `main`.`group_member_id`
			AND `main`.`group_member_flag` = 0
			AND `grp`.`group_deleted` = 0	
		LEFT JOIN `crm_member` `mbr1` ON `mbr1`.`group_id` = `grp`.`group_id`
			AND `mbr1`.`member_deleted` = 0

		-- Xu ly cho truong hop cong ty group
		LEFT JOIN  ( -- Chuyen thanh `view_union_company`
			SELECT DISTINCT a.`company_id`, a.`company_union_key` , c.`group_member_id` `member_id`
			FROM `crm_union_company_rel` a 
				INNER JOIN `crm_role` b ON a.`company_id` = b.`company_id` 
				INNER JOIN `crm_system_use_auth_rel` c  ON b.`role_id` = c.`role_id`
				INNER JOIN `crm_member` d ON c.group_member_id = d.member_id
			WHERE b.`role_deleted` = 0 AND b.`role_flag` = 0 AND c.`group_member_flag` = 1 AND d.member_global_flag = 1
		) `union` ON `main`.company_id = `union`.company_id AND `union`.`member_id` = IFNULL(`mbr`.`member_id`, `mbr1`.`member_id`)
		JOIN `crm_company` `com` ON ((`union`.company_union_key IS NOT NULL AND `com`.`company_union_key` = `union`.company_union_key) OR (`union`.company_union_key IS NULL AND `com`.`company_id` = `main`.company_id) )

		-- Lien ket vo bang danh sach role, module, controller, method
		JOIN `crm_role` ON `main`.`role_id` = `crm_role`.`role_id`
		JOIN `crm_role_page_method_rel` `rol_rel2` ON `rol_rel2`.`role_id` = `main`.`role_id`
		JOIN `crm_page` `pag` ON `pag`.`page_id` = `rol_rel2`.`page_id`
		JOIN `crm_method` `mtd` ON `mtd`.`method_id` = `rol_rel2`.`method_id`
		JOIN `crm_page_method_rel` `pag_rel` ON `pag_rel`.`page_id` = `pag`.`page_id`
			AND `pag_rel`.`method_id` = `mtd`.`method_id`
		JOIN `crm_module_page_rel` `mod_rel` ON `mod_rel`.`page_id` = `pag`.`page_id`
		JOIN `crm_system_module` `mdl` ON `mdl`.`module_id` = `mod_rel`.`module_id`
	WHERE
		`crm_role`.`role_deleted` = 0
		AND `pag`.`page_deleted` = 0
		AND `mdl`.`module_deleted` = 0
		AND `com`.`company_deleted` = 0
) a
WHERE a.member_id = 64
and a.company_id = 2
;

-- ###############################################################################################






-- ORG SQL
SELECT DISTINCT
        IFNULL(`mbr`.`member_id`, `mbr1`.`member_id`) AS `member_id`,
        `main`.`company_id` AS `company_id`,
        `crm_role`.`role_flag` AS `role_flag`,
        `mdl`.`module_name` AS `module_name`,
        `pag`.`page_name` AS `page_name`,
        `mtd`.`method_name` AS `method_name`
    FROM
        (((((((((((`crm_system_use_auth_rel` `main`
        JOIN `crm_company` `com` ON ((`com`.`company_id` = `main`.`company_id`)))
        LEFT JOIN `crm_group` `grp` ON (((`grp`.`group_id` = `main`.`group_member_id`)
            AND (`main`.`group_member_flag` = 0)
            AND (`grp`.`group_deleted` = 0))))
        LEFT JOIN `crm_member` `mbr` ON (((`mbr`.`member_id` = `main`.`group_member_id`)
            AND (`main`.`group_member_flag` = 1)
            AND (`mbr`.`member_deleted` = 0))))
        LEFT JOIN `crm_member` `mbr1` ON (((`mbr1`.`group_id` = `grp`.`group_id`)
            AND (`mbr1`.`member_deleted` = 0))))
        JOIN `crm_role` ON ((`main`.`role_id` = `crm_role`.`role_id`)))
        JOIN `crm_role_page_method_rel` `rol_rel2` ON ((`rol_rel2`.`role_id` = `main`.`role_id`)))
        JOIN `crm_page` `pag` ON ((`pag`.`page_id` = `rol_rel2`.`page_id`)))
        JOIN `crm_method` `mtd` ON ((`mtd`.`method_id` = `rol_rel2`.`method_id`)))
        JOIN `crm_page_method_rel` `pag_rel` ON (((`pag_rel`.`page_id` = `pag`.`page_id`)
            AND (`pag_rel`.`method_id` = `mtd`.`method_id`))))
        JOIN `crm_module_page_rel` `mod_rel` ON ((`mod_rel`.`page_id` = `pag`.`page_id`)))
        JOIN `crm_system_module` `mdl` ON ((`mdl`.`module_id` = `mod_rel`.`module_id`)))
    WHERE
        ((`crm_role`.`role_deleted` = 0)
            AND (`pag`.`page_deleted` = 0)
            AND (`mdl`.`module_deleted` = 0)
            AND (`com`.`company_deleted` = 0))