package utils;

import javax.servlet.http.Cookie;

public class CookieUtil {
	
	private static final String COOKIE_KEY = "gitgrove_";
	static CookieUtil cookie = null;
	
	private CookieUtil() {
		
	}
	
	public static CookieUtil getInstance() {
		if(cookie == null) {
			cookie = new CookieUtil();
		}
		return cookie;
	}
	
	public Cookie getCookie(String username,String token) {
		Cookie cookie = new Cookie(COOKIE_KEY+username, token);
		return cookie;
	}
	
}
