package models;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Branch {
	private int id;
	private String name;

	private LocalDateTime createdAt;

	private ArrayList<Commit> commits;

	public Branch(String name) {
		super();
		this.name = name;
		commits = new ArrayList<>();
	}

	public Branch(int id, String name, LocalDateTime createdAt) {
		this.id = id;
		this.name = name;
		this.createdAt = createdAt;
		commits = new ArrayList<>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public ArrayList<Commit> getCommits() {
		return commits;
	}

	public void setCommits(ArrayList<Commit> commits) {
		this.commits = commits;
	}



}
