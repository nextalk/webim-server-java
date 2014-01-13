/*
 * Config.java
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package webim.server;

import java.util.Map;

public class Config {

	String server;
	String port;
	String domain;
	String apikey;

	public Config(Map<String, String> map) {
		String s = map.get("webim.server");
		if (s != null)
			server = s;
		s = map.get("webim.port");
		if (s != null)
			port = s;
		s = map.get("webim.domain");
		if (s != null)
			domain = s;
		s = map.get("webim.apikey");
		if (s != null)
			apikey = s;
	}

	public String url() {
		return String.format("http://%s:%s", server, port);
	}
}
