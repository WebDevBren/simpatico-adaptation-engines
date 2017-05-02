// Log Core Client (log-core.js)
//-----------------------------------------------------------------------------
// This JavaScript contains the client side of the Log component. The main 
// functionality is to log the main events through the calls to the server side 
// of the corresponding Log instance
// - The Log server side code is available in:
//              https://github.com/SIMPATICOProject/logs
//-----------------------------------------------------------------------------


var logCORE = (function () {
  var instance;
  function Singleton () {
	// Variables to store the used API and URL paths
	var insertLogEventAPI = '';


	var serverEndpoint = '';  
	var ctzpEndpoint = '';
	var taeEndpoint = '';
	var waeEndpoint = '';
	var ifeEndpoint = '';
	var sfEndpoint = '';
	var logsEndpoint = '';
	
	var log = function(url, data) {
		var token = authManager.getInstance().getToken();
		var userId = authManager.getInstance().getUserId();
		data.userID = userId;
		$.ajax({
			url: url,
			type: 'POST',
			data: JSON.stringify(data),
			contentType: "application/json; charset=utf-8",
			dataType: 'json',
			success: (function (resp) {
				console.log(resp);

			}),
			error: function (jqxhr, textStatus, err) {
				console.log(textStatus + ", " + err);
			},
			beforeSend: function (xhr) {
				xhr.setRequestHeader('Authorization', 'Bearer ' + token);

			}

		});			
	}
	
	var ctzpLogger = {
		logContentRequest: function(eservice, contentId) {
			log(ctzpEndpoint+'/contentrequest', {'e-serviceID': eservice, annotableElementID: contentId});
		},	
		logQuestionRequest: function(eservice, contentId, questionId) {
			log(ctzpEndpoint+'/questionrequest', {'e-serviceID': eservice, annotableElementID: contentId, questionID: questionId});
		},	
		logNewQuestionRequest: function(eservice, contentId) {
			log(ctzpEndpoint+'/newquestion', {'e-serviceID': eservice, annotableElementID: contentId});
		},	
		logTermRequest: function(eservice, contentId, term) {
			log(ctzpEndpoint+'/termrequest', {'e-serviceID': eservice, annotableElementID: contentId, selected_term: term});
		},	
	};  
	var taeLogger = {
		logParagraph: function(eservice, paragraphID) {
			log(taeEndpoint+'/paragraph', {'e-serviceID': eservice, paragraphID: paragraphID});
		},
		logPhrase: function(eservice, phraseID) {
			log(taeEndpoint+'/phrase', {'e-serviceID': eservice, phraseID: phraseID});
		},
		logWord: function(eservice, wordID) {
			log(taeEndpoint+'/word', {'e-serviceID': eservice, wordID: wordID});
		},
		logFreetext: function(eservice, selected_text) {
			log(taeEndpoint+'/freetext', {'e-serviceID': eservice, selected_text: selected_text});
		}
	}
	var waeLogger = {
		logWae: function(eservice) {
			log(waeEndpoint, {'e-serviceID': eservice, timestamp: ''+new Date().getTime()});
		}
	}
	var ifeLogger = {
		sessionStart: function(eservice) {
			var ts = new Date().getTime();
			// record session start for sessionEnd reference
			localStorage.logSessionStart = ts;
			log(ifeEndpoint+'/sessionstart', {'e-serviceID': eservice, timestamp: ''+ts});
		},
		sessionEnd: function(eservice) {
			var ts = parseInt(localStorage.logSessionStart || (''+new Date().getTime()));
			var diff = new Date().getTime() - ts;
			log(ifeEndpoint+'/sessionend', {'e-serviceID': eservice, timestamp: ''+ts, sessionDuration: ''+diff, averageTime: diff});
		},
		formStart: function(eservice, form) {
			var ts = new Date().getTime();
			log(ifeEndpoint+'/formstart', {'e-serviceID': eservice, formID: form, timestamp: ''+ts});
		},
		formEnd: function(eservice, form) {
			var ts = new Date().getTime();
			log(ifeEndpoint+'/formend', {'e-serviceID': eservice, formID: form, timestamp: ''+ts});
		}
	}
	var sfLogger = {
		feedbackEvent: function(eservice, complexity) {
			log(sfEndpoint, {'e-serviceID': eservice, complexity: complexity});
		},
		feedbackData: function(eservice, data) {
			data['e-serviceID'] = eservice;
			log(logsEndpoint, data);
		}
	}
	  
	  
    // In inits the main used variables
    // In this case it generates the used API and URL paths
    // An object with an endpoint 
    function initComponent(parameters) {
      insertLogEventAPI = parameters.endpoint + '/logs/insert';
      ctzpEndpoint = parameters.endpoint + '/ctzp/insert';
      taeEndpoint = parameters.endpoint + '/tae/insert';
      waeEndpoint = parameters.endpoint + '/wae/insert';
      ifeEndpoint = parameters.endpoint + '/ife/insert';
      sfEndpoint = parameters.endpoint + '/sf/insert';
      logsEndpoint = parameters.endpoint + '/logs/insert';
    }

    // TODO: HIB - Implement it
    function insertLogEvent(data) {
      console.warn("TO-DO: HIB Implement the log insertion in [" + insertLogEventAPI + "] ---> " + JSON.stringify(data));
    }


    // It logs an event caused when a user uses a Simpatico feature.
    // - component: Component which produces the event (e.g. tae, wae, ctz...)
    // - element: Id of the element that causes the event (e.g. paragraphID...)
    // - event: Id of the element that causes the event (e.g. paragraphID...)
    // - details: Optional parameter to pass additional info if it is required
    function logSimpaticoEvent(component, element, event, details) {
      var timestamp = new Date().getTime()
      //TODO: HIB- Implement it
      var postData = {
        "component": component, // Component which produces the event
        "element": element,
        "event": event,
        "details": details,
        "userID": "userData.userId", // the id of the logged user
        "serviceID": simpaticoEservice, // the id of the corresponding e-service
        "timestamp": timestamp
      }
      insertLogEvent(postData);
    }


    // TODO: HIB - Complete it
    // It logs an event caused when a user uses interacts with a hooked element.
    // - element: Id of the element that causes the event (e.g. paragraphID...)
    // - details: Optional parameter to pass additional info if it is required
    function logTimeEvent(element, details) {
      var timestamp = new Date().getTime()
      var postData = {
        "duration": "", // Component which produces the event
        "userID": userData.userId, // the id of the logged user
        "datatype": "duration",
        "timeForElement": element
      }
      insertLogEvent(postData);
    }

    return {
        init: initComponent,
        logSimpaticoEvent: logSimpaticoEvent,
        logTimeEvent: logTimeEvent,
        ctzpLogger: ctzpLogger,
        taeLogger: taeLogger,
        waeLogger: waeLogger,
        ifeLogger: ifeLogger,
        sfLogger: sfLogger
      };
  }
  return {
    getInstance: function() {
      if(!instance) instance = Singleton();
      return instance;
    }
  };
})();