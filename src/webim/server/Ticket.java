/*
 * Ticket.java
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

public class Ticket {

	final String clazz;

	final String name;
	
	final String token;
	
	public Ticket(String clazz, String name) {
		this(clazz, name, String.valueOf(System.currentTimeMillis()));
	}

	public Ticket(String clazz, String name, String token) {
		this.clazz = clazz;
		this.name = name;
		this.token = token;
	}
	
	public static Ticket parse(String string) {
		String[] parts = string.split(":");
		return new Ticket(parts[0], parts[1], parts[2]);
	}

	public boolean equals(Object o) {
		if(o instanceof Ticket) {
			return ((Ticket)o).toString().equals(this.toString());
		}
		return false;
	}

	public String toString() {
		return clazz + ":" + name + ":" + token;
	}

	public int hashCode() {
		return this.toString().hashCode();
	}
	
}
