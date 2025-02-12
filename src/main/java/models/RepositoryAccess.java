package models;

import enums.Role;

public class RepositoryAccess {
	private int id;
	private Role role;
	public RepositoryAccess(Role role) {
		super();
		this.role = role;
	}
	public RepositoryAccess(int id, Role role) {
		super();
		this.id = id;
		this.role = role;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}

}
