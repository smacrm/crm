/* global ion, BASE_URL, Twilio, moment, deviceMonitor */

/**
 * Twilio call center processing
 * 
 * @author hungpd
 * @since 2017/02
 * @type type
 */
var CRMCloudTwilio = {
    companyId: null,
    memberId: null,
    token: false,
    agent: null,
    currentConnection: null,
    callStartTime: null,
    
    _debug: true,
    _timeCounter: null,
    _privateIncomming: false,
    _ringing: null,
    _audio: null,
    _callPhoneNumber: null,
    
    _calling: false,
    _allowAnswer: false,
    _autoAnswer: false,
    _allowCloseWindow: false,
    
    setCallSid: function(_callSid){
        $.cookie('twilio_callsid', _callSid);
    },
    getCallSid: function(){
        return $.cookie('twilio_callsid');
    },
    setAllowIncomming: function(_allowIncomming){
        $.cookie('twilio_allow_incomming', _allowIncomming);
    },
    isAllowIncomming: function(){
        return $.cookie('twilio_allow_incomming') === 'true';
    },
    isSetAllowIncomming: function(){
        return $.cookie('twilio_allow_incomming') !== undefined;
    },
    isIncomming: function(){
        return $.cookie('twilio_is_incomming') === 'true';
    },
    setIncomming: function(_isIncomming){
        $.cookie('twilio_is_incomming', _isIncomming);
    },
    isSetIncomming: function(){
        return $.cookie('twilio_is_incomming') !== undefined;
    },
    isCalling: function(){
        return $.cookie('twilio_is_calling') === 'true';
    },
    setCalling: function(_isCalling){
        $.cookie('twilio_is_calling', _isCalling);
    },
    isSetCalling: function (){
        return $.cookie('twilio_is_calling') !== undefined;
    },
    getCallingName: function(){
        return $.cookie('twilio_calling_name');
    },
    getCallingPhone: function(){
        return $.cookie('twilio_calling_phone');
    },
    status: function(){
        console.info('AllowIncomming: ' + this.isAllowIncomming());
        console.info('Incomming: ' + this.isIncomming());
        console.info('Calling: ' + this.isCalling());
        console.info('AutoAnswer: ' + this._autoAnswer);
    },
    reset: function(){
        this.setAllowIncomming(true);
        this.setIncomming(false);
        this.setCalling(false);
        this._autoAnswer = false;
        console.info('reset');
    },
    checkIncommingCall: function(){
        var instance = this;
        $.getJSON(BASE_URL + "/rest/twilio/check/" + this.agent, function(data){
            if(data && data.size > 0){
                var hasPrivate = data.hasPrivate;
                var name = data.name;
                var phoneNumber = data.phoneNumber;
                var callSid = data.callsid;
                instance.setCallSid(callSid);
                instance._autoAnswer = true;
                if(hasPrivate){
                    if(instance._debug) console.log("Private incomming call");
                    instance.notifyPrivateIncommingCall(name, phoneNumber, callSid);
                }else{
                    if(instance._debug) console.log("Public incomming call");
                    instance.notifyIncommingCall(name, phoneNumber);
                }
            }
        });
    },
    start: function(opts){
        this.companyId = opts.companyId;
        this.memberId = opts.memberId;
        this.token = opts.token;
        this._debug = opts.debug;
        this._ringing = opts.ringing;
        this.changeStatusStart(false);
        if(this.token){
            Twilio.Device.setup(this.token, {debug: this._debug, closeProtection: true});
        }
        if(this._ringing){
            this._audio = new Audio(this._ringing);
            this._audio.loop = true;
            this._audio.preload = true;
            this._audio.stop = function(){
                this.pause();
                    this.currentTime = 0;
            };
        }
        this.listen();
    },
    listen: function(){
        var instance = this;
        Twilio.Device.ready(function (device) {
            instance.agent = device._clientName;
            instance.currentConnection = null;
            if(instance._debug){
                console.log("Ready: " + instance.agent);
                console.log("Status: " + Twilio.Device.status());
            }
            if(!instance.isCalling() && instance.isIncomming()){
                console.log('Ready: Auto answer');
                instance.notifyIncommingCall(instance.getCallingName(), instance.getCallingPhone());
                instance._autoAnswer = true;
            }else{
                console.log('Ready: Change to start');
                instance.changeStatusStart(false);
                if(!instance.isSetAllowIncomming()) instance.setAllowIncomming(true);
                if(!instance.isSetIncomming()) instance.setIncomming(false);
                if(!instance.isSetCalling()) instance.setCalling(false);
                
                //check incomming call from start
                instance.checkIncommingCall();
            }
        });
        Twilio.Device.error(function (error) {
            instance.changeStatusStart(false);
            instance.currentConnection = null;
            instance.reset();
            if(instance._debug){
                console.error("Error: " + error.message);
            }
        });
        Twilio.Device.disconnect(function (conn) {
            instance.changeStatusStart(false);
            instance.currentConnection = null;
            instance.reset();
            if(instance._debug) console.log("Call ended");
            
            //check incomming call after avoid from previous call
            instance.checkIncommingCall();
        });
        Twilio.Device.incoming(function (conn) {
            $(".call-status.incoming h3").text(conn.parameters.From);
            $(".call-status.incoming span").text(conn.parameters.From);
            $(".call-status.answering h3").text(conn.parameters.From);
            $(".call-status.answering span").text(conn.parameters.From);
            if(instance._debug){
                console.log("Incoming connection:" + conn.parameters.From);
                console.log("Status: " + Twilio.Device.status());
                //console.log(conn);
            }
            
            if(instance._autoAnswer){
                instance.currentConnection = conn;
                instance._autoAnswer = false;
                instance.setAllowIncomming(false);
                instance.setIncomming(false);
                instance.setCalling(true);
                instance._allowCloseWindow = true;
                instance.changeStatusAnswering();
                instance.currentConnection.accept();
            }else{
                instance.changeStatusStart();
                conn.disconnect();
            }
        });
//        Twilio.Device.cancel(function (conn) {
//            if(instance._debug) console.log("Cancel");
//            instance.currentConnection = null;
//            instance.reset();
//        });
        
        $("#answer-call").click(function (e) {
            e.preventDefault();
            instance.playTouchSound();
            
            instance.setAllowIncomming(false);
            
            if(instance._audio) instance._audio.stop();
            if(instance.currentConnection){
                instance.changeStatusAnswering();
                instance.currentConnection.accept();
            }else{
                instance.setAllowIncomming(false);
                instance.setIncomming(false);
                instance.setCalling(true);
                instance._allowCloseWindow = true;
                instance._autoAnswer = true;
                $.post(BASE_URL + "/rest/twilio/answer/", {
                    To: instance.agent, companyId: instance.companyId , memberId: instance.memberId,
                    Private: instance._privateIncomming,
                    CallPhoneNumber: instance._callPhoneNumber,
                    CallSid: instance.getCallSid()
                });
                instance._privateIncomming = false;
            }
        });
        $("#start-call").click(function (e) {
            e.preventDefault();
            instance.playTouchSound();

            var tel = $("#telNumber").inputmask('unmaskedvalue');
            instance.startCallToNumber(tel);
        });
        $("#forward-call").click(function (e) {
            e.preventDefault();
            instance.playTouchSound();
            
            if(instance._audio) instance._audio.stop();
            var fwdAgent = $("#forwardUser").val();
            var fwdAgentStatus = $("#forwardUser").data('status');
            var fwdAgentTitle = $("#forwardUser").data('title');
            var fwdAgentAccount = $("#forwardUser").data('account');
            if($.trim(fwdAgent).length > 3 && fwdAgentStatus === 'online' ){
                if(instance.currentConnection){ //do forward
                    if(instance._debug) console.log("Foward call to: " + fwdAgent);
                    $.post(BASE_URL + "/rest/twilio/forward/", {
                        To: fwdAgent, 
                        CallPhoneNumber: instance._callPhoneNumber,
                        CallSid: instance.getCallSid()
                    });
                    Twilio.Device.disconnectAll();
                    instance.changeStatusStart();
                }else{ //do call
                    if(instance._debug) console.log("Start call to: " + fwdAgent);
                    currentConnection = Twilio.Device.connect({"To": fwdAgent, "Source": "CRMCLOUD"});
                    $(".call-status.calling h3").text(fwdAgentTitle);
                    $(".call-status.calling span").text(fwdAgentAccount);
                    instance.changeStatusCalling();
                    instance.setAllowIncomming(false);
                    instance.setIncomming(false);
                    instance.setCalling(true);
                    instance._allowCloseWindow = true;
                }
            }
        });
        $(".hangup-call").click(function (e) {
            e.preventDefault();
            instance.playTouchSound();
            
            if(instance._debug) console.log("Hang up ALL ");

            instance.stopIncomingCall(true);
            
            setTimeout(function(){
                //check incomming call after hang-up previous call (10 seconds)
                instance.checkIncommingCall();
                
            }, 10 * 1000);
        });
        $(".finish-call").click(function (e) {
            e.preventDefault();
            instance.playTouchSound();
            
            if(instance._debug) console.log("Finish CALL ");
            instance.reset();
            instance.changeStatusStart();
            instance.stopIncomingCall(false);
        });
        $(".control-mute").click(function(e){
            e.preventDefault();
            instance.playTouchSound();
            
            var i = $(this).find('i');
            var isMuted = i.hasClass("fa-microphone");

            Twilio.Device.mute(!isMuted);
            if(instance._debug) console.log(isMuted ? "no-Muted" : "Muted");

            if(isMuted){
                i.removeClass("fa-microphone");
                i.addClass("fa-microphone-slash");
            }else{
                i.removeClass("fa-microphone-slash");
                i.addClass("fa-microphone");
            }
        });
        $(".control-forward").click(function(e){
            e.preventDefault();
            instance.playTouchSound();
            instance.changeStatusForward();
        });
        $(".control-call-ext").click(function(e){
            e.preventDefault();
            instance.playTouchSound();
            $('#forward-call').data('text', $('#forward-call').text());
            $('#forward-call').text($('#start-call').text());
            instance.changeStatusForward();
        });
        $(".cancel-forward").click(function(e){
            e.preventDefault();
            instance.playTouchSound();
            if(instance.currentConnection){
                instance.changeStatusAnswering();
            }else{
                instance.changeStatusStart();
                $('#forward-call').text($('#forward-call').data('text'));
            }
        });
        $("#navigation #searchUser").keyup(function(e){
            if(e.which === 13) e.preventDefault();
            var searchText = $.trim($('#navigation #searchUser').val()).toLowerCase();
            $("#navigation #forward-list li").each(function(){
               var text = $(this).text().toLowerCase() ;
               if(text.indexOf(searchText) < 0){
                   $(this).addClass('hide');
               }else{
                   $(this).removeClass('hide');
               }
            });
        });
        $("#navigation #reload-forward-list").click(function(e){
            instance.reloadForwardList();
        });
    },
    startCallToNumber: function(tel, receiver){
        if( !this.isCalling() && tel && tel.length >= 9 ){
            tel = '+81' + tel.replace(/^0/ig, ''); //add japan phone code
            if(this._debug) console.log("Start call to: " + tel);
            currentConnection = Twilio.Device.connect({"To": tel, "Source": "CRMCLOUD"});
            if(typeof receiver !== 'undefined') $(".call-status.calling h3").text(receiver);
            $(".call-status.calling span").text(tel);
            this.changeStatusCalling(true);
            this.setAllowIncomming(false);
            this.setIncomming(false);
            this.setCalling(true);
            this._allowCloseWindow = true;
        }
    },
    playTouchSound: function(){
        try{
            $('#touch_sound')[0].play();
            setTimeout(function(){
                $('#touch_sound')[0].pause();
                $('#touch_sound')[0].currentTime = 0;
            }, 150);
        }catch(e){}
    },
    reloadForwardList: function(){
        $("#navigation #forward-list").html('waiting...');
        $("#navigation #reload-forward-list i").addClass('fa-spin');
        $("#navigation #forward-call").removeClass('btn-success').addClass('btn-default').attr('disabled', 'true');
        var instance = this;
        $.getJSON(BASE_URL + "/rest/softphone/customer/forward", {cid: this.companyId}, function(list){
            $("#navigation #forward-list").html('');
            $("#searchUser").val('');
            $.each(list, function(i, item){
                if(item.account !== instance.agent){
                    var li = $('<li/>',{
                        class: 'list-group-item'
                    });
                    var account = item.account.replace(/GN[0-9]{3}/g, '');
                    li.html('<h5 class="list-group-item-heading">'+item.name+' <small>('+account+')</small></h5>');
                    li.append('<p class="list-group-item-text text-sm"><i class="fa fa-circle '+(item.online ? 'text-success' : 'text-danger')+'" aria-hidden="true"></i> '+(item.online ? 'オンライ' : 'オフ')+'</p>');
                    li.click(function(){
                        if(item.online){
                            $("#navigation #forward-call").removeClass('btn-default').addClass('btn-success').removeAttr('disabled');
                        }else{
                            $("#navigation #forward-call").removeClass('btn-success').addClass('btn-default').attr('disabled', 'true');
                        }
                        $("#forwardUser").val(item.account)
                                .data('status', item.online ? 'online' : 'offline')
                                .data('title', item.name)
                                .data('account', account);
                        $("#navigation #forward-list .active").removeClass('active');
                        $(this).addClass('active');
                    });
                    $("#navigation #forward-list").append(li);
                }
            });
            setTimeout(function(){
                $("#navigation #reload-forward-list i").removeClass('fa-spin');
            }, 300);
        });
    },
    openNewTab: function(name, phoneNumber, callSid, allowMemberList){
        if(this.isAllowIncomming() && $.inArray(this.memberId, allowMemberList)){
            var instance = this;
            $.cookie('twilio_calling_name', name);
            $.cookie('twilio_calling_phone', phoneNumber);
            this.setCallSid(callSid);
            this.setIncomming(true);
            //open issue
            phoneNumber = phoneNumber.replace(/(?:\D)\d{2}/i, "0"); //remove +81 by 0
            $.get(BASE_URL + "/rest/softphone/customer/issue", {cid: instance.companyId, pn: phoneNumber}, function(issueId){
                if(!!issueId && !isNaN(issueId)){
                    if(instance._debug) console.log("view Issue " + issueId);
                    $("#navigation\\:relateIssueId").val(issueId);
                    $("#navigation\\:twilioViewIssueAction").trigger('click');
                }else{
                    if(instance._debug) console.log("create new issue");
                    $("#navigation\\:relateCallSid").val(callSid);
                    $("#navigation\\:custPhoneNumber").val(phoneNumber);
                    $("#navigation\\:twilioCreateIssueAction").trigger('click');
                }
            });
        }
    },
    notifyIncommingCall: function(name, phoneNumber){
        if(this.isAllowIncomming()){
            this.setAllowIncomming(false);
            $(".call-status.incoming h3").text(name);
            $(".call-status.incoming span").text(phoneNumber);
            this.changeStatusIncoming();
            this._callPhoneNumber = phoneNumber;
            $.cookie('twilio_calling_name', name);
            $.cookie('twilio_calling_phone', phoneNumber);
        }
    },
    notifyStopIncommingCall: function(checkIncommingCall){
        if(this._audio) this._audio.stop();
        if(!this.currentConnection){
            this.changeStatusStart();
        }
        if( !this.isCalling() ){
            this.reset();
            if(this._allowCloseWindow){
                window.close(); //close current window if had one other answered
            }
        }
        
        checkIncommingCall = checkIncommingCall || false;
        if(checkIncommingCall){
            var instance = this;
            setTimeout(function(){
                //check incomming call after hang-up previous call (10 seconds)
                instance.checkIncommingCall();

            }, 5 * 1000);
        }
    },
    notifyPrivateIncommingCall: function(name, phoneNumber, callSid){
        this._privateIncomming = true;
        this.setCallSid(callSid);
        this.notifyIncommingCall(name, phoneNumber);
    },
    startTimeCounter: function(){
        var instance = this;
        if(!this._timeCounter){
            
            this.callStartTime = moment();
            this._timeCounter = setInterval(function(){
                var seconds = moment().diff(instance.callStartTime, 'seconds');
                var interval = moment().minutes(0).seconds(seconds).format('mm:ss');
                $(".call-status.answering h4").text(interval);
                $(".call-status.calling h4").text(interval);
            }, 1000);
        }
    },
    stopTimeCounter: function(){
        this.callStartTime = null;
        if(this._timeCounter){
            clearInterval(this._timeCounter);
            this._timeCounter = null;
        }
    },
    stopIncomingCall: function(rejected){
        if(this._audio) this._audio.stop();
        if(this.currentConnection){
            this.currentConnection.reject();
            this.currentConnection.disconnect();
            this.currentConnection = null;
        }
        Twilio.Device.disconnectAll();
    },
    showDialPad: function(){
        var dialPad = $("#navigation .dropdown.phone");
        if(!dialPad.hasClass('open')){
            dialPad.addClass('open');
        }
    },
    changeStatusStart: function(showDialPad){
        if(this._debug) console.log('changeStatusStart');
        if(this._audio) this._audio.stop();
        showDialPad = typeof showDialPad === 'undefined' ? true :  showDialPad;
        if(showDialPad) this.showDialPad();
        $(".call-status").hide();
        $(".call-status.start").show();
        $(".dropdown.phone > a")
                .removeClass("phone-answering")
                .removeClass("phone-incomming");
        this.stopTimeCounter();
    },
    changeStatusIncoming: function(showDialPad){
        if(this._debug) console.log('changeStatusIncoming');
        if(this._audio){
            try{
                this._audio.play();
            }catch(e){}
        }
        showDialPad = typeof showDialPad === 'undefined' ? true :  showDialPad;
        if(showDialPad) this.showDialPad();
        $(".call-status").hide();
        $(".call-status.incoming").show();
        $(".dropdown.phone > a")
                .removeClass("phone-answering")
                .addClass("phone-incomming");
    },
    changeStatusAnswering: function(showDialPad){
        if(this._debug) console.log('changeStatusAnswering');
        if(this._audio) this._audio.stop();
        showDialPad = typeof showDialPad === 'undefined' ? true :  showDialPad;
        if(showDialPad) this.showDialPad();
        $(".call-status").hide();
        $(".call-status.answering").show();
        $(".dropdown.phone > a")
                .removeClass('phone-incomming')
                .addClass("phone-answering");
        this.startTimeCounter();

    },
    changeStatusCalling: function(showDialPad){
        if(this._debug) console.log('changeStatusCalling');
        showDialPad = typeof showDialPad === 'undefined' ? true :  showDialPad;
        if(showDialPad) this.showDialPad();
        $(".call-status").hide();
        $(".call-status.calling").show();
        setTimeout(this.startTimeCounter, 5000);
    },
    changeStatusForward: function(showDialPad){
        if(this._debug) console.log('changeStatusForward');
        this.reloadForwardList();
        showDialPad = typeof showDialPad === 'undefined' ? true :  showDialPad;
        if(showDialPad) this.showDialPad();
        $(".call-status").hide();
        $(".call-status.forward").show();
        $('.navbar .dropdown.phone #telNumber').focus();
    }
};