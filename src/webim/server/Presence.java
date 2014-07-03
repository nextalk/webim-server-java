/*
 * Presence.java
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

public class Presence extends Packet {

	final String type;

	String nick;

	String show;

	String status = "";

	public Presence(String type, EndOid fromOid, EndOid toOid) {
		super(fromOid, toOid);
		this.type = type;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public void setShow(String show) {
		this.show = show;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public void feed(Map<String, String> data) {
		data.put("from", fromOid.name);
		data.put("to", toOid.name);
		data.put("nick", nick);
		data.put("type", type);
		data.put("show", show);
		data.put("status", status);
	}

}
