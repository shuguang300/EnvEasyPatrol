package com.env.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;

public class HttpUtil {
	private static HttpUtil INSTANCE = null;
	public static final int CONNECT_TIME_OUT = 60000;
	public static final int READ_TIME_OUT = 90000;
	public static String IP_ADDRESS;
	public static String URL_Datacenter;
	public static String URL_UploadLogFile;

    private HttpUtil (){
    	
    }
    
    public static HttpUtil getInstance(Context context){
    	if(INSTANCE == null){    		
    		INSTANCE = new HttpUtil();
    	}
    	return INSTANCE;
    }
    
    public void setIPAddr(String ip){
    	IP_ADDRESS = ip;
    	URL_UploadLogFile = "http://"+IP_ADDRESS+"/Data2EasyPatrol/UploadLogFile.ashx";
    	URL_Datacenter = "http://"+IP_ADDRESS+"/Data/DataService_Mobile.aspx?method=";
    }

    public String getIPAddr(){
    	return IP_ADDRESS;
    }

    
	public String HTTPClientPost(String url,String method,JSONObject params){
		HttpPost request = new HttpPost(url+method);
		request.addHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8");
		request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIME_OUT);
		request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, READ_TIME_OUT);
		try {
			if(params!=null){
				HttpEntity  paramEntity = new StringEntity(params.toString(),"utf-8");
				request.setEntity(paramEntity);				
			}
			HttpResponse response =  new DefaultHttpClient().execute(request);
			if(response.getStatusLine().getStatusCode()==200){
				String result = EntityUtils.toString(response.getEntity());
				return result;
			}else if (response.getStatusLine().getStatusCode()==500) {
				return "false";          
			}else if(response.getStatusLine().getStatusCode()==404) {
				return "false";
			}else {
				return "false";
			}
		} catch (ClientProtocolException e) {			
			e.printStackTrace();
			return "false";              
		} catch (IOException e) {
			e.printStackTrace();
			return "false";
		}
	}
	


	public  String HTTPClientPostLogFile(MultipartEntity me){
		HttpPost post = new HttpPost(URL_UploadLogFile);
		post.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIME_OUT);
		post.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, READ_TIME_OUT);
		post.setHeader("charset", HTTP.UTF_8);
		post.setEntity(me);
		try {
			HttpResponse response = new DefaultHttpClient().execute(post);
			if(response.getStatusLine().getStatusCode()==200){
				return EntityUtils.toString(response.getEntity());
			}else if (response.getStatusLine().getStatusCode()==500) {
				return "false";          
			}else if(response.getStatusLine().getStatusCode()==404) {
				return "false";
			}else {
				return "false";
			}					
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return "false";
		} catch (IOException e) {
			e.printStackTrace();
			return "false";
		}
	}
	

	public String UrlConnectionPost(String target,String key,String value){
		String result = "";
		URL url = null;
		HttpURLConnection conn = null;
		InputStream is = null;		
		try {
			byte [] data = (key+"="+value).getBytes(HTTP.UTF_8);
			byte [] temp = new byte[10240];
			url = new URL(target);
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(false);
//			conn.setChunkedStreamingMode(102400);
			conn.setFixedLengthStreamingMode(data.length);
			conn.setRequestProperty(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");			
			OutputStream os = conn.getOutputStream();
			int pos;			
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			while ((pos = bais.read(temp))!=-1) {
				os.write(temp,0,pos);
			}
//			os.flush();
			os.close();
			conn.connect();
			is = conn.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((pos=is.read(temp))!=-1) {
				baos.write(temp,0,pos);
			}
			baos.flush();
			baos.close();
			result = new String(baos.toByteArray(),HTTP.UTF_8);	
		} catch (MalformedURLException e) {
			e.printStackTrace();
			result = "false";
		} catch (IOException e) {
			e.printStackTrace();
			result = "false";
		}finally{
			if(is!=null){
				try {
					is.close();
					conn.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		}
		return result.toString();
	}
	public String UrlConnectionGet(String target){
		String result = "";
		URL url  = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			url = new URL(target);
			conn = (HttpURLConnection)url.openConnection();
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(false);
//			conn.setRequestProperty(HTTP.CONTENT_TYPE, "application/json; charset=utf-8");
			conn.connect();
			is = conn.getInputStream();
			byte [] temp = new byte [1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (is.read(temp)!=-1) {
				baos.write(temp);
			}
			baos.flush();
			baos.close();
			result = new String(baos.toByteArray(),HTTP.UTF_8);			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			result = "false";
		} catch (IOException e) {
			e.printStackTrace();
			result = "false";
		}finally{
			if(is!=null){
				try {
					is.close();
					conn.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		return result.toString();
	}
}
