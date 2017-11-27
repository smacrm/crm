/* global phoneNumber, BASE_URL, moment, Mousetrap, COMPANY_ID */

var CRMCLOUD = {
    edit_project_1: false,
    edit_project_3: false,
    
    dialogFrameWorkTop: function(formId, buttonId) {
        //var selector = $('#$1\\:$2_dlg'.replace('$1', formId).replace('$2', buttonId));
        var selector = $( "div[id$='\\:$1_dlg']".replace('$1', buttonId) );
        var visibility = selector.css('visibility');
        if (visibility !== 'hidden') {
            $(selector).css({top: '60px'});
        } else {
            setTimeout(function () {
                CRMCLOUD.dialogFrameWorkTop(formId, buttonId);
            }, 50);
        }
    },
    
    binding_edit_project: function() {
        $("body").on('click', '#sidebar-menu .edit-project', function(e){
            e.preventDefault();
            
            var self = $(this);
            if(self.attr('name') === 'edit_project_1') CRMCLOUD.edit_project_1 = true;
            if(self.attr('name') === 'edit_project_3') CRMCLOUD.edit_project_3 = true;
            
            var li = self.closest('li');
            var ul = li.next().find('ul');
            if( self.attr('ref') === 'edit' ){ //revert
                li.find(".add-project-item-action").trigger('click');
            }else{ //edit
                self.removeClass('fa-pencil').addClass('fa-plus');
                self.attr('ref', 'edit');
                ul.find('.project-item-count').hide();
                ul.find('.project-item-action').show();
                li.find('.close-edit-project').show();
            }
            e.stopImmediatePropagation();
        });
    },
    
    binding_close_edit_project: function() {
        $("body").on('click', "#sidebar-menu .close-edit-project", function(e){
            e.preventDefault();
            
            var self = $(this);
            if(self.attr('name') === 'edit_project_1') CRMCLOUD.edit_project_1 = false;
            if(self.attr('name') === 'edit_project_3') CRMCLOUD.edit_project_3 = false;
            
            var li = self.closest('li');
            var ul = li.next().find('ul');
            
            $(this).parent().find('.edit-project').removeClass('fa-plus');
            $(this).parent().find('.edit-project').attr('ref', 'none');
            ul.find('.project-item-count').show();
            ul.find('.project-item-action').hide();
            li.find('.close-edit-project').hide();
        });
    },
    
    show_project_item_action: function() {
        if(CRMCLOUD.edit_project_1) $('#menuListGroup').find('.project-item-action').show();
        if(CRMCLOUD.edit_project_3) $('#menuSearchGroup').find('.project-item-action').show();
    }
};

