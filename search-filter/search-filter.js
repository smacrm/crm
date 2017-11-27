'use strict';

(function ($) {
	var SearchFilter = function SearchFilter(options, element) {
		var searchFilter = element;
		var $searchFilter = $(searchFilter);

		var defaults = {
			fields: false,
			data: false
		};

		var opts = Object.assign({}, defaults, options);

		$searchFilter.addClass("search-filter");
		$searchFilter.sortable({
				placeholder: "group ui-state-highlight"
		}).disableSelection();

		var createFieldInput = function(currentField){
			currentField = currentField  || {"type": "string", "data": ''};

			var field = null;
			switch(currentField.type){
				case 'select':
					field = $("<select/>",{
						class: 'value',
						name: 'value',
					});
					$.each(currentField.data, function(index, el) {
						field.append('<option value="'+el.value+'">'+el.label+'</option>');
					});
				break;
				case 'date':
					field = $("<input/>",{
						class: 'value date',
						name: 'value'
					});
					field.datepicker({
				      changeMonth: true,
				      changeYear: true,
				      dateFormat: 'yy-mm-dd'
				    });
				break;
				case 'string':
				default:
					field = $("<input/>",{
						class: 'value',
						name: 'value',
					});
				break;
			}
			return field;
		}

		var createFieldRow = function(container, data){
			data = data || false;
			
			var filters = [];

			var fieldFilter = $("<li/>", {
				class: ''
			});
			
			var field = $("<select/>",{
				class: 'field',
				name: 'field',
			});
			var currentField = null;
			$.each(opts.fields, function(index, el) {
				var value = el.name;
				var label = el.label;
				var type = el.type || 'string';
				field.append($('<option/>', {
					value: value,
					type: type
				}).text(label));

				if(data && value === data.name) currentField = el;
			});

			var operator = $("<select/>",{
				class: 'operator',
				name: 'operator',
			});
			operator.append('<option value="=">=</option>');
			operator.append('<option value=">">&gt;</option>');
			operator.append('<option value="<">&lt;</option>');
			operator.append('<option value=">=">&gt;</option>');
			operator.append('<option value="<=">&gt;</option>');

			var condition = $("<select/>",{
				class: 'condition',
				name: 'condition',
			});
			condition.append('<option value="and">AND</option>');
			condition.append('<option value="or">OR</option>');

			var input = createFieldInput(currentField);

			var addAction = $("<a/>",{
				href: '#add-field',
			});
			addAction.html('<span class="btn btn-xs btn-primary glyphicon glyphicon-plus"></span>');

			var removeAction = $("<a/>",{
				href: '#remove-field',
			});
			removeAction.html('<span class="btn btn-xs btn-danger glyphicon glyphicon-minus"></span>');

			fieldFilter.append(field, operator, input, condition, addAction, removeAction);

			container.append(fieldFilter);
			
			///////////////// FIELD ACTION /////////////////
			field.change(function(event) {
				event.preventDefault();
				var self = $(this);
				var f;
				$.each(opts.fields, function(index, el) {
					var value = el.name;
					if(value === self.val()) f = el;
				});
				var newInput = createFieldInput(f);
				newInput.insertBefore(input);
				input.remove();
				input = newInput;
			});

			addAction.click(function(event) {
				event.preventDefault();
				var fw = createFieldRow(container);
				fw.find('.value').focus();
			    container.animate({
			        scrollTop: container.find('li').length * fw.innerHeight()
			    });
			});

			removeAction.click(function(event) {
				event.preventDefault();
				fieldFilter.remove();

			});

			///////////////// FIELD DATA /////////////////
			if(data){
				field.val(data.name);
				condition.val(data.condition);
				operator.val(data.operator);
				input.val(data.value);
			}

			return fieldFilter;
		}

		var createGroup = function(data){
			data = data || [];
			var group = $("<div/>", {
				class: 'group'
			});

			var fieldContainer = $("<ul/>",{
				class: 'list-unstyled'
			});

			var groupExtra = $("<div/>", {
				class: 'extra'
			});
			groupExtra.html("<span/>");

			var condition = $("<select/>",{
				class: 'condition',
				name: 'condition',
			});
			condition.append('<option value="and">AND</option>');
			condition.append('<option value="or">OR</option>');

			var addGroupAction = $("<a/>",{
				href: '#add-group',
			});
			addGroupAction.html('<span class="btn btn-xs btn-primary glyphicon glyphicon-plus"></span>');

			var removeGroupAction = $("<a/>",{
				href: '#remove-group',
			});
			removeGroupAction.html('<span class="btn btn-xs btn-danger glyphicon glyphicon-minus"></span>');

			groupExtra.find("span").append(condition, addGroupAction, removeGroupAction);
			group.append(fieldContainer, groupExtra);

			$searchFilter.append(group);

			///////////////// GROUP ACTION /////////////////
			//add group action
			addGroupAction.click(function(event) {
				event.preventDefault();
				var gc = createGroup();

			    $searchFilter.animate({
			        scrollLeft: gc.innerWidth()*$searchFilter.find(".group").length
			    });
			});

			removeGroupAction.click(function(event) {
				event.preventDefault();
				group.remove();
			});

			fieldContainer.sortable({
				placeholder: "ui-state-highlight"
			}).disableSelection();

			if(data.length > 0){
				$.each(data, function(index, el) {
					createFieldRow(fieldContainer, el);		
				});
			}else{
				//create first row
				createFieldRow(fieldContainer);
			}

			return fieldContainer;
		}

		//get data filters
		searchFilter.getData = function(){
			var groupJson = [];
			$searchFilter.find('.group').each(function(index, group) {
				var $group = $(group);
				var groupCondition = $group.find("> .extra [name=condition]");
				var groupJsonEl = [];
				$group.find('li').each(function(index, field) {
					var $field = $(field);
					var fieldName = $field.find('[name=field]').val();
					var operatorVal = $field.find('[name=operator]').val();
					var conditionVal = $field.find('[name=condition]').val();
					var fieldVal = $field.find('[name=value]').val();
					
					var fieldJson = {
						name: fieldName,
						operator: operatorVal,
						condition: ($group.find('li').length-1 === index ) ? '' : conditionVal,
						value: fieldVal
					};

					groupJsonEl.push(fieldJson);
				});
				groupJson.push({
					'operator':  ($searchFilter.find('.group').length-1 === index ) ? '' : groupCondition.val(), 
					'filters': groupJsonEl
				});
			});

			return JSON.stringify(groupJson);
		};

		//parse data
		if( !$.isEmptyObject(opts.fields) && !$.isEmptyObject(opts.data) ){
			var defaultData = $.parseJSON(opts.data);
			$.each(defaultData, function(index, data) {
				createGroup(data);
			});
		}else{
			//create first group
			createGroup();
		}

		return searchFilter;
	};

	$.fn.searchFilter = function (options) {
		options = options || {};
		return this.each(function () {
			var searchFilter = new SearchFilter(options, this);
			$(this).data('searchFilter', searchFilter);

			return searchFilter;
		});
	};
})(jQuery);