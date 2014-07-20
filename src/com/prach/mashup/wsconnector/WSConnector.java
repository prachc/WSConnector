package com.prach.mashup.wsconnector;

import java.util.Arrays;
import java.util.Vector;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class WSConnector extends Activity {
	private final String TAG = "com.prach.mashup.WSConnector";
	private TextView tv;
	private StringBuffer log = new StringBuffer();
    /** Called when the activity is first created. */
	
	public String base;
	public String[] paths; 
	public String[] keys;
	public String[] values;
	public String format;
	
	public String[] name;
	public String[] type;
	public String[] query;
	public String[] index;
	//public String[] outputs;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tv = (TextView)findViewById(R.id.tv);
        tv.setHorizontallyScrolling(true);
    }
    
    public void debug(String msg){
		Log.d(TAG,msg);
	}
    
    public void addLog(String msg){
    	log.append(msg);
    	log.append("\n");
    	tv.setText(log.toString());
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		log = new StringBuffer();
		addLog("WSConnector: external intent call");
		
		Intent intent = getIntent();
		
		base = intent.getStringExtra("BASE"); 
		paths = intent.getStringArrayExtra("PATHS");
		keys = intent.getStringArrayExtra("KEYS");
		values = intent.getStringArrayExtra("VALUES");
		format = intent.getStringExtra("FORMAT");
		
		name = intent.getStringArrayExtra("NAME");
		type = intent.getStringArrayExtra("TYPE");
		query = intent.getStringArrayExtra("QUERY");
		index = intent.getStringArrayExtra("INDEX");
		
		debug("intent==null:" + (intent == null));
		debug("base==null:" + (base == null));
		debug("paths==null:" + (paths == null));
		debug("keys==null:" + (keys == null));
		debug("values==null:" + (values == null));
		debug("format==null:" + (format == null));
		
		debug("name==null:" + (name == null));
		debug("type==null:" + (type == null));
		debug("query==null:" + (query == null));
		debug("index==null:" + (index == null));
		
		debug("logic:" + (intent != null && base != null && format!=null));

		if (intent != null && base != null && format!=null) {
			addLog("mode ="+format);
			String msg = WSCLibrary.getMessage(WSCLibrary.genUrl(base,paths,keys,values));
			if(msg==null){
				for (int i = 0; i < name.length; i++) {
					intent.putExtra(name[i], new String[]{""});
				}
				//intent.putExtra("DEBUG", "connection failed");
				this.setResult(Activity.RESULT_OK, intent);
				debug( "connection failed");
			}else if(format.equals("JSON")){
				try {
					JsonParser jparser = new JsonParser(msg);
					jparser.setTAG(this.TAG);
					///name type query index
					for (int i = 0; i < name.length; i++) {
						if(type[i].equals("single")){
							if(index[i].equals("null")){
								intent.putExtra(name[i],jparser.getArray(query[i]));
							}else{
								int rindex = Integer.parseInt(index[i]);
								intent.putExtra(name[i],new String[]{jparser.getArray(query[i])[rindex]});
							}
						}else if(type[i].equals("multiple")){
							if(index[i].equals("null")){
								intent.putExtra(name[i],jparser.getArray(query[i]));
							}else if(index[i].contains(",")){
								String[] fullArray = jparser.getArray(query[i]);
								
								String[] sindices = index[i].split(",");
								Vector<String> v = new Vector<String>();
								//int[] rindices = new int[sindices.length];
								for (int j = 0; j < sindices.length; j++)
									v.add(fullArray[Integer.parseInt(sindices[j])]);
								
								String[] cutArray = Arrays.copyOf(v.toArray(), v.size(), String[].class);
								intent.putExtra(name[i],cutArray);
							}
						}
					}
					//outputs = jparser.getArray(query);
				} catch (JSONException e) {
					debug("JSONException:"+e.toString());
					for (int i = 0; i < name.length; i++) {
						intent.putExtra(name[i], new String[]{""});
					}
				}
			}else if(format.equals("SELF")){
				for (int i = 0; i < name.length; i++) {
					intent.putExtra(name[i],new String[]{msg});
				}
				
				//outputs = new String[]{msg};
				
			}else if(format.equals("XML")){
				//outputs = new String[]{""};
				debug( "XML mode is not yet implemented");
				for (int i = 0; i < name.length; i++) {
					intent.putExtra(name[i], new String[]{""});
				}
			}
			//intent.putExtra("OUTPUTS", outputs);
			this.setResult(Activity.RESULT_OK, intent);
			debug( "finish();");
			finish();
		}else{
			log=new StringBuffer(); 
			addLog("WSConnector:");
			addLog("This application cannot be run in stand-alone mode,");
			addLog("Intent call required");
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
	}
}