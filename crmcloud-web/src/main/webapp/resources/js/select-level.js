/* global PF */

/**
 * 「TieredMenu」メニューをクリックするとき、処理
 * 1、一階層場合、そのまま選択したオブジェクトのテキストを取得
 * 2、二階層以上場合、一番外側親オブジェクトに戻って、子オブジェクトのツリーテキストを取得
 * @param {type} obj
 * @param {type} hiddenId
 * @param {type} widgetVar
 * @param {type} oneLevel
 * @returns {undefined}
 */
function getValTieredMenu(obj, hiddenId, widgetVar, oneLevel) {
    var div = $("div.tieredMenu_" + hiddenId).get(0);
    if(isEmpty(div) || isEmpty(obj) || isEmpty(hiddenId)) return;
    var putObj = $("#" + hiddenId).get(0);
    var objClass = $(obj).attr('class');
    if(isEmpty(putObj) || isEmpty(objClass)) return;
    var name = objClass.toString();
    if(name.indexOf(hiddenId) === -1) {
        name = $(obj).parent().attr('class');
    }
    if(isEmpty(name)) return;
    var val = name.toString().split(hiddenId + '_');
    if(!$.isArray(val) || isEmpty(val[1])) return;
    val = val[1].split(' ');
    if(isEmpty(val) || isEmpty(val[0])) return;
    var arrayClass = val[0].split('_');
    var labelObj = "";
    var valObj = "";
    if(arrayClass.length > 1) {
        labelObj = getLabelFromDiv(div, hiddenId, arrayClass, oneLevel);
    } else {
        if(arrayClass.length === 1 && oneLevel) return;
        labelObj = getLabelFromSpan($(obj));
    }
    if(oneLevel) {
        var vals = val[0].split("_");
        valObj = vals[vals.length-1];
    } else {
        valObj = val[0];
    }
    console.log('>> ' + hiddenId);
    $("#" + hiddenId).val(valObj);
    var hiddenName = hiddenId.replace('_id','_name');
    $("#" + hiddenName).val(labelObj);
    $("#label_" + hiddenId).text(labelObj);
    var minus = $("#button-minus_" + hiddenId).get(0);
    if(!isEmpty(minus)) {
        $(minus).show();
    }
    if(!isEmpty(widgetVar) && !oneLevel) {
        PF(widgetVar).hide();
    }
}

function resetLabelValue(hiddenId) {
    if(isEmpty(hiddenId)) return;
    var label = $("#label_" + hiddenId).get(0);
    if(!isEmpty(label)) $(label).html('');
    var minus = $("#button-minus_" + hiddenId).get(0);
    if(!isEmpty(minus)) $(minus).hide('');
    var input = $("#" + hiddenId).get(0);
    if(!isEmpty(input)) $(input).val('');
}

function loadButtonMinus(hiddenIds) {
    if(!$.isArray(hiddenIds)) return;
    var obj;
    var viewObj;
    for(var i in hiddenIds) {
        obj = $("#"+ hiddenIds[i]).get(0);
        if(isEmpty(obj) || isBlank($(obj).val())) continue;
        viewObj = $("#button-minus_"+ hiddenIds[i]).get(0);
        if(!isEmpty(viewObj)) $(viewObj).show();// && !isBlank($(viewObj).val())
    }
}

/**
 * Divの中に選択したの子オブジェクト「li、a」ツリーテキストを取得
 * @param {type} obj
 * @param {type} hiddenId
 * @param {type} arrayId
 * @param {type} onelevel
 * @returns {undefined|String}
 */
function getLabelFromDiv(obj, hiddenId, arrayId, onelevel) {
    if(isEmpty(obj) || isEmpty(arrayId)) return;
    var val = '';
    $(obj).find('li,a').each(function() {
        var objClass = $(this).attr('class');
        objClass = objClass.toString().replace(' ui-menuitem-active', '').replace(' ui-state-hover', '');
        if(!isEmpty(objClass)) {
            var arrayClass = objClass.split(' ');
            var nextId = hiddenId;
            for(var i in arrayId) {
                nextId += '_' + arrayId[i];
                if(nextId === arrayClass[arrayClass.length-1]) {
                    if(onelevel) {
                        val = getLabelFromSpan($(this).get(0));
                        break;
                    } else {
                        if(isBlank(val)) {
                            val += getLabelFromSpan($(this).get(0));
                        } else {
                            val += ' > ' + getLabelFromSpan($(this).get(0));
                        }
                    }
                }
            }
        }
    });
    return val;
}

/**
 * 選択したオブジェクトの中に「span」のテキストを取得
 * @param {type} obj
 * @returns {String|jQuery|undefined}
 */
