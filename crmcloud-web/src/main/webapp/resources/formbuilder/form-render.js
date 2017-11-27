/*
 formBuilder - https://formbuilder.online/
 Version: 1.20.2
 Author: Kevin Chappell <kevin.b.chappell@gmail.com>
 */
'use strict';

// Element.remove() polyfill

if (!('remove' in Element.prototype)) {
    Element.prototype.remove = function () {
        if (this.parentNode) {
            this.parentNode.removeChild(this);
        }
    };
}

// Event polyfill
if (typeof Event !== 'function') {
    (function () {
        window.Event = function (evt) {
            var event = document.createEvent('Event');
            event.initEvent(evt, true, true);
            return event;
        };
    })();
}

// Object.assign polyfill
if (typeof Object.assign != 'function') {
    Object.assign = function (target) {
        'use strict';

        if (target == null) {
            throw new TypeError('Cannot convert undefined or null to object');
        }

        target = Object(target);
        for (var index = 1; index < arguments.length; index++) {
            var source = arguments[index];
            if (source != null) {
                for (var key in source) {
                    if (Object.prototype.hasOwnProperty.call(source, key)) {
                        target[key] = source[key];
                    }
                }
            }
        }
        return target;
    };
}
'use strict';

(function ($) {
    'use strict';

    var Toggle = function Toggle(element, options) {

        var defaults = {
            theme: 'fresh',
            labels: {
                off: 'Off',
                on: 'On'
            }
        };

        var opts = $.extend(defaults, options),
                $kcToggle = $('<div class="kc-toggle"/>').insertAfter(element).append(element);

        $kcToggle.toggleClass('on', element.is(':checked'));

        var kctOn = '<div class="kct-on">' + opts.labels.on + '</div>',
                kctOff = '<div class="kct-off">' + opts.labels.off + '</div>',
                kctHandle = '<div class="kct-handle"></div>',
                kctInner = '<div class="kct-inner">' + kctOn + kctHandle + kctOff + '</div>';

        $kcToggle.append(kctInner);

        $kcToggle.click(function () {
            element.attr('checked', !element.attr('checked'));
            $(this).toggleClass('on');
        });
    };

    $.fn.kcToggle = function (options) {
        var toggle = this;
        return toggle.each(function () {
            var element = $(this);
            if (element.data('kcToggle')) {
                return;
            }
            var kcToggle = new Toggle(element, options);
            element.data('kcToggle', kcToggle);
        });
    };
})(jQuery);
'use strict';

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) {
    return typeof obj;
} : function (obj) {
    return obj && typeof Symbol === "function" && obj.constructor === Symbol ? "symbol" : typeof obj;
};

var fbUtils = {};

// cleaner syntax for testing indexOf element
fbUtils.inArray = function (needle, haystack) {
    return haystack.indexOf(needle) !== -1;
};

// Remove null or undefined values
fbUtils.trimObj = function (attrs) {
    var xmlRemove = [null, undefined, '', false];
    for (var i in attrs) {
        if (fbUtils.inArray(attrs[i], xmlRemove)) {
            delete attrs[i];
        }
    }
    return attrs;
};

/**
 * Make an ID for this element using current date and tag
 *
 * @param  {Boolean} element
 * @return {String}  new id for element
 */
fbUtils.makeId = function () {
    var element = arguments.length <= 0 || arguments[0] === undefined ? false : arguments[0];

    var epoch = new Date().getTime();

    return element.tagName + '-' + epoch;
};

fbUtils.validAttr = function (attr) {
    var invalid = ['values', 'enableOther', 'other', 'label',
        // 'style',
        'subtype'];
    return !fbUtils.inArray(attr, invalid);
};

/**
 * Convert an attrs object into a string
 *
 * @param  {Object} attrs object of attributes for markup
 * @return {string}
 */
fbUtils.attrString = function (attrs) {
    var attributes = [];

    for (var attr in attrs) {
        if (attrs.hasOwnProperty(attr) && fbUtils.validAttr(attr)) {
            attr = fbUtils.safeAttr(attr, attrs[attr]);
            attributes.push(attr.name + attr.value);
        }
    }
    return attributes.join(' ');
};

