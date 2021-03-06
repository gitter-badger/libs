/*
 * Copyright (c) 2021 Henry 李恒 (henry.box@outlook.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pub.ihub.secure.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR_DESCRIPTION;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR_URI;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.GRANT_TYPE;

/**
 * @author liheng
 */
@Controller
public class AuthorizationController {

	@GetMapping(value = "/authorize", params = GRANT_TYPE + "=authorization_code")
	public String authorizationCodeGrant(Model model) {
		return "redirect:/index";
	}

	@GetMapping(value = "/authorized", params = ERROR)
	public Map<String, Object> authorizationFailed(@RequestParam(ERROR) String error,
												   @RequestParam(ERROR_DESCRIPTION) String errorDescription,
												   @RequestParam(ERROR_URI) String errorUri) {
		Map<String, Object> result = new HashMap<>(3);
		result.put("errorCode", error);
		result.put("description", errorDescription);
		result.put("uri", errorUri);
		return result;
	}

	@GetMapping(value = "/login/oauth2/code/{registrationId}", params = ERROR)
	public Map<String, Object> authorizationFailed(@PathVariable("registrationId") String registrationId) {
		Map<String, Object> result = new HashMap<>(3);
		result.put("registrationId", registrationId);
		return result;
	}

	@GetMapping(value = "/authorize", params = GRANT_TYPE + "=client_credentials")
	public String clientCredentialsGrant() {
		return "redirect:/index";
	}

}
