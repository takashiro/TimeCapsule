package org.mogara.sunny.timecapsule;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FileServer {
	public static final String BASIC_URL = "http://120.27.100.136/";
	
	public static String upload(String fileName, InputStream input){
		String twoHyphens = "--";
		String boundary = "*****";
		String lineEnd = "\r\n";
		
		try {
			URL api = new URL(BASIC_URL + "uploader.php");
			HttpURLConnection connection = (HttpURLConnection) api.openConnection();
			
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		
			DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName + "\"" + lineEnd);
			dos.writeBytes(lineEnd);

			byte[] buffer = new byte[1024];
			do {
				int byteRead = input.read(buffer);
				if (byteRead == -1)
					break;
				dos.write(buffer, 0, byteRead);
			} while (true);
			
			dos.writeBytes(lineEnd);
	        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	        
	        input.close();
	        dos.flush();
	        dos.close();
	        
	        InputStream dis = connection.getInputStream();
			int length = dis.read(buffer);
			String result = new String(buffer, 0, length);
			return BASIC_URL + result;
	        
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
