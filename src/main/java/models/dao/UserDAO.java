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

	public User signUp(String userName, String emailId, String password ) {
		User user = null;
		password = encrypt(password);
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into users(username,email,password_hash) values(?,?,?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, userName);
			stmt.setString(2, emailId);
			stmt.setString(3, password);

			int affected = stmt.executeUpdate();
			ResultSet keys = stmt.getGeneratedKeys();
			int id = -1;
			if(keys.next()) {
				id = keys.getInt(1);
			}
			if(affected>0) {
				
				 user = getUserByEmail(emailId);
				
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
			stmt.setString(2, usernameOrEmail);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				if(BCrypt.checkpw(password, rs.getString(4))) {
					int id = rs.getInt(1);
					user = new User(id,rs.getString(2),rs.getString(3), rs.getString(4), rs.getString(5),rs.getTimestamp(6).toLocalDateTime());
					user.setRepositories(RepositoryDAO.getInstance().getAllRepositoryOfUser(id));
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return user;
	}
	
	public boolean userNameExists(String username) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from users where username = ?");
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	public boolean userNameOrEmailExists(String usernameoremail) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from users where username = ? or email=?");
			stmt.setString(1, usernameoremail);
			stmt.setString(2, usernameoremail);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	public boolean emailExists(String email) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from users where email = ?");
			stmt.setString(1, email);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	public int getUserId(String username) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select id from users where username = ?");
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return -1;
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

	public User updateUserProfile(String oldusername,User user,User olduser) {
		try {
		

			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt =null;
			if(olduser.getUsername().equals(user.getUsername())&&!olduser.getEmailaddress().equals(user.getEmailaddress())){
				stmt= connection.prepareStatement("update users set email=?,profile_url=? where username=? ");
				stmt.setString(1, user.getEmailaddress());
				stmt.setString(2, user.getProfile_url());
				stmt.setString(3, oldusername);
				int affected = stmt.executeUpdate();
	
					User newuser = getUserByEmail(user.getEmailaddress());
				 return newuser;
				
			}else if(!olduser.getUsername().equals(user.getUsername())&&olduser.getEmailaddress().equals(user.getEmailaddress())){
			stmt= connection.prepareStatement("update users set username=?,profile_url=? where username=? ");
			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getProfile_url());
			stmt.setString(3, oldusername);
			int affected = stmt.executeUpdate();
	
			User newuser = getUserByUserName(user.getUsername());
				 return newuser;
				 
				 
			}else {
				stmt= connection.prepareStatement("update users set username=?,email=?,profile_url=? where username=? ");
				stmt.setString(1, user.getUsername());
				stmt.setString(2, user.getEmailaddress());
				stmt.setString(3, user.getProfile_url());
				stmt.setString(4, oldusername);
				int affected2 = stmt.executeUpdate();
				
					User newuser = getUserByUserName(user.getUsername());
					 return newuser;
			
			
				
			
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return null;
	}
}