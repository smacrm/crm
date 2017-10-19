/*
 * jQuery gentleSelect plugin (version 0.1.4.1)
 * http://shawnchin.github.com/jquery-cron
 *
 * Copyright (c) 2010-2013 Shawn Chin.
 * Dual licensed under the MIT or GPL Version 2 licenses.
 *
 * Requires:
 * - jQuery
 *
 * Usage:
 *  (JS)
 *
 *  // initialise like this
 *  var c = $('#cron').cron({
 *    initial: '9 10 * * *', # Initial value. default = "* * * * *"
 *    url_set: '/set/', # POST expecting {"cron": "12 10 * * 6"}
 *  });
 *
 *  // you can update values later
 *  c.cron("value", "1 2 3 4 *");
 *
 * // you can also get the current value using the "value" option
 * alert(c.cron("value"));
 *
 *  (HTML)
 *  <div id='cron'></div>
 *
 * Notes:
 * At this stage, we only support a subset of possible cron options.
 * For example, each cron entry can only be digits or "*", no commas
 * to denote multiple entries. We also limit the allowed combinations:
 * - Every minute : * * * * *
 * - Every hour   : ? * * * *
 * - Every day    : ? ? * * *
 * - Every week   : ? ? * * ?
 * - Every month  : ? ? ? * *
 * - Every year   : ? ? ? ? *
 */
