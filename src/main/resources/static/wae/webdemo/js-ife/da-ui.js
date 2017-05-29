var daUI = (function () {
  var instance;
  var featureEnabled = false;

  function Singleton () {
    var endpoint = '';
    var elementsToTrackTimeClassName = '';

    // Internal vars
    var links = [];
    var start;

    function initComponent (parameters) {
      elementsToTrackTimeClassName = parameters.elementsToTrackTimeClassName;
      endpoint = parameters.apiEndpoint;

      // Start measuring time
      start = new Date().getTime();
    }

    function enableComponentFeatures () {
      if (featureEnabled) return;
      featureEnabled = true;

      // Get the tagged elements
      if (links.length == 0) {
        links = document.getElementsByClassName(elementsToTrackTimeClassName);
      }

      // When a new element is clicked, measure the time spent
      for (var i=0; i<links.length; i++) {
        links[i].setAttribute("onclick", "daUI.getInstance().checkTime()")
      }
    }

    function disableComponentFeatures () {
      if (!featureEnabled) return;
      featureEnabled = false;

      // Remove onclick event
      for (var i=0; i<links.length; i++) {
        links[i].removeAttribute("onclick");
      }
    }

    function checkTime () {
      var end = new Date().getTime();

      // Send data to /logs/insert
      var userData = JSON.parse(localStorage.userData); // Get the user's ID from localStorage
      var postData = {
        "duration": end - start, // milliseconds
        "userId": userData.userId,
        "datatype": "duration",
        "timeForElement": $(this)
      }
      $.post(endpoint+"/logs/insert", JSON.stringify(postData)); // TODO Handle error

      // Restart the count
      start = new Date().getTime();
    }

    return {
      init: initComponent,
      enable: enableComponentFeatures,  // Called when the Component button is enabled
      disable: disableComponentFeatures, // Called when the Component button is disabled or another one enabled
      isEnabled: function () { return featureEnabled; }
    }
  }

  return {
    getInstance: function () {
      if (!instance) instance = Singleton();
      return instance;
    }
  };
})();