fbUtils.safeAttr = function (name, value) {
    name = fbUtils.safeAttrName(name);

    var valString = fbUtils.escapeAttr(value);

    value = value ? '="' + valString + '"' : '';
    return {
        name: name,
        value: value
    };
};

fbUtils.safeAttrName = function (name) {
    var safeAttr = {
        className: 'class'
    };

    return safeAttr[name] || fbUtils.hyphenCase(name);
};

/**
 * Convert strings
 into lowercase-hyphen
 *
 * @param  {string} str
 * @return {string}
 */
fbUtils.hyphenCase = function (str) {
    str = str.replace(/[^\w\s\-]/gi, '');
    str = str.replace(/([A-Z])/g, function ($1) {
        return '-' + $1.toLowerCase();
    });

    return str.replace(/\s/g, '-').replace(/^-+/g, '');
};

/**
 * convert a hyphenated string to camelCase
 * @param  {String} str
 * @return {String}
 */
fbUtils.camelCase = function (str) {
    return str.replace(/-([a-z])/g, function (m, w) {
        m = m;
        return w.toUpperCase();
    });
};

/**
 * Generate markup wrapper where needed
 *
 * @param  {string}              tag
 * @param  {String|Array|Object} content we wrap this
 * @param  {object}              attrs
 * @return {String}
 */
fbUtils.markup = function (tag) {
    var content = arguments.length <= 1 || arguments[1] === undefined ? '' : arguments[1];
    var attrs = arguments.length <= 2 || arguments[2] === undefined ? {} : arguments[2];

    var contentType = void 0,
            field = document.createElement(tag),
            getContentType = function getContentType(content) {
                return Array.isArray(content) ? 'array' : typeof content === 'undefined' ? 'undefined' : _typeof(content);
            },
            appendContent = {
                string: function string(content) {
                    field.innerHTML = content;
                },
                object: function object(content) {
                    return field.appendChild(content);
                },
                array: function array(content) {
                    for (var i = 0; i < content.length; i++) {
                        contentType = getContentType(content[i]);
                        appendContent[contentType](content[i]);
                    }
                }
            };

    for (var attr in attrs) {
        if (attrs.hasOwnProperty(attr)) {
            var name = fbUtils.safeAttrName(attr);
            field.setAttribute(name, attrs[attr]);
        }
    }

    contentType = getContentType(content);

    if (content) {
        appendContent[contentType].call(this, content);
    }

    return field;
};

fbUtils.parseAttrs = function (elem) {
    var attrs = elem.attributes;
    var data = {};

    for (var attr in attrs) {
        if (attrs.hasOwnProperty(attr)) {
            data[attrs[attr].name] = attrs[attr].value;
        }
    }

    return data;
};

fbUtils.parseOptions = function (field) {
    var options = field.getElementsByTagName('option'),
            optionData = {},
            data = [];

    if (options.length) {
        for (var i = 0; i < options.length; i++) {
            optionData = fbUtils.parseAttrs(options[i]);
            optionData.label = options[i].textContent;
            data.push(optionData);
        }
    }

    return data;
};

fbUtils.parseXML = function (xmlString) {
    var parser = new window.DOMParser();
    var xml = parser.parseFromString(xmlString, 'text/xml'),
            formData = [];

    if (xml) {
        var fields = xml.getElementsByTagName('field');
        for (var i = 0; i < fields.length; i++) {
            var fieldData = fbUtils.parseAttrs(fields[i]);
            fieldData.values = fbUtils.parseOptions(fields[i]);
            formData.push(fieldData);
        }
    }

    return formData;
};

fbUtils.escapeHtml = function (html) {
    var escapeElement = document.createElement('textarea');
    escapeElement.textContent = html;
    return escapeElement.innerHTML;
};

