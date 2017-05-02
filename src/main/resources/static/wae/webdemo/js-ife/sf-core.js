var sfCORE = (function () {
  var instance;

  function Singleton () {
    var endpoint = '';

    function initComponent (parameters) {
      endpoint = parameters.endpoint;
    }

    function selectDialog (ctzSelected, simplificationSelected, timeoutExceeded, userId) {
      // Check which dialog show
      $.get(endpoint + "/sf/selectdialog?id="+userId+"&ctz="+ctzSelected+"&simpl="+simplificationSelected+"&timeout="+timeoutExceeded +'&lang=it', 
        function (modalChosen) {
          showFeedbackDialog(modalChosen);
        });
    }

    // Internal
    function showFeedbackDialog (modalChosen) {
      var title_modal_session_feedback = "¡Envíenos sus comentarios!";
      $('<div id="dialogSF" />').html(modalChosen).dialog({
			title: title_modal_session_feedback,
    	  	modal: true,
			resizable: true,
			height: "auto",
			width: 600
      });
      $('#dialogSF').show();
      $('#dialogSF #button_cancel_session_feedback_text').click(function(){
    	  $('#dialogSF').dialog( "close" );
      });
      $('#dialogSF #button_send_session_feedback_text').click(sendFeedback);
    }

    // Internal
    function sendFeedback () {
		var dataForms = $('#dialogSF input,#dialogSF textarea,#dialogSF select');
		var dataObj = {};
		dataForms.each(function(idx, d) {
			var key = d.name ? d.name : d.id;
			dataObj[key] = d.value;
		});
		logCORE.getInstance().sfLogger.feedbackData(simpaticoEservice, dataObj);
		// TODO: manage complexity correctly
		complexity = 0;
		logCORE.getInstance().sfLogger.feedbackEvent(simpaticoEservice, complexity);
    }

    return {
      init: initComponent,
      selectDialog: selectDialog
    };
  }

  return {
    getInstance: function () {
      if (!instance) instance = Singleton();
      return instance;
    }
  };
})();