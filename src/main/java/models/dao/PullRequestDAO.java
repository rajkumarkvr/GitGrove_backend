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
	
	public void createPullRequest(int repo_id, int sourceBranchId, int targetBranchId, int requestCreaterId, String descripiton) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into pull_requests(repo_id,source_branch_id,target_branch_id,created_by,description) values(?,?,?,?,?)");
			stmt.setInt(1, repo_id);
			stmt.setInt(2, sourceBranchId);
			stmt.setInt(3, targetBranchId);
			stmt.setInt(4, requestCreaterId);
			stmt.setString(5, descripiton);
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("Pull request creating error : "+e.getMessage());
		}
	}
	
	public void changeStatus(int repo_id, int sourceBranchId, int targetBranchId, int requestCreaterId, PullRequestStatus pullRequestStatus) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("update pull_requests set status = ? where repo_id = ? and source_branch_id = ? and target_branch_id = ? and created_by =? and status = ?");
			stmt.setString(1, pullRequestStatus.toString());
			stmt.setInt(2, repo_id);
			stmt.setInt(3, sourceBranchId);
			stmt.setInt(4, targetBranchId);
			stmt.setInt(5, requestCreaterId);
			stmt.setString(6, PullRequestStatus.OPEN.toString());
		
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("Pull request creating error : "+e.getMessage());
		}
	}
	
	public ArrayList<PullRequest> getPullRequest(int repoId){
		ArrayList<PullRequest> pullRequests = new ArrayList<PullRequest>();
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select pr.description,b.name,b1.name,pr.status,pr.createdAt,u.username,u.email,u.profile_url,pr.updatedAt from pull_requests pr join branches b on b.id = pr.source_branch_id join branches b1 on b1.id = pr.target_branch_id join users u on u.id = pr.created_by where pr.repo_id = ?");
			stmt.setInt(1, repoId);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				Branch sourceBranch = new Branch(rs.getString(2));
				Branch targetBranch = new Branch(rs.getString(3));
				User user = new User(rs.getString(6),rs.getString(7),rs.getString(8));
				pullRequests.add(new PullRequest(rs.getString(1),sourceBranch,targetBranch,PullRequestStatus.valueOf(rs.getString(4)),rs.getTimestamp(5).toLocalDateTime(),user,rs.getTimestamp(9).toLocalDateTime()));
			}
 		} catch (Exception e) {
			System.out.println("Getting pull requests : "+e.getMessage());
		}
		
		return pullRequests;
	}
	
	public int getPullRequestId(int repoId, int sourceBranchId, int targetBranchId, int createdBy) {
		int id = -1;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt  = connection.prepareStatement("select id from pull_requests where repo_id = ? and source_branch_id = ? and target_branch_id = ? and created_by = ? and status = ?");
			stmt.setInt(1, repoId);
			stmt.setInt(2, sourceBranchId);
			stmt.setInt(3, targetBranchId);
			stmt.setInt(4, createdBy);
			stmt.setString(5, PullRequestStatus.OPEN.name());
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				id = rs.getInt(1);
			}
		} catch (Exception e) {
			System.out.println("Getting pull request ID error : "+e.getMessage());
		}
		
		return id;
	}
	
}
