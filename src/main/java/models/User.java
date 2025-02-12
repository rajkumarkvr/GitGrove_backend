package models;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class User {
	private int id;
	private String username;
	private String emailaddress;
	private String password;
	private String profile_url;
	private LocalDateTime createdAt;

	private ArrayList<Repository> repositories;

	public User(int id, String username, String emailaddress, String password, String profile_url,
			LocalDateTime createdAt) {

		this.id = id;
		this.username = username;
		this.emailaddress = emailaddress;
		this.password = password;
		this.profile_url = profile_url;
		this.createdAt = createdAt;
		this.repositories = new ArrayList<>();
	}
	public User(String username, String emailaddress, String password, String profile_url) {

		this.username = username;
		this.emailaddress = emailaddress;
		this.password = password;
		this.profile_url = profile_url;
		this.repositories = new ArrayList<>();
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
	public ArrayList<Repository> getRepositories() {
		return repositories;
	}
	public void setRepositories(ArrayList<Repository> repositories) {
		this.repositories = repositories;
	}





}
