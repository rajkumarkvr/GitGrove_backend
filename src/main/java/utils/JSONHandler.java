package utils;

import java.io.BufferedReader;
import java.io.IOException;

import org.json.JSONObject;

public class JSONHandler {

	public static JSONObject parse(BufferedReader reader) throws IOException {
		StringBuilder sb = new StringBuilder();

		String line;

		while((line=reader.readLine())!=null) {
	
			sb.append(line);
		}

		return new JSONObject(sb.toString());
	}
}
