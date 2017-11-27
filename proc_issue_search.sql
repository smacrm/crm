/**
* Advance search function for issue table
*
* @author hungpham
* @since 2016/11/15
*/
DROP PROCEDURE IF EXISTS proc_issue_search;
DELIMITER //
CREATE PROCEDURE proc_issue_search(IN company_id INT, IN display_columns TEXT, IN select_conditions TEXT)
BEGIN
	-- crm_issue alias is a
    -- crm_multiple_data_value alias is b
    declare static_cols TEXT DEFAULT '';
    declare dynamic_cols TEXT DEFAULT '';

    DECLARE dynamic_col_index INT DEFAULT 0;
    DECLARE static_col_index INT DEFAULT 0;
    DECLARE col varchar(200);
    DECLARE has_id_col BOOLEAN DEFAULT FALSE;
    DECLARE has_proposal_id_col BOOLEAN DEFAULT FALSE;

    WHILE display_columns != '' DO

		-- get column name
		SET col = TRIM(SUBSTRING_INDEX(display_columns, ',', 1));

        -- check column name for static or dynamic type
        IF col REGEXP '^dynamic_([0-9]+)$' THEN
			SET dynamic_cols = CONCAT(dynamic_cols, REPLACE(col, 'dynamic_', IF(dynamic_col_index = 0, '', ',')));
            SET dynamic_col_index = dynamic_col_index + 1;
        ELSE
			IF(has_id_col = FALSE AND col = 'issue_id') THEN
				SET has_id_col = TRUE;
            END IF;
            IF(has_proposal_id_col = FALSE AND col = 'issue_proposal_level_id') THEN
				SET has_proposal_id_col = TRUE;
            END IF;
            IF(col != 'issue_proposal_level_id') THEN
				SET static_cols = CONCAT(static_cols, IF(static_col_index = 0, '', ','), 'a.', col);
				SET static_col_index = static_col_index + 1;
            END IF;
		END IF;

        -- update display_columns variable
		IF LOCATE(',', display_columns) > 0 THEN
		  SET display_columns = SUBSTRING(display_columns, LOCATE(',', display_columns) + 1);
		ELSE
		  SET display_columns = '';
		END IF;
	END WHILE;

    SELECT '' INTO @sql;

    IF dynamic_cols != '' THEN
		SELECT CONCAT('select
		group_concat(
			DISTINCT CONCAT(''max(if (b.item_id='''''', b.item_id, '''''', b.item_data, '''''''')) as ''''dynamic_'',b.item_id, '''''' '')
		) into @sql from crm_multiple_data_value a inner join crm_company c on a.company_id = c.company_id right join crm_page_tab_div_item_rel b on a.item_id = b.item_id AND b.company_id = ', company_id, ' WHERE a.company_id = ', company_id,' and c.company_deleted = 0 AND b.item_id IN (', dynamic_cols, ')') INTO @tmp_sql;

		-- select @tmp_sql;

		PREPARE dynamic_stmt FROM @tmp_sql;

		EXECUTE dynamic_stmt;
		DEALLOCATE PREPARE dynamic_stmt;
    END IF;

	set @sql = concat('select (select mente_issue_field_value from crm_mente_item_value 
			where mente_issue_item_name = ''issue_proposal_id''
				and mente_issue_field_level = 1
				and issue_id = a.issue_id limit 1) issue_proposal_level_id,', IF(has_id_col,'', 'a.issue_id, '), static_cols, IF(@sql != '', concat(', ', @sql), ''), ' from crm_issue a left join crm_multiple_data_value b on a.issue_id = b.target_id AND b.company_id = ', company_id,' WHERE a.issue_deleted = 0 AND a.company_id = ', company_id,' group by a.issue_id',
		IF(ISNULL(select_conditions) OR select_conditions = '', '', CONCAT(' HAVING ', select_conditions)),
        ';');

	-- select @sql;

	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END //
DELIMITER ;

-- testing
CALL proc_issue_search(1, 'issue_id, dynamic_225, dynamic_226, dynamic_234', '');
