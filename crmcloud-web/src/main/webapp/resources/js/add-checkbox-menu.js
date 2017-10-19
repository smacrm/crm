/** TieredMenuオプジェットから値を取得、フォームパラメタに値を追加
 * menuClass：TieredMenuクラス名
 * formId：フォームID
 * rexg：menuClassに消す文字
 * @returns {undefined}
 */
function addCheckBoxToMenu(menuClass, formId, rexg) {
    var obj = $('.' + menuClass).get(0);
    if(obj === undefined || obj === null) return;
    var key = $.trim(menuClass.replace('tieredMenu_', ''));
    var inputHidden = $('<input type="hidden" name="' + key + '" value="" />');
    $('#' + formId).append(inputHidden);
    var strCheckBox = '<div class="ui-chkbox ui-widget">'
                        + '<div class="ui-helper-hidden-accessible">'
//                        +   '<input type="checkbox" >'
                        + '</div>'
                        + '<div class="ui-chkbox-box ui-widget ui-corner-all ui-state-default">'
                        +   '<span class="ui-chkbox-icon ui-icon ui-icon-blank ui-c"></span>'
                        + '</div>'
                        + '</div>';

    var pCheckbox = $(strCheckBox);
//    $('.selectBooleanCheckbox_todo').clone();
//    pCheckbox.removeAttr('id');
//    pCheckbox.removeClass('selectBooleanCheckbox_todo');
//    $(pCheckbox).find('input').each(function(){
//        $(this).remove();
//    });
    var divCheckbox = $(pCheckbox).children().first().get(0);
    $(divCheckbox).append($('<input type="checkbox" value="" />'));//name="' + key + '" 
//    $('.selectBooleanCheckbox_todo').hide();

    var divBox = $('#' + menuClass.replace('tieredMenu', 'div')).get(0);
    $(obj).find('a').each(function(){
        var child = $(this).get(0);
        var parent = $(child).parent();
        var divCheckbox = $(pCheckbox).clone();
        $(parent).css({'white-space':'nowrap', 'display':'inline-flex'});
        //console.log(parent);
        $(parent).prepend(divCheckbox);
//        console.log(parent);
//        console.log(divCheckbox);

        $(divCheckbox).click(function(){
            var childClassArray = $.trim($(child).attr('class').replace('ui-state-hover', '')).split(' ');
            var val = getValueInArrayClassByKey(childClassArray, key, rexg);
            if(val === null || val.length <= 0) {
                var parentClassArray = $.trim($(parent).attr('class').replace('ui-menuitem-active', '')).split(' ');
                val = getValueInArrayClassByKey(parentClassArray, key, rexg);
            }
            
            var div = $(this).children().first();
            var divSpan = $(this).children().get(1);
            var span = $(divSpan).children().first();
            var checkbox = $(div).children().first().get(0);
            var checked = $(checkbox).attr("checked");
            if(!checked) {
                $(checkbox).val(val);
                $(divSpan).addClass('ui-state-active');
                $(span).addClass('ui-icon-check');
                $(span).removeClass('ui-icon-blank');
                if(val.indexOf('_') === -1) {
                    var ul = $(this).next().next().get(0);
                    if(ul !== null) {
                        checkRemoveAllChildrens(ul, key, inputHidden, true, rexg, divBox);
                    }
                } else {
                    var ul = $(this).parent().parent().get(0);
                    var add = allCheck(ul, true);
                    var parenVal = val.split('_');
                    checkRemoveParent(child, add, $.trim(parenVal[0]));
                    var spanText = $(divSpan).parent().next().children().first().clone();

                    var addText = addAndRemoveValue(inputHidden, val, true);
                    if(addText) {
                        addDivSpanText(divBox, add, val, spanText);
                    }
                }
            } else {
                $(checkbox).val('');
                $(divSpan).removeClass('ui-state-active');
                $(span).addClass('ui-icon-blank');
                $(span).removeClass('ui-icon-check');
                if(val.indexOf('_') === -1) {
                    var ul = $(this).next().next().get(0);
                    if(ul !== null) {
                        checkRemoveAllChildrens(ul, key, inputHidden, false, rexg, divBox);
                    }
                } else {
                    var ul = $(this).parent().parent().get(0);
                    var add = allCheck(ul, false);
                    var parenVal = val.split('_');
                    checkRemoveParent(child, add, $.trim(parenVal[0]));

                    var addText = addAndRemoveValue(inputHidden, val, false);
                    if(addText) {
                        addDivSpanText(divBox, add, val, null);
                    }
                }
            }

            $(checkbox).attr("checked", !checked);
        });

        $(child).click(function(){
            $(divCheckbox).click();
        });
    });

    $(obj).find(".ui-menuitem").on("mousedown mouseup click", function(e){
        var self = $(e.target);
        if(e.type === 'click' && self.hasClass('ui-menuitem-text')){
            self.closest('.ui-menuitem').find('.selectBooleanCheckbox_todo:first').trigger('click');
        }
        e.stopPropagation();
    });
}

