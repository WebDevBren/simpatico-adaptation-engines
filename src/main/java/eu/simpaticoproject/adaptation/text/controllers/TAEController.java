/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.simpaticoproject.adaptation.text.controllers;

import javax.naming.OperationNotSupportedException;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import eu.simpaticoproject.adaptation.text.Handler;
import eu.simpaticoproject.adaptation.text.tae.SimpaticoInput;
import eu.simpaticoproject.adaptation.text.tae.SimpaticoOutput;
import io.swagger.annotations.ApiOperation;

/**
 * @author raman
 *
 */
@Controller
public class TAEController {

	@Autowired
	private Handler handler;
	
	@RequestMapping(value = "/tae/simp", method = RequestMethod.GET)
	@ApiOperation(value = "Process text",
				  response = SimpaticoOutput.class,
				  notes = "Obtain text annotations and simplifications")
	public @ResponseBody String simp(
			@RequestParam(required = false) String word,
			@RequestParam(required = false) Integer position,
			@RequestParam(required = false) String lang,
			@RequestParam(required = false) String text) throws Exception {
		String json = handler.service(word, position, lang, text);
		return json;
//		return new ObjectMapper().readValue(json, SimpaticoOutput.class);
	}
	@RequestMapping(value = "/tae/simp", method = RequestMethod.POST)
	@ApiOperation(value = "Process text",
	  response = SimpaticoOutput.class,
	  notes = "Obtain text annotations and simplifications")
	public @ResponseBody SimpaticoOutput simp(@RequestBody SimpaticoInput input) throws Exception {
		String json = handler.service(input.getWord(), input.getPosition(), input.getLang(), input.getText());
		return new ObjectMapper().readValue(json, SimpaticoOutput.class);
	}

	@ExceptionHandler(OperationNotSupportedException.class)
	@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
	@ResponseBody
	public String handleNotSupportedError(HttpServletRequest request, Exception exception) {
		exception.printStackTrace();
		return "{\"error\":\"" + exception.getMessage() + "\"}";
	}
	
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public String handleError(HttpServletRequest request, Exception exception) {
		exception.printStackTrace();
		return "{\"error\":\"" + exception.getMessage() + "\"}";
	}
	
}
