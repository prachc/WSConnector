package com.prach.mashup.wsconnector;

import java.util.Arrays;
import java.util.Vector;

import org.json.JSONException;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

public class WSCService extends Service {
	private static final String TAG = "com.prach.mashup.WSCService";
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return new WSCServiceBinder();
	}
	
	public void debug(String msg){
		Log.d(TAG,msg);
	}
	
	private class WSCServiceBinder extends Binder {
		@Override
		protected synchronized boolean onTransact(int code, Parcel data, Parcel reply,int flags) {
			if (code == 0x101) {
				Bundle bundle = data.readBundle();
				
				String base = bundle.getString("BASE"); 
				String[] paths = bundle.getStringArray("PATHS");
				String[] keys = bundle.getStringArray("KEYS");
				String[] values = bundle.getStringArray("VALUES");
				String format = bundle.getString("FORMAT"); //SELF, XML, JSON
				
				String[] name = bundle.getStringArray("NAME");
				String[] type = bundle.getStringArray("TYPE");  //single, multiple
				String[] query = bundle.getStringArray("QUERY"); //null(SELF), xpath(XML), jsonpath(JSON)
				String[] index = bundle.getStringArray("INDEX"); //
				//String outputname = bundle.getString("OUTPUT_NAME");
				//String[] outputs = null;
				
				debug("base==null:" + (base == null));
				debug("paths==null:" + (paths == null));
				debug("keys==null:" + (keys == null));
				debug("values==null:" + (values == null));
				debug("format==null:" + (format == null));
				
				debug("name==null:" + (name == null));
				debug("type==null:" + (type == null));
				debug("query==null:" + (query == null));
				debug("index==null:" + (index == null));
				//debug("outputname:" + outputname);
				debug("logic:" + (base != null && format!=null));

				if (base != null && format!=null) {
					System.out.println("URL="+WSCLibrary.genUrl(base,paths,keys,values));
					String msg = WSCLibrary.getMessage(WSCLibrary.genUrl(base,paths,keys,values));
					bundle = new Bundle();
					//debug(msg);
					
					if(msg==null){
						debug( "connection failed");
						for (int i = 0; i < name.length; i++) {
							bundle.putStringArray(name[i], new String[]{""});
						}
						//outputs = null;
						//bundle.putStringArray("OUTPUTS", outputs);
						reply.writeBundle(bundle);
						debug("finished(connection failed)");
						//mHandler.sendEmptyMessage(FINISH_MSG);
						return false;
					}else if(format.equals("XML")){
						try {
							XMLParser xparser = new XMLParser(msg);
							xparser.setTAG(TAG);
							//debug( "XML mode is not yet implemented");
							for (int i = 0; i < name.length; i++) {
								if(type[i].equals("single")){
									if(index[i].equals("null")){
										bundle.putStringArray(name[i],xparser.getArray(query[i]));
									}else{
										int rindex = Integer.parseInt(index[i]);
										bundle.putStringArray(name[i],new String[]{xparser.getArray(query[i])[rindex]});
									}
								}else if(type[i].equals("multiple")){
									if(index[i].equals("null")){
										bundle.putStringArray(name[i],xparser.getArray(query[i]));
									}else if(index[i].contains(",")){
										String[] fullArray = xparser.getArray(query[i]);
										
										String[] sindices = index[i].split(",");
										Vector<String> v = new Vector<String>();
										//int[] rindices = new int[sindices.length];
										for (int j = 0; j < sindices.length; j++)
											v.add(fullArray[Integer.parseInt(sindices[j])]);
										
										String[] cutArray = Arrays.copyOf(v.toArray(), v.size(), String[].class);
										bundle.putStringArray(name[i],cutArray);
									}
								}
							}
						}catch (Exception e) {
							debug(e.toString());
							for (int i = 0; i < name.length; i++) {
								bundle.putStringArray(name[i], new String[]{""});
							}
						}
					}else if(format.equals("JSON")){
						try {
							JsonParser jparser = new JsonParser(msg);
							jparser.setTAG(TAG);
							///name type query index
							for (int i = 0; i < name.length; i++) {
								if(type[i].equals("single")){
									if(index[i].equals("null")){
										bundle.putStringArray(name[i],jparser.getArray(query[i]));
									}else{
										int rindex = Integer.parseInt(index[i]);
										bundle.putStringArray(name[i],new String[]{jparser.getArray(query[i])[rindex]});
									}
								}else if(type[i].equals("multiple")){
									if(index[i].equals("null")){
										bundle.putStringArray(name[i],jparser.getArray(query[i]));
									}else if(index[i].contains(",")){
										String[] fullArray = jparser.getArray(query[i]);
										
										String[] sindices = index[i].split(",");
										Vector<String> v = new Vector<String>();
										//int[] rindices = new int[sindices.length];
										for (int j = 0; j < sindices.length; j++)
											v.add(fullArray[Integer.parseInt(sindices[j])]);
										
										String[] cutArray = Arrays.copyOf(v.toArray(), v.size(), String[].class);
										bundle.putStringArray(name[i],cutArray);
									}
								}
							}
							//outputs = jparser.getArray(query);
						} catch (JSONException e) {
							debug(e.toString());
							for (int i = 0; i < name.length; i++) {
								bundle.putStringArray(name[i], new String[]{""});
							}
						}
					}else if(format.equals("SELF")){
						for (int i = 0; i < name.length; i++) {
							bundle.putStringArray(name[i],new String[]{msg});
						}
						
					}
					debug("finished(true)");
					//bundle = new Bundle();
					//bundle.putStringArray("OUTPUTS", outputs);
					reply.writeBundle(bundle);
					//reply.writeStringArray(outputs);
					//mHandler.sendEmptyMessage(FINISH_MSG);
					return true;
				}else{
					//outputs = null;
					bundle = new Bundle();
					//bundle.putStringArray("OUTPUTS", outputs);
					for (int i = 0; i < name.length; i++) {
						bundle.putStringArray(name[i], new String[]{""});
					}
					reply.writeBundle(bundle);
					debug("finished(false)");
					//mHandler.sendEmptyMessage(FINISH_MSG);
					return false;
				}	
			}else{
				Log.e(getClass().getSimpleName(),"Transaction code should be "+ 0x101 + ";"	+ " received instead " + code);
				return false;
			}
			
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
	}
}
