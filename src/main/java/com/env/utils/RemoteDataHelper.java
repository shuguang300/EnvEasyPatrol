package com.env.utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.env.bean.RequestResult;

public class RemoteDataHelper {
	
	
	public static String setMobile(MultipartEntity me,String method){
    	HttpPost request = new HttpPost(HttpUtil.URL_Datacenter+method);
    	request.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, HttpUtil.CONNECT_TIME_OUT);
    	request.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, HttpUtil.READ_TIME_OUT);
    	request.setEntity(me);
		try {
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
	
	public static String getMobile(String EquipID){
		HttpGet getRequest = new HttpGet(HttpUtil.URL_Datacenter+"GetDevice&Device="+EquipID);
		getRequest.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, HttpUtil.CONNECT_TIME_OUT);
		getRequest.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, HttpUtil.READ_TIME_OUT);
		try {
			HttpResponse response = new DefaultHttpClient().execute(getRequest);
			if(response.getStatusLine().getStatusCode()==200){
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity, "utf-8");
				return result;
			}else if (response.getStatusLine().getStatusCode()==500) {
				return "false";          
			}else if(response.getStatusLine().getStatusCode()==404) {
				return "false";
			}else {
				return "false";
			}	
		} catch (Exception e) {
			e.printStackTrace();
			return "false";
		}
	}
	
	public static RequestResult getRightData(int plantId) {
		RequestResult rs = new RequestResult();
		HttpPost post = new HttpPost(HttpUtil.URL_Datacenter+"GetRightDataByPlantID&PlantID="+plantId);
		post.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, HttpUtil.CONNECT_TIME_OUT);
		post.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, HttpUtil.READ_TIME_OUT);
		try {
			HttpResponse response = new DefaultHttpClient().execute(post);
			if(response.getStatusLine().getStatusCode()==200){
				rs.setErrorcode(RequestResult.NO_ERROR);
				String result = EntityUtils.toString(response.getEntity());
				Log.v("data", result);
				rs.setData(result);
			}else {
				rs.setErrorcode(0);
			}
		} catch (ClientProtocolException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rs;
	}

	public static String uploadTaskOnlyText(MultipartEntity entity){
		HttpPost post = new HttpPost(HttpUtil.URL_Datacenter+"SaveUploadTask");
		post.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, HttpUtil.CONNECT_TIME_OUT);
		post.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, HttpUtil.READ_TIME_OUT);
		post.setEntity(entity);
		try{
			HttpResponse response = new DefaultHttpClient().execute(post);
			if(response.getStatusLine().getStatusCode() == 200){
				String result = EntityUtils.toString(response.getEntity(),"utf-8");
				Log.v("uploadResult", result);
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return  "false";
	}

	public static String uploadTaskMedia(MultipartEntity entity){
		HttpPost post = new HttpPost(HttpUtil.URL_Datacenter+"SaveUploadTaskMedia");
		post.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, HttpUtil.CONNECT_TIME_OUT);
		post.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, HttpUtil.READ_TIME_OUT);
		post.setEntity(entity);
		try{
			HttpResponse response = new DefaultHttpClient().execute(post);
			if(response.getStatusLine().getStatusCode() == 200){
				String result = EntityUtils.toString(response.getEntity(),"utf-8");
				Log.v("uploadMediaResult", result);
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "false";
	}

	public static RequestResult getNewestApkInfo(){
		HttpGet get = new HttpGet(HttpUtil.URL_Datacenter+"GetNewestAppInfo");
		get.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,HttpUtil.CONNECT_TIME_OUT);
		get.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, HttpUtil.READ_TIME_OUT);
		RequestResult rs = new RequestResult();
		try{
			HttpResponse response = new DefaultHttpClient().execute(get);
			if(response.getStatusLine().getStatusCode() == 200){
				String result = EntityUtils.toString(response.getEntity(),"utf-8");
				if(result!=null && result.length()>0){
					rs.setErrorcode(RequestResult.NO_ERROR);
					rs.setData(result);
				} else{
					rs.setErrorcode(RequestResult.SERVER_ERROR);
				}
				Log.v("getNewestApkInfoResult", result);
			}else{
				rs.setErrorcode(RequestResult.SERVER_ERROR);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			rs.setErrorcode(RequestResult.IOEXCEPTION_ERROR);
			rs.setErrormsg(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			rs.setErrorcode(RequestResult.IOEXCEPTION_ERROR);
			rs.setErrormsg(e.getMessage());
		}
		return rs;
	}

	public static String UploadLogFiles(MultipartEntity mpe){
		HttpPost post = new HttpPost(HttpUtil.URL_Datacenter+"SaveUploadLogFile");
		post.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,HttpUtil.CONNECT_TIME_OUT);
		post.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, HttpUtil.READ_TIME_OUT);
		post.setEntity(mpe);
		try{
			HttpResponse response = new DefaultHttpClient().execute(post);
			if(response.getStatusLine().getStatusCode() == 200){
				String result = EntityUtils.toString(response.getEntity(),"utf-8");
				Log.v("UploadLogFiles", result);
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "false";
	}
}
