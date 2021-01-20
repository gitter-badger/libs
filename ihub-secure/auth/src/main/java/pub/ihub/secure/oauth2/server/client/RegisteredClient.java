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

package pub.ihub.secure.oauth2.server.client;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.io.Serializable;
import java.time.Duration;
import java.util.Set;

import static pub.ihub.core.IHubLibsVersion.SERIAL_VERSION_UID;

/**
 * OAuth 2.0授权服务器注册客户端
 *
 * @author henry
 */
@Getter
@Setter
@ToString
public class RegisteredClient implements Serializable {

	private static final long serialVersionUID = SERIAL_VERSION_UID;
	private String id;
	private String clientId;
	private String clientSecret;
	private Set<ClientAuthenticationMethod> clientAuthenticationMethods;
	private Set<AuthorizationGrantType> authorizationGrantTypes;
	private Set<String> redirectUris;
	private Set<String> scopes;

	/**
	 * 需要验证密钥
	 */
	private boolean requireProofKey = false;
	/**
	 * 需要用户确认授权
	 */
	private boolean requireUserConsent = false;

	/**
	 * 认证令牌时效（秒）
	 */
	private Duration accessTokenTimeToLive = Duration.ofMinutes(5);
	/**
	 * 重复使用刷新令牌
	 */
	private boolean reuseRefreshTokens = true;
	/**
	 * 刷新令牌时效（秒）
	 */
	private Duration refreshTokenTimeToLive = Duration.ofMinutes(60);

}