/**
 * Class名から値を取得するs
 * @param {type} arrayClass
 * @param {type} key
 * @param {type} rexg
 * @returns {String}
 */
function getValueInArrayClassByKey(arrayClass, key, rexg) {
    if(!$.isArray(arrayClass) || key === undefined || key === null) return '';
    if(rexg !== null) {
        key = key.replace(rexg, '');
    }
    if(arrayClass.length === 1) {
        if(arrayClass[0].indexOf(key) > -1) {
            return arrayClass[0].replace(key, '');
        }
    } else {
        for(var i in arrayClass) {
            if(arrayClass[i].indexOf(key) === -1) continue;
            return arrayClass[i].replace(key + '_', '');
        }
    }
     return '';
}

/**
 * 値が追加または外す処理
 * @param {type} obj
 * @param {type} val
 * @param {type} add
 * @returns {undefined}
 */
function addAndRemoveValue(obj, val, add) {
    if(obj === undefined || obj === null
            || val === undefined || val === null) return false;
    var thisVal = $.trim($(obj).val());
//    console.log(thisVal);
    if(val.indexOf('_') > -1) {
        val = val.split('_')[1];
        val = 'u:'.concat(val);
    } else {
        val = 'g:'.concat(val);        
    }
    var newVal = $.trim(','.concat(val));
    if(add) {
        if(thisVal.indexOf(val) > -1) return false;
        $(obj).val(thisVal + newVal);
    } else {
        $(obj).val(thisVal.replace(newVal, ''));
    }
    return true;
}

/**
 * 子チェックボックスリストをチェックまたは外す処理
 * @param {type} obj
 * @param {type} key
 * @param {type} inputHidden
 * @param {type} add
 * @param {type} rexg
 * @param {type} divBox
 * @returns {undefined}
 */
function checkRemoveAllChildrens(obj, key, inputHidden, add, rexg, divBox) {
    $(obj).find('a').each(function(){
        var childClassArray = $(this).attr('class').split(' ');
        var val = getValueInArrayClassByKey(childClassArray, key, rexg);
        var div = $(this).prev().get(0);
        var divSpan = $(div).children().get(1);
        var span = $(divSpan).children().first();
        var checkbox = $(div).children().children().get(0);
        var spanText = $(divSpan).parent().next().children().first().clone();

        if(add) {
            $(checkbox).val(val);
            $(checkbox).attr("checked", add);
            $(divSpan).addClass('ui-state-active');
            $(span).addClass('ui-icon-check');
            $(span).removeClass('ui-icon-blank');

            var addText = addAndRemoveValue(inputHidden, val, add);
            if(addText) {
                addDivSpanText(divBox, add, val, spanText);
            }
        } else {
            $(checkbox).val('');
            $(checkbox).attr("checked", add);
            $(divSpan).removeClass('ui-state-active');
            $(span).addClass('ui-icon-blank');
            $(span).removeClass('ui-icon-check');

//            addAndRemoveValue(inputHidden, val, add);
            var addText = addAndRemoveValue(inputHidden, val, add);
            if(addText) {
                addDivSpanText(divBox, add, val, null);
            }
        }
    });
}

function addDivSpanText(divBox, add, val, spanText) {
    if(add) {
        if(spanText !== undefined) {
            var addDiv;
            $(divBox).find('div').each(function(){
                if($.trim($(this).attr('id')) === $.trim('divSpan_'.concat(val))) {
                    addDiv = true;
                }
            });
            if(!addDiv) {
                var divSpan = document.createElement('div');
                divSpan.setAttribute('id', $.trim('divSpan_'.concat(val)));
//                var spanBox = document.createElement('span');
//                $(spanBox).html(spanText);
                $(spanText).appendTo(divSpan);
                $(divSpan).appendTo(divBox);
            }
        }
    } else {
        $(divBox).find('div').each(function(){
            if($.trim($(this).attr('id')) === $.trim('divSpan_'.concat(val))) {
                $(this).remove();
            }
        });
    }
}

/**
 * 親チェックボックスをチェックまたは外す処理
 * @param {type} obj
 * @param {type} add
 * @param {type} val
 * @returns {undefined}
 */
