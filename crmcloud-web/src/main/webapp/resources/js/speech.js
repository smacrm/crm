/* global artyom */

$('a[href="#speech-record"]').click(function (e) {
    e.preventDefault();

    var input = $($(this).attr('target'));
    var locale = $(this).attr('locale');
    locale = locale || 'ja-JP';

    if (input.length < 0) {
        console.error('Speach record have no target');
        return;
    }

    var oldVal = input.val();

    SENTENCE_LIST = [];
    BREAK_LINE = '。\r\n';

    $(this).toggleClass('active');
    if ($(this).hasClass('active')) {
        $(this).removeClass('btn-primary').addClass('btn-warning');
        artyom.initialize({
            lang: locale, // A lot of languages are supported. Read the docs !
            continuous: true, // Artyom will listen forever
            listen: true, // Start recognizing
            debug: false, // Show everything in the console
            speed: 1, // talk normally
            // mode: 'quick',
            soundex: true,
        }).then(function () {
            console.log("Ready to work !");

            artyom.addCommands({
                indexes: ["*"],
                action: function () {
                    // Do No Thing
                }
            });
            artyom.redirectRecognizedTextOutput((recognized, isFinal) => {
                // console.log('[' + isFinal + '] ' + recognized);
                if (isFinal) {
                    // Nothing
                    SENTENCE_LIST.push(recognized);
                    input.val(oldVal + '\r\n' + SENTENCE_LIST.join(BREAK_LINE));
                } else {
                    input.val(oldVal + '\r\n' + (SENTENCE_LIST.length > 0 ? (SENTENCE_LIST.join(BREAK_LINE) + BREAK_LINE) : '') + recognized);
                }
                input.scrollTop(input[0].scrollHeight);
            });

        }).catch(function () {
            console.error("An error occurred during the initialization");
        });

    } else {
        $(this).removeClass('btn-warning').addClass('btn-primary');
        try {
            artyom.fatality();
        } catch (e) {
            console.error(e);
        }
    }
});

$('a[href="#speech-summary"]').click(function (e) {
    e.preventDefault();
    // var UPLOAD_URL = 'https://bizwrapup.gnext.asia/recognizing';
    //var UPLOAD_URL = 'https://d4f69dd3.ngrok.io/recognizing';
    var UPLOAD_URL = 'http://localhost:8000/api/recognizing';

    var $source = $($(this).attr('source'));
    var $target = $($(this).attr('target'));

    var fd = new FormData();
    var text = '';

    if ($source.is('input') || $source.is('textarea'))
        text = $source.val();
    else
        text = $source.text();

    fd.append('input', text);

    $.ajax({
        xhr: function () {
            var xhrobj = $.ajaxSettings.xhr();
            if (xhrobj.upload) {
                xhrobj.upload.addEventListener("progress", function (event) {
                    var percent = 0;
                    var position = event.loaded || event.position;
                    var total = event.total;
                    if (event.lengthComputable) {
                        percent = Math.ceil(position / total * 100);
                    }

                    // Set the progress bar.
                    console.log(percent);
                    if (percent >= 100) {
                        //finish upload content

                    }
                }, false);
            }
            return xhrobj;
        },
        url: UPLOAD_URL,
        method: "POST",
        contentType: false,
        processData: false,
        crossDomain: true,
        cache: false,
        data: fd,
        beforeSend: function (req) {
            req.setRequestHeader('Authorization', 'Basic ' + btoa('gnext:geneco11m__'));
        },
        success: function (data) {
            if (data.status !== true) {
                console.error(data.translate);
            }
            //$source.val(data.origin);
            $target.text(data.translate);
        }
    }).always(function () {

    });
});