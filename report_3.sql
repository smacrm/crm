--  get header
-- ----- type = 1

select REPLACE(get_lineage(a.item_id, 2), ',', '-') as item_id
from crm_mente_item a 
where a.item_name = 'issue_issue_proposal_id' AND a.item_level <= 2 and a.company_id = 1
order by item_id DESC;

-- get product
select a.item_id
from crm_mente_item a
where a.item_name = 'issue_issue_product_id' AND a.item_level = 1 and a.company_id = 1
order by item_id DESC;
            
-- ----- type = 2
-- het header level 1-2

select REPLACE(get_lineage(a.item_id, (SELECT max(a1.item_level) from crm_mente_item a1 where a1.item_name = a.item_name and a1.company_id = a.company_id)), ',', '-') as item_id
from crm_mente_item a 
where a.item_name = 'issue_issue_proposal_id' and a.company_id = 1
order by item_id DESC;

-- =============================================================
-- Dem so luong issue theo thang

-- ----- type = 1
select REPLACE(
	CASE ROUND (
        (
            LENGTH(a.item_path)
            - LENGTH( REPLACE ( a.item_path, ',', '') ) 
        ) / LENGTH(',')        
    ) 
    WHEN 3 THEN REPLACE ( a.item_path, ',0', '')
    WHEN 2 THEN a.item_path 
    WHEN 1 THEN CONCAT(a.item_path, '-0')
    WHEN 0 THEN CONCAT(a.item_path, '-0-0') END, ',', '-'
    ) as id,
	YEAR(a.issue_receive_date) as `year`, MONTH(a.issue_receive_date) as `month`,
	count(a.issue_id) `current`,
    count(b.issue_id) `last`
from 
	(
		select
			dedup_item_path(group_concat(
				if( (a.mente_issue_item_name = 'issue_issue_product_id') or 
					(a.mente_issue_item_name = 'issue_issue_proposal_id' and a.mente_issue_field_level = 2)
				, if(a.mente_issue_item_name = 'issue_issue_product_id', 
					concat('0,',get_lineage(a.mente_issue_field_value, 0)), 
					get_lineage(a.mente_issue_field_value, 0)), 
				a.mente_issue_field_value
				)order by mente_issue_item_name desc)
			) as item_path, 
			a.issue_id,
            d.issue_receive_date
		from crm_mente_item_value a
			inner join crm_mente_item b on a.mente_issue_field_value = b.item_id
			inner join crm_mente_option_data_value c on b.item_id = c.item_id
			inner join crm_issue d on a.issue_id = d.issue_id
		where ((a.mente_issue_item_name = 'issue_issue_proposal_id' and a.mente_issue_field_level <= 2)
			 or (a.mente_issue_item_name = 'issue_issue_product_id' and a.mente_issue_field_level = 1))
			 and d.issue_receive_date >= '2016-12-01' and d.issue_receive_date < '2017-12-01' 
			 and a.company_id = 1
		group by a.issue_id
		order by a.issue_id asc 
    ) a
    left join crm_issue b on a.issue_id = b.issue_id
			and DATE_FORMAT(b.issue_receive_date, '%Y%C') = CONCAT(YEAR(a.issue_receive_date)-1, MONTH(a.issue_receive_date))
group by `year`, `month`, a.`item_path`
order by a.issue_receive_date ASC;

-- ----- type = 2
select a.id, a.year, a.month, sum(current) current, a.last
from(
select
	YEAR(d.issue_receive_date) as `year`, MONTH(d.issue_receive_date) as `month`,
	REPLACE(get_lineage(b.item_id, (select max(a1.item_level) from crm_mente_item a1 where a1.item_name = b.item_name and a1.company_id = a.company_id)), ',', '-') as `id`, 
	count(d.issue_id) `current`, 
    count(d1.issue_id) `last`
from crm_mente_item_value a
	inner join crm_mente_item b on a.mente_issue_field_value = b.item_id
	inner join crm_mente_option_data_value c on b.item_id = c.item_id
	inner join crm_issue d on a.issue_id = d.issue_id
	left join crm_mente_item b1 on a.mente_issue_field_value = b1.item_id
	left join crm_issue d1 on a.issue_id = d1.issue_id
		and DATE_FORMAT(d1.issue_receive_date, '%Y%C') = CONCAT(YEAR(d.issue_receive_date)-1, MONTH(d.issue_receive_date))
where c.item_language = 'ja'
	and b.item_name = 'issue_issue_proposal_id'
	and d.issue_receive_date >= '2016-12-01' and d.issue_receive_date < '2018-12-01' 
    and a.company_id = 1
group by `year`, `month`, a.issue_id
order by d.issue_receive_date ASC
) a
group by `year`, `month`, a.id;