package models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import models.Comment;
import services.DBconnection;

public class CommentsDAO {
	
	static CommentsDAO commentsDAO = null;
	
	private CommentsDAO() {
		
	}
	
	public static CommentsDAO getInstance() {
		if(commentsDAO == null) {
			commentsDAO = new CommentsDAO();
		}
		
		return commentsDAO;
	}
	
	public void addComment(int userId, int pullRequestId, String content){
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into comments(pull_request_id,user_id,content) values(?,?,?)");
			stmt.setInt(1, pullRequestId);
			stmt.setInt(2, userId);
			stmt.setString(3, content);
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("Adding comment error : "+e.getMessage());
		}
	}
	
	public ArrayList<Comment> getComments(int pullRequestId){
		ArrayList<Comment> comments = new ArrayList<Comment>();
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select u.username,c.content,c.createdAt,u.profile_url from comments c join users u on c.user_id = u.id where c.pull_request_id = ?");
			stmt.setInt(1, pullRequestId);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				comments.add(new Comment(rs.getString(1), rs.getString(2), rs.getTimestamp(3).toLocalDateTime(), rs.getString(4)));
			}
		} catch (Exception e) {
			System.out.println("Getting comments error : "+e.getMessage());
		}
		
		return comments;
	}
}
