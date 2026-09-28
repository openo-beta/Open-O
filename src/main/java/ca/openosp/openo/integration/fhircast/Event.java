/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License. This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 * <p>
 * This software was written for the Department of Family Medicine McMaster University Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.integration.fhircast;

import org.codehaus.jettison.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event {

  private Date timestamp = null;
  private String id = null;
  private String hubTopic = null;
  private String hubEvent = null;
  private final List<JSONObject> contexts = new ArrayList<>();

  public Event() {
  }

  public Event(String id, String hubTopic, String hubEvent) {
    this.timestamp = new Date();
    this.id = id;
    this.hubTopic = hubTopic;
    this.hubEvent = hubEvent;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getHubTopic() {
    return hubTopic;
  }

  public void setHubTopic(String hubTopic) {
    this.hubTopic = hubTopic;
  }

  public String getHubEvent() {
    return hubEvent;
  }

  public void setHubEvent(String hubEvent) {
    this.hubEvent = hubEvent;
  }

  public void addContext(String key, String resource) throws Exception {
    JSONObject fhir = new JSONObject(resource);
    JSONObject context = new JSONObject();
    context.put("key", key);
    context.put("resource", fhir);
    this.contexts.add(context);
  }

  public String getFhirCastEvent() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    JSONObject json = new JSONObject();
    json.put("timestamp", sdf.format(timestamp));
    json.put("id", id);
    JSONObject event = new JSONObject();
    event.put("hub.topic", hubTopic);
    event.put("hub.event", hubEvent);
    event.put("context", contexts);
    json.put("event", event);
    return json.toString(3);
  }
}
