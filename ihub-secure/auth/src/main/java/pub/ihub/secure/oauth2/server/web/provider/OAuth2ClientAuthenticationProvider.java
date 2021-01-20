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

package pub.ihub.secure.oauth2.server.web.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.RegisteredClientRepository;
import pub.ihub.secure.oauth2.server.TokenType;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.web.token.OAuth2ClientAuthenticationToken;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

/**
 * 用于验证OAuth 2.0客户端的AuthenticationProvider实现。
 *
 * @author henry
 */
@RequiredArgsConstructor
public class OAuth2ClientAuthenticationProvider implements AuthenticationProvider {

	private final RegisteredClientRepository registeredClientRepository;
	private final OAuth2AuthorizationService authorizationService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		OAuth2ClientAuthenticationToken clientAuthentication =
			(OAuth2ClientAuthenticationToken) authentication;

		String clientId = clientAuthentication.getPrincipal().toString();
		RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
		if (registeredClient == null) {
			throwInvalidClient();
		}

		if (!registeredClient.getClientAuthenticationMethods().contains(
			clientAuthentication.getClientAuthenticationMethod())) {
			throwInvalidClient();
		}

		boolean authenticatedCredentials = false;

		if (clientAuthentication.getCredentials() != null) {
			String clientSecret = clientAuthentication.getCredentials().toString();
			// TODO Use PasswordEncoder.matches()
			if (!registeredClient.getClientSecret().equals(clientSecret)) {
				throwInvalidClient();
			}
			authenticatedCredentials = true;
		}

		authenticatedCredentials = authenticatedCredentials ||
			authenticatePkceIfAvailable(clientAuthentication, registeredClient);
		if (!authenticatedCredentials) {
			throwInvalidClient();
		}

		return new OAuth2ClientAuthenticationToken(registeredClient);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication);
	}

	private boolean authenticatePkceIfAvailable(OAuth2ClientAuthenticationToken clientAuthentication,
												RegisteredClient registeredClient) {

		Map<String, Object> parameters = clientAuthentication.getAdditionalParameters();
		if (CollectionUtils.isEmpty(parameters) || !authorizationCodeGrant(parameters)) {
			return false;
		}

		OAuth2Authorization authorization = this.authorizationService.findByToken(
			(String) parameters.get(OAuth2ParameterNames.CODE),
			TokenType.AUTHORIZATION_CODE);
		if (authorization == null) {
			throwInvalidClient();
		}

		OAuth2AuthorizationRequest authorizationRequest = authorization.getAttribute(
			OAuth2Authorization.AUTHORIZATION_REQUEST);

		String codeChallenge = (String) authorizationRequest.getAdditionalParameters()
			.get(PkceParameterNames.CODE_CHALLENGE);
		if (!StringUtils.hasText(codeChallenge) && registeredClient.isRequireProofKey()) {
			throwInvalidClient();
		}

		String codeChallengeMethod = (String) authorizationRequest.getAdditionalParameters()
			.get(PkceParameterNames.CODE_CHALLENGE_METHOD);
		String codeVerifier = (String) parameters.get(PkceParameterNames.CODE_VERIFIER);
		if (!codeVerifierValid(codeVerifier, codeChallenge, codeChallengeMethod)) {
			throwInvalidClient();
		}

		return true;
	}

	private static boolean authorizationCodeGrant(Map<String, Object> parameters) {
		return AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(
			parameters.get(OAuth2ParameterNames.GRANT_TYPE)) &&
			parameters.get(OAuth2ParameterNames.CODE) != null;
	}

	private static boolean codeVerifierValid(String codeVerifier, String codeChallenge, String codeChallengeMethod) {
		if (!StringUtils.hasText(codeVerifier)) {
			return false;
		} else if (!StringUtils.hasText(codeChallengeMethod) || "plain".equals(codeChallengeMethod)) {
			return codeVerifier.equals(codeChallenge);
		} else if ("S256".equals(codeChallengeMethod)) {
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
				String encodedVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
				return encodedVerifier.equals(codeChallenge);
			} catch (NoSuchAlgorithmException ex) {
				// It is unlikely that SHA-256 is not available on the server. If it is not available,
				// there will likely be bigger issues as well. We default to SERVER_ERROR.
			}
		}
		throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR));
	}

	private static void throwInvalidClient() {
		throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
	}

}