function getLabelFromSpan(obj) {
    if(isEmpty(obj)) return;
    var val = '';
    $(obj).find('span').each(function(idx) {
        if(idx === 0) {
            val = $(this).text();
        }
    });
    return val;
}

function activeCurentTap(tabId, searchTabId) {
    $('#' + searchTabId).find('a').each(function(){
        var herf = $(this).attr('href');
        if(tabId === herf) {
            $(this).click();
        }
    });
    $('#' + searchTabId +' a').click(function(){
        var herf = $(this).attr('href');
        if(isEmpty(herf)) return;
        if(herf.indexOf('#tabViewCustomer') > -1 && herf.indexOf(':') > 0) {
            $('#issueCustActiveTab').val(herf);
        }
        if(herf.indexOf('#tabViewIssue') > -1 && herf.indexOf(':') > 0) {
            $('#issueActiveTab').val(herf);
        }
    });
}

function loadIssueJS() {
    activeCurentTap($.trim($('#issueActiveTab').val()), 'tabViewIssue');
    activeCurentTap($.trim($('#issueCustActiveTab').val()), 'tabViewCustomer');

    var inputs = ['issue_receive_person_id','issue_proposal_id'];//,'issue_product_id'
    loadButtonMinus(inputs);
    $('.ui-menu-list a').click(function () {
        /** 受付者をクリックするとき */
        getValTieredMenu($(this), 'issue_creator_id', 'widgetIssueCreatorIdList', true);
        /** 作成者をクリックするとき */
        getValTieredMenu($(this), 'issue_receive_person_id', 'widgetIssueReceivePersonList', true);
        /** 申出分類をクリックするとき */
        getValTieredMenu($(this), 'issue_proposal_id', 'widgetIssueProposalList', false);
    });
}

function loadIssueShowJS() {
    var show_inputs = ['issue_comment_person_id', 'issue_customer_support_person_id'];
    loadButtonMinus(show_inputs);
    $('.ui-menu-list a').click(function () {
        /** コメント入力者をクリックするとき */
        getValTieredMenu($(this), 'issue_comment_person_id', 'widgetIssueCommentPersonList', true);
        /** 顧客対応者をクリックするとき */
        getValTieredMenu($(this), 'issue_customer_support_person_id', 'widgetIssueCustomerSupportPersonList', true);
    });
}

/**
 * 
 * @param {type} init
 * @param {type} objClass
 * @returns {undefined}
 */
function callInputKeyUp(init, objClass) {
    if(isBlank(objClass)) return;
    var dataList = $('.tabViewCustomerLoadCallButton').get(0);
    if(!isEmpty(dataList)) {
        $(dataList).find('input[type="text"]').each(function(){
            var thisClass = $(this).attr('class');
            if(!isBlank(thisClass)) {
                if(thisClass.indexOf(objClass) > 0) {
                    var btnClassArray = thisClass.split(' ');
                    if($.isArray(btnClassArray)) {
                        var btnClass;
                        for(var i in btnClassArray) {
                            if(btnClassArray[i].indexOf(objClass) === -1) continue;
                            btnClass = $.trim(btnClassArray[i]);
                        }
                        if(!isBlank(btnClass)) {
                            if(init) {
                                $(this).keyup(function(){
                                    var val = $(this).val();
                                    if(val !== undefined) {
                                        val = val.replace('-', '');
                                        var button = $('.' + btnClass.replace('Input','Button')).get(0);
                                        if(val.length > 9) {
                                            $(button).attr('style', 'display: block !important;float: right;');
                                        } else {
                                            $(button).attr('style', 'display: none !important');
                                        }
                                    } else {
                                        $(button).attr('style', 'display: none !important');
                                    }
                                });
                            } else {
                                var val = $(this).val();
                                if(val !== undefined) {
                                    val = val.replace('-', '');
                                    var button = $('.' + btnClass.replace('Input','Button')).get(0);
                                    if(val.length > 9) {
                                        $(button).attr('style', 'display: block !important;float: right;');
                                    } else {
                                        $(button).attr('style', 'display: none !important');
                                    }
                                } else {
                                    $(button).attr('style', 'display: none !important');
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}

//$(function(){
//    loadIssueJS();
//    callInputKeyUp(false, 'callInputGridTel_');
//    callInputKeyUp(true, 'callInputGridTel_');
//    callInputKeyUp(false, 'callInputGridMobile_');
//    callInputKeyUp(true, 'callInputGridMobile_');
//});

/**
 * オブジェクトNULLをチェック
 * @param {type} obj
 * @returns {Boolean}
 */
function isEmpty(obj) {
    if(obj === undefined || obj === null) return true;
    return false;
}

/**
 * オブジェクト空白をチェック
 * @param {type} obj
 * @returns {Boolean}
 */
function isBlank(obj) {
    if(obj === undefined || obj === null || obj.length === 0) return true;
    return false;
}