DELIMITER $$
DROP FUNCTION IF EXISTS json_get$$
CREATE FUNCTION json_get(
  json_string TEXT,
  key_string VARCHAR(30)
)
RETURNS VARCHAR(500) DETERMINISTIC
BEGIN 
    DECLARE key_length INT DEFAULT 0;
	SET key_length = LENGTH(key_string) + 3;

	RETURN TRIM(BOTH '"' FROM substring(json_string, locate(concat('"', key_string, '":'),json_string)+key_length, locate(',"', json_string, locate(concat('"', key_string, '":'),json_string))-locate(concat('"', key_string, '":'),json_string)-key_length));
END$$

DELIMITER ;