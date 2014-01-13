/*
 * Encoder.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Encoder {

	final List<Packet> packets;
	
	public Encoder(List<Packet> packets) {
		this.packets = packets;
	}
	
	public JSONResult jsonResult() {
		Map<String, Object> result = new HashMap<String, Object>();
		
		List<Map<String,String>> presences = new ArrayList<Map<String,String>>();
		List<Map<String,String>> messages = new ArrayList<Map<String,String>>();
		List<Map<String,String>> statuses = new ArrayList<Map<String,String>>();
		for(Packet packet : packets) {
			Map<String, String> data = new HashMap<String,String>();
			packet.feed(data);
			if(packet instanceof Presence) {
				presences.add(data);
			} else if(packet instanceof Message) {
				messages.add(data);
			} else if(packet instanceof Status) {
				statuses.add(data);
			}
		}
		result.put("status", "ok");
		result.put("messages", messages);
		result.put("presences", presences);
		result.put("statuses", statuses);
		return new JSONResult(result);
	}

}
