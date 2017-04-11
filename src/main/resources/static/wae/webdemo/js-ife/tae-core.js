
var taeCORE = ( function () {
  var instance;
  function Singleton () {
    // Variables to store the used API and URL paths
    var simplifyTextURL = '';
    var language = '';

    // Structure to store the simplified words of each paragraph
    // It has the folloging format:/*
    //  {
    //    "paragraph1": {
    //      "w1": {
    //        "synonyms": ["s1", "s2"],
    //        "definition": "ladefinicion"
    //      },
    //      "w2": {
    //        "synonyms": ["s1", "s2"],
    //        "definition": "ladefinicion"
    //      }
    //    },
    //    "paragraph2": {
    //      "w1": {
    //        "synonyms": ["s1", "s2"],
    //        "definition": "ladefinicion"
    //      },
    //      "w2": {
    //        "synonyms": ["s1", "s2"],
    //        "definition": "ladefinicion"
    //      }
    //    }
    //  }
    var words = {};
    var definitionsKey = 'definition';
    var synonymsKey = 'synonyms';

    //In inits the main used variables (e.g. The URLs used)
    function initComponent(parameters) {
      simplifyTextURL = parameters.endpoint + '/simp';
      language = parameters.language;
    }

    // Calls the TAE for the simplified text
    function getSimplifiedText(name, originalText, simplifyCallback) {
      // As we don't have a TAE working environment, we emulate the original 
      // text and the json response
      // When we have the TAE working we will change it

      // TODO: REMOVE [START]
      originalText = 'Ai sensi del regolamento per la disciplina del ' +
                      'sistema dei servizi socio educativi per la prima ' +
                      'infanzia approvato con deliberazione del Consiglio comunale'
      jQuery.getJSON('https://simpatico.morelab.deusto.es/replica/demo/tae-demo-resp.json',
        function(jsonResponse) {
          storeWords(name, jsonResponse);
          simplifyCallback(name, originalText, jsonResponse);
        }
      );
      // TODO: REMOVE [END]
      // TODO: UNCOMMENT [BEGIN]
      /*
      jQuery.getJSON(simplifyTextURL + '?' +
                            'text="' + originalText +
                            '"&lang="' + language +
                            '"',
        function(jsonResponse) {
          storeWords(name, jsonResponse);
          simplifyCallback(name, originalText, jsonResponse);
        }
      );//*/
      // TODO: UNCOMMENT [END]
    }

    // Given a jsonResponse, it stores/or updates new details of each 
    // simplification requested to the TAE server
    function storeWords(paragraphID, jsonResponse) {
      var wordsContainer = {};
      var newWord = {};
      var formsObject;
      for (var i = jsonResponse.simplifications.length -1; i >= 0; i--) {
        newWord[synonymsKey] = jsonResponse.simplifications[i].simplification;
        formsObject = jsonResponse.readability.forms[jsonResponse.simplifications[i].start.toString()];
        if (formsObject != null) {
          newWord[definitionsKey] = formsObject["description"].description;
        }
        wordsContainer[jsonResponse.simplifications[i].originalValue] = newWord;
        newWord = {}
      }
      words[paragraphID] = wordsContainer;
    }

    // It returns the definition of the word passed as parameter caontained 
    // in the paragraph passed as parameter
    // - paragraphID: the id of the paragraph
    // - term: the word which is going to be defined
    function getTermDefinition(paragraphID, term) {
      return words[paragraphID][term][definitionsKey];
    }

    // It returns the synonyms of the word passed as parameter caontained 
    // in the paragraph passed as parameter
    // - paragraphID: the id of the paragraph
    // - term: the word which has synonyms 
    function getTermSynonyms(paragraphID, term) {
      return words[paragraphID][term][synonymsKey];
    }

    return {
        init: initComponent,
        simplifyText: getSimplifiedText,
        termDefinition: getTermDefinition,
        termSynonyms: getTermSynonyms
      };
  }  
  return {
    getInstance: function() {
      if(!instance) instance = Singleton();
      return instance;
    }
  };
})();