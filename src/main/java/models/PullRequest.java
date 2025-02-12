package models;

import java.time.LocalDateTime;

import enums.PullRequestStatus;

public class PullRequest {
	private int id;
	private Branch sourceBranch;
	private Branch targetBranch;
	private PullRequestStatus status;
	private LocalDateTime createdAt;
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




}
