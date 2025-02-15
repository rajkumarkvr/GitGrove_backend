package utils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public class IPLocationInfo {
	 public static String getLocationInfo(String ipAddress) {
	        try {
	            String apiUrl = "http://ip-api.com/json/" + ipAddress;
	            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
	            connection.setRequestMethod("GET");
	            
	            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	            StringBuilder response = new StringBuilder();
	            String line;
	            while ((line = reader.readLine()) != null) {
	                response.append(line);
	            }
	            reader.close();

	            JSONObject json = new JSONObject(response.toString());
	            System.out.println(json);  // Debugging: Print API response

	            // Check if keys exist before accessing them
	            String country = json.optString("country", "Unknown Country");
	            String city = json.optString("city", "Unknown City");

	            return city + ", " + country;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return "Unknown Location";
	        }
	    }
	
	public static String getIPAddress(HttpServletRequest request) {
	      String ipAddress = request.getHeader("X-Forwarded-For");
	        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
	            ipAddress = request.getHeader("X-Real-IP");
	        }
	        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
	            ipAddress = request.getRemoteAddr();
	        }
			return ipAddress;
	}
}
