// Session Feedback User Interface (sf-ui.js)
//-----------------------------------------------------------------------------
// This Javascripts contains the methods to open the Session Feedback
// dialog and send it via POST using its API
//-----------------------------------------------------------------------------

var sfUI = (function () {
  var instance; // Singleton Instance of the UI component

  function Singleton() {
    // Compònent-related variables
    var buttonToShowSfId = '';

    // Internal parts
    var ctzSelected = false;
    var simplificationSelected = false;
    var timeoutExceeded = false;
    var timeout = 5 * 60 * 1000; // 5 minutes in ms
    var startTime;

    function initComponent (parameters) {
      buttonToShowSfId = parameters.buttonToShowSfId;
      sfCORE.getInstance().init({
        endpoint: parameters.apiEndpoint
      });

      // Add the onclick event
      if (buttonToShowSfId) {
          var button = document.getElementById(buttonToShowSfId);
          if (button != null) {
              button.addEventListener('click', function(){
            	  sfUI.getInstance().showSF();
              });
          }
      }

      // Start counting time
      startTime = new Date().getTime();
    }

    function showSF () {
      if (!authManager.getInstance().isEnabled()) return; // If there isn't an user logged in, SF won't work

      var data = JSON.parse(localStorage.userData); // Get the user's ID from localStorage
      if (citizenpediaUI.getInstance().isEnabled()) ctzSelected = true;
      if (taeUI.getInstance().isEnabled()) simplificationSelected = true;
      // Check if timeout exists
      var currentTime = new Date().getTime();
      timeoutExceeded = isTimeExceeded(currentTime - startTime);
      sfCORE.getInstance().selectDialog(ctzSelected, simplificationSelected, timeoutExceeded, data.userId);
    }

    function isTimeExceeded (timeMs) {
      if (timeMs > timeout)
        timeoutExceeded = true;

      return timeoutExceeded;
    }

    return {
      init: initComponent,
      isTimeExceeded: isTimeExceeded,
      showSF: showSF
    };
  }

  return {
    getInstance: function () {
      if (!instance) instance = Singleton();
      return instance;
    }
  };
})();
