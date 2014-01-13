/*
 * Buddy.java
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

public class Buddy {

	final EndOid oid;
	
	final EndOid fid;

	public Buddy(EndOid oid, EndOid fid) {
		this.oid = oid;
		this.fid = fid;
	}
	
	public boolean equals(Object o) {
		if(o instanceof Buddy) {
			Buddy b = (Buddy)o;
			return (b.oid.equals(this.oid) && b.fid.equals(this.fid));
		}
		return false;
	}

	public int hashCode() {
		return oid.hashCode() * 37 + fid.hashCode();
	}
	
	public String toString() {
		return String.format("Buddy(oid=%s, fid=%s", oid.name, fid.name);
	}

}
