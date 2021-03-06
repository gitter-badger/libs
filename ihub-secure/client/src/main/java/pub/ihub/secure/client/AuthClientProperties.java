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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

import static pub.ihub.secure.core.Constant.SECURE_CLIENT_PROPERTIES_PREFIX;

/**
 * 客户端服务配置属性
 *
 * @author liheng
 */
@Data
@ConfigurationProperties(SECURE_CLIENT_PROPERTIES_PREFIX)
public class AuthClientProperties {

	/**
	 * 客户端域名
	 */
	private String domain;

	/**
	 * 客户端密钥
	 */
	private String secret;

	/**
	 * 作用域
	 */
	private Set<String> scope;

}
