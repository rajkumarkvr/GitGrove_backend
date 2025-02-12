package models;

import java.time.LocalDateTime;

public class Commit {
	private int id;
	private String commitHash;
	private String message;
	private LocalDateTime createdAt;
	public Commit(String commitHash, String message) {
		super();
		this.commitHash = commitHash;
		this.message = message;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCommitHash() {
		return commitHash;
	}
	public void setCommitHash(String commitHash) {
		this.commitHash = commitHash;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}


}
