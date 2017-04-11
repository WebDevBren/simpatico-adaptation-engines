$( function() {
	$(document).tooltip();
});

/**
 * TAE UI OPERATIONS
 */
var taeUIPopup = (function () {
	  var instance; // Singleton Instance of the UI component

	  function Singleton() {
		_instance = this;  

		_instance.colors = {
			simplify: '#000'
		}
	
		_instance.labels = {
			dialogTitle: 'Text enrichment',
			tabSyntSimpTitle: 'Simplified text',
			tabDefinitionsTitle: 'Definitions',
			tabSimplificationTitle: 'Semplification',
			tabWikipediaTitle: 'Wikipedia',
			entryMessage: 'Select an action',
			notextMessage: 'No text selected'
	
		};
	
		/**
		 * INITIALIZE UI COMPONENT.
		 * CONFIG PARAMETERS:
		 * - endpoint: URL OF THE TAE API ENDPOINT
		 * - dialogTitle:  title of the dialog
		 * - tabSyntSimpTitle: title of the syntactic simplification tab
		 * - tabDefinitionsTitle: title of the definitions tab
		 * - tabSimplificationTitle: title of the simplification tab
		 * - tabWikipediaTitle: text on the wikipedia tab
		 * - entryMessage: text on the default tab
		 * - notextMessage: text to shwo when no text selected
		 */
		_instance.init = function(config) {
			config = config || {};
			taeEngine.getInstance().init({endpoint: config.endpoint, lang: config.lang});
			_instance.labels.dialogTitle = config.dialogTitle || _instance.labels.dialogTitle;
			_instance.labels.tabSyntSimpTitle = config.tabSyntSimpTitle || _instance.labels.tabSyntSimpTitle;
			_instance.labels.tabDefinitionsTitle = config.tabDefinitionsTitle || _instance.labels.tabDefinitionsTitle;
			_instance.labels.tabSimplificationTitle = config.tabSimplificationTitle || _instance.labels.tabSimplificationTitle;
			_instance.labels.tabWikipediaTitle = config.tabWikipediaTitle || _instance.labels.tabWikipediaTitle;
			_instance.labels.entryMessage = config.entryMessage || _instance.labels.entryMessage;
			_instance.labels.notextMessage = config.notextMessage || _instance.labels.notextMessage;
			_instance.colors.simplify = config.simplifyColor || _instance.colors.simplify;
	
			_instance.dialog_simplify = $(
					'<div id="dialog-simplify" title="'+_instance.labels.dialogTitle+'">'+
					'	<div id="tabs">'+
					'		<ul>'+
					'			<li><a href="#tab-0">Simpatico</a></li>'+
					'			<li><a href="#tab-synt-simp">'+_instance.labels.tabSyntSimpTitle+'</a></li>'+
					'			<li><a href="#tab-definizioni">'+_instance.labels.tabDefinitionsTitle+'</a></li>'+
					'			<li><a href="#tab-semplificazione">'+_instance.labels.tabSimplificationTitle+'</a></li>'+
					'			<li><a href="#tab-wikipedia">'+_instance.labels.tabWikipediaTitle+'</a></li>'+
					'		</ul>'+
					'		<div id="tab-0">'+
					'			<p>'+_instance.labels.entryMessage+'</p>'+
					'		</div>'+
					'		<div id="tab-synt-simp">'+
					'			<p>Loading...</p>'+
					'		</div>'+
					'		<div id="tab-definizioni">'+
					'			<p>Loading...</p>'+
					'		</div>'+
					'		<div id="tab-semplificazione">'+
					'			<p>Loading...</p>'+
					'		</div>'+
					'		<div id="tab-wikipedia">'+
					'			<p>Funzione non implementata</p>'+
					'		</div>'+
					'	</div>'+
					'</div>'
				).dialog({
				autoOpen: false,
				modal: true,
				resizable: true,
				height: "auto",
				width: 600,
				buttons: {
					close: function() {
						$(this).dialog( "close" );
					}
				}
			});
			_instance.dialog_simplify.tabs({
				beforeActivate: function( event, ui ) {
					var cb = setInnerText(ui.newPanel["0"].id);
					var errCb = setError(ui.newPanel["0"].id);
	
					if(ui.newPanel["0"].id == "tab-0") {
						if(!!_instance.selectedText) {
							ui.newPanel["0"].innerHTML = '<p>'+_instance.labels.entryMessage+'</p>';
						} else {
							ui.newPanel["0"].innerHTML = '<p>'+_instance.labels.notextMessage+'</p>';
						}
					} if(ui.newPanel["0"].id == "tab-synt-simp") {
						if(!!_instance.selectedText) {
							ui.newPanel["0"].innerHTML = '<p>Loading...</p>';
							taeEngine.getInstance().getSimplifiedText(_instance.selectedText, cb, errCb);
						} else {
							ui.newPanel["0"].innerHTML = '<p>'+_instance.labels.notextMessage+'</p>';
						}
					} if(ui.newPanel["0"].id == "tab-definizioni") {
						if(!!_instance.selectedText) {
							ui.newPanel["0"].innerHTML = '<p>Loading...</p>';
							taeEngine.getInstance().getDefinitions(_instance.selectedText, cb, errCb);
						} else {
							ui.newPanel["0"].innerHTML = '<p>'+_instance.labels.notextMessage+'</p>';
						}
					} if(ui.newPanel["0"].id == "tab-semplificazione") {
						if(!!_instance.selectedText) {
							ui.newPanel["0"].innerHTML = '<p>Loading...</p>';
							taeEngine.getInstance().getExplanations(_instance.selectedText, cb, errCb);
						} else {
							ui.newPanel["0"].innerHTML = '<p>'+_instance.labels.notextMessage+'</p>';
						}
					} else if(ui.newPanel["0"].id == "tab-wikipedia") {
						if(!!_instance.selectedText) {
							ui.newPanel["0"].innerHTML = '<p>Loading...</p>';
							taeEngine.getInstance().wikipedia(_instance.selectedText, cb, errCb);
						} else {
							ui.newPanel["0"].innerHTML = '<p>'+_instance.labels.notextMessage+'</p>';
						}
					}
				},
				load:function( event, ui ) {
		  		/* After page load*/
		  	}
			});
	
		}
	
		/**
		 * CURRENTLY SELECTED TEXT DATA: text, word, position (if apply)
		 */
		_instance.selectedText = null;
	
		_instance.dialog_simplify = null;
	
		/**
		 * OPEN TAE UI DIALOG FOR CURRENTLY SELECTED TEXT
		 */
		_instance.showDialog = function() {
			_instance.shown = true;
			_instance.selectedText = getSelectedTextData();
			_instance.dialog_simplify.tabs( "option", "active", 0);
			var disabled = [];
			if (!_instance.selectedText || !_instance.selectedText.text) disabled = [0,1,2,3,4];
			else if (_instance.selectedText.word) disabled.push(1);
			_instance.dialog_simplify.tabs("option", "disabled", disabled);
			_instance.dialog_simplify.dialog("open");
	
		}
		_instance.hideDialog = function() {
			_instance.shown = false;
			_instance.dialog_simplify.dialog("close");
		}
	
		/**
		 * FIND AND UPDATE THE TEXT ELEMENTS FOR SIMPLIFICATION. THE
		 * TEXT TO SIMPLIFY SHOULD BE ANNOTATED WITH THE simp-text-paragraph ANNOTATION
		 */
		_instance.activateSimplification = function() {
			$('.simp-text-paragraph').each(function(i){
				var p = $(this);
				p.attr("id", "sp"+i++);
		        p.css('position','relative');
		        p.css('borderLeft', "thick solid " + _instance.colors.simplify);
		        p.on("click", function(){
		        	doSimplify(p.attr('id'));
		        });
			});
		}
		/**
		 * FIND AND UPDATE THE TEXT ELEMENTS FOR SIMPLIFICATION
		 */
		_instance.deactivateSimplification = function() {
			$('.simp-text-paragraph').each(function(i){
				var p = $(this);
				if (p.attr('id').indexOf('_simplified')>0) {
					p.remove();
				}
				else {
			        p.css('display', "");
			        p.css('borderLeft', "0");
			        p.css('borderBottom', "0");
			        p.unbind("click");
				}
			});
		}
	
		_instance.isEnabled = function() {
			return _instance.shown;
		}
		
		function doSimplify(id) {
			var p = $('#'+id);
			p.css('borderBottom', 'thick solid ' + _instance.colors.simplify);
			taeEngine.getInstance().getSimplifiedText(p.text(),function(txt) {
				var cpy = p.clone();
				cpy.attr('id',id+'_simplified');
				cpy.attr('display','none');
				cpy.on('click', function() {
					cpy.remove();
					p.fadeIn(400);
				});
				cpy.text(txt);
				p.fadeOut(400, function(){
					cpy.appendTo(p.parent());
				});
			});
		}
	
		function getSelectedTextData(){
		  var textData = {text:''};
	
		  if (window.getSelection()) {
		      textData.text = window.getSelection().toString().trim();
		      if (!textData.text.match(/(\s)/i)) {
		    	  textData.word = textData.text;
		    	  var selection = window.getSelection();
		    	  if (selection && selection.anchorNode) {
		    		  textData.text = selection.anchorNode.textContent;
		    	  }
		      }
		  } else if (document.selection && document.selection.type != "Control") {
			  textData.text = document.selection.createRange().text;
		      if (!textData.text.match(/(\s)/i)) {
		    	  textData.word = textData.text;
		    	  var selection = window.getSelection();
		    	  if (selection && selection.anchorNode) {
		    		  textData.text = selection.anchorNode.value;
		    	  }
		      }
		  }
		  if (!textData.text) return null;
		  return textData;
		};
	
		function setInnerText(target) {
			var targetElement = document.getElementById(target);
			return function(text) {
				targetElement.innerHTML = '<p>' + text + '</p>';
			}
		}
	
		function setError(target) {
			var targetElement = document.getElementById(target);
			return function(text) {
				targetElement.innerHTML = '<p>' + text + '</p>';
			}
		}
	
	};
    return {
    	getInstance: function() {
    		if(!instance) instance = new Singleton();
    		return instance;
    	}
    };
})();