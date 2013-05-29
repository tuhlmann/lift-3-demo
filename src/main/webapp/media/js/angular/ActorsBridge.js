App.namespace("angular");
App.angular.ActorsBridge = function(sendFunc) {
  "use strict";

  var self = this;

  self.send = sendFunc;

  self.messageFromServer = function(data) {
    $(document).trigger('new-chat-msg', data);
  }

}
