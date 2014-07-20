package com.prach.mashup.wsconnector;

import org.json.JSONException;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WSCServiceAIDL extends Service {
	private static final String TAG = "WSCServiceAIDL";
	
	public void debug(String msg){
		Log.d(TAG,msg);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return new IWSCService.Stub(){
			public String[] connectWS(String mode, String query,String base,String[] paths,String[] keys,String[] values){ 
				String[] outputs = null;
				
				debug("base==null:" + (base == null));
				debug("paths==null:" + (paths == null));
				debug("keys==null:" + (keys == null));
				debug("values==null:" + (values == null));
				debug("mode==null:" + (mode == null));
				debug("query==null:" + (query == null));
				debug("logic:" + (base != null && mode!=null));

				if (base != null && mode!=null) {
					String msg = WSCLibrary.getMessage(WSCLibrary.genUrl(base,paths,keys,values));
					debug(msg);
					if(msg==null){
						debug( "connection failed");
						outputs =  null;
						debug("finished 1");
						return null;
					}else if(mode.equals("JSON")){
						
						JsonParser jparser;
						try {
							jparser = new JsonParser(msg);
							jparser.setTAG(TAG);
							outputs = jparser.getArray(query);
						} catch (JSONException e) {
							debug(e.toString());
							outputs = new String[]{""};
						}
					}else if(mode.equals("SELF")){
						outputs = new String[]{msg};
						
					}else if(mode.equals("XML")){
						outputs = new String[]{""};
						debug( "XML mode is not yet implemented");
					}
					debug("finished 2");
					return outputs;
				}else{
					outputs = null;
					debug("finished 3");
					return null;
				}
			}
		};
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
	}
}
