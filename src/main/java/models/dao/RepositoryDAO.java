package models.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.json.JSONObject;

import enums.Collaborator_Request;
import enums.Role;
import enums.Visibility;
import models.Repository;
import services.DBconnection;



public class RepositoryDAO {

	static RepositoryDAO repositoryDAO = null;

	private RepositoryDAO() {

	}

	public static RepositoryDAO getInstance() {
		if(repositoryDAO == null) {
			repositoryDAO = new RepositoryDAO();
		}
		return repositoryDAO;
	}

	public boolean addRepository(String name, String description, String visibility, int owner_id){
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into repositories(name,visibility,owner_id,description) values(?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			stmt.setString(2, Visibility.valueOf(visibility).toString());
			stmt.setInt(3, owner_id);
			stmt.setString(4, description);
			int affected = stmt.executeUpdate();
			int repo_id = -1;
			
			// getting the repository key which is generated by mysql database.
			ResultSet keys = stmt.getGeneratedKeys();
			if(keys.next()) {
				repo_id = keys.getInt(1);
			}
			
			// Maping the Repository with user in repository_access table if repository added.
			if(affected>0) {
				mapRepository(owner_id, repo_id , Role.OWNER);
				return affected>0;
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public ArrayList<Repository> getAllRepositoryExceptCurrentUser(int userId, int limit, int startPoint){
		ArrayList<Repository> repositories = new ArrayList<Repository>();
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select r.id, r.name, r.description, r.createdAt, r.stars_count ,u1.username from repositories r join users u1 on r.owner_id = u1.id where r.owner_id != ? and r.visibility = ? limit ? offset ?");
			stmt.setInt(1, userId);
			stmt.setString(2, Visibility.PUBLIC.toString());
			stmt.setInt(3, limit);
			stmt.setInt(4, startPoint);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				Repository repository = new Repository(rs.getInt(1),rs.getString(2),Visibility.PUBLIC,rs.getString(3),rs.getTimestamp(4).toLocalDateTime(),rs.getInt(5));
				repository.setOwnerName(rs.getString(6));
				repositories.add(repository);
			}
			
		} catch (Exception e) {
			System.out.println("Get all repositories except current user : "+e.getMessage());
		}
		return repositories;
	}
	
	public ArrayList<Repository> searchRepositoryExceptCurrentUser(int userId, int limit, String query){
		ArrayList<Repository> repositories = new ArrayList<Repository>();
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select r.id, r.name, r.description, r.createdAt, r.stars_count ,u1.username from repositories r join users u1 on r.owner_id = u1.id where r.owner_id != ? and r.visibility = ? and r.name limit ?");
			stmt.setInt(1, userId);
			stmt.setString(2, Visibility.PUBLIC.toString());
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				Repository repository = new Repository(rs.getInt(1),rs.getString(2),Visibility.PUBLIC,rs.getString(3),rs.getTimestamp(4).toLocalDateTime(),rs.getInt(5));
				repository.setOwnerName(rs.getString(6));
				repositories.add(repository);
			}
			
		} catch (Exception e) {
			System.out.println("Get all repositories except current user : "+e.getMessage());
		}
		return repositories;
	}


	public ArrayList<Repository> repositoriesByOwnerId(int ownerId){
		ArrayList<Repository> repositories = new ArrayList<Repository>();
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from repositories where owner_id = ?");
			stmt.setInt(1, ownerId);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				repositories.add(new Repository(rs.getInt(1),rs.getString(2),Visibility.valueOf(rs.getString(3)),rs.getString(7),rs.getTimestamp(4).toLocalDateTime(),rs.getInt(6)));
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return repositories;
	}
	
	// Takes all the repositories in which the user id exists in any roles.
	public ArrayList<Repository> getAllRepositoryOfUser(int id) {
		ArrayList<Repository> repositories = new ArrayList<Repository>();
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select r.id,r.name,r.description,r.visibility,r.createdAt,r.stars_count from repositories r join repository_access ra on r.id=ra.repo_id join users u on u.id = ra.user_id where ra.user_id=?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
		
				repositories.add(new Repository(rs.getInt(1), rs.getString(2), Visibility.valueOf(rs.getString(4)), rs.getString(3), rs.getTimestamp(5).toLocalDateTime(), rs.getInt(6)));
			}
		} catch (Exception e) {
			System.out.println("Getting all repositories : "+e.getMessage());
		}
		return repositories;
	}
	
	// Getting repository details using repository Id.
	public Repository getRepository(int id) {
		Repository repository = null;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from repositories where id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				repository = new Repository(rs.getInt(1),rs.getString(2),Visibility.valueOf(rs.getString(3)),rs.getString(7),rs.getTimestamp(4).toLocalDateTime(),rs.getInt(6));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return repository;
	}
	
	public int getRepositoryId(String repoName) {
		int id = -1;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select id from repositories where name = ?");
			stmt.setString(1, repoName);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				id = rs.getInt(1);
			}
		} catch (Exception e) {
			System.out.println("Get Repository Id : "+e.getMessage());
		}
		
		return id;
	}
	
	// To Map repositories with the users using repositoy_access database.
	public void mapRepository (int user_id,int repo_id,Role role) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into repository_access(user_id,repo_id,role) values(?,?,?)");
			stmt.setInt(1, user_id);
			stmt.setInt(2, repo_id);
			stmt.setString(3, role.toString());
			stmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addStar(int userId, int repoId) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = null;
			
			if(needToMap(userId,repoId)) {
				stmt = connection.prepareStatement("update repositories set stars_count = stars_count+1 where id = ?");
				stmt.setInt(1, repoId);
			}
			else {
				stmt = connection.prepareStatement("update repositories set stars_count = stars_count-1 where id = ?");
				stmt.setInt(1, repoId);
			}
			
			stmt.executeUpdate();
			
		} catch (Exception e) {
			System.out.println("Add star : "+e.getMessage());
		}
	}
	
	public boolean needToMap(int userId, int repoId) {
		
		boolean needToMap = false;
		
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = null;
			if(isStarred(userId,repoId)) {
				stmt = connection.prepareStatement("delete from repo_stared_details where userid = ? and repoid = ?");
				stmt.setInt(1, userId);
				stmt.setInt(2, repoId);
			}
			else {
				stmt = connection.prepareStatement("insert into repo_stared_details values(?,?)");
				stmt.setInt(1, userId);
				stmt.setInt(2, repoId);
				needToMap = true;
			}
			
			stmt.executeUpdate();
			
		} catch (Exception e) {
			System.out.println("Map star : "+e.getMessage());
		}
		
		return needToMap;
	}
	
	public boolean isStarred(int userId, int repoId) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from repo_stared_details where userid = ? and repoid =?");
			stmt.setInt(1, userId);
			stmt.setInt(2, repoId);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch (Exception e) {
			System.out.println("Is starred : "+e.getMessage());
		}
		
		return false;
	}
	
	public ArrayList<Repository> getStarredRepositoriesByUser(int userid){
		ArrayList<Repository> repositoryList = new ArrayList<Repository>();
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select r.id,r.name,r.description,r.visibility,r.createdAt,r.stars_count from repositories r join repo_stared_details rs on r.id=rs.repoid join users u on u.id = rs.userid where rs.userid=?");
			stmt.setInt(1, userid);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				repositoryList.add(new Repository(rs.getInt(1), rs.getString(2), Visibility.valueOf(rs.getString(4)), rs.getString(3), rs.getTimestamp(5).toLocalDateTime(), rs.getInt(6)));
			}
		} catch (Exception e) {
			System.out.println("Get starred repositories : "+e.getMessage());
		}
		
		return repositoryList;
	}
	
	public String getOwnerName(int repoId) {
		String ownerName = null;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select username from users u join repositories r on u.id = r.owner_id where r.id = ?");
			stmt.setInt(1, repoId);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				ownerName = rs.getString(1);
			}
		} catch (Exception e) {	
			System.out.println("Get owner name : "+e.getMessage());
		}
		
		return ownerName;
	}
	
	public boolean isRepositoryLikedByUser(int repoId, int userId) {
		boolean isLiked = false;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from repo_stared_details where repoid = ? and userid = ?");
			stmt.setInt(1, repoId);
			stmt.setInt(2, userId);
			ResultSet rs = stmt.executeQuery();
			isLiked = rs.next();
		} catch (Exception e) {
			System.out.println("Is repository like by user : "+e.getMessage());
		}
		
		return isLiked;
	}
	
	public boolean requestCollaborator(int invitee_id, int repo_id, int owner_id) {
		boolean isRequested = false;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into collaborator_requests(owner_id,invitee_id,repo_id) values(?,?,?)");
			stmt.setInt(1, owner_id);
			stmt.setInt(2, invitee_id);
			stmt.setInt(3, repo_id);
			int affected = stmt.executeUpdate();
			isRequested = affected>0;
		} catch (Exception e) {
			System.out.println("Adding collaborator error : "+e.getMessage());
		}
		
		return isRequested;
	}

	public boolean addCollaborator(int invitee_id, int repo_id, int owner_id) {
		boolean isAdded = false;
		try {
			Connection connection = DBconnection.getConnection();
			
			
			PreparedStatement stmt =null;
			stmt= connection.prepareStatement("update collaborator_requests set status = ? where owner_id = ? and invitee_id = ? and repo_id = ?");
			stmt.setString(1, Collaborator_Request.ACCEPTED.name());
			stmt.setInt(2, owner_id);
			stmt.setInt(3, invitee_id);
			stmt.setInt(4, repo_id);
			int affected = stmt.executeUpdate();
			stmt= connection.prepareStatement("insert into repository_access(user_id,repo_id,role) values(?,?,?)");
			stmt.setInt(1, invitee_id);
			stmt.setInt(2, repo_id);
			stmt.setString(3, Role.COLLABORATOR.name());
			int newAffected = stmt.executeUpdate();
			isAdded = affected>0&&newAffected>0;
		} catch (Exception e) {
			System.out.println("Adding collaborator error : "+e.getMessage());
		}
		
		return isAdded;
	}
	
	
	public Collaborator_Request isAlreadyCollaboratorOrRequested(int ownerid,int userid,int repoid) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from collaborator_requests where invitee_id=? and owner_id=? and repo_id=?");
			stmt.setInt(1,userid);
			stmt.setInt(2,ownerid);
			stmt.setInt(3, repoid);

			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				System.out.println("Collab");
				return Collaborator_Request.valueOf(rs.getString(4));
			}
			else {
				System.out.println("no collaboration");
			}
		}catch(Exception e) {
			System.out.println("Error from isAlready a collaborator func"+"\n"+e.getMessage());
		}
		return null;
	}
	
	public ArrayList<JSONObject> getCollaborators(int repoid,int ownerid){
		try {
			Connection connection = DBconnection.getConnection();
			String query ="select u.username,u.email,u.profile_url as avator from repository_access r left join users u on r.user_id=u.id where r.user_id!=? and r.repo_id=?";
			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setInt(1,ownerid);
			stmt.setInt(2,repoid);
		

			ResultSet rs = stmt.executeQuery();
			ArrayList<JSONObject> users = new ArrayList<JSONObject>();
			while(rs.next()) {
				JSONObject user = new JSONObject();
				
				user.put("username", rs.getString(1));
				user.put("email", rs.getString(2));
				user.put("avatar", rs.getString(3));
				users.add(user);
			}
			
			return users;
		}catch(Exception e) {
			System.out.println("Error from getCollaborators"+"\n"+e.getMessage());
		}
		return null;
	}
	
	public ArrayList<JSONObject> getCollabRequests(int repoid,int ownerid){
		try {
			Connection connection = DBconnection.getConnection();
			String query ="select u.username,u.email,u.profile_url as avator from collaborator_requests r left join users u on r.invitee_id=u.id where status!='ACCEPTED' and owner_id=? and repo_id=?";
			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setInt(1,ownerid);
			stmt.setInt(2,repoid);
		

			ResultSet rs = stmt.executeQuery();
			ArrayList<JSONObject> users = new ArrayList<JSONObject>();
			while(rs.next()) {
				JSONObject user = new JSONObject();
				
				user.put("username", rs.getString(1));
				user.put("email", rs.getString(2));
				user.put("avatar", rs.getString(3));
				users.add(user);
			}
			
			return users;
		}catch(Exception e) {
			System.out.println("Error from getCollabRequests"+"\n"+e.getMessage());
		}
		return null;
	}
	
	public ArrayList<Repository> getTopStarredRepositories(int limit) {
		ArrayList<Repository> topRepositories = new ArrayList<Repository>();
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select r.id, r.name, r.description, r.createdAt, r.stars_count ,u1.username from repositories r join users u1 on r.owner_id = u1.id where r.visibility = ? limit ?");
			stmt.setString(1, Visibility.PUBLIC.toString());
			stmt.setInt(2, limit);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				topRepositories.add(new Repository(rs.getInt(1), rs.getString(2), Visibility.valueOf(rs.getString(4)), rs.getString(3), rs.getTimestamp(5).toLocalDateTime(), rs.getInt(6)));
			}
		} catch (Exception e) {
			System.out.println("Get top starred repositories error : "+e.getMessage());
		}
		
		return topRepositories;
	}
	
	
}
