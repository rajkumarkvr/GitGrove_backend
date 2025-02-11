package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.mysql.cj.jdbc.Driver;
public class DBconnection {

	private static String connectionString;
	private static String userName;
	private static String password;
	

	static Connection con = null;
	private DBconnection(){

	}

		public static void config(String conString,String username,String pass) {
			connectionString=conString;
			userName = username;
			password = pass;
		}
		
		
		
	public static  Connection getConnection() throws SQLException {
		 try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Driver not found");	
			
		}

		if(con==null) {
			con = DriverManager.getConnection(connectionString,userName,password);
			return con;
		}

		return con;
	}


}
