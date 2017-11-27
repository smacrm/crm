delimiter //
CREATE TRIGGER upd_quicksearch BEFORE UPDATE ON crm_quick_search
    FOR EACH ROW
    BEGIN
		SET NEW.quick_search_binary = multibyte2cv(NEW.quick_search_binary);
    END;//
CREATE TRIGGER ins_quicksearch BEFORE INSERT ON crm_quick_search
    FOR EACH ROW
    BEGIN
		SET NEW.quick_search_binary = multibyte2cv(NEW.quick_search_binary);
    END;//
delimiter ;
