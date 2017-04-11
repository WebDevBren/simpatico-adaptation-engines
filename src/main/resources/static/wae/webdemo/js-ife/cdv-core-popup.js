// Citizen Data Vault Core Client (cdv-core-popup.js)
//-----------------------------------------------------------------------------
// This JavaScript contains the client side of the CDV component
// related to form fill features. The main functionality is to
// create the calls to the server side of the CV instance
// - Used by cdv-ui-popup.js
// - The CDV server side code is available in:
//              https://github.com/SIMPATICOProject/CDV
//-----------------------------------------------------------------------------

var cdvCORE = (function () {
	var instance;
	function Singleton() {

		var endpoint = "http://localhost:8080";
		var serviceID = 2;
		var serviceURL = "http://localhost:8080/service2";
		var dataFields = [];
		var serviceLink = '';
		var username = '';
		/**
		 * INIT THE ENGINE CONFIG. PARAMETERS:
		 * - endpoint: URL OF THE CDV API
		 */
		function initComponent(parameters) {

			if (parameters.endpoint) {
				endpoint = parameters.endpoint;
			}
			if (parameters.serviceID) {
				serviceID = parameters.serviceID;
			}
			if (parameters.serviceURL) {
				serviceURL = parameters.serviceURL;
			}
			if (parameters.dataFields) {
				dataFields = parameters.dataFields;
			}

		}

		this.cdv_getdata = function (updatePDataFields, errorCallback) {
			var properties = {};
			console.log("SERVICEID:" + serviceID);
			var userData = JSON.parse(localStorage.userData || 'null');
			console.log("USERDATA:" + JSON.parse(localStorage.userData || 'null'));

			var url = endpoint + "/pdata-manager/api/v1/getPData";
			var data = JSON.parse(localStorage.userData || 'null');
			var tokenData = JSON.parse(localStorage.aacTokenData || 'null');
			console.log(tokenData);
			var pdata = new PData(data.userId, serviceLink);
			$.ajax({
				url: url,
				type: 'POST',
				data: pdata.toJsonString(),
				contentType: "application/json; charset=utf-8",
				dataType: 'json',
				success: (function (json) {
					console.log(JSON.stringify(json));
					updatePDataFields(json);

				}),
				error: function (jqxhr, textStatus, err) {
					console.log(textStatus + ", " + err);
					errorCallback("Errore nella comunicazione col server");
				},
				beforeSend: function (xhr) {
					xhr.setRequestHeader('Authorization', 'Bearer ' + tokenData.access_token);

				}

			});
		}

		this.cdv_postdata = function (callback) {

			var data = JSON.parse(localStorage.userData || 'null');
			var url = endpoint + "/pdata-manager/api/v1/postPData?mode=append";
			var tokenData = JSON.parse(localStorage.aacTokenData || 'null');
			console.log(tokenData);
			var pdata = formFieldsToJSON(serviceLink, data.userId, dataFields);

			$.ajax({
				url: url,
				type: 'POST',
				data: pdata,
				contentType: "application/json; charset=utf-8",
				success: function (resp) {
					console.log("pdata saved!");
					callback(true);

				},
				error: function (jqxhr, textStatus, err) {
					console.log(textStatus + ", " + err);
					callback(false);
				},
				beforeSend: function (xhr) {
					xhr.setRequestHeader('Authorization', 'Bearer ' + tokenData.access_token);

				}

			});

		}

		this.cdv_getSLink = function (callback) {

			var data = JSON.parse(localStorage.userData || 'null');
			var url = endpoint + "/account-manager/api/v1/users/" + data.userId + "/services/" + serviceID + "/serviceLink";
			var tokenData = JSON.parse(localStorage.aacTokenData || 'null');
			console.log(tokenData);
			var pdata = formFieldsToJSON(serviceLink, data.userId, dataFields);

			$.ajax({
				url: url,
				type: 'GET',
				contentType: "application/json; charset=utf-8",
				dataType: 'json',
				success: function (json) {
					console.log(json._id);
					serviceLink = json._id;
					callback(true, true);

				},
				error: function (jqxhr, textStatus, err) {
					console.log(textStatus + ", " + err);
					callback(true, false);

				},
				beforeSend: function (xhr) {
					xhr.setRequestHeader('Authorization', 'Bearer ' + tokenData.access_token);

				}

			});

		}

		this.cdv_getAccount = function (callback) {

			var data = JSON.parse(localStorage.userData || 'null');
			var url = endpoint + "/account-manager/api/v1/users/" + data.userId + "/serviceLink";
			var tokenData = JSON.parse(localStorage.aacTokenData || 'null');
			console.log(tokenData);
			var pdata = formFieldsToJSON(serviceLink, data.userId, dataFields);

			$.ajax({
				url: url,
				type: 'GET',
				contentType: "application/json; charset=utf-8",
				dataType: 'json',
				success: function (json) {
					console.log(json.username);
					username = json.username;
					callback(true);

				},
				error: function (jqxhr, textStatus, err) {
					console.log(textStatus + ", " + err);
					callback(false);

				},
				beforeSend: function (xhr) {
					xhr.setRequestHeader('Authorization', 'Bearer ' + tokenData.access_token);

				}

			});

		}

		this.cdv_createAccount = function (callback) {

			var data = JSON.parse(localStorage.userData || 'null');
			var url = endpoint + "/account-manager/api/v1/accounts";
			var tokenData = JSON.parse(localStorage.aacTokenData || 'null');
			console.log(tokenData);
			var account = accountToJSON(data.userId, data.name, data.surname);

			$.ajax({
				url: url,
				type: 'POST',
				data: account,
				contentType: "application/json; charset=utf-8",
				success: function (resp) {
					console.log("account created");
					username = resp.username;
					callback(true);

				},
				error: function (jqxhr, textStatus, err) {
					console.log(textStatus + ", " + err);
					callback(false);
				},
				beforeSend: function (xhr) {
					xhr.setRequestHeader('Authorization', 'Bearer ' + tokenData.access_token);

				}

			});

		}

		this.cdv_createSLR = function (callback) {

			var data = JSON.parse(localStorage.userData || 'null');
			var url = endpoint + "/account-manager/api/v1/accounts/" + username + "/serviceLinks";
			var tokenData = JSON.parse(localStorage.aacTokenData || 'null');
			console.log(tokenData);
			var slr = slrToJSON(data.userId, serviceID, serviceURL);

			$.ajax({
				url: url,
				type: 'POST',
				data: slr,
				contentType: "application/json; charset=utf-8",
				success: function (resp) {
					console.log("slr saved!");
					serviceLink = resp._id;
					callback(true, true);

				},
				error: function (jqxhr, textStatus, err) {
					console.log(textStatus + ", " + err);
					callback(false, false);
				},
				beforeSend: function (xhr) {
					xhr.setRequestHeader('Authorization', 'Bearer ' + tokenData.access_token);

				}

			});

		}

		this.cdv_exportData = function () {

			var dataUser = JSON.parse(localStorage.userData || 'null');
			var url = endpoint + "/pdata-manager/api/v1/pData/download?fileFormat=CSV";
			var tokenData = JSON.parse(localStorage.aacTokenData || 'null');
			console.log(tokenData);

			$.ajax({
				url: url,
				type: 'GET',
				contentType: "application/json; charset=utf-8",
				success: function (json) {
					console.log(json);
					var data = encodeURIComponent(json);


					$("<a />", {
						"download": "data.csv",
						"href": "data:application/json;charset=utf-8," + data
					}).appendTo("body")
					.click(function () {
						$(this).remove()
					})[0].click()
					
				},
				error: function (jqxhr, textStatus, err) {
					console.log(textStatus + ",	" + err);
					
				},
				beforeSend: function (xhr) {
					xhr.setRequestHeader('Authorization', 'Bearer ' + tokenData.access_token);
					xhr.setRequestHeader('accountId', username);

				}

			});

		}
		
		
		
		this.cdv_removeCDV = function () {

			var dataUser = JSON.parse(localStorage.userData || 'null');
			var url = endpoint + "/account-manager/api/v1/accounts/"+username;
			var tokenData = JSON.parse(localStorage.aacTokenData || 'null');
			console.log(tokenData);

			$.ajax({
				url: url,
				type: 'DELETE',
				contentType: "application/json; charset=utf-8",
				success: function (json) {
					console.log(json);
										
				},
				error: function (jqxhr, textStatus, err) {
					console.log(textStatus + ",	" + err);
					
				},
				beforeSend: function (xhr) {
					xhr.setRequestHeader('Authorization', 'Bearer ' + tokenData.access_token);
					
				}

			});

		}

		// pdata
		function PData(userId, slrId) {
			this.user_id = userId;
			this.slr_id = slrId;
			this.properties = [];
			this.toJsonString = function () {
				return JSON.stringify(this);
			};
		};

		// Helper function to serialize all the form fields into a JSON string
		function formFieldsToJSON(slrId, userId, fields) {
			var properties = [];
			var jsonStr = JSON.stringify({
					"slr_id": slrId,
					"user_id": userId,
					"properties": []
				});
			var obj = JSON.parse(jsonStr);
			var n = fields.length;
			for (var i = 0; i < n; i++) {
				
				var propertyField= fields[i];
					propertyField=propertyField.replace( /(:|\.|\[|\]|,|=|@)/g, "\\$1" );
				
				
				console.log(propertyField+"-"+$('#'+propertyField).val());
				
				if ($('#' + propertyField).val())
					obj['properties'].push({
						"key": fields[i],
						"values": [$('#' + propertyField).val()]
					});
			}
			jsonStr = JSON.stringify(obj);
			return jsonStr;
		}

		// Helper function to serialize all account fields into a JSON string
		function accountToJSON(userId, firstname, lastname) {
			var properties = [];
			var jsonStr = JSON.stringify({
					"username": firstname + "." + lastname + userId + serviceID
					
				});
			var partStr = JSON.stringify({
					"firstname": firstname,
					"lastname": lastname
				});
			var obj = JSON.parse(jsonStr);
			var part = JSON.parse(partStr);
			obj['particular'] = part;

			jsonStr = JSON.stringify(obj);
			return jsonStr;
		}

		// Helper function to serialize all slr fields into a JSON string
		function slrToJSON(userId, serviceId, serviceURL) {
			var properties = [];
			var jsonStr = JSON.stringify({
					"serviceId": serviceId,
					"serviceUri": serviceURL,
					"userId": userId
				});
			return jsonStr;
		}

		return {
			init: initComponent,
			cdv_getdata: cdv_getdata,
			cdv_postdata: cdv_postdata,
			initializeSLR: cdv_getSLink,
			initializeAccount: cdv_getAccount,
			createSLR: cdv_createSLR,
			createAccount: cdv_createAccount,
			exportData: cdv_exportData,
			removeCDV: cdv_removeCDV
		};

	}

	return {
		getInstance: function () {
			if (!instance)
				instance = Singleton();
			return instance;
		}
	};

})();

function setFieldValue(target, value) {
	$('#' + target).val(value).focus();
	$('#' + target).css({
		'border-width': '2px'
	});
}

function openCDV() {
	window.open("https://simpatico.eng.it/CDV/ng-admin/index.html#/dashboard", "_blank")
}
