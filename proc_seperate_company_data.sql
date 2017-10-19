/**
* Duplicate table for seperated company 
* with moving data from old table to new one 
* and revert data from new table to master table
*
* @author hungpham
* @since 2016/11/16
*/
DROP PROCEDURE IF EXISTS proc_seperate_company_data;
DELIMITER //
CREATE PROCEDURE proc_seperate_company_data(IN company_id INT, IN duplicate_tables VARCHAR(1000))
BEGIN
	-- declare tables which want to duplicate schema & data
	-- DECLARE duplicate_tables VARCHAR(1000) DEFAULT CONCAT(
	-- 	'crm_mail_attachment, crm_cust_target_info, crm_customer', 
	-- 	', crm_issue, crm_issue_cust_rel, crm_item, crm_div, crm_tab'
	-- 	', crm_item_global, crm_mail_data, crm_mente_item', 
	-- 	', crm_mente_option_data_value, crm_multiple_data_value', 
	-- 	', crm_project_cust_search_list, crm_project_role_rel', 
	-- 	', crm_property_item_label, crm_page_tab, crm_page_tab_div_item_rel');
	
    DECLARE is_seperate INT DEFAULT 0;
    DECLARE tbl VARCHAR(200) DEFAULT '';
    DECLARE data_transferedList VARCHAR(1000) DEFAULT '';
    DECLARE table_exists BOOLEAN DEFAULT FALSE;
    DECLARE tbl_index INT DEFAULT 0;
    
    DECLARE sql_list LONGTEXT DEFAULT 'SET FOREIGN_KEY_CHECKS = 0; SET SQL_SAFE_UPDATES = 0;';
    
    -- temp variable for DELETE DATA CASE
    DECLARE revert_complex_data_sql TEXT DEFAULT '';
    DECLARE revert_data_sql_prefix TEXT DEFAULT '';
    
    DECLARE error_list LONGTEXT DEFAULT '';
    
    -- Duplicate sql exception errors hander
    DECLARE _rollback BOOL DEFAULT 0;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION 
		BEGIN
			SET _rollback = 1;
			GET DIAGNOSTICS CONDITION 1
			@s_error = MESSAGE_TEXT;
            
            SET error_list = CONCAT(error_list, @s_error, '\r\n');
			-- for debug
			-- INSERT INTO my_log (error_message) 
			-- SELECT @p2;
		END;
    
    -- get seperate-data's flag
    SELECT a.company_seperate_data INTO is_seperate FROM crm_company a WHERE a.company_id = company_id; 
    
    -- create table code with pattern XXX
    SELECT LPAD(company_id, 3, '0') INTO @new_table_code;
    
    WHILE duplicate_tables != '' DO

		-- get table name
		SET tbl = TRIM(SUBSTRING_INDEX(duplicate_tables, ',', 1));
        
        -- create table suffix code
        SELECT CONCAT(tbl, '_', @new_table_code) INTO @new_tbl_name;
        
        SELECT EXISTS(
			SELECT 1 FROM information_schema.TABLES 
			WHERE TABLE_SCHEMA = (SELECT DATABASE() FROM DUAL) 
				AND TABLE_NAME = @new_tbl_name
			) INTO table_exists;
        
        IF is_seperate THEN
			-- create new table with name's pattern old_table_<XXX>
			SET sql_list = CONCAT(sql_list, 'CREATE TABLE IF NOT EXISTS ', @new_tbl_name, ' LIKE ', tbl, '; ');
            
            -- check company_id column exists
            SELECT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() 
				AND TABLE_NAME = tbl AND COLUMN_NAME = 'company_id') 
			INTO @column_exist;
            
            -- update data
			-- !IMPORTANT
            SET sql_list = CONCAT(sql_list, 'INSERT INTO ', @new_tbl_name, ' SELECT * FROM ', tbl);
            
			-- optimize data for each table not have column name as `company_id`
			CASE tbl
				WHEN 'crm_issue_cust_rel' THEN
				BEGIN
					SET sql_list = CONCAT(sql_list, ' WHERE issue_id IN (SELECT issue_id FROM crm_issue WHERE company_id = ', company_id,')');
                    SET sql_list = CONCAT(sql_list, ' AND cust_id IN (SELECT cust_id FROM crm_customer WHERE company_id = ', company_id,')');
				END;
				WHEN 'crm_item_global' THEN
				BEGIN
					SET sql_list = CONCAT(sql_list, ' WHERE item_id IN (SELECT item_id FROM crm_item WHERE company_id = ', company_id,')');
				END;
                ELSE -- else case for all table which include `company_id` inside
					SET sql_list = CONCAT(sql_list, IF(@column_exist, CONCAT(' WHERE company_id = ', company_id), ''));
			END CASE;
			SET sql_list = CONCAT(sql_list, ';');
            
            -- delete data from origin table
            IF @column_exist THEN
				SET sql_list = CONCAT(sql_list, 'DELETE FROM ', tbl, ' WHERE company_id = ', company_id, ';');
			ELSE
				SET sql_list = CONCAT(sql_list, 'DELETE FROM ', tbl);
				CASE tbl
					WHEN 'crm_issue_cust_rel' THEN
					BEGIN
						SET sql_list = CONCAT(sql_list, ' WHERE issue_id IN (SELECT issue_id FROM crm_issue WHERE company_id = ', company_id,')');
						SET sql_list = CONCAT(sql_list, ' AND cust_id IN (SELECT cust_id FROM crm_customer WHERE company_id = ', company_id,')');
					END;
					WHEN 'crm_item_global' THEN
					BEGIN
						SET sql_list = CONCAT(sql_list, ' WHERE item_id IN (SELECT item_id FROM crm_item WHERE company_id = ', company_id,')');
					END;
					ELSE BEGIN END;
				END CASE;
                SET sql_list = CONCAT(sql_list, ';');
            END IF;
			-- /update data
            
            IF table_exists = FALSE THEN
				SET data_transferedList = CONCAT(data_transferedList, IF(tbl_index = 0, '', ', '), tbl);
                SET tbl_index = tbl_index + 1;
            END IF;
        ELSE
			IF table_exists = TRUE THEN
            -- revert data
			-- !IMPORTANT
            CASE 
				WHEN tbl IN ('crm_mail_attachment', 'crm_cust_target_info', 'crm_issue', 
						'crm_customer', 'crm_item', 'crm_mail_data', 'crm_project_cust_search_list',
                        'crm_page_tab', 'crm_div', 'crm_tab') THEN
                BEGIN
					SELECT `COLUMN_NAME` into @primary_key FROM INFORMATION_SCHEMA.`COLUMNS`
					WHERE (`TABLE_SCHEMA` = database())
					  AND (`TABLE_NAME` = tbl) AND (`COLUMN_KEY` = 'PRI');
					
					SELECT GROUP_CONCAT(a.COLUMN_NAME) INTO @columns
 					FROM INFORMATION_SCHEMA.COLUMNS a
 					WHERE a.table_schema = database() AND a.table_name = tbl
 						AND a.column_name != @primary_key;
                        
					SET sql_list = CONCAT(sql_list, ' ALTER TABLE ', tbl,' ADD tmp_id_', @new_table_code, ' INT NULL;');
                    SET sql_list = CONCAT(sql_list, ' INSERT INTO ', tbl,'(',@columns,', tmp_id_', @new_table_code,') SELECT ',@columns,',', @primary_key,' FROM ', @new_tbl_name, ';');
                    
                    SET revert_data_sql_prefix = CONCAT(revert_data_sql_prefix, ' ALTER TABLE ', tbl,' DROP COLUMN tmp_id_', @new_table_code, ';');
                    
                END;
                WHEN tbl = 'crm_mente_item' THEN
                BEGIN
					SELECT GROUP_CONCAT(a.COLUMN_NAME) INTO @columns
					FROM INFORMATION_SCHEMA.COLUMNS a
					WHERE a.table_schema = database() AND a.table_name = tbl
						AND a.column_name != 'item_id';
                        
					SET sql_list = CONCAT(sql_list, ' ALTER TABLE ', tbl,' ADD tmp_id_', @new_table_code, ' INT NULL;');
                    SET sql_list = CONCAT(sql_list, ' INSERT INTO ', tbl,'(',@columns,', tmp_id_', @new_table_code,') SELECT ',@columns,', item_id FROM ', @new_tbl_name, ';');
                    
                    -- update parent reference
                    SET sql_list = CONCAT(sql_list, 
						' UPDATE ', tbl, ' a',
							' INNER JOIN ', tbl,' b ON a.item_parent_id = b.tmp_id_', @new_table_code, 
						' SET a.item_parent_id = b.item_id 
						  WHERE a.company_id = ', company_id,' AND b.tmp_id_', @new_table_code, ' IS NOT NULL;');
                          
                    SET revert_data_sql_prefix = CONCAT(revert_data_sql_prefix, ' ALTER TABLE ', tbl,' DROP COLUMN tmp_id_', @new_table_code, ';');
                    
                END;
                WHEN tbl = 'crm_property_item_label' THEN
                BEGIN
                    SET sql_list = CONCAT(sql_list, ' INSERT INTO ', tbl,' SELECT * FROM ', @new_tbl_name, ';');
                END;
                
                -- OTHER COMPLEX CASE
                WHEN tbl = 'crm_mente_option_data_value' THEN
                BEGIN
                
					SELECT GROUP_CONCAT(a.COLUMN_NAME) INTO @columns
					  FROM INFORMATION_SCHEMA.COLUMNS a
					  WHERE a.table_schema = database() AND a.table_name = tbl AND a.column_name != 'item_id';
					
					SET revert_complex_data_sql = CONCAT(revert_complex_data_sql, 
						' INSERT INTO ', tbl,'(',@columns,', item_id) 
							SELECT a.', REPLACE(@columns, ',', ',a.') ,', b.tmp_id_', @new_table_code,' FROM ', @new_tbl_name, ' a 
								LEFT JOIN crm_mente_item b 
									ON a.item_id = b.tmp_id_', @new_table_code,'
							WHERE b.company_id = ', company_id,';');
					
                END;
                WHEN tbl = 'crm_issue_cust_rel' THEN
                BEGIN
					SET revert_complex_data_sql = CONCAT(revert_complex_data_sql, 
					'insert into ', tbl,'
					select b.issue_id, c.cust_id from ', @new_tbl_name, ' a
					inner join (select issue_id, tmp_id_', @new_table_code,' from crm_issue where company_id = ', company_id,') b
						on a.issue_id = b.issue_id
					inner join (select cust_id, tmp_id_', @new_table_code,' from crm_customer where company_id = ', company_id,') c
						on a.cust_id = c.cust_id', 
					';');
                    
                END;
                WHEN tbl = 'crm_item_global' THEN
                BEGIN
					SELECT GROUP_CONCAT(a.COLUMN_NAME) INTO @columns
					  FROM INFORMATION_SCHEMA.COLUMNS a
					  WHERE a.table_schema = database() AND a.table_name = tbl AND a.column_name != 'item_id';
                      
					SET revert_complex_data_sql = CONCAT(revert_complex_data_sql, 
					'insert into ', tbl,'(', @columns,', item_id)
					select a.', REPLACE(@columns, ',', ',a.') ,', b.item_id from ', @new_tbl_name, ' a
						inner join crm_item b on a.item_id = b. tmp_id_', @new_table_code,'
					where b.company_id = ', company_id, 
					';');
                END;
                WHEN tbl = 'crm_multiple_data_value' THEN
                BEGIN
					SET revert_complex_data_sql = CONCAT(revert_complex_data_sql, 
					'insert into ', tbl,'(target_id, page_id, page_type, item_id, 
						item_data, company_id, creator_id, created_time, updated_id, updated_time)
					select a.target_id, c.page_id, a.page_type, b.item_id, 
						a.item_data, a.company_id, a.creator_id, a.created_time, a.updated_id, a.updated_time
					from ', @new_tbl_name, ' a
						inner join crm_item b on a.item_id = b.tmp_id_', @new_table_code,'
						inner join crm_page_tab c on a.page_id = c.tmp_id_', @new_table_code,'
					where b.company_id = ', company_id,' 
						and c.company_id = ', company_id, 
					';');
					
                END;
                WHEN tbl = 'crm_page_tab_div_item_rel' THEN
                BEGIN
					SET revert_complex_data_sql = CONCAT(revert_complex_data_sql, 
					'insert into ', tbl,'(page_id, tab_id, div_id, item_id, company_id, item_order, div_order)
					select b.page_id, d.tab_id, c.div_id, e.item_id, a.company_id, a.item_order, a.div_order 
					from ', @new_tbl_name, ' a
						inner join crm_page_tab b on a.page_id = b.tmp_id_', @new_table_code,'
						inner join crm_div c on a.div_id = b.tmp_id_', @new_table_code,'
						inner join crm_tab d on a.tab_id = b.tmp_id_', @new_table_code,'
						inner join crm_item e on a.item_id = e.tmp_id_', @new_table_code,'
					where b.company_id = ', company_id,' 
						and c.company_id = ', company_id,' 
						and d.company_id = ', company_id,' 
						and e.company_id = ', company_id, 
					';');
                END;
                WHEN tbl = 'crm_project_role_rel' THEN
                BEGIN
					SELECT GROUP_CONCAT(a.COLUMN_NAME) INTO @columns
					  FROM INFORMATION_SCHEMA.COLUMNS a
					  WHERE a.table_schema = database() AND a.table_name = tbl AND a.column_name != 'list_id';
                      
					SET revert_complex_data_sql = CONCAT(revert_complex_data_sql, 
					'insert into ', tbl,'(', @columns,', list_id)
					select a.', REPLACE(@columns, ',', ',a.') ,', b.list_id
					from ', @new_tbl_name, ' a
						inner join crm_project_cust_search_list b on a.list_id = b.tmp_id_', @new_table_code,'
					where b.company_id = ', company_id, 
					';');
                END;
				-- comment for test
                -- ELSE BEGIN END;
			END CASE;
            -- /revert data
            
            -- drop table
            SET revert_data_sql_prefix = CONCAT(revert_data_sql_prefix, 'DROP TABLE IF EXISTS ', @new_tbl_name, '; ');
            
            SET data_transferedList = CONCAT(data_transferedList, IF(tbl_index = 0, '', ', '), tbl);
			SET tbl_index = tbl_index + 1;
            END IF;
        END IF;
        
        -- update duplicate_tables variable
		IF LOCATE(',', duplicate_tables) > 0 THEN
		  SET duplicate_tables = SUBSTRING(duplicate_tables, LOCATE(',', duplicate_tables) + 1);
		ELSE
		  SET duplicate_tables = '';
		END IF;
    
	END WHILE;
    
    IF is_seperate = FALSE THEN
        SET sql_list = CONCAT(sql_list, revert_complex_data_sql);
        SET sql_list = CONCAT(sql_list, revert_data_sql_prefix);
    END IF;
    
    SET sql_list = CONCAT(sql_list, 'SET FOREIGN_KEY_CHECKS = 1; SET SQL_SAFE_UPDATES = 1;');
    
    -- select sql_list;
    
    -- execute sql_list;
    START TRANSACTION;
	WHILE sql_list != '' DO
 		CALL exec_query(TRIM(SUBSTRING_INDEX(sql_list, ';', 1)));
 		
 		IF LOCATE(';', sql_list) > 0 THEN
 		  SET sql_list = SUBSTRING(sql_list, LOCATE(';', sql_list) + 1);
 		ELSE
 		  SET sql_list = '';
 		END IF;
	END WHILE;
    
    IF _rollback THEN
		select error_list as `ERROR`;
        ROLLBACK;
    ELSE
        COMMIT;
        SELECT data_transferedList as `SUCCESS`;
    END IF;
END //
DELIMITER ;

-- testing
-- CALL proc_seperate_company_data(1);