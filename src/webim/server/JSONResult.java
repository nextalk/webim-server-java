/*
 * JSONResult.java
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

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONResult {

	public static final JSONResult SUCCESS = new JSONResult("{\"status\": \"ok\"}");

	private String data;

	public JSONResult(Map map) {
		this.data = new JSONObject(map).toString();
	}

	public JSONResult(Collection list) {
		this.data = new JSONArray(list).toString();
	}

	public JSONResult(String s) {
		this.data = s;
	}

	public JSONResult(Object o) {
		this.data = new JSONObject(o).toString();
	}

	public byte[] toJSON() {
		try {
			return data.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "".getBytes();
		}
	}

	public String toString() {
		return data;
	}

}
