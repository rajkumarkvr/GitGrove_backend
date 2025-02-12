package models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

import org.mindrot.jbcrypt.BCrypt;

import models.User;
import services.DBconnection;

public class UserDAO {

	static UserDAO userDao = null;

	private UserDAO() {

	}

	public static UserDAO getInstance() {
		if(userDao == null) {
			userDao = new UserDAO();
		}
		return userDao;
	}

	public User signUp(String userName, String emailId, String password , String profile_url) {
		User user = null;
		password = encrypt(password);
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into users(username,email,password_hash,profile_url) values(?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, userName);
			stmt.setString(2, emailId);
			stmt.setString(3, password);
			stmt.setString(4, profile_url);
			int affected = stmt.executeUpdate();
			ResultSet keys = stmt.getGeneratedKeys();
			int id = -1;
			if(keys.next()) {
				id = keys.getInt(1);
			}
			if(affected>0) {
				user = new User(id,userName, emailId, password, profile_url,LocalDateTime.now());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return user;
	}

	public User signIn(String usernameOrEmail , String password) {
		User user = null;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from users where username = ? or email = ?");
			stmt.setString(1, usernameOrEmail);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				if(BCrypt.checkpw(password, rs.getString(4))) {
					user = new User(rs.getInt(1),rs.getString(2),rs.getString(3), rs.getString(4), rs.getString(5),rs.getTimestamp(6).toLocalDateTime());
				}
			}
		} catch (Exception e) {
			e.getMessage();
		}
		return user;
	}

	public User getUserByEmail(String email) {
		User user = null;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from users where email = ?");
			stmt.setString(1, email);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				user = new User(rs.getInt(1),rs.getString(2),rs.getString(3), rs.getString(4), rs.getString(5),rs.getTimestamp(6).toLocalDateTime());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return user;
	}

	public User getUserByUserName(String username) {
		User user = null;

		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from users where username = ?");
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				user = new User(rs.getInt(1),rs.getString(2),rs.getString(3), rs.getString(4), rs.getString(5),rs.getTimestamp(6).toLocalDateTime());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return user;
	}

	public User getUserById(int id) {
		User user = null;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from users where id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				user = new User(rs.getInt(1),rs.getString(2),rs.getString(3), rs.getString(4), rs.getString(5),rs.getTimestamp(6).toLocalDateTime());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return user;
	}

	public boolean updatePassword(String email, String password) {
		password = encrypt(password);
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("update collaboration_system set password_hash = ? where email = ?");
			stmt.setString(1, password);
			stmt.setString(2, email);
			int affected = stmt.executeUpdate();
			if(affected>0) {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	public String encrypt(String text) {
    	String salt = BCrypt.gensalt(12);
    	return BCrypt.hashpw(text, salt);
    }

}