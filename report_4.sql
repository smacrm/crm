select get_lineage(a.mente_issue_field_value) as item_path, 
	a.issue_id, a.mente_issue_item_name, a.mente_issue_field_level
from crm_mente_item_value a
where ((a.mente_issue_item_name = 'issue_issue_proposal_id' and a.mente_issue_field_level <= 2)
	or (a.mente_issue_item_name = 'issue_issue_product_id' and a.mente_issue_field_level = 1))
    and a.issue_id = 276
-- group by a.issue_id
order by a.issue_id asc, a.mente_issue_item_name desc;
;
