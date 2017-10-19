DROP PROCEDURE IF EXISTS proc_report;
DELIMITER $$
CREATE PROCEDURE `proc_report`()
BEGIN
    DECLARE v_item_path varchar(30);
    DECLARE v_year int;
    DECLARE v_month int;
    DECLARE v_id int;
    DECLARE v_name varchar(255);
    DECLARE v_current int;
    DECLARE v_last int;
    DECLARE v_level int;
    DECLARE v_parent int;
    DECLARE v_child_count int;
    
	CREATE TEMPORARY TABLE IF NOT EXISTS report_data 
		select get_lineage(b.item_id) as item_path,
			YEAR(d.issue_receive_date) as `year`, MONTH(d.issue_receive_date) as `month`,
			b.item_id as `id`, c.item_data as `name`, 
			count(d.issue_id) `current`, 
			count(d1.issue_id) `last`,
			b.item_level as `level`, 
			b.item_parent_id as `parent`,
			(select count(*) from crm_mente_item ct where ct.item_parent_id = b.item_id) child_count
		from crm_mente_item_value a
			inner join crm_mente_item b on a.mente_issue_field_value = b.item_id
			inner join crm_mente_option_data_value c on b.item_id = c.item_id
			inner join crm_issue d on a.issue_id = d.issue_id
			left join crm_mente_item b1 on a.mente_issue_field_value = b1.item_id
			left join crm_issue d1 on a.issue_id = d1.issue_id
				and DATE_FORMAT(d1.issue_receive_date, '%Y%C') = CONCAT(YEAR(d.issue_receive_date)-1, MONTH(d.issue_receive_date))
		where c.item_language = 'ja'
			and b.item_name = 'issue_issue_proposal_id'
			and d.issue_receive_date >= '2016-12-01' and d.issue_receive_date < '2017-12-01' 
			and a.company_id = 1
			and b.item_level <= 2
		group by `year`, `month`, b.`item_id`
		order by d.issue_receive_date ASC;
    
	DECLARE csr CURSOR FOR select item_path, year, month, id, name, current, last, level, parent, child_count from report_data;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN csr;
    read_loop: LOOP
    FETCH csr INTO v_item_path, v_year, v_month, v_id, v_name, v_current, v_last, v_level, v_parent, v_child_count;
        
        IF done THEN
            LEAVE read_loop;
        END IF;
        
    END LOOP;
    close csr;
END$$
DELIMITER ;

call proc_report;