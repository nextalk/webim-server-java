/*
 * Member.java
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

public class Member {

	final EndOid grpOid;

	final EndOid userOid;

	public Member(EndOid grpOid, EndOid userOid) {
		this.grpOid = grpOid;
		this.userOid = userOid;
	}

	public boolean equals(Object o) {
		if (o instanceof Member) {
			Member m = (Member) o;
			return m.grpOid.equals(grpOid) && m.userOid.equals(userOid);
		}
		return false;
	}

	public int hashCode() {
		return grpOid.hashCode() * 37 + userOid.hashCode();
	}

	public String toString() {
		return String.format("Member(grpOid=%s, userOid=%s)", grpOid, userOid);
	}

}
