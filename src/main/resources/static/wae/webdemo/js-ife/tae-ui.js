// Text Adaptation Engine User Interface (tae-ui.js)
//-----------------------------------------------------------------------------
// This JavaScript contains the functionality related to the User Interface
// which enriches the Interactive Front-End component with the features of 
// the Text Adaptation Engine component.
// - It uses the methods implemented in tae-core.js
// - The Text Adaptation Engine server side code is available in:
//              https://github.com/SIMPATICOProject/SimpaticoTAEServer
//-----------------------------------------------------------------------------

var taeUI = (function () {
  var instance; // Singleton Instance of the UI component
  var featureEnabled = false;
  function Singleton () {


    // Component-related variables
    var primaryColor = '';
    var secondaryColor = '';
    var elementsToEnhanceClassName = '';
    var simplifyBoxTitle = '';
    var simplifyBoxClassName = '';
    var wordPropertiesClassName = '';


    // Internal usage variables
    var paragraphs = []; // Used to store all the tagged paragraphs
    var originalStyles = []; // Used to store the tagged paragraphs CSSstyles
    var simplifyBoxIdSuffix = '-simp-text-paragraph';

    // Component-related methods and behaviour
    function initComponent(parameters) {
      primaryColor = parameters.primaryColor;
      secondaryColor = parameters.secondaryColor;
      elementsToEnhanceClassName = parameters.elementsToEnhanceClassName;
      simplifyBoxTitle = parameters.simplifyBoxTitle;
      simplifyBoxClassName = parameters.simplifyBoxClassName;
      wordPropertiesClassName = parameters.wordPropertiesClassName;
      taeCORE.getInstance().init({
          endpoint: parameters.endpoint,
          language: parameters.language
        });
    }
    
    function enableComponentFeatures() {
      if (featureEnabled) return;
      featureEnabled = true;

      // Gets the tagged paragraphs the first time
      if (paragraphs.length === 0) {
        paragraphs = document.getElementsByClassName(elementsToEnhanceClassName);
      }

      // Add special format and add a couple of attributes to the paragraphs
      var paragrapId = 1;
      var paragraphName = '';
      for (var i = 0, len = paragraphs.length; i < len; i++) {
        // Store original style
        originalStyles[i] = paragraphs[i].style;

        paragraphName = "Paragraph" + paragrapId;
        paragraphs[i].style.position = 'relative';
        paragraphs[i].style.borderLeft = "12px solid " + primaryColor;
        paragraphs[i].style.borderRadius = "16px";

        paragraphs[i].style.padding = '0px 0px 0px 8px';
        paragraphs[i].style.margin = '0px 0px 8px 0px';

        paragraphs[i].setAttribute("id", paragraphName);
        paragraphs[i].setAttribute("onclick", 
          "taeUI.getInstance()." + 
              "paragraphEvent('" + paragraphName + "');");
        paragrapId++;
      }
    }
  
    function disableComponentFeatures() {
      if (!featureEnabled) return;
      featureEnabled = false;

      // Remove Question Boxes
      var questionsBoxes = document.getElementsByClassName(simplifyBoxClassName);
      for (var i = questionsBoxes.length - 1; i >= 0; i--) {
        questionsBoxes[i].parentNode.removeChild(questionsBoxes[i]);
      }
      
      // Reformat the paragraphs with the original style
      for (var i = 0, len = paragraphs.length; i < len; i++) {
        // Restore the original style
        paragraphs[i].style = originalStyles[i];
        // Remove the onclick event to enhance the paragraph
        paragraphs[i].removeAttribute("onclick");
      }
    }

    // If the Component feature is enabled it calls to the TAE engine instance to 
    // get the simplifications related to the paragraph passed as parameter
    // - paragraphID: the id of the paragraph which has produced the event
    function paragraphEvent(paragraphID) {
      if (!featureEnabled) return;
      var currentParagraph = document.getElementById(paragraphID + simplifyBoxIdSuffix);
      
      if ( currentParagraph === null) {
        currentParagraph = document.getElementById(paragraphID);
        var text = currentParagraph.textContent ? currentParagraph.textContent : currentParagraph.innerText;//IE uses innerText
        taeCORE.getInstance().simplifyText(paragraphID, text, showSimplificationBox);
      } else {
        hideSimplificationBox(paragraphID);
      }
    }
    

    // It creates the HTML content of a complex word
    // Used by createSimplifiedTextHTML(...)
    // - item: the object wich contains the description passed as parameter
    function createSimplifiedWordLabel(item) {
      return '<span class="simp-word" ' +
                    'onclick="taeUI.getInstance().wordEvent(event, this)">' +
                     item.originalValue + 
              '</span>';
    }

    // It creates the HTML content of a simplified paraghaph
    // Used by getSimplifiedText(...)
    // - originalText: the original text contained in a paragraph
    // - simplifications: A list of simplified words of the text
    function createSimplifiedTextHTML(originalText, simplifications) {
      var result = originalText;
      var item = '';
      // for each simplified word add an element containing it
      for (var i = simplifications.length -1; i >= 0; i--) {
        item = simplifications[i];
        result = result.substring(0, item.start) + 
                      createSimplifiedWordLabel(item) + 
                        result.substring(item.end, result.length);
      }
      return result;
    }


    // Method used to cancel the propagation of the events
    // - event: the event to cancel
    function cancelEventPropagation(event) {
      event = event || window.event // cross-browser event
      if (event.stopPropagation) {
          event.stopPropagation(); // W3C standard variant
      } else {
          event.cancelBubble = true; // IE variant
      }
    }

    // Function called when an user clicks on a difficult word
    // It manages the event and shows the synonyms and definition of the 
    // selected word calling to showWordProperties(...) 
    // - event: the click event. It is cancelled
    // - wordHTMLelement:  the element that contains the word
    function wordEvent(event, wordHTMLelement) {
      cancelEventPropagation(event);
      showWordProperties(wordHTMLelement);
    }

    // Function called when an user clicks on a highlighted word
    // It shows synonyms and the definition of the word contailed by the 
    // HTML element passed as parameter
    // - wordHTMLelement: the element that contains the word
    function showWordProperties(wordHTMLelement) {
      var simplifiedBoxNode = document.getElementById(wordHTMLelement
                                                          .parentNode
                                                          .parentNode
                                                          .parentNode.id);
      var paragraphId = simplifiedBoxNode.parentNode.id;
      var currentBox = simplifiedBoxNode
                          .getElementsByClassName(wordPropertiesClassName)[0];

      // If the currentBox is not created, create and attach it
      if (currentBox == null) {
        currentBox = document.createElement('li');
        currentBox.className = wordPropertiesClassName;
        currentBox.setAttribute("onclick", 
          "taeUI.getInstance()." + 
          "wordPropertiesEvent(event,'" + paragraphId + "');");
        simplifiedBoxNode.getElementsByTagName('ul')[0].appendChild(currentBox);
      }
      // Get the synonyms and definition
      var definition = taeCORE.getInstance()
                  .termDefinition(paragraphId, wordHTMLelement.innerHTML);
      var synonyms = taeCORE.getInstance()
                  .termSynonyms(paragraphId, wordHTMLelement.innerHTML);
      
      // Update the content
      currentBox.innerHTML = '<b>' + wordHTMLelement.innerText + '</b></br>';
      if (definition != null) // If the word has definition show it
        currentBox.innerHTML += '<i>' + 'Definition:' + '</i>' 
                                + definition 
                                + '</br>';
      if (synonyms != null) // If the word has synonyms show them
        currentBox.innerHTML += '<i>' + 'Synonyms:' + '</i>' + synonyms;
    }

    // Function called when an user clicks on a WordProperties box
    // It hides the selected WordProperties box
    // - event: the click event. It is cancelled
    // - paragraphID:  the id paragraph that contains the WordProperties box
    function hideWordProperties(event, paragraphID) {
      cancelEventPropagation(event);
      var paragraphNode = document.getElementById(paragraphID);
      var currentBox = paragraphNode
                            .getElementsByClassName(wordPropertiesClassName)[0];
      if (currentBox != null) {
        currentBox.parentNode.removeChild(currentBox);
      }
    }



    // Draw the simplification box
    // - paragraphID: the id of the paragraph
    // - originalText: the original text contained in the paragraph
    // - response: the JSON Object of the questions related to the paragraph
    function showSimplificationBox(paragraphID, originalText, response) {
      // Create the Simplification Box div
      var questionsBox = document.createElement('div');
      questionsBox.id = paragraphID + simplifyBoxIdSuffix;
      questionsBox.className = simplifyBoxClassName;
      
      // 1. The title is attached 
      var questionsHtml = '<p>' + simplifyBoxTitle + '</p>';

      // 2. The simplification is attached
      questionsHtml += '<ul>';
      questionsHtml += '<li>' + createSimplifiedTextHTML(
                                      originalText,
                                      response.simplifications) + '</li>';
      questionsHtml += '</ul>';

      // 3. The Simplification Box div is attached to the corresponding paragraph
      questionsBox.innerHTML = questionsHtml;
      document.getElementById(paragraphID).appendChild(questionsBox);
    } //showSimplificationBox

    // Hide the simplification box attached to a paragraph passed as paramether
    // - paragraphID: the id of the paragraph
    function hideSimplificationBox(paragraphID) {
      var sBoxToRemove = document.getElementById(paragraphID + simplifyBoxIdSuffix);
      sBoxToRemove.parentNode.removeChild(sBoxToRemove);
    }

    return {
      // Public definitions
      init: initComponent, // Called only one time
      enable: enableComponentFeatures,  // Called when the Component button is enabled
      disable: disableComponentFeatures, // Called when the Component button is disabled or another one enabled
      isEnabled: function() { return featureEnabled;}, // Returns if the feature is enabled
      
      paragraphEvent: paragraphEvent,
      wordEvent: wordEvent,
      wordPropertiesEvent: hideWordProperties
    };
  }
  
  return {
    getInstance: function() {
      if(!instance) instance = Singleton();
      return instance;
    }
  };
})();