select
			dedup_item_path(group_concat(
				if( (a.mente_issue_item_name = 'issue_issue_product_id') or 
					(a.mente_issue_item_name = 'issue_issue_proposal_id' and a.mente_issue_field_level = 2)
				, if(a.mente_issue_item_name = 'issue_issue_product_id', 
					concat('0-',get_lineage(a.mente_issue_field_value)), 
					get_lineage(a.mente_issue_field_value)), 
				a.mente_issue_field_value
				)order by mente_issue_item_name desc separator '-')
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