/*
 * Endpoint.java
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

import java.util.Set;

public class Endpoint {
	
	final EndOid endOid;
	String status;
	String nick;
	Set<EndOid> buddyOids;
	Set<EndOid> groupOids;
	String show;

	/**
	 * idle？
	 */
	boolean idle = false;
	long idleTime = -1;
	
	public Endpoint(EndOid endOid, String nick) {
		this.endOid = endOid;
		this.nick = nick;
	}

	public void setBuddies(Set<EndOid> buddyOids) {
		this.buddyOids = buddyOids;
	}

	public void setGroups(Set<EndOid> groupOids) {
		this.groupOids = groupOids;
	}

	public void setShow(String show) {
		this.show = show;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
