package models;

import java.time.LocalDateTime;

public class User {
	private int id;
	private String username;
	private String emailaddress;
	private String password;
	private String profile_url;
	private LocalDateTime createdAt;
	public User(int id, String username, String emailaddress, String password, String profile_url,
			LocalDateTime createdAt) {
		super();
		this.id = id;
		this.username = username;
		this.emailaddress = emailaddress;
		this.password = password;
		this.profile_url = profile_url;
		this.createdAt = createdAt;
	}
	public User(String username, String emailaddress, String password, String profile_url) {
		super();
		this.username = username;
		this.emailaddress = emailaddress;
		this.password = password;
		this.profile_url = profile_url;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmailaddress() {
		return emailaddress;
	}
	public void setEmailaddress(String emailaddress) {
		this.emailaddress = emailaddress;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getProfile_url() {
		return profile_url;
	}
	public void setProfile_url(String profile_url) {
		this.profile_url = profile_url;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	
	
	
	
}
