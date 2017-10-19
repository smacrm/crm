var reRenderDynamicForm = function(){
    $(".autoFormData").each(function(){
        var self = $(this);
        var mode = self.attr("mode");
        mode = mode || 'readonly';
        var parent = self.parent();
        var formData = $.parseJSON(self.val());
   
        $.each(formData, function(i, div){
            var formBuilderItem = $("<div/>",{
                class: "box box-widget no-shadow no-margin"
            });
            var formBuilderItemHeader = $("<div/>",{
                class: "box-header with-border"
            });
            formBuilderItemHeader.text(div.name);
            if(div.name){
                formBuilderItem.append(formBuilderItemHeader);
            }
            var formBuilderItemContainer = $("<div/>",{
                class: "box-body col-" + div.col
            });
            formBuilderItem.append(formBuilderItemContainer);
            
            self.parent().append(formBuilderItem);
            var formRenderOpts = {
                container: formBuilderItemContainer,
                formData: JSON.stringify(div.fields),
                dynamicRestUrl : 'rest/customize/dynamic/list',
                readonly: mode === 'readonly',
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
            formBuilderItemContainer.formRender(formRenderOpts);
        });
        
        /**
        * Fix Dropdown menu, how to append dropdown menu to body element
        * 
        * @returns {undefined}
        */
        // and when you show it, move it to the body                                     
        $(this).parent().find(".dropdown-menu.multi-level").parent().each(function(){
            // hold onto the drop down menu                                             
            var dropdownMenu;
            
            $(this).on('show.bs.dropdown', function (e) {
                // grab the menu        
                dropdownMenu = $(e.target).find('.dropdown-menu.multi-level');

                // detach it and append it to the body
                $('body').append(dropdownMenu.detach());

                // grab the new offset position
                var eOffset = $(e.target).offset();

                // make sure to place it where it would normally go (this could be improved)
                dropdownMenu.css({
                    'display': 'inline-block',
                    'top': eOffset.top + $(e.target).outerHeight(),
                    'left': eOffset.left
                });
            })
            // and when you hide it, reattach the drop down, and hide it normally                                                   
            .on('hide.bs.dropdown', function (e) {
                $(e.target).append(dropdownMenu.detach());
                dropdownMenu.hide();
            });
        });
        
    });
}
jQuery(function ($) {
    'use strict';
    
    reRenderDynamicForm();
    
//    $(".customize-tabview").each(function(){
//        if($(this).find(".ui-tabs-nav > li").length === 1){
//            $(this).find(".ui-tabs-nav").remove();
//            $(this).addClass("no-tab");
//        }
//    });
});