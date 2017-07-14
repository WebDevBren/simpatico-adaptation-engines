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

  var TEST_MODE = false;

	var serverEndpoint = '';  
	var ctzpEndpoint = '';
	var taeEndpoint = '';
	var waeEndpoint = '';
	var ifeEndpoint = '';
	var sfEndpoint = '';
	var logsEndpoint = '';
	
	var syncMode = false;
	
	var activityStart = {};
  
	var start;
	var log = function(url, data) {
		if (TEST_MODE) return;
		var token = authManager.getInstance().getToken();
		var userId = authManager.getInstance().getUserId();
		data.userID = userId;
		data.sessionId = localStorage.logSessionStart;
		$.ajax({
			url: url,
			type: 'POST',
			data: JSON.stringify(data),
			async: !syncMode,
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
	    logNewAnswer: function(eservice, contentId, questionId) {
	      log(ctzpEndpoint+'/newanswer', {'e-serviceID': eservice, annotableElementID: contentId, questionID: questionId});
	    }
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
		},
		logAction: function(eservice, action, word) {
	      var timestamp = new Date().getTime();
	      var postData = {
	        "component": 'tae', 
	        "e-serviceID": eservice, // the id of the corresponding e-service
	        "timestamp": timestamp,
	        "action": action	        
	      }
	      if (!!word) postData.word = word;
	      insertLogEvent(postData);
		}
	}
	var waeLogger = {
		logWae: function(eservice) {
			log(waeEndpoint, {'e-serviceID': eservice, timestamp: ''+new Date().getTime()});
		},
		logBlockStart: function(eservice, blockId) {
			startActivity('wae','block',blockId);
		},
		logBlockEnd: function(eservice, blockId) {
			endActivity('wae','block',blockId);
		}
	}
	var cdvLogger = {
		saveData: function(eservice) {
		      var timestamp = new Date().getTime();
		      var postData = {
		        "component": 'cdv', 
		        "e-serviceID": eservice, // the id of the corresponding e-service
		        "timestamp": timestamp,
		        "action": "savedata"	        
		      }
		      insertLogEvent(postData);
		},
		useData: function(eservice, fieldId) {
		      var timestamp = new Date().getTime();
		      var postData = {
		        "component": 'cdv', 
		        "e-serviceID": eservice, // the id of the corresponding e-service
		        "timestamp": timestamp,
		        "action": "usedata"	        
		      }
		      if (fieldId != null) postData.fieldId = fieldId;
		      insertLogEvent(postData);
		}
	}
	var ifeLogger = {
		sessionStart: function(eservice) {
			var ts = new Date().getTime();
			// record session start for sessionEnd reference
			localStorage.logSessionStart = ts;
			log(ifeEndpoint+'/sessionstart', {'e-serviceID': eservice, timestamp: ''+ts});
			startActivity('ife','session', null, null, true);
		},
		sessionEnd: function(eservice) {
			var ts = parseInt(localStorage.logSessionStart || (''+new Date().getTime()));
			var diff = new Date().getTime() - ts;
			log(ifeEndpoint+'/sessionend', {'e-serviceID': eservice, timestamp: ''+ts, sessionDuration: ''+diff, averageTime: diff});
			endActivity('ife','session', null, null, true);
		},
		formStart: function(eservice, form) {
			var ts = new Date().getTime();
			log(ifeEndpoint+'/formstart', {'e-serviceID': eservice, formID: form, timestamp: ''+ts});
			startActivity('ife','form', null, null, true);
		},
		formEnd: function(eservice, form) {
			var ts = new Date().getTime();
			log(ifeEndpoint+'/formend', {'e-serviceID': eservice, formID: form, timestamp: ''+ts});
			endActivity('ife','form', null, null, true);
		},
		clicks: function(eservice, contentId, clicks) {
			log(ifeEndpoint+'/clicks', {'e-serviceID': eservice, annotableElementID: contentId, clicks: clicks});
		}
	}
	var sfLogger = {
		feedbackEvent: function(eservice, complexity) {
			log(sfEndpoint, {'e-serviceID': eservice, complexity: complexity});
		},
		feedbackData: function(eservice, data) {
			data['e-serviceID'] = eservice;
			// SEND NUMERIC DATA OTHERWISE ELASTICSEARCH DOES NOT INDEX AS NUMERIC 
			if (data.slider_session_feedback_paragraph) data.slider_session_feedback_paragraph = parseInt(data.slider_session_feedback_paragraph);
			if (data.slider_session_feedback_phrase) data.slider_session_feedback_phrase = parseInt(data.slider_session_feedback_phrase);
			if (data.slider_session_feedback_word) data.slider_session_feedback_word = parseInt(data.slider_session_feedback_word);
			if (data.slider_session_feedback_ctz) data.slider_session_feedback_ctz = parseInt(data.slider_session_feedback_ctz);
			data['datatype'] = 'session-feedback'; // to distinguish it
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
      start = new Date().getTime();
    }

    // TODO: HIB - Implement it
    function insertLogEvent(data) {
      log(insertLogEventAPI, data);
    }


    // It logs an event caused when a user uses a Simpatico feature.
    // - component: Component which produces the event (e.g. tae, wae, ctz...)
    // - element: Id of the element that causes the event (e.g. paragraphID...)
    // - event: Id of the element that causes the event (e.g. paragraphID...)
    // - details: Optional parameter to pass additional info if it is required
    function logSimpaticoEvent(component, element, event, details) {
      var timestamp = new Date().getTime();
      //TODO: HIB- Implement it
      var postData = {
        "component": component, // Component which produces the event
        "event": event,
        "e-serviceID": simpaticoEservice, // the id of the corresponding e-service
        "timestamp": timestamp
      }
      if (!!element) postData.element = element;
      if (!!details) postData.details = details;
      insertLogEvent(postData);
    }

    function startActivity(component, activity, element, details, skipLog) {
    	if (activityStart[component] == null) activityStart[component] = {};
    	activityStart[component][activity] = new Date().getTime();
    	if (!skipLog) logSimpaticoEvent(component, element, activity+'_start', details);
    }
    function endActivity(component, activity, element, details, skipLog) {
    	var end = new Date().getTime();
    	if (activityStart[component] == null) activityStart[component] = {};
    	var start = activityStart[component][activity];
    	if (start != null) {
			var postData = {
				"duration" : end - start,
				"datatype" : "duration",
		        "e-serviceID": simpaticoEservice, // the id of the corresponding e-service
				"timeForElement" : activity,
				"component": component
			}
			if (element)
				postData.element = element;
			if (details)
				postData.details = details;
			insertLogEvent(postData);
			activityStart[component][activity] = null;
			if (!skipLog) logSimpaticoEvent(component, element, activity+'_end', details);
    	}
    }
    
        // TODO: HIB - Complete it
    // It logs an event caused when a user uses interacts with a hooked element.
    // - element: Id of the element that causes the event (e.g. paragraphID...)
    // - details: Optional parameter to pass additional info if it is required
    function logTimeEvent(element, details) {
      var end = new Date().getTime();
      var postData = {
        "duration": end - start,
        "datatype": "duration",
        "timeForElement": element, // Component which produces the event
        "details": details
      }
      insertLogEvent(postData);
      start = new Date().getTime();
    }

    return {
        init: initComponent,
        logSimpaticoEvent: logSimpaticoEvent,
        logTimeEvent: logTimeEvent,
        startActivity: startActivity,
        endActivity: endActivity,
        ctzpLogger: ctzpLogger,
        cdvLogger: cdvLogger,
        taeLogger: taeLogger,
        waeLogger: waeLogger,
        ifeLogger: ifeLogger,
        sfLogger: sfLogger,
        setSyncMode: function() {
        	syncMode = true;
        }
      };
  }
  return {
    getInstance: function() {
      if(!instance) instance = Singleton();
      return instance;
    }
  };
})();