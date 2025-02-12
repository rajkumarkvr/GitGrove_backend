package models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import services.DBconnection;

public class SessionDAO {

	static SessionDAO sessionDAO = null;

	private SessionDAO() {

	}

	public static SessionDAO getInstance() {
		if(sessionDAO == null) {
			sessionDAO = new SessionDAO();
		}
		return sessionDAO;
	}

	public boolean storeSession(int user_id, String jwtToken, String userAgent) {

		String browser = "Unknown";
        if (userAgent.contains("Chrome")) {
            browser = "Google Chrome";
        } else if (userAgent.contains("Firefox")) {
            browser = "Mozilla Firefox";
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            browser = "Apple Safari";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            browser = "Internet Explorer";
        } else if (userAgent.contains("Edge")) {
            browser = "Microsoft Edge";
        }

        // Extract OS
        String os = "Unknown OS";
        if (userAgent.contains("Windows")) {
            os = "Windows";
        } else if (userAgent.contains("Mac OS X")) {
            os = "macOS";
        } else if (userAgent.contains("Linux")) {
            os = "Linux";
        } else if (userAgent.contains("Android")) {
            os = "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            os = "iOS";
        }


		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into sessions(user_id,session_value,agent) values(?,?,?)");
			stmt.setInt(1, user_id);
			stmt.setString(2, jwtToken);
			stmt.setString(3, os+" - "+browser);
			int affected = stmt.executeUpdate();
			return affected>0;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return false;
	}

	public boolean clearSession(String token) {

		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("delete from sessions where session_value = ?");
			stmt.setString(1, token);
			int affected = stmt.executeUpdate();
			return affected>0;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return false;

	}

}
