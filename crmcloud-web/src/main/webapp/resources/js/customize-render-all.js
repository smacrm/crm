jQuery(function ($) {
    'use strict';
    /**
     * Need div with id #fb-rendered-form
     * 
     * Sample Form:
     * 
     * <h:inputHidden value="#{customizeController.autoFormContent}" id="autoFormContent" />
     * <div id="fb-rendered-form" class="customize-form">
     *       <ul id="tabs">
     *           <!--<a href="#new-page">Tab</a>-->
     *       </ul>
     *       <!--<div id="new-page">Page Content</div>-->
     * </div>
     * 
     */
    var $formContainer = $('#fb-rendered-form');
    var $tabContainer = $formContainer.find("ul#tabs");
    $formContainer.tabs({
        beforeActivate: function (event, ui) {
            if (ui.newPanel.selector === '#new-page') {
                return false;
            }self.val()
        }
    });
    
    var formData;
    if ($("#autoFormContent").val().trim()) {
        formData = $("#autoFormContent").val();
        
    }
    var formRenderOpts = {
        dataType: 'json',
        notify: {
            error: function(message) {
              return console.error(message);
            },
            success: function(message) {
              //return console.log(message);
            },
            warning: function(message) {
              //return console.warn(message);
            }
          }
    };
    
    var newTab = function(data, tab){
        var tabCount = $tabContainer.find("li").length,
                tabId = 'page-' + tabCount.toString(),
                $newPage = $("<div />").attr('id', tabId),
                $newTab = $("<li><a/></li>"),
                $tabLink = $('a', $newTab).attr('href', '#' + tabId).attr("ref", tab.id).text(tab.name);

        $newPage.appendTo($formContainer);
        $newTab.appendTo($tabContainer);
        $formContainer.tabs('refresh');
        $formContainer.tabs("option", "active", 0);
        
        var settings = $.extend(true, formRenderOpts, {container: $newPage,formData: JSON.stringify(data)});
        $formContainer.formRender(settings);
    };
    
    var tabs = $.parseJSON(formData);
    for(var i = 0; i< tabs.length; i++){
        var tab = tabs[i];
        var fields = tab.fields;
        newTab(fields, tab);
    }
});
