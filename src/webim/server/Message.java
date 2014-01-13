/*
 * Message.java
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

public class Message extends Packet {

	String nick;
	String type;
	String body;
	String style = "";
	String timestamp;

	public Message(EndOid fromOid, EndOid toOid, String nick, String type,
			String body, String timestamp) {
		super(fromOid, toOid);
		this.setNick(nick);
		this.setType(type);
		this.setBody(body);
		this.setTimestamp(timestamp);
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void feed(Map<String, String> data) {
		data.put("from", fromOid.name);
		data.put("to", toOid.name);
		data.put("nick", nick);
		data.put("timestamp", timestamp);
		data.put("type", type);
		data.put("body", body);
		data.put("style", style);
	}

	public void setStyle(String style) {
		this.style = style;
	}

}
