/* global LANG */

/**
 * Search filters
 * @author hungpham
 * @since 2016/10
 */

'use strict';

(function ($) {
    var SearchFilter = function SearchFilter(options, element) {
        var searchFilter = element;
        var $searchFilter = $(searchFilter);
        var $boxSearch = $searchFilter.closest('.box.box-search');
        var defaultOprs = ['EQ', 'NE', 'GT', 'LT', 'GE', 'LE', 'LIKE', 'LIKE_START', 'LIKE_END', 'NOT_LIKE', 'BLANK', 'NOT_BLANK'];

        var defaults = {
            hiddenSearchPanel: false, // co hien thi panel search khong?
            hideOnEmpty: true, //hide on empty conditions
            prependConds: false, //prepend conditions
            fillPrepend: false, //fill prepends field to form
            disablePrepend: false, //disable prepended fields !!!現在この設定値がまだできない。
            fields: false, //field list
            extraFieldNames: [], // extra field name list, while fetching label, if there is not in fields -> search in extraFieldNames
            data: false, //field data
            maxGroup: 3, //max number display groups
            maxRow: 1, //max number display rows
            displayQuery: false, //display query on screen
            queryContainer: null, //query container to display,
            callback: {
                onValue: function(field, value){
                    return value;
                },
                onDisplayValue: function(field, value){
                    return value;
                }
            },
            langs:{
                AND: 'AND',
                OR: 'OR',
                LIKE: 'CONTAIN',
                LIKE_START: 'START WITH',
                LIKE_END: 'END WITH',
                NOT_LIKE: 'NOT CONTAIN',
                BLANK: 'BLANK',
                NOT_BLANK: 'NOT BLANK'
            }
        };

        var opts = Object.assign({}, defaults, options);
        opts.prependedConds = false; //temporary variable for disable prepended conditions
        if(opts.fillPrepend && opts.prependConds){
            if(!$.trim(opts.data)) opts.data = opts.prependConds;
            opts.prependConds = false;
            opts.prependedConds = true;
        }

        $searchFilter.addClass("search-filter");
        var col = Math.min((12 * opts.maxRow)/opts.maxGroup, 6);
        $searchFilter.sortable({
            placeholder: "group ui-state-highlight col-md-"+col,
            update: function( event, ui ) {
                refreshGroupAction();
                if(opts.displayQuery){
                    displayQuery();
                }
            }
        }).disableSelection();
        var getOperator = function(o){
            switch (o){
                case 'EQ':
                    return '<option value="=" ref="'+o+'">=</option>';
                    break;
                case 'NE':
                    return '<option value="!=" ref="'+o+'">!=</option>';
                    break;
                case 'LT':
                    return '<option value="<" ref="'+o+'">&lt;</option>';
                    break;
                case 'GT':
                    return '<option value=">" ref="'+o+'">&gt;</option>';
                    break;
                case 'LE':
                    return '<option value="<=" ref="'+o+'">&le;</option>';
                    break;
                case 'GE':
                    return '<option value=">=" ref="'+o+'">&ge;</option>';
                    break;
                case 'LIKE':
                    return '<option value="%" ref="'+o+'">%</option>';
                    break;
                case 'LIKE_START':
                    return '<option value="-%" ref="'+o+'">→%</option>';
                    break;
                case 'LIKE_END':
                    return '<option value="%-" ref="'+o+'">%→</option>';
                    break;
                case 'NOT_LIKE':
                    return '<option value="!%" ref="'+o+'">！%</option>';
                    break;
                case 'BLANK':
                    return '<option value="bl" ref="'+o+'">'+opts.langs.BLANK+'</option>';
                    break;
                case 'NOT_BLANK':
                    return '<option value="nbl" ref="'+o+'">'+opts.langs.NOT_BLANK+'</option>';
                    break;
            }
        };

        var numberParse = function(val, default_val, radix) {
            try {
                radix = radix || 10;
                default_val = default_val || 0;
                if (val !== null) {
                    if (val !== '' && val.length > 0) {
                        if (!isNaN(val)) return parseInt(val, radix);
                    }
                }
            } catch (err) {
                console.log(err);   
            }
            return default_val;
        };

        var createFieldInput = function (currentField) {
            currentField = currentField || {"type": "string", "data": ''};

            var field = null;
            switch (currentField.type) {
                case 'select':
                    field = $("<select/>", {
                        class: 'value',
                        name: 'value'
                    });
                    if(currentField.data === undefined) break;
                    field.append('<option value="" class="ui-state-highlight" >&nbsp;</option>');
                    $.each(currentField.data, function (index, el) {
                        try{
                            var value = el.value.toString();
                            if(value === '' || value.length <= 0) {
                                field.append('<option value="' + value + '">' + el.label + '</option>');
                            } else {
                                if(value.indexOf('issue_proposal_') > -1 || value.indexOf('issue_product_') > -1) {
                                    var labels = value.split('_');
                                    if(labels !== null && labels.length > 0 && parseInt(labels[labels.length-1]) > 0) {
                                        field.append('<option value="' + value + '">' + el.label + '_' + labels[labels.length-1] + '</option>');
                                    } else {
                                        field.append('<option value="' + value + '">' + el.label + '</option>');
                                    }
                                } else {
                                    field.append('<option value="' + value + '">' + el.label + '</option>');
                                }
                            }
                        }catch(e){
                            console.error('Error on change to select field at ' + index + ': ' + e.message);
                        }
                    });
                    /////////////// CALLBACK /////////////
                    // try{ field.select2(); }catch(e){}
                    break;
                case 'date':
                    field = $("<input/>", {
                        class: 'value date',
                        name: 'value',
                        maxLength: 30
                    });
                    var dateFormat;
                    switch(LANG){
                        case 'vi': case 'vn':
                           dateFormat = 'dd/mm/yy';
                           break;
                        case 'en':
                            dateFormat = 'yy-mm-dd';
                           break;
                       default:
                           dateFormat = 'yy/mm/dd';
                    }
                    field.datepicker({
                        changeMonth: true,
                        changeYear: true,
                        dateFormat: dateFormat
                    });
                    break;
                case 'string':
                default:
                    field = $("<input/>", {
                        class: 'value',
                        name: 'value',
                        maxLength: 200
                    });
                    break;
            }
            return field;
        };

        var createFieldRow = function (container, data) {
            data = data || false;
            
            var fieldFilter = $("<li/>", {
                class: ''
            });

            var field = $("<select/>", {
                class: 'field',
                name: 'field'
            });
            var currentField = null;
            $.each(opts.fields, function (index, el) {
                var value = el.name; if(numberParse(value, 0, 10) < 0) value = '';
                var label = el.label;
                var type = el.type || 'string';
                var options = {
                    value: value,
                    type: type
                };
                if(value === '' && label.startsWith('---')) options.disabled  = 'disabled';
                field.append($('<option/>', options).text(label));

                if (data && value === data.name){
                    currentField = el;
                }
            });

            var operator = $("<select/>", {
                class: 'operator',
                name: 'operator'
            });
            $.each(defaultOprs, function(i, o){
                operator.append(getOperator(o));
            });

            var condition = $("<select/>", {
                class: 'condition',
                name: 'condition'
            });
            condition.append('<option value="and">'+opts.langs.AND+'</option>');
            condition.append('<option value="or">'+opts.langs.OR+'</option>');

            var input = createFieldInput(currentField);

            var addAction = $("<a/>", {
                href: '#add-field'
            });
            addAction.html('<span class="btn btn-xs btn-primary glyphicon glyphicon-plus"></span>');

            var removeAction = $("<a/>", {
                href: '#remove-field'
            });
            removeAction.html('<span class="btn btn-xs btn-danger glyphicon glyphicon-minus"></span>');

            fieldFilter.append(field, operator, input, condition, removeAction, addAction);

            container.append(fieldFilter);

            ///////////////// FIELD ACTION /////////////////
            field.change(function (event) {
                event.preventDefault();
                var self = $(this);
                var f = {};
                $.each(opts.fields, function (index, el) {
                    var value = el.name;
                    if (value === self.val())
                        f = el;
                });
                var newInput = createFieldInput(f);
                newInput.insertBefore(input);
                
                // Remove select2 for selectbox if exists
                if(input.prop("tagName") === 'SELECT' && input.data('select2')) try{ input.select2("destroy"); }catch(e){};
                
                input.remove();
                input = newInput;
                if( !f || !f.operator || f.operator.length === 0 ) f.operator = defaultOprs;
                operator.find('option').each(function(){
                    if( $(this).attr('ref') && $.inArray($(this).attr('ref'), f.operator) >= 0 ){
                        $(this).show();
                    } else{
                        $(this).hide();
                    }
                });
                operator[0].selectedIndex = 0;
                if(f.operator.length > 1){
                    operator.focus();
                }else{
                    input.focus();
                }
                
                /////////// CALL BACK ///////////
                if(input.prop("tagName") === 'SELECT'){
                    try{ input.select2(); }catch(e){}
                }
            });
            
            operator.change(function(event){
                event.preventDefault();
                var self = $(this);
                var val = self.val();
                if(val === 'bl' || val === 'nbl'){
                    input.attr('disabled', 'disabled');
                    input.val('').trigger('change');
                }else{
                    input.attr('disabled', null);
                    if(!input.hasClass('date')) input.focus();
                    if(input.prop("tagName") === 'SELECT'){
                        input.prop('selectedIndex', 0).trigger('change');
                    };
                }
            });

            addAction.click(function (event) {
                event.preventDefault();
                var fw = createFieldRow(container);
                fw.find('.value').focus();
                container.animate({
                    scrollTop: container.find('li').length * fw.innerHeight()
                });
            });

            removeAction.click(function (event) {
                event.preventDefault();
                if($(this).attr('disabled') !== 'disabled'){
                    fieldFilter.remove();
                    displayQuery();
                }
            });

            ///////////////// FIELD DATA /////////////////
            
            if (data) {
                field.val(data.name);
                // field.trigger('change');
                if(data.condition) condition.val(data.condition);
                operator.val(data.operator);
                operator.trigger('change');
                input.val(data.value);
            }else{
                field.trigger('change');
                operator.trigger('change');
            }
            
            ///////////////// CALLBACK ///////////////////
            if(input.prop("tagName") === 'SELECT'){
                try{ input.select2(); }catch(e){}
            }
            
            return fieldFilter;
        };

        var createGroup = function (data) {
            data = data || {filters: [], operator: 'and'};
            var col = Math.min((12 * opts.maxRow)/opts.maxGroup, 6);
            var group = $("<div/>", {
                class: 'group col-md-' + col
            });

            var fieldContainer = $("<ul/>", {
                class: 'list-unstyled'
            });

            var groupExtra = $("<div/>", {
                class: 'extra'
            });
            groupExtra.html("<span/>");

            var condition = $("<select/>", {
                class: 'condition',
                name: 'condition'
            });
            condition.append('<option value="and">'+opts.langs.AND+'</option>');
            condition.append('<option value="or">'+opts.langs.OR+'</option>');

            var addGroupAction = $("<a/>", {
                href: '#add-group'
            });
            addGroupAction.html('<span class="btn btn-xs btn-primary glyphicon glyphicon-plus"></span>');

            var removeGroupAction = $("<a/>", {
                href: '#remove-group'
            });
            removeGroupAction.hide();
            removeGroupAction.html('<span class="btn btn-xs btn-danger glyphicon glyphicon-minus"></span>');

            groupExtra.find("span").append(condition, removeGroupAction, addGroupAction);
            group.append(fieldContainer, groupExtra);

            $searchFilter.append(group);

            ///////////////// GROUP ACTION /////////////////
            //add group action
            addGroupAction.click(function (event) {
                event.preventDefault();
                var gc = createGroup();
                removeGroupAction.show();
                addGroupAction.hide();
                $searchFilter.animate({
                    scrollLeft: gc.innerWidth() * $searchFilter.find(".group").length
                });
            });

            removeGroupAction.click(function (event) {
                event.preventDefault();
                if($(this).attr('disabled') !== 'disabled'){
                    group.remove();
                    displayQuery();
                }
                refreshGroupAction();
            });

            fieldContainer.sortable({
                placeholder: "ui-state-highlight",
                update: function( event, ui ) {
                    if(opts.displayQuery){
                        displayQuery();
                    }
                }
            }).disableSelection();

            if (data.filters.length > 0) {
                if(data.operator) condition.val(data.operator);
                $.each(data.filters, function (index, el) {
                    createFieldRow(fieldContainer, el);
                });
            } else {
                //create first row
                createFieldRow(fieldContainer);
            }
            
            refreshGroupAction();

            return group;
        };
        
        var refreshGroupAction = function(){
            if($searchFilter.find('.group').length >= opts.maxGroup){
                $searchFilter.find('.group:last a[href="#add-group"]').hide();
                $searchFilter.find('.group:last a[href="#remove-group"]').show();
            }else{
                $searchFilter.find('.group:last a[href="#add-group"]').show();
                $searchFilter.find('.group:last a[href="#remove-group"]').show();
            }
            
            //$searchFilter.find('.group:first a[href="#add-group"]').show();
             if($searchFilter.find('.group').length === 1){
                $searchFilter.find('.group:first a[href="#remove-group"]').hide();
            }
        };

        //get data filters
        searchFilter.getData = function () {
            var groupJson = [];
            $searchFilter.find('.group').each(function (index, group) {
                var $group = $(group);
                var groupCondition = $group.find("> .extra [name=condition]");
                var groupJsonEl = [];
                $group.find('li').each(function (index, field) {
                    var $field = $(field);
                    var fieldName = $field.find('[name=field]').val();
                    var operatorVal = $field.find('[name=operator]').val();
                    var conditionVal = $field.find('[name=condition]').val();
                    var fieldValObj = $field.find('[name=value]');
                    var fieldVal = fieldValObj.val();
                    if(fieldVal) fieldVal = fieldVal.trim();
                    else if( fieldVal === null && $field.find('[name=value]').prop("tagName") === 'SELECT' ){
                        fieldVal = $field.find('[name=value] > option:first').attr('value');
                    }
                    
                    var type = fieldValObj.prop("tagName").toLowerCase();
                    if(type === 'input'){
                        type = 'text';
                    }
                    // special point for date field
                    if(fieldValObj.hasClass('date')){ 
                        type = 'date';
                    }
                    
                    var fieldJson = {
                        name: fieldName,
                        operator: operatorVal,
                        type : type,
                        condition: ($group.find('li').length - 1 === index) ? '' : conditionVal,
                        value: opts.callback.onValue(fieldName, getFieldValue(fieldName, fieldVal))
                    };

                    groupJsonEl.push(fieldJson);
                });
                groupJson.push({
                    'operator': ($searchFilter.find('.group').length - 1 === index) ? '' : groupCondition.val(),
                    'filters': groupJsonEl
                });
            });

            return JSON.stringify(groupJson);
        };
        
        //get query string
        searchFilter.getQueryString = function(data){
            if(!data) return;
            var query = '';
            var previouGroupOperator = '';
            $.each(data, function(i, item) {
                var fieldQuery = '';
                var previousFilterOperator = '';
                $.each(item.filters, function(i, filter){
                    //continue loop if value is empty and operator wasnot is blank
                    if(filter.operator !== 'bl' && filter.operator !== 'nbl' && !filter.value) return true;
                    fieldQuery += previousFilterOperator;
                    fieldQuery += ' (';
                    fieldQuery += getFieldLabel(filter.name);
                    if(filter.operator === 'bl'){
                        fieldQuery += ' '+opts.langs.BLANK;
                    }else if(filter.operator === 'nbl'){
                        fieldQuery += ' '+opts.langs.NOT_BLANK;
                    }else if(filter.operator === '%'){
                        fieldQuery += ' '+opts.langs.LIKE+' '+opts.callback.onDisplayValue(filter.name, getFieldValue(filter.name, filter.value));
                    }else if(filter.operator === '%-'){
                        fieldQuery += ' '+opts.langs.LIKE_END+' '+opts.callback.onDisplayValue(filter.name, getFieldValue(filter.name, filter.value));
                    }else if(filter.operator === '-%'){
                        fieldQuery += ' '+opts.langs.LIKE_START+' '+opts.callback.onDisplayValue(filter.name, getFieldValue(filter.name, filter.value));
                    }else if(filter.operator === '!%'){
                        fieldQuery += ' '+opts.langs.NOT_LIKE+' '+opts.callback.onDisplayValue(filter.name, getFieldValue(filter.name, filter.value));
                    }else{
                        fieldQuery += ' '+filter.operator+' ';
                        fieldQuery += opts.callback.onDisplayValue(filter.name, getFieldValue(filter.name, filter.value));
                    }
                    fieldQuery += ') ';
                    previousFilterOperator = opts.langs[filter.condition.toUpperCase()];
                });
                query += fieldQuery ? previouGroupOperator+' (' + fieldQuery + ') ': ' ';
                previouGroupOperator = fieldQuery ? opts.langs[item.operator.toUpperCase()] : '';
            });
            return $.trim(query);
        };
        
        //get field label belong to field key
        var getFieldLabel = function(fieldKey){
            var fieldLabel = fieldKey;
            var found = false;
            $.each(opts.fields, function(i, field){
               if(field.name === fieldKey){
                   fieldLabel = field.label;
                   found = true;
                   return false;
               } 
            });
            if(!found){
                $.each(opts.extraFieldNames, function(i, field){
                   if(field.name === fieldKey){
                       fieldLabel = field.label;
                       found = true;
                       return false;
                   } 
                });
            }
            return fieldLabel;
        };
        
        //get field value belong to entered field value (using for select field type)
        var getFieldValue = function(fieldKey, fieldValue){
            $.each(opts.fields, function(i, field){
               if(field.name === fieldKey){
                    if(field.type === 'select'){
                        if(!field.data || field.data === undefined) return false;
                        $.each(field.data, function(j, item){
                           if(item.value === fieldValue){
                               fieldValue = item.label;
                               return false;
                           } 
                        });
                    }
                    return false;
               }
            });
            return fieldValue;
        };
        
        //display query on query container
        var displayQuery = function(){
            var query = searchFilter.getQueryString($.parseJSON(searchFilter.getData()));
            if(opts.prependConds){
                var appendQuery = searchFilter.getQueryString(opts.prependConds);
                query += appendQuery ? (query ? ' '+opts.langs.AND+' ': '') + appendQuery : '';
            }
            //console.log(query);
            if(opts.queryContainer !== null){
                opts.queryContainer.text(query);
            }
            return query;
        };

        //parse data
        if (opts.data && !$.isEmptyObject(opts.fields) && !$.isEmptyObject(opts.data)) {
            var defaultData = null;
            try{
                defaultData = $.parseJSON(opts.data);
            }catch(e){
                defaultData = opts.data;
            }
            $.each(defaultData, function (index, data) {
                var fc = createGroup(data);
                if(index < defaultData.length - 1 ){
                    fc.find('a[href="#remove-group"]').show();
                }
            });
        } else {
            //create first group
            createGroup();
        }
        
        refreshGroupAction();
        
        //add action listener
        if(opts.displayQuery){
            $searchFilter.on('change keyup keydown keypress', 'input, select', function(e){
                if(e.which === 13) e.preventDefault();
                
                if(e.type === 'change' || e.type === 'keyup'){
                    displayQuery();
                    //on enter text field
                    if(e.currentTarget.tagName === 'INPUT' && e.type === 'keyup' && e.which === 13) {
                        var data = searchFilter.getData();
                        $boxSearch.find('.searchDataJson').val(data);
                        $boxSearch.find(".search-action").trigger('click');
                    }
                }
            });
            displayQuery();
        }
        
        //auto collapse/expand box search
        //alert(opts.hiddenSearchPanel);
        if(!opts.hiddenSearchPanel) {
            if(opts.hideOnEmpty && !$.trim(searchFilter.getQueryString($.parseJSON(searchFilter.getData())))){
                if($boxSearch.find(' > .box-header').data('widget') === 'collapse'){
                    $boxSearch.addClass('collapsed-box');
                }
            }
        } else {
            if($boxSearch.find(' > .box-header').data('widget') === 'collapse'){
                $boxSearch.addClass('collapsed-box');
            }
        }
        
        // Change icon after click to header for collapsing/expanding action
        $boxSearch.find(' > .box-header[data-widget=collapse]').click(function(e){
            var icon  = $(this).find('.btn-box-tool > i');
            if($boxSearch.hasClass('collapsed-box')){
                icon.removeClass('fa-plus');
                icon.addClass('fa-minus');
            }else{
                icon.removeClass('fa-minus');
                icon.addClass('fa-plus');
            }
        });
        
        //disable prepend fields and group contains that fields
        if(opts.fillPrepend && opts.prependedConds && opts.disablePrepend){
            $searchFilter.find('input, select, a[href="#remove-field"]').each(function(){
                $(this).attr('disabled', 'disabled')
                        .find('span.btn').addClass('disabled');
                $(this).closest('.group').find('a[href="#remove-group"]')
                        .attr('disabled', 'disabled')
                        .find('span.btn').addClass('disabled');
            });
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