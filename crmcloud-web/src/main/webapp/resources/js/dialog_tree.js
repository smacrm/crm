function reBuildTree(className) {
    if(className === undefined || className === '') return;
//    $('.'+ className +' a').click(function (e) {
//        e.preventDefault();
//        e.stopPropagation();
//        $(this).parent().find('.selected').removeClass('selected');
//        $(this).addClass('selected');
//    });

    $('.'+ className +' a').each(function(){
        var lv = $(this).attr('treeLevel');
        if(lv === undefined || lv === '' || lv === 'null' || lv === 'NULL') {
        }else {
            var idx = stringCounter(lv, '-');
            if(idx > 0) {
                $(this).css('margin-left', idx/2 + 'em');
            }
        }
    }); 
}

function stringCounter(str1,str2){
    var strlength = str2.length;
    var ans = 0;
    var i = 0;
    while((i=str1.indexOf(str2,i)) !== -1){
        i += strlength;
        ans++;
    }
    return ans;
}