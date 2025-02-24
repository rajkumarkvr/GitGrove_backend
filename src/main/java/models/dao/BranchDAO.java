package models.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;

import services.DBconnection;

public class BranchDAO {
	
	private static final String REPOPATH = "/opt/repo/";
	
	private static BranchDAO branchDAO = null;
	
	private BranchDAO() {
		
	}
	
	public static BranchDAO getInstance() {
		
		if(branchDAO == null) {
			branchDAO = new BranchDAO(); 
		}
		
		return branchDAO;
	}
	
	public void addBranch(String ownerName, String repoName) {
		String repoPath = REPOPATH+ownerName+"/"+repoName;
		
		int owner_id = UserDAO.getInstance().getUserId(ownerName);
		int repoId = RepositoryDAO.getInstance().getRepositoryId(repoName, owner_id);
		
		ArrayList<String> existingBranches = branchList(repoId);
		
		try {
			
			 Git git = Git.open(new File(repoPath));
			 List<Ref> branches = git.branchList().call();
			 
			 Connection connection = DBconnection.getConnection();
			 PreparedStatement stmt = connection.prepareStatement("insert into branches(repo_id,name) values(?,?)");
			 stmt.setInt(1, repoId);
			 
			 for(Ref branch : branches) {
				 String branchName = branch.getName().replace("refs/heads/", "");
				 if(!existingBranches.contains(branchName)) {
					 stmt.setString(2, branchName);
					 stmt.executeUpdate();
				 }
			 }
			 
		} catch (Exception e) {
			System.out.println("Adding branch error : "+e.getMessage());
		}
	}
	
	public ArrayList<String> branchList(int repo_id) {
		ArrayList<String> branches = new ArrayList<String>();
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select name from branches where repo_id = ?");
			stmt.setInt(1, repo_id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				branches.add(rs.getString(1));
			}
		} catch (Exception e) {
			System.out.println("Branch list error : "+e.getMessage());
		}
		
		return branches;
	}
	
	public int getBranchId(int repoId, String branchName) {
		
		int branchId = -1;
		
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select id from branches where repo_id = ? and name = ?");
			stmt.setInt(1, repoId);
			stmt.setString(2, branchName);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				branchId = rs.getInt(1);
			}
		} catch (Exception e) {
			System.out.println("Getting branch Id error : "+e.getMessage());
		}
		
		return branchId;
	}
	
}
