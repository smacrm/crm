DELIMITER //

DROP FUNCTION IF EXISTS `dedup_item_path` //
CREATE FUNCTION `dedup_item_path` (in_str LONGTEXT) RETURNS LONGTEXT
DETERMINISTIC
NO SQL
BEGIN

-- http://dba.stackexchange.com/questions/87144/remove-duplicate-terms-from-column
-- given a comma-separated string of values, return a comma-separated string of
-- unique values found in the list

DECLARE out_str LONGTEXT DEFAULT NULL; -- pending output
DECLARE next_str TEXT DEFAULT NULL;    -- next element under consideration

dedup:
LOOP

  IF LENGTH(TRIM(in_str)) = 0 OR in_str IS NULL THEN
    LEAVE dedup; -- no more data to consider
  END IF;

  SET next_str = SUBSTRING_INDEX(in_str,',',1);                   -- find the next element
  SET in_str = SUBSTRING(in_str FROM (LENGTH(next_str) + 1 + 1)); -- remove that element

  SET in_str = TRIM(in_str), next_str = TRIM(next_str); -- trim the new and the rest

  IF FIND_IN_SET(next_str,out_str) OR LENGTH(next_str) = 0 THEN -- if empty or already found
    ITERATE dedup;
  END IF;

  SET out_str = CONCAT_WS(',',out_str,next_str); -- append the new to pending output 

END LOOP;

RETURN out_str;

END //

DELIMITER ;