package models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import enums.PullRequestStatus;
import models.Branch;
import models.PullRequest;
import models.User;
import services.DBconnection;

public class PullRequestDAO {
	
	private static PullRequestDAO pullRequestDAO = null;
	
	private PullRequestDAO() {
		
	}
	
	public static PullRequestDAO getInstance() {
		if(pullRequestDAO == null) {
			pullRequestDAO = new PullRequestDAO();
		}
		
		return pullRequestDAO;
	}
	
	public void createPullRequest(int sourceBranchId, int targetBranchId, int requestCreaterId, String descripiton) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into pull_requests(source_branch_id,target_branch_id,created_by,description) values(?,?,?,?)");
			stmt.setInt(1, sourceBranchId);
			stmt.setInt(2, targetBranchId);
			stmt.setInt(3, requestCreaterId);
			stmt.setString(4, descripiton);
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("Pull request creating error : "+e.getMessage());
		}
	}
	
	public void changeStatus(int id, PullRequestStatus pullRequestStatus) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("update pull_requests set status = ? where id = ?");
			stmt.setString(1, pullRequestStatus.toString());
			stmt.setInt(2,id);
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("Pull request creating error : "+e.getMessage());
		}
	}
	
	public ArrayList<PullRequest> getPullRequest(int repoId){
		ArrayList<PullRequest> pullRequests = new ArrayList<PullRequest>();
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select pr.id,pr.description,b.name,b1.name,pr.status,pr.createdAt,u.username,u.email,u.profile_url,pr.updatedAt from pull_requests pr join branches b on b.id = pr.source_branch_id join branches b1 on b1.id = pr.target_branch_id join users u on u.id = pr.created_by where b.repo_id = ?");
			stmt.setInt(1, repoId);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				Branch sourceBranch = new Branch(rs.getString(3));
				Branch targetBranch = new Branch(rs.getString(4));
				User user = new User(rs.getString(7),rs.getString(8),rs.getString(9));
				pullRequests.add(new PullRequest(rs.getInt(1),rs.getString(2),sourceBranch,targetBranch,PullRequestStatus.valueOf(rs.getString(5)),rs.getTimestamp(6).toLocalDateTime(),user,rs.getTimestamp(10).toLocalDateTime()));
			}
 		} catch (Exception e) {
			System.out.println("Getting pull requests : "+e.getMessage());
		}
		
		return pullRequests;
	}
	
	public int getPullRequestId(int sourceBranchId, int targetBranchId, int createdBy) {
		int id = -1;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt  = connection.prepareStatement("select id from pull_requests where source_branch_id = ? and target_branch_id = ? and created_by = ? and status = ?");
			stmt.setInt(1, sourceBranchId);
			stmt.setInt(2, targetBranchId);
			stmt.setInt(3, createdBy);
			stmt.setString(4, PullRequestStatus.OPEN.name());
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				id = rs.getInt(1);
			}
		} catch (Exception e) {
			System.out.println("Getting pull request ID error : "+e.getMessage());
		}
		
		return id;
	}
	
	public boolean isIdExists(int id) {
		boolean isExists = false;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select status from pull_requests where id = ?");
			stmt.setInt(1,id);
			ResultSet rs = stmt.executeQuery();
			isExists = rs.next();
		} catch (Exception e) {
			System.out.println("Is ID exists in pull request error : "+e.getMessage());
		}
		
		return isExists;
	}
	
	public int getRepoId(int id) {
		int repoId = -1;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select b.repo_id from pull_requests pr join branches b on pr.source_branch_id = b.id where pr.id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				repoId = rs.getInt(1);
			}
 		} catch (Exception e) {
			System.out.println("Getting repository Id error : "+e.getMessage());
		}
		
		return repoId;
	}
	
	public ArrayList<String> getTargetAndSourceBranch(int id){
		ArrayList<String> branchesName = new ArrayList<String>();
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select b.name,b1.name from pull_requests pr join branches b on pr.source_branch_id = b.id join branches b1 on pr.target_branch_id = b1.id where pr.id  = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				branchesName.add(rs.getString(1));
				branchesName.add(rs.getString(2));
			}
		} catch (Exception e) {
			System.out.println("getting target and source branch error : "+e.getMessage());
		}
		return branchesName;
	}
	
}
