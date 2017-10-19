/* global moment, ISSUE_STATUS_FLOW, ISSUE_ESCALATION_EXISTS */

/**
 * Validate Issue's customize form
 * 
 * @returns {undefined}
 */
String.prototype.format = function() {
    var args = arguments;

    return this.replace(/\{(\d+)\}/g, function() {
        return args[arguments[1]];
    });
};

var $currentSummitButton, allowExecuteCallbackDialog = true;

function doValidateAndSubmitIssue($submitButton, requireMessagePostfix = ' is required.'){
    console.log('validate issue form');
    var isValid = true;
    //validate main elm in form
    $currentSummitButton = $submitButton;
    
    //issue creator
    var $creator = $('#form_issue #label_issue_creator_id');
    var $creatorLabelContainer = $creator.parent().prev();
    if( !$creator.text() ){
        var label = $creatorLabelContainer.addClass('has-error').find('label:first').text();
        isValid = false;
        showAlert(requireMessagePostfix.format(label));
    }else{
        $creatorLabelContainer.removeClass('has-error');
    }
    
    //issue receive date
    var $rcDate =$('#form_issue #issue_receive_date input');
    var $rcDateLabelContainer = $rcDate.closest('td').prev();
    if( !$rcDate.val() || !moment($rcDate.val(), 'YYYY/MM/DD HH:ss',true).isValid() ){
        var label = $rcDateLabelContainer.addClass('has-error').find('label:first').text();
        isValid = false;
        showAlert(requireMessagePostfix.format(label));
    }else{
        $rcDateLabelContainer.removeClass('has-error');
    }
    
    //issue receive person
    var $receiver = $('#form_issue #label_issue_receive_person_id');
    var $receiverLabelContainer = $receiver.parent().prev();
    if( !$receiver.text() ){
        var label = $receiverLabelContainer.addClass('has-error').find('label:first').text();
        isValid = false;
        showAlert(requireMessagePostfix.format(label));
    }else{
        $receiverLabelContainer.removeClass('has-error');
    }
    
    //issue status id
    var $status =$('#form_issue #issue_status_id select');
    var $statusLabelContainer = $status.closest('td').prev();
    if( !$status.val() ){
        var label = $statusLabelContainer.addClass('has-error').find('label:first').text();
        isValid = false;
        showAlert(requireMessagePostfix.format(label));
    }else{
        $statusLabelContainer.removeClass('has-error');
    }
    
    //issue public id
    var $public = $('#form_issue #issue_public_id');
    var $publicLabelContainer = $public.parent().prev();
    if( !$public.find('.ui-button.ui-state-active').length ){
        var label = $publicLabelContainer.addClass('has-error').find('label:first').text();
        isValid = false;
        showAlert(requireMessagePostfix.format(label));
    }else{
        $publicLabelContainer.removeClass('has-error');
    }
    
    //issue proposal
    var $proposal = $("#form_issue #tabViewIssue .outputLabelProposalName");
    if($proposal.length > 0){ // Check trong TH mode la issue
        var $proposalLabelContainer = $proposal.closest('tr');
        if( !$proposal.text() ){
            var label = $proposalLabelContainer.addClass('has-error').find('label:first').text();
            isValid = false;
            showAlert(requireMessagePostfix.format(label));
        }else{
            $proposalLabelContainer.removeClass('has-error');
        }
    }
    
    //validate customize form
    var tabList = $('#form_issue .customize-tabview');
    for( var i = 0; i < tabList.length; i++ ){
        var tab = tabList[i];
        $(tab).find('label > span.required').each(function(idx1, span){
            var $parent = $(span).parent();
            var inputId = $parent.attr('for');
            var label = $parent.clone().children().remove().end().text();
            
            var hasVal = false;
            $('[name="'+inputId+'"]').each(function(idx, elm){
                var $elm = $(elm);
                //if($elm.)
                var val = $elm.val();
                if(elm.tagName === 'INPUT'){
                    var type = $elm.attr('type').toLowerCase();
                    if( type === 'checkbox' || type === 'radio' ){
                        val = $elm.is(':checked');
                    }else{
                        var rawData = $.trim($elm.val());
                        val = rawData ? true : false;
                        if(val){ // Neu du lieu thuoc dang json
                            try {
                                if(JSON.parse(rawData).length === 0){
                                    val = false;
                                }
                            } catch (e) {}
                        }
                    }
                }
                if(val){
                    hasVal |= true;
                }else{
                    hasVal |= false;
                }
                // console.log(inputId + ':' + idx + ' >> ' + hasVal);
                // console.log($elm.val());
            });
            if(!hasVal){
                $parent.parent().addClass('has-error');
                showAlert(requireMessagePostfix.format(label));
            }else{
                $parent.parent().removeClass('has-error');
            }
            isValid &= hasVal;
        });
    }
    
    // validate email
    $('#form_issue input.input_mail').each(function(){
        var email = $(this).val().trim();
        if(email){
            var label = $(this).closest("tr").find('> td:first');
            if(!isValidEmailAddress( email )){
                $(this).addClass('ui-state-error');
                isValid &= false;
                showAlert(requireMessagePostfix.format(label.text()));
            }else{
                $(this).removeClass('ui-state-error');
                isValid &= true;
            }
        }
    });
    
    var newStatus = $('#issue_status_id_input').val();
    //issue close date
    var meta = newStatus in ISSUE_STATUS_FLOW ? ISSUE_STATUS_FLOW[newStatus] : {step: 0, data_required: false, end_time_required: false};
    var $clDate =$('#form_issue #issue_close_date input');
    var $clDateLabelContainer = $clDate.closest('td').prev();
    if(meta.end_time_required === true){
        if( !$clDate.val() || !moment($clDate.val(), 'YYYY/MM/DD HH:ss',true).isValid() ){
            var label = $clDateLabelContainer.addClass('has-error').find('label:first').text();
            isValid = false;
            showAlert(requireMessagePostfix.format(label));
        }else{
            $clDateLabelContainer.removeClass('has-error');
        }
    }else{
        $clDateLabelContainer.removeClass('has-error');
    }
    
    console.log('Check issue valid');
    if(isValid){ // neu du lieu form valid va khong hien thi popup thi summit form
        var data = checkIssueStatusFlow(newStatus, true); // kiem tra va show popup neu can thiet
        if(data === false){
            console.log('Summit issue 1');
            $submitButton.trigger('click');
        }
    }
}

