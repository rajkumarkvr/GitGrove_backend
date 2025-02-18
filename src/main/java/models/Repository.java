package models;

import java.time.LocalDateTime;
import java.util.ArrayList;

import enums.Visibility;

public class Repository {
	private int id;
	private String name;

	private Visibility visibility;
	private int stars_count;
	private LocalDateTime createdAt;
	private String description;
	private ArrayList<Branch> branches;
	private ArrayList<PullRequest> pullrequets;
	private String ownerName;

	public Repository(int id, String name, Visibility visibility) {
		this.id = id;
		this.name = name;
		this.visibility = visibility;
		branches = new ArrayList<>();
		pullrequets = new ArrayList<>();
	}
	public Repository(String name, Visibility visibility) {
		this.name = name;
		this.visibility = visibility;
		branches = new ArrayList<>();
		pullrequets = new ArrayList<>();
	}

	public Repository(int id, String name, Visibility visibility, String description, LocalDateTime createdAt, int starsCount) {
		this(id,name,visibility);
		this.description = description;
		this.createdAt = createdAt;
		this.stars_count = starsCount;
	}


	public Repository(int id, String name, Visibility visibility, String description, LocalDateTime createdAt, int starsCount,String ownername) {
		this(id,name,visibility);
		this.description = description;
		this.createdAt = createdAt;
		this.stars_count = starsCount;
		this.ownerName=ownername;
	}
	
	public Repository(String name, Visibility visibility, String description) {
		super();
		this.name = name;
		this.visibility = visibility;
		this.description = description;
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
	public Visibility getVisibility() {
		return visibility;
	}
	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}
	public int getStars_count() {
		return stars_count;
	}
	public void setStars_count(int stars_count) {
		this.stars_count = stars_count;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public ArrayList<Branch> getBranches() {
		return branches;
	}
	public void setBranches(ArrayList<Branch> branch) {
		this.branches = branch;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ArrayList<PullRequest> getPullrequets() {
		return pullrequets;
	}
	public void setPullrequets(ArrayList<PullRequest> pullrequets) {
		this.pullrequets = pullrequets;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}


}
