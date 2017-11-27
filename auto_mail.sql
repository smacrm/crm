select * from crm_issue where issue_id in(311, 308);

select * from crm_auto_mail;
select * from crm_auto_mail_mente;
select * from crm_auto_mail_member;
select * from crm_auto_mail_sent_history;

select * from view_auto_mail where issue_view_code = '1706000001';

 -- view_auto_mail

SELECT a.issue_id, a.issue_view_code, b.auto_config_id, b.option_id, f.member_id, f.type, a.company_id
	-- , '|||', a.issue_receive_date, b.`mode`,  b.`day`, b.`hour`, DATE(DATE_ADD(a.issue_receive_date, INTERVAL b.`day` DAY)) , DATE(NOW()) , HOUR(NOW()) 
FROM crm_issue a
	INNER JOIN crm_auto_mail b ON a.issue_status_id = b.item_id AND a.company_id = b.company_id
	INNER JOIN crm_auto_mail_mente c ON b.auto_config_id = c.auto_id
	INNER JOIN crm_mente_item_value d 
		ON a.issue_id = d.issue_id -- AND d.mente_issue_item_name = 'issue_proposal_id' AND d.mente_issue_field_level = 1  -- Lay ve proposal level 1 cua issue
			AND c.item_id = d.mente_issue_field_value -- Link giua bang cau hinh auto mail cho mente va mente level 1 cua issue
	INNER JOIN crm_mente_item e ON d.mente_issue_field_value = e.item_id -- link voi bang meta cua mente, kiem tra mente khong bi xoa
	INNER JOIN crm_auto_mail_member f ON b.auto_config_id = f.auto_id -- lien ket voi bang member de lay danh sach member gui mail
WHERE a.issue_deleted = 0  -- kiem tra issue khong bi xoa
	-- AND a.company_id = 1  -- kiem ta xu ly o cong ty nao !! THAM SO
	AND e.item_deleted = 0 -- kiem tra mente khong bi xoa
	AND (
		( -- voi truong hop mode = 0 va flag auto_send = TRUE -> Gui luon va chi gui 1 lan va khong can check ngay thang va lich su gui
			b.`mode` = 0 and b.`auto_send` = 1
		)
		OR
		( -- voi truong hop mode = 0 va flag auto_send = FALSE -> chi gui mail 1 lan vao ngay xac dinh, cach nay issue_receive_date mot khoang thoi gian cau hinh trong crm_auto_mail
			b.`mode` = 0 and b.`auto_send` = 0
			AND DATE(DATE_ADD(a.issue_receive_date, INTERVAL b.`day` DAY)) = DATE(NOW()) -- ngay hien tai bang ngay issue_receive_date cong voi ngay duoc cau hinh trong crm_auto_mail
			AND b.auto_config_id NOT IN (SELECT h.auto_id FROM crm_auto_mail_sent_history h WHERE h.company_id = a.company_id and h.issue_id = a.issue_id)-- Kiem tra truoc do da gui chua
			AND b.`hour` = HOUR(NOW()) -- Kiem tra GIO (hour) gui bang thoi gian hien tai
		)
		OR
		(  -- voi truong hop mode = 1 -> tu ngay issue_receive_date mot khoang thoi gian cau hinh trong crm_auto_mail, thi ngay nao cung gui mail
			b.`mode` = 1
			AND DATE(a.issue_receive_date) <= DATE(NOW()) -- ngay hien tai lon hon hoac bang ngay issue_receive_date
			AND DATE(DATE_ADD(a.issue_receive_date, INTERVAL b.`day` DAY)) >= DATE(NOW()) -- ngay hien tai nho hon hoac bang ngay issue_receive_date cong voi ngay duoc cau hinh trong bang crm_auto_mail
			AND b.`hour` = HOUR(NOW()) -- Kiem tra GIO (hour) gui bang thoi gian hien tai
		)
	)
	AND a.issue_view_code = '1706000001' -- Test 1 issue
GROUP BY a.issue_id
HAVING count(*) = (SELECT COUNT(*) FROM crm_auto_mail_mente g WHERE g.auto_id = b.auto_config_id ) -- Danh sach mente cau hinh trong auto mail phai trung khop voi danh sach mente (proposal & product) co trong issue
;