jQuery(document).ready(function($){
    $(window).click(function() {
        $('.navbar .search').removeClass('active');
    });
    $('.navbar .search').click(function(e){
        e.stopPropagation();
        $(this).toggleClass('active');
        $(this).find('input').focus();
    });
    $('.navbar .search .inline').click(function(e){
        e.stopPropagation();
    });
    
    // phone/tel processing'
    $('.navbar .phone .dropdown-menu').click(function(e){
        e.stopPropagation();
    });
    
    //nav icon
    $('.sidebar-toggle').hover(function(){
        $("#nav-icon").addClass('hover');
    }, function(){
        if($("#nav-icon").hasClass('open')) return;
        $("#nav-icon").removeClass('hover');
    });
    $("body").bind('expanded.pushMenu', function(e) {
        $("#nav-icon").addClass('hover open');
    });
    $("body").bind('collapsed.pushMenu', function(e) {
        $("#nav-icon").removeClass('hover open');
    });
    
    
    $('.num').click(function () {
        var num = $(this);
        var text = $.trim(num.find('.txt').clone().children().remove().end().text());
        var num = $("#telNumber").inputmask('unmaskedvalue').replace(/_/g, '')+text;
        // console.log(num);
        $("#telNumber").val(num);
        playTouchSound();
        suggestCustomer();
    });
    
    var playTouchSound = function(){
        $('#touch_sound')[0].play();
        setTimeout(function(){
            $('#touch_sound')[0].pause();
            $('#touch_sound')[0].currentTime = 0;
        }, 150);
    };
    
    var _touchKeypadTimeout = null;
    var suggestCustomer = function(){
        if(_touchKeypadTimeout) clearTimeout(_touchKeypadTimeout);
        _touchKeypadTimeout = setTimeout(function(){
            var searchPhoneNumber = $("#telNumber").inputmask('unmaskedvalue');
            if(searchPhoneNumber.charAt(0) !== '0') searchPhoneNumber = '0' + searchPhoneNumber;
            
            var nameContainer = $('#navigation .dropdown.phone .search-cus-name > span:first');
            var phoneContainer = $('#navigation .dropdown.phone .search-cus-phonenumber');
            var lastCallContainer = $('#navigation .dropdown.phone .search-cus-lastcall');
            
            var name = nameContainer.data('name'), 
                phone = phoneContainer.data('phone'), 
                lastCall = lastCallContainer.data('last-call');
            
            if(searchPhoneNumber !== '0'){
                $.ajax({
                    dataType: "json",
                    url: BASE_URL + "/rest/softphone/customer/search",
                    async: false,
                    data: {cid: COMPANY_ID, pn: searchPhoneNumber},
                    success: function(list){
                        $.each(list, function(i, item){
                            name = item.name;
                            phone = item.phone;
                            lastCall = moment(item.lastCall).format("YYYY/MM/DD H:mm");
                            return false;
                        });
                    }
                });
            }
            
            if(!nameContainer.data('name')) nameContainer.data('name', nameContainer.html());
            nameContainer.html(name);
            
            if(!phoneContainer.data('phone')) phoneContainer.data('phone', phoneContainer.html());
            phoneContainer.html(phone);
            
            if(!lastCallContainer.data('last-call')) lastCallContainer.data('last-call', lastCallContainer.html());
            lastCallContainer.html(lastCall);
            
        },300);
    };
   
    $("#telNumber").keyup(function(e){
        suggestCustomer();
        playTouchSound();
    });
    $("#telNumber").keypress(function(e) {
        if(e.which === 13) {
            e.preventDefault();
            $("#telNumber").focus();
            $("#start-call").trigger("click");
        }
    });
    $("#navigation .back-action").click(function(e) {
        e.preventDefault();
        playTouchSound();
        var val = $("#telNumber").inputmask('unmaskedvalue');
        val = val.replace(/[0-9]$/ig, '');
        $("#telNumber").val(val);
        suggestCustomer();
    });
    //end
    
    $(document).on('click', '.cmd-remote-command', function(){
       var cmd = $(this).attr("cmd");
       $(cmd).trigger('click');
    });
    
    $.ajaxPrefilter(function( options ) {
        var orgId = $.url(options.url).param('id');
        var s = $.url().param('s');
        var id = $.url().param('id');
        if (typeof s === 'undefined' || !s ) s = '';
        if (typeof id === 'undefined' || !id ) id = '';
        
        if (typeof orgId !== 'undefined' && orgId ) id = orgId;
        
        if(options.url.indexOf('index.xhtml') >= 0){
            options.url = '/index.xhtml?s=' + s + ( id ? '&id=' + id : '');
        }
    });
    
    var _quickSearchIssueTimeout = null;
    var suggestIssue = function(container, code){
        if(_quickSearchIssueTimeout) clearTimeout(_quickSearchIssueTimeout);
        listContainer = container.find('.dropdown-menu .list-group');
        if($.trim(code).length > 0){
            listContainer.show();
            _quickSearchIssueTimeout = setTimeout(function(){
                listContainer.html('<div class="text-center"><i class="fa fa-spinner fa-spin"></i></div>');
                $.ajax({
                    dataType: "json",
                    url: BASE_URL + "/rest/issue/search/" + code,
                    async: false,
                    success: function(list){
                        listContainer.html('');
                        $.each(list, function(i, item){
                            id = item.id;
                            code = item.code;
                            receiveDate = item.receiveDate;
                            updatedPersonName = item.hasOwnProperty('updatedPersonName') ? item.updatedPersonName: '';
                            status =  item.hasOwnProperty('status') ? item.status: '';
                            closedDate = item.hasOwnProperty('closedDate') ? item.closedDate: '';

                            if(receiveDate) receiveDate = moment(receiveDate).format("YYYY/MM/DD H:mm");
                            if(closedDate) closedDate = moment(closedDate).format("YYYY/MM/DD H:mm");

                            $li = $('<li/>', {
                                id: id,
                                class: 'list-group-item ' + (i === 0 ? 'active': '')
                            });
                            $li.append('<h4>'+code+'<i class="text-sm pull-right">'+receiveDate+'</i></h4>');
                            if(status) $li.append(status);
                            if(updatedPersonName) $li.append('<i class="text-sm pull-right" title="'+(closedDate ? closedDate : '')+'">'+updatedPersonName+'</i>');

                            $li.click(function(e){
                                e.preventDefault();
                                $(".quick-search-match-id").val($(this).attr('id'));
                                $(".quick-search-action").trigger('click');
                                $('.navbar .search').removeClass('active');
                                $('.navbar .search input').blur();
                            });

                            listContainer.append($li);
                            container.addClass('open');
                        });
                    }
                });
            }, 300);
        }else{
            listContainer.html('<div class="text-center"><i class="fa fa-spinner fa-spin"></i></div>');
            listContainer.hide(); 
        }
    };
    
    $(".txt-quick-search").keyup(function(event) {
        var code = $(this).val();
        var container = $(".txt-quick-search").closest('.search.dropdown');
        if(event.which === 37 || event.which === 39) { //left 
            
        }else if(event.which === 13) {
            container.find('li.list-group-item.active').trigger('click');
            $('.navbar .search').removeClass('active');
            $('.navbar .search input').blur();
            event.preventDefault();
        }else if(event.which === 38) { //up
            var active = container.find('li.list-group-item.active');
            var prev = active.prev();
            if(prev.length){
                active.removeClass('active');
                prev.addClass('active');
                var scrollContainer = container.find('.suggest-issue-list');
                scrollContainer.scrollTop(scrollContainer.scrollTop() + prev.position().top - prev.outerHeight());
            }
            event.preventDefault();
        }else if(event.which === 40) { //down
            var active = container.find('li.list-group-item.active');
            var next = active.next();
            if(next.length){
                active.removeClass('active');
                next.addClass('active');
                var scrollContainer = container.find('.suggest-issue-list');
                scrollContainer.scrollTop(scrollContainer.scrollTop() + next.position().top - next.outerHeight());
            }
            event.preventDefault();
        }else{
            suggestIssue(container, code);
        }
    });
    
    //process paste data for input_post, input_tel, input_email
    $('body').on("paste", 'input.input_post, input.input_number', function(e){
        var ctl = $(this);
        setTimeout(function() {
            ctl.val(ctl.val().replace(/\D/g,''));
        }, 100);
    });
    
    $('body').on("paste", 'input.input_tel', function(e){
        var ctl = $(this);
        setTimeout(function() {
            ctl.val(ctl.val().replace(/[^0-9\-\+]/g,''));
        }, 100);
    });
    
    $('body').on("paste", 'input.input_mail', function(e){
        var ctl = $(this);
        setTimeout(function() {
            ctl.val(ctl.val().replace(/[^a-z0-9_\.\-@]/g,''));
        }, 100);
    });
    
    $('body').on("paste", 'input.input_url', function(e){
        var ctl = $(this);
        setTimeout(function() {
            ctl.val(ctl.val().replace(/[^a-z0-9\-:\/\/]/ig,''));
        }, 100);
    });
    
    //bind shortcut key
    Mousetrap.bind(['ctrl+space', 'f2'], function() {
        $('.navbar .search').trigger('click');
    });
    Mousetrap.bind(['f4'], function() {
        $('.navbar .dropdown.phone > a').trigger('click');
        $('.navbar .dropdown.phone #telNumber').focus();
    });
    $('.navbar .search input').keyup(function(e){
        if (e.keyCode === 27){
            $('.navbar .search').removeClass('active');
            $(this).blur();
        }
    });
});