function checkIssueStatusFlow(status, showPopup){
    // alert(ISSUE_STATUS_FLOW);
    /**
     * Step execution
     * 1: cmdCustomerSupportMail
     * 2: cmdSendSupportMail
     * 3: cmdSendComment
     * 4: cmdSupport
     * 5: 承認依頼
     * 6: END
     */
    var meta = status in ISSUE_STATUS_FLOW ? ISSUE_STATUS_FLOW[status] : {step: 0, data_required: false, end_time_required: false};
    if (typeof ISSUE_ESCALATION_EXISTS === 'undefined') {
        ISSUE_ESCALATION_EXISTS = [];
    }
    var isShowPopup = false;
    if(meta){
        /* InterfaceUtil.java
        public interface ISSUE_TYPE {
            short SUPPORT = 1;
            short REQUEST = 2;
            short COMMENT = 3;
            short CUSTOMER = 4;
            short EMAIL = 5;
            short SIGNATURE = 6;
        }*/
        switch(meta.step){
            case 1: 
                if( $.inArray(1, ISSUE_ESCALATION_EXISTS) === -1 ){ // Kiem tra xem da co du lieu duoc insert truoc do chua
                    if(showPopup) $('#cmdSendSupportMail').trigger('click');
                    if($('#cmdSendSupportMail').length) isShowPopup = true;
                    console.log('Show popup with date type 1');
                }else{
                    console.log('Date type 1 exists');
                }
                break;
            case 2:
                if( $.inArray(2, ISSUE_ESCALATION_EXISTS) === -1 ){ // Kiem tra xem da co du lieu duoc insert truoc do chua
                    if(showPopup) $('#cmdSupport').trigger('click');
                    if($('#cmdSupport').length) isShowPopup = true;
                    console.log('Show popup with date type 2');
                }else{
                    console.log('Date type 2 exists');
                }
                break;
            case 3:
                if( $.inArray(3, ISSUE_ESCALATION_EXISTS) === -1 ){ // Kiem tra xem da co du lieu duoc insert truoc do chua
                    if(showPopup) $('#cmdSendComment').trigger('click');
                    if($('#cmdSendComment').length) isShowPopup = true;
                    console.log('Show popup with date type 3');
                }else{
                    console.log('Date type 3 exists');
                }
                break;
            case 4:
                if( $.inArray(4, ISSUE_ESCALATION_EXISTS) === -1 ){ // Kiem tra xem da co du lieu duoc insert truoc do chua
                    if(showPopup) $('#cmdCustomerSupportMail').trigger('click');
                    if($('#cmdCustomerSupportMail').length) isShowPopup = true;
                    console.log('Show popup with date type 4');
                }else{
                    console.log('Date type 4 exists');
                }
                break;
            case 5:
            case 6:
                break;
        }
    }else{
        console.log('Issue status have no step with ' + status);
    }
    return isShowPopup;
}

/**
 * Sau khi close dialog, thi function nay se duoc call, thuc hien check xem du lieu da duoc valid chua de summit form issue
 * @returns {undefined}
 */
function callbackIssueCloseDialog(requireMsg = ''){
    if( !allowExecuteCallbackDialog ){
        allowExecuteCallbackDialog = true;
        return;
    }
    var newStatus = $('#issue_status_id_input').val();
    var meta = newStatus in ISSUE_STATUS_FLOW ? ISSUE_STATUS_FLOW[newStatus] : {step: 0, data_required: false, end_time_required: false};
    if(meta.step > 0){
        if(meta.data_required){
            var data = checkIssueStatusFlow(newStatus, false); // kiem tra va khong show popup
            if(data === false && $currentSummitButton){ // neu du lieu form valid va khong hien thi popup thi summit form
                console.log('Summit issue 2');
                $currentSummitButton.trigger('click');
            }else{
                console.log('Show alert ', requireMsg);
                // show alert
                if(requireMsg) showAlert(requireMsg);
            }
        }else{
            console.log('Summit issue 3');
            $currentSummitButton.trigger('click');
        }
    }
}

function isValidEmailAddress(emailAddress) {
    var pattern = /^([a-z\d!#$%&'*+\-\/=?^_`{|}~\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]+(\.[a-z\d!#$%&'*+\-\/=?^_`{|}~\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]+)*|"((([ \t]*\r\n)?[ \t]+)?([\x01-\x08\x0b\x0c\x0e-\x1f\x7f\x21\x23-\x5b\x5d-\x7e\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|\\[\x01-\x09\x0b\x0c\x0d-\x7f\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))*(([ \t]*\r\n)?[ \t]+)?")@(([a-z\d\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|[a-z\d\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF][a-z\d\-._~\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]*[a-z\d\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])\.)+([a-z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|[a-z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF][a-z\d\-._~\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]*[a-z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])\.?$/i;
    return pattern.test(emailAddress);
};

$(document).ready(function(){
    var $autoBlurElms = $('#tabViewCustomer .auto_blur');
    if($autoBlurElms.length){
        $autoBlurElms.each(function(){
            if($(this).val()){
                $(this).trigger('blur');
            }
        });
    }
});