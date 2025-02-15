package models;

import java.time.LocalDateTime;

public class Session {
	private String id;
	private String agent,location,ipaddress;
	private LocalDateTime startedTime;
	public Session(String id, String agent, String location, String ipaddress, LocalDateTime startedTime) {

		this.id = id;
		this.agent = agent;
		this.location = location;
		this.ipaddress = ipaddress;
		this.startedTime = startedTime;
	}
	public Session(String agent, String location, String ipaddress, LocalDateTime startedTime) {

		this.agent = agent;
		this.location = location;
		this.ipaddress = ipaddress;
		this.startedTime = startedTime;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getIpaddress() {
		return ipaddress;
	}
	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}
	public LocalDateTime getStartedTime() {
		return startedTime;
	}
	public void setStartedTime(LocalDateTime startedTime) {
		this.startedTime = startedTime;
	}
	
	
}
