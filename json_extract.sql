DELIMITER $$
DROP FUNCTION IF EXISTS json_extract$$
CREATE FUNCTION json_extract(
  json_string TEXT,
  key_string VARCHAR(30),
  value_condition TEXT 
)
RETURNS VARCHAR(500) DETERMINISTIC
BEGIN 
    DECLARE num_found INT DEFAULT 0;
    DECLARE loop_idx INT DEFAULT 1;
    DECLARE split_char VARCHAR(1) DEFAULT '}';
    DECLARE json_item_string TEXT;
    DECLARE value_string TEXT;

	SET num_found = ROUND (   
        (
            LENGTH(json_string)
            - LENGTH( REPLACE ( json_string, split_char, '') ) 
        ) / LENGTH(split_char)        
    );

  	check_value_loop: WHILE loop_idx <= num_found DO
    	SET json_item_string = SPLIT_STR(json_string, split_char, loop_idx);
    	
      IF (json_get(json_item_string, 'value') = value_condition) THEN
        SET value_string = json_get(json_item_string, key_string);
        LEAVE check_value_loop;
      END IF;
    	
      SET loop_idx = loop_idx + 1;
  	END WHILE;

	RETURN value_string;
END$$

DELIMITER ;