/**
 * TEXT ADAPTATION AND SIMPLIFICATION SERVICE
 */
var taeEngine = ( function () {
	var instance;

	function Singleton () {
		var endpoint = "/tae";
		var lang = "it";
		/**
		 * INIT THE ENGINE CONFIG. PARAMETERS:
		 * - endpoint: URL OF THE TAE API
		 * - lang: LANGUAGE TO USE FOR THE TOOL
		 */
		this.init = function(config) {
			config = config || {};
			if (config.endpoint) {
				endpoint = config.endpoint;
			}
			if (config.lang) {
				lang = config.lang;
			}
		}
		/**
		 * RETRIEVE DEFINITIONS-ANNOTATED TEXT GIVEN NORMAL TEXT
		 */
		this.getDefinitions = function(data, callback, errorCallback) {
			var value = data.text.replace(/[\t\r\n]/g, ' ');
			var word = data.word;
			var position = !!word ? value.indexOf(data.word) : -1;
			var url = endpoint + "/simp?lang="+lang+"&text=" + value;
			if (word != null) {
				url += "&word="+data.word;
				url += "&position="+position;
			}
			$.getJSON(url)
			  .done(function(json) {
			    var index = 0;
			    var annotatedText = "";
			    if (word == null) {
				    for (itemName in json.readability.forms) {
				    	item = json.readability.forms[itemName];
				    	annotatedText = annotatedText + value.substring(index, item.start-1);
				    	var originalText = value.substring(item.start, item.end);
				      annotatedText = annotatedText + 
				      ' <a class="simpatico-label" title="' + item.description.description + 
				      '">' + originalText +'</a> ';
				      index = item.end;
				    }
				    annotatedText = annotatedText + value.substring(index, value.length);
				    //console.log('annotatedText ' + annotatedText);
				    callback(annotatedText);
			    } else {
				    for (itemName in json.readability.forms) {
				    	item = json.readability.forms[itemName];
				    	if (item.start == position) {
						      var text = ' <a class="simpatico-label" title="' + item.description.description + 
						      '">' + word +'</a> ';
						      callback(text);
						      return;
				    	}
				    }
				    callback(word);
			    }
			  })
			  .fail(function( jqxhr, textStatus, error) {
			  	console.log(textStatus + ", " + error);
			  	errorCallback("Errore nella comunicazione col server");
			  });
		};
		
		/**
		 * RETRIEVE SIMPLIFICATION-ANNOTATED TEXT GIVEN NORMAL TEXT
		 */
		this.getExplanations = function(data, callback, errorCallback) {
			var value = data.text.replace(/[\t\r\n]/g, ' ');
			var url = endpoint + "/simp?lang="+lang+"&text=" + value;
			if (data.word != null) {
				url += "&word="+data.word;
				url += "&position="+value.indexOf(data.word);
			}
			$.getJSON(url)
			  .done(function(json) {
			    //console.log(JSON.stringify(baconGoodness));
			    var index = 0;
			    var annotatedText = "";
			    if(json.simplifications) {
			    	if (data.word == null) {
					    for (item in json.simplifications) {
					    	var originalText = value.substring(json.simplifications[item].start, json.simplifications[item].end);
					        annotatedText = annotatedText + value.substring(index, json.simplifications[item].start-1);
					        annotatedText = annotatedText + ' <a class="simpatico-label" title="' + json.simplifications[item].simplification + 
					        '">' + originalText +'</a> '
					        index = json.simplifications[item].end;
					    }
					    annotatedText = annotatedText + value.substring(index, value.length);
					    callback(annotatedText);
			    	} else {
					    for (item in json.simplifications) {
					    	if (json.simplifications[item].start == value.indexOf(data.word)) {
					    		callback(' <a class="simpatico-label" title="' + json.simplifications[item].simplification + 
								        '">' + data.word +'</a> ');
					    		return;
					    	}
					    }
					    callback(data.word);
			    	}
			    } else {
			    	callback(data.word != null ? data.word : data.text);
			    } 
			  })
			  .fail(function( jqxhr, textStatus, error) {
			  	console.log(textStatus + ", " + error);
			  	errorCallback("Errore nella comunicazione col server");
			  });
		};
		
		/**
		 * RETRIEVE WIKIPEDIA TEXT GIVEN NORMAL TEXT
		 */
		this.wikipedia = function(data, callback, errorCallback) {
			var value = data.text.replace(/[\t\r\n]/g, ' ');
			var url = endpoint + "/simp?lang="+lang+"&text=" + value;
			if (data.word != null) {
				url += "&word="+data.word;
				url += "&position="+value.indexOf(data.word);
			}
			$.getJSON(url)
			  .done(function(json) {
			    if (data.word == null) {
			    	var index = 0;
				    var annotatedText = "";
				    json.linkings.sort(compareLinkItem);
				    var actualOffset = -1;
				    var actualLinkItem = null;
				    for (itemName in json.linkings) {
				    	var item = json.linkings[itemName];
				    	if(actualOffset == -1) {
				    		actualOffset = item.offset;
				    	}
				    	if(item.offset > actualOffset) {
					    	annotatedText = annotatedText + value.substring(index, actualLinkItem.offset-1);
					    	var originalText = value.substring(actualLinkItem.offset, actualLinkItem.offset + actualLinkItem.length);
					      annotatedText = annotatedText + 
					      ' <a class="simpatico-label" target="_blank" href="' + actualLinkItem.page + 
					      '">' + originalText +'</a> ';
					      index = actualLinkItem.offset + actualLinkItem.length;
					      actualLinkItem = item;
					      actualOffset = item.offset;
				    	} else {
				    		actualLinkItem = item;
				    	}
				    }
				    if(actualLinkItem != null) {
				    	annotatedText = annotatedText + value.substring(index, actualLinkItem.offset-1);
				    	var originalText = value.substring(actualLinkItem.offset, actualLinkItem.offset + actualLinkItem.length);
				      annotatedText = annotatedText + 
				      ' <a class="simpatico-label" target="_blank" href="' + actualLinkItem.page + 
				      '">' + originalText +'</a> ';
				      index = actualLinkItem.offset + actualLinkItem.length;
				    }
				    annotatedText = annotatedText + value.substring(index, value.length);
				    //console.log('annotatedText ' + annotatedText);
				    callback(annotatedText);		    	
			    } else {
				    for (itemName in json.linkings) {
				    	var item = json.linkings[itemName];
				    	if(item.offset == value.indexOf(data.word)) {
					      callback(' <a class="simpatico-label" target="_blank" href="' + item.page + 
					      '">' + data.word +'</a> ');
					      return;
				    	}
				    }
				    callback(data.word);	
			    }
			  })
			  .fail(function( jqxhr, textStatus, error) {
			  	console.log(textStatus + ", " + error);
			  	errorCallback("Errore nella comunicazione col server");
			  });
		};
		
		/**
		 * RETRIEVE SYNTACTICALLY SIMPLIFIED TEXT GIVEN NORMAL TEXT
		 */
		this.getSimplifiedText = function(data, callback, errorCallback) {
			var value = data.text.replace(/[\t\r\n]/g, ' ');
			var url = endpoint + "/simp?lang="+lang+"&text=" + value;
			$.getJSON(url)
			  .done(function(json) {
				  if (!!json.simplifiedText) {
					  callback(json.simplifiedText);
				  } else {
					  callback(value);
				  }
			  })
			  .fail(function( jqxhr, textStatus, error) {
			  	console.log(textStatus + ", " + error);
			  	errorCallback("Errore nella comunicazione col server");
			  });
		}
	
		
		function compareLinkItem(a, b) {
			if(a.offset < b.offset) {
				return -1;
			} else if(a.offset > b.offset) {
				return 1;
			} else {
				if(a.score < b.score) {
					return -1;
				}
				if(a.score > b.score) {
					return 1;
				}
				return 0;
			}
		}
		
		return this;
	}

    return {
	    getInstance: function() {
	      if(!instance) instance = Singleton();
	      return instance;
	    }
    };
})();