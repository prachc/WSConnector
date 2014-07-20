package com.prach.mashup.wsconnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class WSCLibrary {
	private final static String TAG = "com.prach.mashup.WSCLibrary";
	
	public static void debug(String msg){
		Log.d(TAG,msg);
	}
	
	public static synchronized String getMessage(String sUrl) {
		//addLog("connecting...");
		System.out.println(sUrl);
		debug(sUrl);
		//debug("url="+sUrl);
		HttpClient objHttp = new DefaultHttpClient();
		HttpParams params = objHttp.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 60000); // 接続のタイムアウト
		HttpConnectionParams.setSoTimeout(params, 60000); // データ取得のタイムアウト
		String sReturn = "";
		try {
			HttpGet objGet = new HttpGet(sUrl);
			HttpResponse objResponse = objHttp.execute(objGet);
			if (objResponse.getStatusLine().getStatusCode() < 400) {
				InputStream objStream = objResponse.getEntity().getContent();
				InputStreamReader objReader = new InputStreamReader(objStream);
				BufferedReader objBuf = new BufferedReader(objReader);
				StringBuilder objMessage = new StringBuilder();
				String sLine;
				while ((sLine = objBuf.readLine()) != null) {
					objMessage.append(sLine);
				}
				sReturn = objMessage.toString();
				objStream.close();
			}
		} catch (IOException e) {
			debug("getMessage():"+e.toString());
			debug("getMessage():connection failed, return=null");
			//addLog("WSConnector: connectiion failed");
			return null;
		}
		//addLog("WSConnector: connectiion successful");
		return sReturn;
	}
	
	public static synchronized String genUrl(String base,String[] paths, String[] keys, String[] values){
		StringBuffer result = new StringBuffer();
		if(base.endsWith("/"))
			result.append(base.substring(0,base.length()-1));
		else
			result.append(base);
		
		for (int i = 0; i < paths.length; i++) {
			result.append("/");
			if(!paths[i].equals("null"))
				result.append(paths[i]);
		}
		
		result.append("?");
		
		for (int i = 0; i < keys.length; i++) {
			result.append(keys[i]+"="+URLEncoder.encode(values[i]));
			if(i!=keys.length-1)
				result.append("&");	
		}
		
		return result.toString();
	}
}
