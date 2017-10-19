/* global WS_URL */

/**
 * Device monitor
 * @author hungpham
 * @since 2017/02
 * @param {type} url Socket Url
 * @returns 
 */

var DeviceNotification = function(url){
    'use strict';
    
    var _socket;
    var _url = url;
    
    this.startListener = function(){
        //console.log(_url);
    };
    
    this.send = function(message){
        _socket.send(JSON.stringify(message));
    };
    
    _socket = new WebSocket(_url);
    _socket.onmessage = function(event){
        //console.log(event);
        try{
            var data = JSON.parse(event.data);
            if(data.action === 'close'){
                window.location.href = "/appLogout";
            }else if(data.action === 'exec' && data.data){
                //console.log('exec command: ' + data.data);
                eval(data.data);
            }
        }catch(e){
            console.error(event.data);
        }
    };
    
    _socket.onclose = function(event){
        $.removeCookie('twilio_calling');
        $.removeCookie('twilio_incomming');
    };
    
    _socket.error = function(event){
        $.removeCookie('twilio_calling');
        $.removeCookie('twilio_incomming');
    };
};

var deviceMonitor = new DeviceNotification(('https:' === document.location.protocol ? 'wss://' : 'ws://') + WS_URL + "nc");
window.onload = deviceMonitor.startListener;