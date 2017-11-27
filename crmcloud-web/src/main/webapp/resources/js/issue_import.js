$(document).ready(function () {
    var dropped = false;
    var draggable_sibling;
    var mapping_result = {};
    
    var dropItem = function(source, target){
        if(target.hasClass('disabled')) return;
        dropped = true;
        source.hide();
        target.addClass('list-group-item-success');
        if (target.find("i").length < 1) {
            target.append('<i class="fa fa-times-circle pull-right" aria-hidden="true"></i>');
        }
        sourceText = source.find('label').text();
        target.find('span').html('<i class="fa fa-long-arrow-left" aria-hidden="true"></i>').append(sourceText);
        target.data('connect', source.attr('key'));
        target.addClass('disabled');
        mapping_result[source.attr('key')] = target.attr('key');
        //console.log(mapping_result);
        $(".mapping-fields").val(window.JSON.stringify(mapping_result));
    };
    
    var revertItem = function(source){
        source.removeClass('list-group-item-success disabled');
        source.find('span').text('');
        
        var key = source.data('connect');
        delete mapping_result[key];
        
        $("#sortable li[key='"+key+"']").show();
        $(".mapping-fields").val(window.JSON.stringify(mapping_result));
    };

    $("#sortable").sortable({
        start: function (event, ui) {
            draggable_sibling = $(ui.item).prev();
        },
        stop: function (event, ui) {
            if (dropped) {
                if (draggable_sibling.length === 0)
                    $('#sortable').prepend(ui.item);
                draggable_sibling.after(ui.item);
                dropped = false;
            }
        }
    });

    $(".droppable").droppable({
        activeClass: 'list-group-item-warning',
        hoverClass: 'hovered',
        drop: function (event, ui) {
            var source = $(ui.draggable);
            var target = $(event.target);
            dropItem(source, target);
        }
    });
    $('#drop-panel').on('click', 'i.fa-times-circle', function(){
        var source = $(this).parent();
        revertItem(source);
        $(this).remove();
    });
    
    $('#matching-fields').click(function(e){
        $('#drag-panel .list-group-item:visible').each(function(){
            $(this).find('.matching-field').trigger('click');
        });
    });
    
    $('.matching-field').click(function(e){
        e.preventDefault();
        var source = $(this).parent();
        var sourceText = source.find('label').text();
        $('#drop-panel .list-group-item').each(function(){
            var target = $(this);
            if(target.find('label').text() === sourceText){
                dropItem(source, target);
                return false;
            }
        });
    });
    
    $('#revert-fields').click(function(e){
        $('#drop-panel i.fa-times-circle').each(function(){
            $(this).trigger('click');
        });
    });
});
