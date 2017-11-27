-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DROP FUNCTION IF EXISTS `get_lineage`;
DELIMITER $$

CREATE FUNCTION `get_lineage`(the_id INT, max_level INT) RETURNS text CHARSET utf8
    READS SQL DATA
BEGIN

 DECLARE v_rec INT DEFAULT 0;

 DECLARE done INT DEFAULT FALSE;
 DECLARE v_res text DEFAULT '';
 DECLARE v_papa int;
 DECLARE v_papa_papa int DEFAULT -1;
 DECLARE csr CURSOR FOR 
  select _id,parent_id -- @n:=@n+1 as rownum,T1.* 
  from 
    (SELECT @r AS _id,
        (SELECT @r := item_parent_id FROM crm_mente_item WHERE item_id = _id) AS parent_id,
        @l := @l + 1 AS lvl
    FROM
        (SELECT @r := the_id, @l := 0,@n:=0) vars,
        crm_mente_item m
    WHERE @r <> 0
    ) T1
    where T1.parent_id is not null
 ORDER BY T1.lvl DESC;
 DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    open csr;
    read_loop: LOOP
    fetch csr into v_papa,v_papa_papa;
        SET v_rec = v_rec+1;
        IF done THEN
            LEAVE read_loop;
        END IF;
        -- add first
        IF v_rec = 1 THEN
            SET v_res = v_papa_papa;
        END IF;
        SET v_res = CONCAT(v_res,',',v_papa);
    END LOOP;
    close csr;
    if(v_res = '') then
		SET v_res = the_id;
    end if;
    
    if max_level > 0 then
		SELECT max_level - 1 INTO @max_level;
		SELECT ROUND (   
			(
				LENGTH(v_res)
				- LENGTH( REPLACE ( v_res, ",", "") ) 
			) / LENGTH(",")        
		) INTO @occurs;
        WHILE @occurs < @max_level DO
			SET v_res = CONCAT(v_res,',0');
            SET @occurs = @occurs + 1;
		END WHILE;
    end if;
    
    return v_res;
END