/**
 * オーバーレイのLINKを選択するとき、色が変更するように処理
 * @param {type} divId
 * @returns {undefined}
 */
function aSlected(divId) {
    var obj = $('#' + $.trim(divId)).get(0);
    $(obj).find('a').each(function(){
        $(this).click(function(){
            var className = $(this).attr('class');
            if(className.indexOf('aSelected') > -1) {
                $(this).removeClass('aSelected');
            } else {
                $(this).addClass('aSelected');
            }
            var label = $(this).children().first().get(0);
            var div = $(this).parent().parent().parent().get(0);
            if(!isEmpty(label)) {
                if(!isEmpty(div)) {
                    $(div).find('a').each(function(){
                        var chkLabel = $(this).children().first().get(0);
                        if($.trim($(label).html()) !== $.trim($(chkLabel).html())) {
                            $(this).removeClass('aSelected');
                        }
                    });
                }
            }
        });
    });
}

function reloadASelect(divId, hiddenId) {
    if(isBlank(divId) || isBlank(hiddenId)) return;
    var hidden = $('#'.concat(hiddenId)).get(0);
    if(isEmpty(hidden) || isEmpty($(hidden).val())) return;
    var values = $(hidden).val().split('_');
    if(!$.isArray(values)) return;
    for(var i=1; i<=values.length; i++) {
        var div = $('#'.concat(divId).concat('_').concat(i)).get(0);
        if(isEmpty(div)) continue;
        $(div).find('a').each(function(){
            var className = $(this).attr('class');
            var label = $(this).children().first().get(0);
            if($.trim(values[i-1]) === $.trim($(label).html())) {
                $(this).addClass('aSelected');
            } else {
                if(className.indexOf('aSelected') > -1) $(this).removeClass('aSelected');
            }
        });
    }
}

$(function(){
    for(var i=1; i<=4; i++) {
        aSlected('dataListIssueProductList_'.concat(i));
    }
//    aSlected('dataListIssueProductList_2');
//    aSlected('dataListIssueProductList_3');
//    aSlected('dataListIssueProductList_4');
});

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