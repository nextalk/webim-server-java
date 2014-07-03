/*
 * HttpHandler.java
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class HttpHandler extends AbstractHandler {

	private Config config;

	private Router router;

	public HttpHandler(Config config, Router router) {
		this.config = config;
		this.router = router;
	}

	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		System.out.println(target);
		Enumeration<String> e = request.getParameterNames();
		while (e.hasMoreElements()){
			String key = e.nextElement();
			System.out.println(key+" = "+request.getParameter(key));
		}
		if (!target.equals("/v5/packets")) {
			String domain = g(request, "domain");
			String apikey = g(request, "apikey");
			if (!auth(domain, apikey)) {
				jsonReturn(
						new JSONResult(
								"{\"status\": \"error\", \"message\": \"authentication failure\"}"),
						response, HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}
		if (target.equals("/v5/presences/online")) {
			handleOnline(baseRequest, request, response);
		} else if (target.equals("/v5/presences/offline")) {
			handleOffline(baseRequest, request, response);
		} else if (target.equals("/v5/presences/show")) {
			handleShow(baseRequest, request, response);
		} else if (target.equals("/v5/presences")) {
			handlePresences(baseRequest, request, response);
		} else if (target.equals("/v5/statuses")) {
			handleStatuses(baseRequest, request, response);
		} else if (target.equals("/v5/messages")) {
			handleMessages(baseRequest, request, response);
		} else if (target.equals("/v5/packets")) {
			handlePackets(baseRequest, request, response);
		} else if ( target.startsWith("/v5/rooms") && target.endsWith("members") ) {
			handleRoomMembers(baseRequest, request, response);
		} else if ( target.startsWith("/v5/rooms") && target.endsWith("join") ) {
			handleRoomJoin(baseRequest, request, response);
		} else if (  target.startsWith("/v5/rooms") && target.endsWith("leave") ) {
			handleRoomLeave(baseRequest, request, response);
		} else {
			jsonReturn(new JSONResult(
					"{\"status\": \"error\", \"message\": \"Bad Request\"}"),
					response, HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	private boolean auth(String domain, String apikey) {
		if (domain == null || apikey == null) {
			return false;
		}
		return domain.equals(config.domain) && apikey.equals(config.apikey);
	}

	/**
	 * 
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void handleOnline(Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		// parameter
		String domain = g(request, "domain");
		String nick = g(request, "nick");
		String name = g(request, "name");
		String rooms = g(request, "rooms");
		String buddies = g(request, "buddies");
		String show = g(request, "show");
		if (show == null)
			show = "available";
		String status = g(request, "status");
		if (status == null)
			status = "";

		// oids
		EndOid userOid = new EndOid(domain, "uid", name);
		Set<EndOid> buddyOids = new HashSet<EndOid>();
		if (!buddies.trim().isEmpty()) {
			for (String id : buddies.split(",")) {
				buddyOids.add(new EndOid(domain, "uid", id));
			}
		}
		Set<EndOid> roomOids = new HashSet<EndOid>();
		if (!rooms.trim().isEmpty()) {
			for (String id : rooms.split(",")) {
				roomOids.add(new EndOid(domain, "gid", id));
			}
		}
		Endpoint endpoint = new Endpoint(userOid, nick);
		endpoint.setBuddies(buddyOids);
		endpoint.setRooms(roomOids);
		endpoint.setShow(show);
		endpoint.setStatus(status);

		Ticket ticket = router.bind(endpoint);

		Map<String, String> presences = new HashMap<String, String>();
		for (EndOid oid : buddyOids) {
			Endpoint ep = router.lookup(oid);
			if (ep != null) {
				presences.put(oid.name, ep.show);
			}
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("success", true);
		data.put("ticket", ticket.toString());
		data.put("server", config.url() + "/v5/packets");
		data.put("jsonpd", config.url() + "/v5/packets");
		data.put("presences", presences);
		jsonReturn(new JSONResult(data), response);
	}

	/**
	 * 
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void handlePresences(Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String domain = g(request, "domain");
		String ids = g(request, "ids");
		Map<String, String> presences = new HashMap<String, String>();
		for (String id : ids.split(",")) {
			EndOid oid = new EndOid(domain, "uid", id);
			Endpoint ep = router.lookup(oid);
			if (ep != null) {
				presences.put(id, ep.show);
			}
		}
		jsonReturn(new JSONResult(presences), response);
	}

	/**
	 * 
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void handleOffline(Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String domain = g(request, "domain");
		Ticket ticket = Ticket.parse(g(request, "ticket"));
		EndOid useroid = makeoid(domain, ticket);
		this.router.unbind(useroid, ticket);
		jsonReturn(JSONResult.SUCCESS, response);
	}

	private void handleRoomMembers(Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String domain = g(request, "domain");
		Ticket ticket = Ticket.parse(g(request, "ticket"));
		EndOid userOid = makeoid(domain, ticket);
		if (router.lookup(userOid) != null) {
			String gid = g(request, "room");
			EndOid grpOid = new EndOid(domain, "gid", gid);
			List<Member> members = router.roster().members(grpOid);
			Map<String, String> result = new HashMap<String, String>();
			for (Member m : members) {
				Endpoint ep = router.lookup(m.userOid);
				if (ep != null) {
					result.put(ep.endOid.name, ep.show);
				}
			}
			jsonReturn(new JSONResult(result), response);
			return;
		}
		jsonReturn(new JSONResult(
				"{\"status\": \"error\", \"message\": \"client not found\"}"),
				response, HttpServletResponse.SC_BAD_REQUEST);
	}

	/**
	 * TODO: 离开群组
	 * 
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void handleRoomLeave(Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String domain = request.getParameter("domain");
		Ticket ticket = Ticket.parse(request.getParameter("ticket"));
		EndOid useroid = makeoid(domain, ticket);
		String gid = g(request, "room");
		EndOid grpOid = new EndOid(domain, "gid", gid);
		router.leave(grpOid, useroid);
		jsonReturn(JSONResult.SUCCESS, response);
	}

	/**
	 * TODO: 
	 * 
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void handleRoomJoin(Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String domain = request.getParameter("domain");
		Ticket ticket = Ticket.parse(request.getParameter("ticket"));
		EndOid useroid = makeoid(domain, ticket);
		String gid = g(request, "room");
		// String nick = g(request, "nick");
		EndOid grpOid = new EndOid(domain, "gid", gid);
		router.join(grpOid, useroid);
		jsonReturn(JSONResult.SUCCESS, response);
	}

	private void handlePackets(Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String domain = request.getParameter("domain");
		Ticket ticket = Ticket.parse(request.getParameter("ticket"));
		EndOid useroid = makeoid(domain, ticket);
		String callback = g(request, "callback");

		Subscriber subscriber = router.lookup(useroid, ticket);
		if (subscriber == null) {
			jsonReturn(
					new JSONResult(
							callback
									+ "({\"status\": \"error\", \"message\": \"subscriber not found\"})"),
					response, HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		synchronized (subscriber) {
			if (subscriber.queue.size() > 0) {
				List<Packet> packets = new ArrayList<Packet>();
				Packet packet = null;
				while ((packet = subscriber.queue.poll()) != null) {
					packets.add(packet);
				}
				JSONResult result = new Encoder(packets).jsonResult();
				// Send one chat message
				response.setContentType("application/javascript;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getOutputStream().write((callback + "(").getBytes());
				response.getOutputStream().write(result.toJSON());
				response.getOutputStream().write(new byte[] { ')' });
				response.getOutputStream().flush();
			} else {
				Continuation continuation = ContinuationSupport
						.getContinuation(request);
				if (continuation.isInitial()) {
					// No chat in queue, so suspend and wait for timeout or chat
					continuation.setTimeout(20000);
					continuation.suspend();
					subscriber.continuation = continuation;
				} else {
					// Timeout so send empty response
					response.setContentType("application/javascript;charset=utf-8");
					response.setStatus(HttpServletResponse.SC_OK);
					response.getWriter().print(
							callback + "({\"status\": \"ok\"})");
					response.getWriter().flush();
				}
			}
			subscriber.lastActive = System.currentTimeMillis();

		}
	}

	/**
	 * 
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void handleMessages(Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String domain = request.getParameter("domain");
		Ticket ticket = Ticket.parse(request.getParameter("ticket"));
		EndOid userOid = makeoid(domain, ticket);

		String to = request.getParameter("to");
		String type = request.getParameter("type");
		if (type == null)
			type = "chat";
		EndOid toOid = new EndOid(domain, "uid", to);
		if ("grpchat".equals(type)) {
			toOid = new EndOid(domain, "gid", to);
		}

		String body = g(request, "body");
		String nick = g(request, "nick");
		String style = g(request, "style");
		if (style == null)
			style = "";
		String timestamp = g(request, "timestamp");

		Message message = new Message(userOid, toOid, nick, type, body,
				timestamp);
		message.setStyle(style);
		router.route(ticket, message);
		jsonReturn(JSONResult.SUCCESS, response);
	}

	/**
	 * 
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void handleStatuses(Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String domain = request.getParameter("domain");
		Ticket ticket = Ticket.parse(request.getParameter("ticket"));
		EndOid userOid = makeoid(domain, ticket);

		EndOid toOid = new EndOid(domain, "uid", request.getParameter("to"));
		String nick = request.getParameter("nick");
		String show = request.getParameter("show");

		Status status = new Status(userOid, toOid, nick, show);
		router.route(ticket, status);
		jsonReturn(JSONResult.SUCCESS, response);
	}

	/**
	 * 
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void handleShow(Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String domain = g(request, "domain");
		Ticket ticket = Ticket.parse(request.getParameter("ticket"));
		EndOid userOid = makeoid(domain, ticket);
		String nick = request.getParameter("nick");
		String show = request.getParameter("show");
		if (show == null)
			show = "available";
		String status = request.getParameter("status");
		if (status == null)
			status = "";
		List<Buddy> buddies = router.roster().buddies(userOid);
		for (Buddy b : buddies) {
			Presence presence = new Presence("show", userOid, b.fid);
			presence.setNick(nick);
			presence.setShow(show);
			presence.setStatus(status);
			router.route(ticket, presence);
		}
		jsonReturn(JSONResult.SUCCESS, response);
	}

	private EndOid makeoid(String domain, Ticket ticket) {
		return new EndOid(domain, ticket.clazz, ticket.name);
	}

	private void jsonReturn(JSONResult result, HttpServletResponse response)
			throws IOException {
		jsonReturn(result, response, HttpServletResponse.SC_OK);
	}

	private void jsonReturn(JSONResult result, HttpServletResponse response,
			int status) throws IOException {
//		response.addHeader("Content-Type", "application/json;");
		response.setContentType("application/json;charset=utf-8");
		response.setStatus(status);
		response.getOutputStream().write(result.toJSON());
		response.getOutputStream().flush();
	}

	private String g(HttpServletRequest r, String k) {
		return r.getParameter(k);
	}

}