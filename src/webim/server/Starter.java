/*
 * Starter.java
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;

public class Starter {

	public static void main(String[] args) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		for (String arg : args) {
			if(arg.startsWith("webim.")) {
				String[] kv = arg.split("=");
				map.put(kv[0], kv[1]);
			}
		}
		Config config = new Config(map);

		Router router = new Router(new Roster());

		HttpHandler handler = new HttpHandler(config, router);

		Server server = new Server(8000);

		server.setHandler(handler);

		server.start();

		server.join();
	}

}
