/*
 * Router.java
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <ul>
 * <li>Route packets</li>
 * <li>Subscribers</li>
 * </ul>
 * 
 * @author erylee
 * 
 */
public class Router {

	private Roster roster;

	private Map<EndOid, Endpoint> registry = new HashMap<EndOid, Endpoint>();

	// uid-1-*->ticket-1-1->queue
	private Map<EndOid, List<Subscriber>> routes = new HashMap<EndOid, List<Subscriber>>();

	public Router(Roster roster) {
		this.roster = roster;
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try{
					clean();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}, 10 * 1000, 8 * 1000);
	}

	public Roster roster() {
		return this.roster;
	}

	public Endpoint lookup(EndOid oid) {
		return registry.get(oid);
	}

	public Subscriber lookup(EndOid oid, Ticket ticket) {
		List<Subscriber> subscribers = routes.get(oid);
		if (subscribers != null) {
			for (Subscriber subscriber : subscribers) {
				if (subscriber.ticket.equals(ticket)) {
					return subscriber;
				}
			}
		}
		return null;
	}

	public void route(Ticket ticket, Packet packet) {
		if (packet instanceof Presence) {
			update((Presence) packet);
		}
        if(isGrpMessage(packet)) {
			List<Member> members = roster.members(packet.toOid);
			for (Member member : members) {
                if( !packet.fromOid.equals(member.userOid) ) { 
                    route(ticket, member.userOid, packet);
                }
			}
        } else if (isGrpPresence(packet)) {
			List<Member> members = roster.members(packet.toOid);
			for (Member member : members) {
                String type = ((Presence) packet).type;
                if( ("join".equals(type) || "leave".equals(type)) && 
                    packet.fromOid.equals(member.userOid) ) continue;
                route(ticket, member.userOid, packet);
            }
        
		} else {
			route(ticket, packet.toOid, packet);
		}
	}

	public void route(Ticket ticket, EndOid toOid, Packet packet) {
		List<Subscriber> subscribers = routes.get(toOid);
		if (subscribers != null) {
			for (Subscriber subscriber : subscribers) {
				synchronized (subscriber) {
					subscriber.queue.add(packet);
					if (subscriber.continuation != null) {
						if(subscriber.continuation.isSuspended()) {
							subscriber.continuation.resume();
						}
						subscriber.continuation = null;
						subscriber.lastActive = System.currentTimeMillis();
					}
				}
			}
		}
	}

	private boolean isGrpMessage(Packet packet) {
		if (packet instanceof Message) {
			return "grpchat".equals(((Message) packet).type);
		}
		return false;
	}
	
	private boolean isGrpPresence(Packet packet) {
		if (packet instanceof Presence) {
			String type = ((Presence) packet).type;
			return "join".equals(type) || "leave".equals(type) 
					|| "grponline".equals(type) || "grpoffline".equals(type);
		}
		return false;
	}


	private void update(Presence p) {
        if(!p.type.equals("show")) return;
        
		Endpoint ep = registry.get(p.fromOid);
		if (ep != null) {
			if (!ep.show.equals(p.show))
				ep.show = p.show;
			if (!ep.status.equals(p.status))
				ep.status = p.status;
		} else {
			System.err.println("NULL Endpoint when update presence: "
					+ p.fromOid.toString());
		}
	}

	public synchronized Ticket bind(Endpoint endpoint) {
		EndOid oid = endpoint.endOid;
		registry.put(oid, endpoint);
		roster.addBuddies(oid, endpoint.buddyOids);
		roster.joinGroups(oid, endpoint.roomOids);

		Ticket ticket = new Ticket(oid.clazz, oid.name);
		Subscriber subscriber = new Subscriber(ticket);
		List<Subscriber> subscribers = routes.get(oid);
		if (subscribers == null) {
			subscribers = new ArrayList<Subscriber>();
		}
		subscribers.add(subscriber);
		routes.put(oid, subscribers);

		for (EndOid buddyOid : endpoint.buddyOids) {
			Presence presence = new Presence("online", endpoint.endOid,
					buddyOid);
			presence.setShow(endpoint.show);
			presence.setNick(endpoint.nick);
			presence.setStatus(endpoint.status);
			route(ticket, presence);
		}
		
		//join group presence
		for(EndOid grpOid : endpoint.roomOids) {
			Presence p = new Presence("grponline", endpoint.endOid, grpOid);
			p.setNick(endpoint.nick);
			p.setShow("available");
			p.setStatus(grpOid.name);
			route(ticket, p);
		}
		return ticket;
	}

	public synchronized void unbind(EndOid oid, Ticket ticket) {
		List<Subscriber> subscribers = routes.get(oid);
		if (subscribers == null) {
			System.err.println("NULL Subscribers for: " + oid.toString());
			return;
		}
		for (Subscriber sub : subscribers) {
			if (sub.ticket.equals(ticket)) {
				if (sub.continuation != null) {
					sub.continuation.resume();
					sub.continuation = null;
				}
				subscribers.remove(sub);
				break;
			}
		}
		routes.put(oid, subscribers);
		if (subscribers.size() == 0) {
			Endpoint ep = registry.get(oid);
			if (ep == null) {
				System.err.println("Cannot find endpont: " + oid.toString());
				return;
			}
			ep.idle = true;
			ep.idleTime = System.currentTimeMillis();
		}
	}

	protected synchronized void clean() {
		long now = System.currentTimeMillis();
		
//		System.out.println("begin to clean: " + now);
		// clean subscribers
		Set<EndOid> keys = routes.keySet();
		for (EndOid key : keys) {
			List<Subscriber> subscribers = routes.get(key);
			Iterator<Subscriber> it = subscribers.iterator();
			while (it.hasNext()) {
				Subscriber sub = it.next();
				if ((sub.continuation == null || sub.continuation.isExpired())
						&& ((sub.lastActive + 8000) < now)) {
					System.out.println("Clean NoTimeout Sub: " + sub);
					it.remove();
				} else if((sub.lastActive + 28000) < now) {
                    System.out.println("Clean Timeout Sub: " + sub);
                    //TODO: when continuation cannot be expired
                    if (sub.continuation !=null && sub.continuation.isSuspended()) {
                        sub.continuation.resume();
                        sub.continuation = null;
                    }
                    it.remove();
                }
			}
			if (subscribers.size() == 0) {
				Endpoint ep = registry.get(key);
				if (ep != null && !ep.idle) {
					ep.idle = true;
					ep.idleTime = System.currentTimeMillis();
				}
			}
			routes.put(key, subscribers);
		}
		// clean endpoints
		Iterator<EndOid> keysIter = registry.keySet().iterator();
		
//		for (EndOid key : keys) {
		while (keysIter.hasNext()) {
			EndOid key = keysIter.next();
			Endpoint ep = registry.get(key);
			if (ep.idle && (ep.idleTime + 8000) < now) {
				//System.out.println("Clean Endpoint: " + key);
				// presence
				List<Buddy> buddies = roster.buddies(key);
				for (Buddy b : buddies) {
					Presence p = new Presence("offline", key, b.fid);
					p.setNick(ep.nick);
					p.setShow("unavailable");
					p.setStatus(ep.status);
					// TODO:
					route(null, p);
				}
				//leave group presences
				for(EndOid grpOid : roster.groups(key)) {
					Presence p = new Presence("grpoffline", ep.endOid, grpOid);
					p.setNick(ep.nick);
					p.setShow("unavailable");
					p.setStatus(grpOid.name);
					route(null, p);
				}
				// remove
//				registry.remove(key);
				keysIter.remove();
				routes.remove(key);
				roster.clean(key);
			}
		}

	}
	/**
	 * Broadcast Join Presence
	 * 
	 * @param grpOid
	 * @param userOid
	 */
	public void join(EndOid grpOid, EndOid userOid) {
		roster.join(grpOid, userOid);
		Presence p = new Presence("join", userOid, grpOid);
		Endpoint ep = lookup(userOid);
		if(ep != null) {
			p.setNick(ep.nick);
			p.setShow("available");
			p.setStatus(grpOid.name);
		}
		route(null, p);
	}
	
	public void leave(EndOid grpOid, EndOid userOid) {
		roster.leave(grpOid, userOid);
		Presence p = new Presence("leave", userOid, grpOid);
		Endpoint ep = lookup(userOid);
		if(ep != null) {
			p.setNick(ep.nick);
			p.setShow("available");
			p.setStatus(grpOid.name);
		}
		route(null, p);
	}

}
