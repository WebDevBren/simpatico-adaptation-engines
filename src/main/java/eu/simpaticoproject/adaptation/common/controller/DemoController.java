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
package eu.simpaticoproject.adaptation.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import springfox.documentation.annotations.ApiIgnore;

/**
 * @author raman
 *
 */
@ApiIgnore
@Controller
public class DemoController {

	@RequestMapping({"/tae/webdemo", "/tae"})
	public String taeDashboard() {
		return "redirect:/tae/webdemo/index.html";
	}

	@RequestMapping({"/wae/webdemo", "/wae"})
	public String waeDemo() {
		return "redirect:/wae/webdemo/wae-test.html";
	}

	@RequestMapping("/wae/webdemo/logincallback")
	public String login() {
		return "redirect:/wae/webdemo/logincb.html";
	}
}
