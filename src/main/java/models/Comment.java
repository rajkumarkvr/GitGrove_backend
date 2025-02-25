package models;

import java.time.LocalDateTime;

public class Comment {

	private String username;
	private String postedByAvatar;
	private LocalDateTime postedAt;
	private String content;
	

	public Comment(String username, String content, LocalDateTime postedAt, String postedByAvatar) {
		super();
		this.content = content;
		this.username = username;
		this.postedAt = postedAt;
		this.postedByAvatar = postedByAvatar;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public LocalDateTime getPostedAt() {
		return postedAt;
	}

	public void setPostedAt(LocalDateTime postedAt) {
		this.postedAt = postedAt;
	}	
	
	public String getPostedByAvatar() {
		return postedByAvatar;
	}

	public void setPostedByAvatar(String postedByAvatar) {
		this.postedByAvatar = postedByAvatar;
	}	
	
}
