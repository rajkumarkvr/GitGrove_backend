package models;

import java.time.LocalDateTime;


import enums.PullRequestStatus;

public class PullRequest {
	private int id;
	private Branch sourceBranch;
	private Branch targetBranch;
	private PullRequestStatus status;
	private String description;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt; 
	private User createdBy;
	private String title;
	
	public PullRequest(Branch sourceBranch, Branch targetBranch) {
		this.sourceBranch = sourceBranch;
		this.targetBranch = targetBranch;
		this.status = PullRequestStatus.OPEN;
	}
	
	public PullRequest(int id, Branch sourceBranch, Branch targetBranch, PullRequestStatus status,
			LocalDateTime createdAt) {

		this.id = id;
		this.sourceBranch = sourceBranch;
		this.targetBranch = targetBranch;
		this.status = status;
		this.createdAt = createdAt;
	}
	
	public PullRequest(Branch sourceBranch, Branch targetBranch, PullRequestStatus status) {
		this.sourceBranch = sourceBranch;
		this.targetBranch = targetBranch;
		this.status = status;
	}
	
	public PullRequest(int id,String description, Branch sourceBranch, Branch targetBranch, PullRequestStatus status, LocalDateTime createdAt, User createdBy, LocalDateTime updatedAt, String title) {
		this(id,sourceBranch, targetBranch, status, createdAt);
		this.description = description;
		this.createdBy = createdBy;
		this.title = title;
		this.updatedAt = updatedAt;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Branch getSourceBranch() {
		return sourceBranch;
	}
	public void setSourceBranch(Branch sourceBranch) {
		this.sourceBranch = sourceBranch;
	}
	public Branch getTargetBranch() {
		return targetBranch;
	}
	public void setTargetBranch(Branch targetBranch) {
		this.targetBranch = targetBranch;
	}
	public PullRequestStatus getStatus() {
		return status;
	}
	public void setStatus(PullRequestStatus status) {
		this.status = status;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String descripiton) {
		this.description = descripiton;
	}
	public User getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

}
