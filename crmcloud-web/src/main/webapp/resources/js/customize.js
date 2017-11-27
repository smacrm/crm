jQuery(document).ready(function ($) {
    'use strict';
    var $fbPages = $('#form-builder-pages'),
            addPageTab = $('#add-page-tab');

    //test only
    //var formJsonSample = '[{"id":1,"name":"Tab 1","div":[{"name":"DIV1","fields":[{"type":"button","label":"Button","subtype":"button","className":"button-input btn-default btn","name":"button-1475985426137","style":"default"}]},{"name":"DIV2","fields":[{"type":"text","label":"Text Field","subtype":"text","className":"form-control","name":"text-1475985431211"},{"type":"button","label":"Button","subtype":"button","className":"button-input btn-default btn","name":"button-1475985433457","style":"default"}]}]}]';
    //$(".autoFormContent").val(formJsonSample);
    //test only

    var $tabs = $fbPages.tabs({
        beforeActivate: function (event, ui) {
            if (ui.newPanel.selector === '#new-page') {
                return false;
            }
        }
    });
    
    
    // sortable tabs
    $tabs.find(".ui-tabs-nav").sortable({
        axis: "x",
        stop: function (event, ui) {
            $fbPages.find(".ui-tabs-nav > li > a").each(function(index){
                var tabId = $(this).attr('href').replace('#', '');
                var tabOrder = index + 1;
                for (var i = 1; i <= $tabBuuilderList.length; i++) {
                    var formItem = $tabBuuilderList[i - 1];
                    if(formItem.data('tabId') == tabId){
                        formItem.data('tabOrder', tabOrder);
                        break;
                    }
                }
            });
            
            
            $tabs.tabs("refresh");
        }
    });
    
    
    var defaultOptions = {
        messages: formbuilder_langs,
        controlPosition: 'right',
        defaultFields: false,
        dataType: 'json',
        showActionButtons: false,
        fieldRemoveWarn: true,
        language: ['ja', 'en', 'vi'],
        dynamicRestUrl : 'rest/customize/dynamic/list',
        controlOrder: [//'button', 
            'checkbox', 'checkbox-group', 'date',
            //'header', 
            //'paragraph', 
            //'number', 
            'radio-group', 'select', 'text', 'textarea'],
    };
    
    defaultOptions = $.extend(true, rootOptions || {}, defaultOptions);

    var initFormBuilder = function ($elm, options) {
        return $elm.formBuilder(options);
    };

    var $tabBuuilderList = [];

    addPageTab.click(function () {
        newTab();
    });

    var newTab = function (tab) {
        var tabCount = ($tabBuuilderList.length + 1),
                tabId = tab ? tab.id : 'page-' + tabCount,
                $newPageTemplate = $(document.getElementById('new-page')),
                $newPage = $newPageTemplate.clone().attr('id', tabId),
                $newTab = addPageTab.clone().removeAttr('id'),
                $tabLink = $('a', $newTab).attr('href', '#' + tabId)
                                .attr("ref", tab ? tab.id : '')
                                .html('<div class="input-group"><input type="text" name="tabname-' + tabCount + '" value="' + (tab ? tab.name : 'Tab ' + tabCount) + '"/><span class="input-group-btn" style="width: auto"><button class="btn btn-xs btn-danger pull-right" style="z-index: 1"><i class="fa fa-times" aria-hidden="true"></i></button></span></div>');

        $newPage.insertBefore($newPageTemplate);
        $newTab.insertBefore(addPageTab);
        $fbPages.tabs('refresh');
        $fbPages.tabs("option", "active", tab ? 0 : document.getElementById('tabs').children.length-2);
        $tabLink.find("button").click(function(e){
            e.preventDefault();
            var self = $(this);
            var cfm = true;
            if(self.attr('no-cfm') !== 'true'){
                cfm = confirm("ブロックを削除確認してください。");
            }
            if( cfm ){
                $newTab.remove();
                $newPage.remove();
                $fbPages.tabs('refresh');
                $fbPages.tabs("option", "active", document.getElementById('tabs').children.length - 1);
            }
        });
        

        var settings = $.extend(true, {}, defaultOptions);
        settings.defaultDivs = tab ? tab.divs : undefined;
        settings.refId = tab ? tab.id : 0;
        var formBuilder = initFormBuilder($newPage, settings);
        formBuilder.data('tabId', settings.refId);
        formBuilder.data('tabOrder', tab ? tab.order : tabCount + 1);
        $tabBuuilderList.push(formBuilder);

        //formBuilder.find(".cb-wrap").appendTo(formBuilder);
        $newPage.sortable().disableSelection();
    };

    var parseJsonData = function (tabs) {
        for (var i = 0; i < tabs.length; i++) {
            var tab = tabs[i];
            newTab(tab);
        }
    };

    $(".customize-save").click(function () {
        var page = [];
        var isValid = true;

        for (var i = 1; i <= $tabBuuilderList.length; i++) {
            var formItem = $tabBuuilderList[i - 1];
            var formBuilerItem = formItem.data('formBuilder');
            if(formBuilerItem !== undefined){
                var tab = {};
                var divs = JSON.parse(formBuilerItem.getData());
                if(divs.length > 0){
                    var $tabName = $('[name="tabname-' + i + '"]');
                    var strTabName = $tabName.val().trim();
                    if(strTabName){
                        tab.id = formBuilerItem.getRefId();
                        tab.name = strTabName;
                        tab.order = formItem.data('tabOrder');
                        tab.divs = divs;
                        page.push(tab);
                    }else{
                        isValid = false;
                        $tabName.focus();
                        showAlert('タプ名を入力してください。');
                        break;
                    }
                }
            }
            //console.log(formItem.data('formBuilder').getData());
        }
        if(isValid){
            var data = JSON.stringify(page);
//            console.log(page);
            $(".autoFormContent").val(data);
            $(".customize-save-action").click();
        }
    });

    var defaultData, options = {};
    if ($(".autoFormContent").val()) {
        defaultData = $.parseJSON($(".autoFormContent").val());
        options.defaultFields = defaultData;
        parseJsonData(defaultData);
    } else {
        newTab();
    }
});