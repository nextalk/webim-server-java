/*
 * Roster.java
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Roster {

	private Set<Buddy> buddies = new HashSet<Buddy>();

	private Set<Member> members = new HashSet<Member>();

	public synchronized List<Member> members(EndOid grpOid) {
		List<Member> list = new ArrayList<Member>();
		for (Member m : members) {
			if (m.grpOid.equals(grpOid))
				list.add(m);
		}
		return list;
	}

	public synchronized void join(EndOid grpOid, EndOid userOid) {
		members.add(new Member(grpOid, userOid));
	}

	public synchronized void addBuddies(EndOid oid, Set<EndOid> bOids) {
		for (EndOid bOid : bOids) {
			buddies.add(new Buddy(oid, bOid));
			buddies.add(new Buddy(bOid, oid));
		}
	}

	public synchronized void joinGroups(EndOid oid, Set<EndOid> groups) {
		for (EndOid grpOid : groups) {
			members.add(new Member(grpOid, oid));
		}
	}

	public synchronized List<Buddy> buddies(EndOid userOid) {
		List<Buddy> rtBuddies = new ArrayList<Buddy>();
		for (Buddy b : buddies) {
			if (b.oid.equals(userOid)) {
				rtBuddies.add(b);
			}
		}
		return rtBuddies;
	}

	public synchronized void clean(EndOid oid) {
		Iterator<Buddy> it = buddies.iterator();
		while (it.hasNext()) {
			Buddy b = it.next();
			if (b.oid.equals(oid) || b.fid.equals(oid)) {
				it.remove();
			}
		}

		Iterator<Member> it1 = members.iterator();
		while (it1.hasNext()) {
			Member m = it1.next();
			if (m.userOid.equals(oid)) {
				it1.remove();
			}
		}

	}

}