fbUtils.escapeAttr = function (str) {
    var match = {
        '"': '&quot;',
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;'
    };

    function replaceTag(tag) {
        return match[tag] || tag;
    }

    return typeof str === 'string' ? str.replace(/["&<>]/g, replaceTag) : str;
};

// Remove null or undefined values
fbUtils.escapeAttrs = function (attrs) {

    for (var attr in attrs) {
        if (attrs.hasOwnProperty(attr)) {
            attrs[attr] = fbUtils.escapeAttr(attrs[attr]);
        }
    }

    return attrs;
};
'use strict';

// render the formBuilder XML into html

function FormRenderFn(options, element) {

    var utils = fbUtils;

    var formRender = this,
            defaults = {
                destroyTemplate: true, // @todo
                container: false,
                dataType: 'xml',
                formData: false,
                readonly: false,
                label: {
                    formRendered: 'Form Rendered',
                    noFormData: 'No form data.',
                    other: 'Other',
                    selectColor: 'Select Color'
                },
                render: true,
                notify: {
                    error: function error(message) {
                        return console.error(message);
                    },
                    success: function success(message) {
                        //return console.log(message);
                    },
                    warning: function warning(message) {
                        //return console.warn(message);
                    }
                }
            },
            _helpers = {};

    var opts = $.extend(true, defaults, options);

    (function () {
        if (!opts.formData) {
            return false;
        }

        var setData = {
            xml: function xml(formData) {
                return utils.parseXML(formData);
            },
            json: function json(formData) {
                return window.JSON.parse(formData);
            }
        };

        opts.formData = setData[opts.dataType](opts.formData) || false;
    })();
    
    /**
     * Compare value object using == (not ===)
     * 
     * @author hungpd
     * @since 2016/11/01
     * @returns {Boolean}
     */
    _helpers.contains = function(obj, a) {
        var i = a.length;
        while (i--) {
           if (a[i] == obj) { //de la === se bi loi
               return true;
           }
        }
        return false;
    }

    /**
     * Generate multiple level options
     * 
     * @author hungpd
     * @since 2016/11/01
     * @todo make multiple options by ul li html
     * @param {Object} attrs
     * @returns {String} ul content
     */
    _helpers.multiLevelOptionView = function (optionName, attrs) {
        var fieldVal = JSON.parse(attrs.value || '[]');
        var walk_array = function (optionName, items, level, generate_wrapper) {
            generate_wrapper = generate_wrapper || false;
            var options = '';
            if (generate_wrapper) {
                level++;
                options += '<ul class="dropdown-menu">';
            }
            for (var i = items.length - 1; i >= 0; i--) {
                var selected = _helpers.contains(items[i].value, fieldVal) ? 'checked'+ (attrs.multiple ? '' : ' previousValue="checked"'): '';
                var hasChild = items[i].items.length > 0;
                var type = items[i].hasOwnProperty('type') ? items[i].type: 0;
                options += '<li '+(hasChild ? 'class="dropdown-submenu"' : '')+'><a href="#" class="ui-menuitem-link ui-submenu-link ui-corner-all"><label>';
                if( !hasChild ){
                    options += '<input type="' + (attrs.multiple ? 'checkbox' : 'radio') + '" ' + (attrs.multiple ? '' : 'style="display:none" ') + 
                                '" name="' + (attrs.multiple ? optionName + '[]' : optionName) + 
                                '" value="' + items[i].value + '" ' + selected + 
                                (attrs['aria-required'] === 'true' ? ' required="true"' : '') + ' > ';
                }
                
                if(type == 1){
                    options += '<i class="fa fa-group"></i> ';
                }else if(type == 2){
                    options += '<i class="fa fa-user"></i> ';
                }else if(type == 3){
                    options += '<i class="fa fa-file"></i> ';
                }
                
                options += items[i].label + '</label></a>';
                if (hasChild) {
                    options += walk_array(optionName, items[i].items, level, true);
                }
                options += '</li>';

            }
            if (generate_wrapper) {
                options += '</ul>';
            }
            return options;
        };
        return walk_array(optionName, attrs.values, 0, false);
    }

    /**
     * Generate preview markup
     * @param  {object} fieldData
     * @return {string}       preview markup for field
     */
    _helpers.fieldRender = function (fieldData) {
        var fieldMarkup = '',
                fieldLabel = '',
                optionsMarkup = '',
                fieldLabelText = fieldData.label[LANG] || '',
                fieldDesc = fieldData.description || '',
                fieldRequired = '',
                fieldOptions = fieldData.values || [];
        fieldData.id = fieldData.name;
        
        fieldData.type = fieldData.subtype || fieldData.type;

        if (fieldData.required) {
            fieldData.required = null;
            fieldData['aria-required'] = 'true';
            fieldRequired = '<span class="required">*</span>';
        }

        if (fieldData.type !== 'hidden') {
            if (fieldDesc) {
                fieldDesc = '<span class="tooltip-element" tooltip="' + fieldDesc + '">?</span>';
            }
            fieldLabel = '<label for="' + fieldData.id + '">' + fieldLabelText + ' ' + fieldRequired + ' ' + fieldDesc + '</label>';
        }

        var fieldLabelVal = fieldData.label;

        delete fieldData.label;
        delete fieldData.description;

        var fieldDataString = utils.attrString(fieldData);
        switch (fieldData.type) {
            case 'textarea':
            case 'rich-text':
                //delete fieldData.type;
                var fieldVal = fieldData.value || '';
                if(opts.readonly){
                    fieldMarkup = fieldLabel + '<p>' + fieldVal + '</p>';
                }else{
                    fieldMarkup = fieldLabel + '<textarea ' + fieldDataString + ' maxlength="1000">' + fieldVal + '</textarea>';
                }
                break;
            case 'select':
                var fieldVal = fieldData.value || '';
                
                if (fieldOptions.length === 1 && fieldOptions[0].label === '' && fieldOptions[0].selected === true) {
                    var dynamicFieldOptions = [];
                    $.ajax({
                        url: opts.dynamicRestUrl + '/' + fieldOptions[0].value,
                        async: false
                    }).done(function (data) {
                        var json = $.parseJSON(data);
                        if (json) {
                            $.each(json, function (index, item) {
                                var option = {};
                                option.selected = false;
                                option.value = item.value;
                                option.label = item.label;
                                option.type = item.type;
                                option.items = item.items;
                                dynamicFieldOptions.push(option);
                            });
                        }
                    });
                    fieldData.values = dynamicFieldOptions;
                    fieldOptions = fieldData.values || [];
                }

                var hasMultipleLevel = false;
                for (var i = 0; i < fieldData.values.length; i++) {
                    if (fieldData.values[i].hasOwnProperty('items') && fieldData.values[i].items !== undefined && fieldData.values[i].items.length > 0) {
                        hasMultipleLevel = true;
                        break;
                    }
                }
                if (hasMultipleLevel) {
                    var type = fieldData.type.replace('-group', '');
                    var dropdownItems = _helpers.multiLevelOptionView('multiple-'+fieldData.name, fieldData);
                    fieldMarkup = fieldLabel + '<div class="prev-holder dropdown" id="prev-holder-'+fieldData.name+'">'
                            + (!opts.readonly ? ' <button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only dropdown-toggle" type="button" role="button" aria-disabled="false" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'
                            + ' <span class="ui-button-icon-left ui-icon ui-c fa fa-th"></span>'
                            + ' <span class="ui-button-text ui-c">ui-button</span>'
                            + ' </button>' : '')
                            + '<span></span>'
                            + '<input type="hidden" name="'+fieldData.name+'" value="" ' + fieldDataString + '/>'
                            + '<ul class="dropdown-menu multi-level" holder="#prev-holder-'+fieldData.name+'">' + dropdownItems + '</ul></div>';
                } else {
                    var optionAttrsString;
                    fieldData.type = fieldData.type.replace('-group', '');

                    if (fieldOptions) {
                        var selectedLabel = [];
                        var normFieldVal = '';
                        try{normFieldVal = $.parseJSON(fieldVal); } catch(e){}
                        //hungpd 20161209
                        if( fieldRequired === '' && (!fieldData.hasOwnProperty("multiple") || fieldData.multiple === false) ){
                            optionsMarkup += '<option value="" '+(fieldVal === '' ? 'selected="selected"':'')+'>--</option>';
                        }
                        for (var _i = 0; _i < fieldOptions.length; _i++) {
                            if (!fieldOptions[_i].selected) {
                                delete fieldOptions[_i].selected;
                            }
                            optionAttrsString = utils.attrString(fieldOptions[_i]);
                            var isSelected = false;
                            if($.isArray(normFieldVal)){
                                isSelected = $.inArray(fieldOptions[_i].value.toString(), normFieldVal) >= 0;
                            }else{
                                var isSelected = fieldVal == fieldOptions[_i].value;
                            }
                            if(isSelected){
                                selectedLabel.push(fieldOptions[_i].label);
                            }
                            optionsMarkup += '<option ' + optionAttrsString + ' '+(isSelected ? 'selected="selected"':'')+'>' + fieldOptions[_i].label + '</option>';
                        }
                    }

                    if(opts.readonly){
                        fieldMarkup = fieldLabel + '<span>'+selectedLabel.join(', ')+'</span>';
                    }else{
                        fieldMarkup = fieldLabel + '<select ' + fieldDataString + '>' + optionsMarkup + '</select>';
                    }
                }
                break;
            case 'checkbox-group':
            case 'radio-group':
                
                var optionAttrs = void 0;
                fieldData.type = fieldData.type.replace('-group', '');
                if (fieldOptions) {
                    var optionName = fieldData.name,
                            _optionAttrsString = void 0;
                    
                    for (var _i2 = 0; _i2 < fieldOptions.length; _i2++) {
                        optionAttrs = Object.assign({}, fieldData, fieldOptions[_i2]);
                        if( fieldData.hasOwnProperty('value') ){
                            var jsonValues = [];
                            try{
                                jsonValues = fieldData.hasOwnProperty('value') ? JSON.parse(fieldData.value) : [];
                            }catch(e){}
                            if ( optionAttrs.value === fieldData.value || _helpers.contains(optionAttrs.value, jsonValues) ) {
                                optionAttrs.checked = null;
                            }
                        }else if(optionAttrs.selected){
                            optionAttrs.checked = null;
                        }
                        
                        delete optionAttrs.selected;
                        
                        optionAttrs.name = optionName;
                        optionAttrs.id = fieldData.id + '-' + _i2;
                        _optionAttrsString = utils.attrString(optionAttrs);
                        optionsMarkup += '<input ' + _optionAttrsString  + (opts.readonly ? ' disabled' : '') + ' /> <label for="' + optionAttrs.id + '">' + optionAttrs.label + '</label> ';
                    }

                    if (fieldData.enableOther || fieldData['enable-other']) {
                        var otherOptionAttrs = {
                            id: fieldData.id + '-' + 'other',
                            className: fieldData.className + ' other-option'
                        };

                        _optionAttrsString = utils.attrString(Object.assign({}, fieldData, otherOptionAttrs));

                        optionsMarkup += '<input ' + _optionAttrsString + (opts.readonly ? ' disabled' : '')+ ' /> <label for="' + otherOptionAttrs.id + '">' + opts.label.other + '</label> <input type="text" data-other-id="' + otherOptionAttrs.id + '" name="' + optionName + '" id="' + otherOptionAttrs.id + '-value" style="display:none;" />';
                    }
                }
                fieldMarkup = fieldLabel + '<div class="' + fieldData.type + '-group">' + optionsMarkup + '</div>';
                break;
            case 'text':
            case 'password':
            case 'email':
            case 'number':
            case 'file':
            case 'hidden':
            case 'date':
            case 'tel':
            case 'autocomplete':
                var fieldVal = fieldData.value || '';
                if(opts.readonly){
                    fieldMarkup = fieldLabel + ' <span>' + fieldVal + '</span>';
                }else{
                    fieldMarkup = fieldLabel + ' <input ' + fieldDataString + ' maxlength="200"/>';
                }
                break;
            case 'color':
                fieldMarkup = fieldLabel + ' <input ' + fieldDataString + ' maxlength="30"> ' + opts.label.selectColor;
                break;
            case 'button':
            case 'submit':
                fieldMarkup = '<button ' + fieldDataString + '>' + fieldLabelVal + '</button>';
                break;
            case 'checkbox':
                var fieldVal = fieldData.value || '';
                fieldMarkup = fieldLabel + '<input ' + fieldDataString + (fieldVal === 'on' ? ' checked' : '') + (opts.readonly ? ' disabled' : '') + '> ';

                if (fieldData.toggle) {
                    setTimeout(function () {
                        $(document.getElementById(fieldData.id)).kcToggle();
                    }, 100);
                }
                break;
            default:
                fieldMarkup = '<' + fieldData.type + ' ' + fieldDataString + '>' + fieldLabelVal + '</' + fieldData.type + '>';
        }

        if (fieldData.type !== 'hidden') {
            var matchWidth = /(^|\s)width-\S+/g.exec(fieldData.className);
            var className = fieldData.id ? 'form-group field-'+fieldData.type+' field-' + fieldData.id + ' ' + (matchWidth ? matchWidth[0].trim(): '') : '';
            fieldMarkup = utils.markup('div', fieldMarkup, {
                className: className
            });
        } else {
            fieldMarkup = utils.markup('input', null, fieldData);
        }

        return fieldMarkup;
    };

    /**
     * Extend Element prototype to allow us to append fields
     *
     * @param  {object} fields Node elements
     */
    Element.prototype.appendFormFields = function (fields) {
        var element = this;
        fields.reverse();
        for (var i = fields.length - 1; i >= 0; i--) {
            element.appendChild(fields[i]);
        }
    };

    /**
     * Extend Element prototype to remove content
     */
    Element.prototype.emptyContainer = function () {
        var element = this;
        while (element.lastChild) {
            element.removeChild(element.lastChild);
        }
    };

    var otherOptionCB = function otherOptionCB() {
        var otherOptions = document.getElementsByClassName('other-option');

        var _loop = function _loop() {
            var otherInput = document.getElementById(otherOptions[i].id + '-value');
            otherOptions[i].onclick = function () {
                var option = this;
                if (this.checked) {
                    otherInput.style.display = 'inline-block';
                    option.nextElementSibling.style.display = 'none';
                    otherInput.oninput = function () {
                        option.value = this.value;
                    };
                } else {
                    otherInput.style.display = 'none';
                    option.nextElementSibling.style.display = 'inline-block';
                    otherInput.oninput = undefined;
                }
            };
        };

        for (var i = 0; i < otherOptions.length; i++) {
            _loop();
        }
    };
    
    var displaySelectedForMultipleSelect = function(self){
        var container = $(self.closest('.dropdown-menu.multi-level').attr("holder"));
        var valueHolder = container.find(' > input[type="hidden"]');
        var vaulueHolderValue = [];
        var holder = container.find(' > span');
        var holderText = [];
        self.closest(".dropdown-menu.multi-level").find('input:checked').each(function(){
            var path = [];
            $(this).parents().each(function(){
                if($(this).hasClass('prev-holder')) return false;
                var val = $(' > a > label', $(this)).text();
                if(val) path.push(val);
            });
            holderText.push("<span>" + path.reverse().join(' &raquo; ') + "</span>");
            vaulueHolderValue.push($(this).val());
        });
        holder.html(holderText.join(''));
        valueHolder.val(JSON.stringify(vaulueHolderValue));
    };

    var runCallbacks = function runCallbacks() {
        otherOptionCB();
        $(opts.container).find('.dropdown-menu.multi-level a').on('click', function(e){
            var self = $(this);
            var input = $(this).find('input');
            if(input.length){
                //display selected
                displaySelectedForMultipleSelect(self);
            }
            e.stopPropagation();
        });
        $(opts.container).find('input[type="radio"]:not([required])').click(function(){
            var previousValue = $(this).attr('previousValue');
            var name = $(this).attr('name');
            if (previousValue === 'checked'){
                $(this).removeAttr('checked');
                $(this).attr('previousValue', false);
            }else{
                $("input[name="+name+"]:radio").attr('previousValue', false);
                $(this).attr('previousValue', 'checked');
            }
        });
        $(opts.container).find('input[type="checkbox"][required]').click(function(){
            var name = $(this).attr('name');
            var checked = $(this).closest('.form-group').find('input[type="checkbox"][required][name="'+name+'"]:checked').length;
            if(checked === 0){
                $(this).prop('checked', true);
            }
        });
        $(opts.container).find('.dropdown-menu.multi-level a').each(function(){
            displaySelectedForMultipleSelect($(this));
        });
        
        $.timepicker.regional['ja'] = {
		closeText: '閉',
                prevText: '前日',
                nextText: '次日',
                currentText: '今日',
                monthNames: ['01月','02月','03月','04月','05月','06月','07月','08月','09月','10月','11月','12月'],
                monthNamesShort: ['01月','02月','03月','04月','05月','06月','07月','08月','09月','10月','11月','12月'],
                dayNames: ['日曜','月曜','火曜','水曜','木曜','金曜','土曜'],
                dayNamesShort: ['日','月','火','水','木','金','土'],
                dayNamesMin: ['日','月','火','水','木','金','土'],
                weekHeader: '週',
                firstDay: 1,
                isRTL: false,
                showMonthAfterYear: true,
                yearSuffix: '',//年
                month: '月',
                week: '週',
                day: '日',
                allDayText : '終日',
                timeOnlyTitle : '時間のみ',
                timeText : '時間',
                hourText : '何時',
                minuteText : '何分',
                secondText : '何秒'
	};
        $.timepicker.regional['vi'] = {
		timeOnlyTitle: 'Chọn giờ',
		timeText: 'Thời gian',
		hourText: 'Giờ',
		minuteText: 'Phút',
		secondText: 'Giây',
		millisecText: 'Mili giây',
		microsecText: 'Micrô giây',
		timezoneText: 'Múi giờ',
		currentText: 'Hiện thời',
		closeText: 'Đóng',
		timeSuffix: '',
		amNames: ['SA', 'S'],
		pmNames: ['CH', 'C'],
		isRTL: false
	};
        
        var dateTimeOpts = $.extend(true, 
            {
//                changeMonth: true,
//                changeYear: true,
                dateFormat: "yy/mm/dd",
                timeFormat: 'HH:mm',
                showButtonPanel: false
            }
            , $.timepicker.regional[PrimeFaces.settings.locale] || {});
        $('input[type="date"]').each(function(){
            var self = $(this);
            var settingVal = self.attr('value');
            self.addClass("form-control");
            self.attr('type', 'text');
            if(settingVal) self.val(settingVal);
            self.datetimepicker(dateTimeOpts);
            var btn = $('<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only dropdown-toggle" type="button" role="button" aria-disabled="false" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'+
                        '<span class="ui-button-icon-left ui-icon ui-icon-calendar"></span> '+
                        '<span class="ui-button-text ui-c">ui-button</span>'+
                    '</button>');
            $(this).after(btn);
            btn.click(function(){
                self.datetimepicker("show");
            });
        });
        $('select[multiple="true"]').each(function(e){
            $(this).multiselect({
                header: false,
                selectedList: 4,
                selectedListSeparator:", "
            });
        });
    };

    var santizeField = function santizeField(field) {
        var sanitizedField = Object.assign({}, field);
        sanitizedField.className = field.className || field.class || null;
        delete sanitizedField.class;

        return utils.trimObj(sanitizedField);
    };

    // Begin the core plugin
    var rendered = [];

    // generate field markup if we have fields
    if (opts.formData) {
        for (var i = 0; i < opts.formData.length; i++) {
            var sanitizedField = santizeField(opts.formData[i]);
            rendered.push(_helpers.fieldRender(sanitizedField));
        }

        if (opts.render) {
            if (opts.container) {
                var renderedFormWrap = utils.markup('div', rendered, {className: 'rendered-form cf'});
                opts.container = opts.container instanceof jQuery ? opts.container[0] : opts.container;
                opts.container.emptyContainer();
                opts.container.appendChild(renderedFormWrap);
                opts.container.appendChild(utils.markup('div', '', {className: 'clearfix'}));
            } else if (element) {
                element.emptyContainer();
                element.appendFormFields(rendered);
            }

            runCallbacks();
            opts.notify.success(opts.label.formRendered);
        } else {
            formRender.markup = rendered.map(function (elem) {
                return elem.innerHTML;
            }).join('');
        }
    } else {
        var noData = utils.markup('div', opts.label.noFormData, {
            className: 'no-form-data'
        });
        rendered.push(noData);
        opts.notify.error(opts.label.noFormData);
    }
    

    return formRender;
}

(function ($) {

    $.fn.formRender = function (options) {
        this.each(function () {
            var formRender = new FormRenderFn(options, this);
            return formRender;
        });
    };
})(jQuery);