package models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.json.JSONObject;

import models.Session;
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

	public ArrayList<JSONObject> getActiveSessions(int userid) {
		ArrayList<JSONObject> sessions = new ArrayList<JSONObject>();
//		  sessionid: "abcd1234",
//	        os_browser: "Windows - Google Chrome",
//	        city_country: "New York, USA",
//	        startedTime: "2025-02-15 10:30 AM",
//	        ipaddress: "192.168.1.1",
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from sessions where user_id=?");
			stmt.setInt(1, userid);
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				JSONObject sn = new JSONObject();
			 	sn.put("sessionid",rs.getString(3));
			 	sn.put("os_browser", rs.getString(4));
				sn.put("city_country", rs.getString(5));
				sn.put("ipaddress", rs.getString(6));
				sn.put("startedTime", rs.getTimestamp(7).toLocalDateTime().toString());
				sessions.add(sn);
			}
			return sessions;
		} catch (Exception e) {
			System.out.println("From getting active sessions"+e.getMessage());
		}
		
		return null;

	}
	public boolean storeSession(int user_id, String jwtToken, String userAgent,String ip,String location) {

		String browser = "Unknown";
        if (userAgent.contains("Chrome")) {
            browser = "Google 	Chrome";
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
			System.out.println(ip+"location - "+location);
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into sessions(user_id,session_value,agent,location,ipaddress) values(?,?,?,?,?)");
			stmt.setInt(1, user_id);
			stmt.setString(2, jwtToken);
			stmt.setString(3, os+" - "+browser);
			stmt.setString(4, location);
			stmt.setString(5, ip);
			int affected = stmt.executeUpdate();
			return affected>0;
		} catch (Exception e) {
			e.printStackTrace();
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
