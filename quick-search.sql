DROP TABLE IF EXISTS crm_quick_search;
CREATE TABLE crm_quick_search (
            quick_search_id int(11) NOT NULL AUTO_INCREMENT ,
            quick_search_module varchar(150),
            quick_search_content TEXT,
            quick_search_cols TEXT,
            quick_search_target_id int(11) NOT NULL ,
            quick_search_deleted tinyint(4) NOT NULL ,
            FULLTEXT (quick_search_content),
            PRIMARY KEY (quick_search_id)
            ) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4;