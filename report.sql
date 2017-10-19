select * from crm_issue;

select a.item_id, b.item_data, a.item_level, 
	get_lineage(a.item_id) as item_path
 from crm_mente_item a
	inner join crm_mente_option_data_value b on a.item_id = b.item_id
where a.item_name = 'issue_issue_product_id' AND b.item_language = 'ja'
order by item_path ASC;

select * from crm_mente_item;

select get_lineage(25);

select a.*
from crm_mente_item_value a 
where a.mente_issue_item_name = 'issue_issue_product_id';

select YEAR(d.issue_receive_date) as `year`, MONTH(d.issue_receive_date) as `month`,
	b.item_id as `id`, c.item_data as `name`, 
    count(d.issue_id) `current`, count(d1.issue_id) `last`,
    b.item_level as `level`, b.item_parent_id as `parent`,
    get_lineage(b.item_id) as `path`
from crm_mente_item_value a 
	inner join crm_mente_item b on a.mente_issue_field_value = b.item_id
    inner join crm_mente_option_data_value c on b.item_id = c.item_id
    inner join crm_issue d on a.issue_id = d.issue_id
    
    left join crm_mente_item b1 on a.mente_issue_field_value = b1.item_id
    left join crm_issue d1 on a.issue_id = d1.issue_id 
		and DATE_FORMAT(d1.issue_receive_date, '%Y%C') = CONCAT(YEAR(d.issue_receive_date)-1, MONTH(d.issue_receive_date)) 
    
where a.mente_issue_item_name = 'issue_issue_product_id' AND c.item_language = 'ja'
	and d.issue_receive_date >= '2016-11-01' and d.issue_receive_date < '2017-11-01'
group by `year`, `month`, b.`item_id`
order by d.issue_receive_date ASC, level DESC;