function checkRemoveParent(obj, add, val) {
    var div = $(obj).parent().parent().prev().prev().get(0);
    var divSpan = $(div).children().get(1);
    var span = $(divSpan).children().first();
    var checkbox = $(div).children().children().get(0);

    if(add) {
//        addAndRemoveValue(inputHidden, val, add);
        $(checkbox).val(val);
        $(divSpan).addClass('ui-state-active');
        $(span).addClass('ui-icon-check');
        $(span).removeClass('ui-icon-blank');
    } else {
//        addAndRemoveValue(inputHidden, val, add);
        $(checkbox).val('');
        $(divSpan).removeClass('ui-state-active');
        $(span).addClass('ui-icon-blank');
        $(span).removeClass('ui-icon-check');
    }
}

/**
 * 全てチェックボックスをチェックする処理
 * @param {type} obj
 * @param {type} add
 * @returns {Boolean}
 */
function allCheck(obj, add) {
    if(!add) return add;
    $(obj).find('a').each(function(){
        var div = $(this).prev().get(0);
        var checkbox = $(div).children().children().get(0);
        if($(checkbox).val() === undefined || $(checkbox).val() === '') {
            add = false;
        }
    });
    return add;
}

function dialogOncloseResetValFromParas(objNames) {
    if(!$.isArray(objNames)) return;
    for(var i in objNames) {
        var name = $.trim(objNames[i]);
        var hiddenObj = $("input[name~='" + name + "']");
        if(hiddenObj === undefined || hiddenObj === null) continue;
        $(hiddenObj).val('');
        var div = $('#div_' + name);
        if(div !== undefined) {
            $(div).html('');
        }
        var tieredMenu = $('.tieredMenu_' + name).get(0);
        if(tieredMenu !== undefined) {
            $(tieredMenu).find('input[type~=checkbox], div, span').each(function(){
                var obj = $(this).get(0);
                if(obj.tagName === 'INPUT') {
                    $(obj).attr("checked", false);
                }
                var objClass = $(this).attr('class');
                if(objClass !== undefined) {
                    if(objClass.indexOf('ui-state-active') > -1) {
                        $(this).removeClass('ui-state-active');
                    }
                    if(objClass.indexOf('ui-icon-check') > -1) {
                        $(this).removeClass('ui-icon-check');
                        $(this).addClass('ui-icon-blank');
                    }
                }
            });
        }
    }
}

/**
 * 選択しているテキスト取得、「TextArea」に追加
 * @param {type} errorMsg
 * @returns {undefined}
 */
function btGetSelection(errorMsg) {
    if(window.getSelection){
        sel = window.getSelection().toString();
    }else if(document.getSelection){
        sel = document.getSelection().toString();
    }else if(document.selection){
        sel = document.selection.createRange().text;
    }
    if(sel === undefined || sel === null || sel.length <= 0) {
        alert(errorMsg.toString());
    } else {
        sel = "\n>" + sel;
        sendSelection([{name: 'selectedText', value: sel}]);
    }
}

/**
 * 現在選択TEXTを取得、Actionへ送信
 * @returns {undefined}
 */
function onSelectInputTextarea() {
    var selectedText =
            (!!document.getSelection) ? document.getSelection()
            : (!!window.getSelection) ? window.getSelection()
            : document.selection.createRange().text;
    sendSelection([{name: 'selectedText', value: selectedText}]);
}
/**
 * Dailogを閉じる
 * @param {type} xhr
 * @param {type} status
 * @param {type} args
 * @param {type} dialog
 * @returns {undefined}
 */
function handleCloseDialog(xhr, status, args, dialog) {
    if(args.close){
        PF(dialog).hide();
     }
}

/**
 * フレームレイアウトをクリックするとき、アップロードWindowsDailog
 * @param {type} divId
 * @returns {undefined}
 */
function loadDivFileUploadClick(divId) {
    $('#' + $.trim(divId)).find('div.ui-fileupload-content').each(function(e){
        $(this).click(function(e){
            $('#' + $.trim(divId) + '_input').click();
        });
    });
}

//function loadIssueShow() {
//    addCheckBoxToMenu('tieredMenu_to_d_o_users', 'form_issue_show', 'to_');
//    addCheckBoxToMenu('tieredMenu_cc_d_o_users', 'form_issue_show', 'cc_');
//    addCheckBoxToMenu('tieredMenu_bcc_d_o_users', 'form_issue_show', 'bcc_');
//    addCheckBoxToMenu('tieredMenu_tto_d_o_users', 'form_issue_show', 'tto_');
//    addCheckBoxToMenu('tieredMenu_ccc_d_o_users', 'form_issue_show', 'ccc_');
//    addCheckBoxToMenu('tieredMenu_bbcc_d_o_users', 'form_issue_show', 'bbcc_');
//}