(function ($) {
    function Cron(type, minute, hour, day, month, dow) {
        this.type = type;
        this.minute = minute;
        this.hour = hour;
        this.day = day;
        this.month = month;
        this.dow = dow;
    }

    var cron;

    var defaults = {
        minuteOpts: {
            minWidth: 100, // only applies if columns and itemWidth not set
            itemWidth: 30,
            columns: 4,
            rows: undefined,
            title: "Minutes Past the Hour"
        },
        timeHourOpts: {
            minWidth: 100, // only applies if columns and itemWidth not set
            itemWidth: 20,
            columns: 2,
            rows: undefined,
            title: "Time: Hour"
        },
        domOpts: {
            minWidth: 100, // only applies if columns and itemWidth not set
            itemWidth: 30,
            columns: undefined,
            rows: 10,
            title: "Day of Month"
        },
        monthOpts: {
            minWidth: 100, // only applies if columns and itemWidth not set
            itemWidth: 100,
            columns: 2,
            rows: undefined,
            title: undefined
        },
        dowOpts: {
            minWidth: 100, // only applies if columns and itemWidth not set
            itemWidth: undefined,
            columns: undefined,
            rows: undefined,
            title: undefined
        },
        timeMinuteOpts: {
            minWidth: 100, // only applies if columns and itemWidth not set
            itemWidth: 20,
            columns: 4,
            rows: undefined,
            title: "Time: Minute"
        },
        effectOpts: {
            openSpeed: 400,
            closeSpeed: 400,
            openEffect: "slide",
            closeEffect: "slide",
            hideOnMouseOut: true
        },
        url_set: undefined,
        customValues: undefined,
        onChange: undefined, // callback function each time value changes
        initCron: undefined,
        periods: ["minute", "hour", "day", "week", "month", "year"],
        days: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
        months: ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
    };

    // -------  build some static data -------
    // options for minutes in an hour
    var str_opt_mih = "";
    // options for hours in a day
    var str_opt_hid = "";
    // options for days of month
    var str_opt_dom = "";
    // options for period
    var str_opt_period = "";
    // options for months
    var str_opt_month = "";
    // options for day of week
    var str_opt_dow = "";
    // display matrix
    var toDisplay = {
        "0": ["txtmin"],
        "1": ["mins"],
        "2": ["time"],
        "3": ["dow", "time"],
        "4": ["dom", "time"],
        "5": ["dom", "month", "time"]
    };
    
    function buildStaticData(periods, days, months) {
        for (var i = 0; i < 60; i++) {
            var j = (i < 10) ? "0" : "";
            str_opt_mih += "<option value='" + i + "'>" + j + i + "</option>\n";
        }
        for (var i = 0; i < 24; i++) {
            var j = (i < 10) ? "0" : "";
            str_opt_hid += "<option value='" + i + "'>" + j + i + "</option>\n";
        }
        for (var i = 1; i < 32; i++) {
            var suffix = "th";
            if (i === 1 || i === 21 || i === 31) {
                suffix = "st";
            } else if (i === 2 || i === 22) {
                suffix = "nd";
            } else if (i === 3 || i === 23) {
                suffix = "rd";
            }
            str_opt_dom += "<option value='" + i + "'>" + i + suffix + "</option>\n";
        }
        for (var i = 0; i < months.length; i++) str_opt_month += "<option value='" + (i + 1) + "'>" + months[i] + "</option>\n";
        for (var i = 0; i < periods.length; i++) str_opt_period += "<option value='" + i + "'>" + periods[i] + "</option>\n";
        for (var i = 0; i < days.length; i++) str_opt_dow += "<option value='" + (i + 1) + "'>" + days[i] + "</option>\n";
    }

    // ------------------ internal functions ---------------
    function defined(obj) {
        if (typeof obj === "undefined") {
            return false;
        } else {
            return true;
        }
    }

    function getCurrentValue(c) {
        var b = c.data("block");
        var min = hour = day = month = dow = "*";
        var selectedPeriod = b["period"].find("select").val();
        switch (selectedPeriod) {
            case "0":
                min = b["txtmin"].find("input").val();
                break;

            case "1":
                min = b["mins"].find("select").val();
                break;
            case "2":
                min = b["time"].find("select.cron-time-min").val();
                hour = b["time"].find("select.cron-time-hour").val();
                break;

            case "3":
                min = b["time"].find("select.cron-time-min").val();
                hour = b["time"].find("select.cron-time-hour").val();
                dow = b["dow"].find("select").val();
                break;

            case "4":
                min = b["time"].find("select.cron-time-min").val();
                hour = b["time"].find("select.cron-time-hour").val();
                day = b["dom"].find("select").val();
                break;

            case "5":
                min = b["time"].find("select.cron-time-min").val();
                hour = b["time"].find("select.cron-time-hour").val();
                day = b["dom"].find("select").val();
                month = b["month"].find("select").val();
                break;

            default:
                // we assume this only happens when customValues is set
                return selectedPeriod;
        }
        
        cron = new Cron(selectedPeriod, min, hour, day, month, dow);
        return JSON.stringify(cron);
    }

    // -------------------  PUBLIC METHODS -----------------
    var methods = {
        init: function (opts) {
            // init options
            var options = opts ? opts : {}; /* default to empty obj */
            var o = $.extend([], defaults, options);
            var eo = $.extend({}, defaults.effectOpts, options.effectOpts);
            $.extend(o, {
                minuteOpts: $.extend({}, defaults.minuteOpts, eo, options.minuteOpts),
                domOpts: $.extend({}, defaults.domOpts, eo, options.domOpts),
                monthOpts: $.extend({}, defaults.monthOpts, eo, options.monthOpts),
                dowOpts: $.extend({}, defaults.dowOpts, eo, options.dowOpts),
                timeHourOpts: $.extend({}, defaults.timeHourOpts, eo, options.timeHourOpts),
                timeMinuteOpts: $.extend({}, defaults.timeMinuteOpts, eo, options.timeMinuteOpts),
                initCron: $.extend({}, defaults.initCron, eo, options.initCron)
            });

            // ---- define select boxes in the right order -----
            // options for minutes in an hour
            str_opt_mih = "";
            // options for hours in a day
            str_opt_hid = "";
            // options for days of month
            str_opt_dom = "";
            // options for period
            str_opt_period = "";
            // options for months
            str_opt_month = "";
            str_opt_dow = "";
            buildStaticData(o.periods, o.days, o.months);

            var block = [], custom_periods = "", cv = o.customValues;
            if (defined(cv)) { // prepend custom values if specified
                for (var key in cv) {
                    custom_periods += "<option value='" + cv[key] + "'>" + key + "</option>\n";
                }
            }

            // loại cron
            block["period"] = $("<span class='cron-period'>" + "Every <select name='cron-period' class='period ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all'>" + custom_periods + str_opt_period + "</select> </span>")
                    .appendTo(this)
                    .data("root", this);
            var select = block["period"].find("select");
            select.bind("change.cron", event_handlers.periodChanged).data("root", this);

            // lưu phút nếu loại cron là minutes
            block["txtmin"] = $("<span class='cron-block cron-block-txtmin'> at <input required type='number' min='0' max='59' name='cron-txtmin' class='txtmin ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all input_text'/> </span>")
                    .appendTo(this)
                    .data("root", this);
            select = block["txtmin"].find("input").data("root", this);
            // chỉ cho phép nhập số, không cho phép nhật chữ và các kí tự khác.
            block["txtmin"].find("input").bind("keyup paste", function(){
                    setTimeout(jQuery.proxy(function() {
                            this.val(this.val().replace(/[^0-9]/g, ''));
                }, $(this)), 0);
            });
            
            block["dom"] = $("<span class='cron-block cron-block-dom'>" + " on the <select name='cron-dom' class='dom ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all'>" + str_opt_dom + "</select> </span>")
                    .appendTo(this)
                    .data("root", this);
            select = block["dom"].find("select").data("root", this);

            block["month"] = $("<span class='cron-block cron-block-month'>" + " of <select name='cron-month' class='cron-month ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all'>" + str_opt_month + "</select> </span>")
                    .appendTo(this)
                    .data("root", this);
            select = block["month"].find("select").data("root", this);

            block["mins"] = $("<span class='cron-block cron-block-mins'>" + " at <select name='cron-mins' class='cron-mins ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all'>" + str_opt_mih + "</select> minutes past the hour </span>")
                    .appendTo(this)
                    .data("root", this);
            select = block["mins"].find("select").data("root", this);

            block["dow"] = $("<span class='cron-block cron-block-dow'>" + " on <select name='cron-dow' class='ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all'>" + str_opt_dow + "</select> </span>")
                    .appendTo(this)
                    .data("root", this);
            select = block["dow"].find("select").data("root", this);

            block["time"] = $("<span class='cron-block cron-block-time'>" + " at <select name='cron-time-hour' class='cron-time-hour ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all'>" + str_opt_hid + "</select>:<select name='cron-time-min' class='cron-time-min ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all'>" + str_opt_mih + " </span>")
                    .appendTo(this)
                    .data("root", this);
            
            select = block["time"].find("select.cron-time-hour").data("root", this);
            select = block["time"].find("select.cron-time-min").data("root", this);

            var initCron = o.initCron;
            var initial = undefined;
            if (initCron) initial = [initCron.type, initCron.minute, initCron.hour, initCron.day, initCron.month, initCron.dow].join(" ");
            
            this.find("input").bind("change.cron-callback", event_handlers.somethingChanged);
            this.find("select").bind("change.cron-callback", event_handlers.somethingChanged);
            this.data("options", o).data("block", block); // store options and block pointer
            
            return methods["value"].call(this, initial); // set initial value
        },
        
        value: function (cron_str) {
            // when no args, act as getter
            if (!cron_str) return getCurrentValue(this);
            
            var block = this.data("block");
            var d = cron_str.split(" ");
            var v = {
                "mins": d[1],
                "hour": d[2],
                "dom": d[3],
                "month": d[4],
                "dow": d[5]
            };

            // update appropriate select boxes
            var targets = toDisplay[d[0]];
            for (var i = 0; i < targets.length; i++) {
                var tgt = targets[i];
                if (tgt === "txtmin") {
                    block[tgt].find("input").val(v["mins"]);
                } else if (tgt === "time") {
                    block[tgt].find("select.cron-time-hour").val(v["hour"]);
                    block[tgt].find("select.cron-time-min").val(v["mins"]);
                } else {
                    block[tgt].find("select").val(v[tgt]);
                }
            }

            // trigger change event
            var bp = block["period"].find("select").val(d[0]);
            bp.trigger("change");

            return this;
        }
    };

    var event_handlers = {
        periodChanged: function () {
            var root = $(this).data("root");
            var block = root.data("block"), opt = root.data("options");
            var period = $(this).val();

            root.find("span.cron-block").hide(); // first, hide all blocks
            if (toDisplay.hasOwnProperty(period)) { // not custom value
                var b = toDisplay[$(this).val()];
                for (var i = 0; i < b.length; i++) {
                    block[b[i]].show();
                }
            }
        },
        somethingChanged: function () {
            root = $(this).data("root");
            // chain in user defined event handler, if specified
            var oc = root.data("options").onChange;
            if (defined(oc) && $.isFunction(oc)) {
                oc.call(root);
            }
        }
    };

    $.fn.cron = function (method) {
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist on jQuery.cron');
        }
    };

})(jQuery);
