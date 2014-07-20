package com.prach.mashup.wsconnector;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonParser {
	private String TAG;
	private String JsonMessage;
	public JSONObject JsonObject;
	
	public JsonParser(String jsonmessage) throws JSONException{
		JsonMessage = jsonmessage;
		JsonObject = new JSONObject(JsonMessage);
		//debug(JsonObject.toString(3));
	}
	
	public JsonParser(JSONObject jsonobj){
		JsonObject = jsonobj;
	}
	
	public void setTAG(String tag){
		TAG = tag;
	}
	
	public void debug(String msg){
		//System.out.println(msg);
		Log.i(TAG,msg);
	}
	
    public String getValue(String notation){
    	String[] namespaces;
    	if(notation.indexOf(".")==-1)
    		namespaces = new String[]{notation};
    	else
    		namespaces = notation.split("\\.");
    	
    	String[][] objects = new String[namespaces.length][];
    	
    	for (int i = 0; i < objects.length; i++) {
			objects[i] = new String[2];
			
			if(!namespaces[i].endsWith("]")){
				objects[i][0] = namespaces[i];
				objects[i][1] = null;
			}else{
				objects[i][0] = namespaces[i].substring(0,namespaces[i].indexOf("["));
				objects[i][1] = namespaces[i].substring(namespaces[i].indexOf("[")+1,namespaces[i].indexOf("]"));
			}
		}
    	
    	debug("getValue()");
    	
    	JSONObject tobj = JsonObject;
    	if(tobj==null)
    		return null;
    	JSONArray tarray = null;
    	
    	for (int i = 0; i < objects.length-1; i++) {
    		debug(objects[i][0]+":"+objects[i][1]);
    		if(!isArrayIndex(objects[i])){
				tobj = tobj.optJSONObject(objects[i][0]);
				//isAfterObject = true;
			}else{
				tarray = tobj.optJSONArray(objects[i][0]);
				if(tarray!=null)
					tobj = tarray.optJSONObject(Integer.parseInt(objects[i][1]));
				else 
					tobj = null;
			}
    		//debug(objects[i][0]+":"+objects[i][1]);
		}
    	if(!isArrayIndex(objects[objects.length-1])){
    		debug(objects[objects.length-1][0]+":"+objects[objects.length-1][1]);
    		if(tobj!=null)
    			return tobj.optString(objects[objects.length-1][0]);
    	}else{
    		debug(objects[objects.length-1][0]+":"+objects[objects.length-1][1]);
    		if(tobj!=null){
    			tarray = tobj.optJSONArray(objects[objects.length-1][0]);
    			if(tarray!=null)
    				return tarray.optString(Integer.parseInt(objects[objects.length-1][1]));
    		}
    	}
    	return null;
    }
    
    public String[] getArray(String notation){
    	String[] namespaces;
    	if(notation.indexOf(".")==-1){
    		namespaces = new String[]{notation};}
    	else
    		namespaces = notation.split("\\.");
    	
    	String[][] objects = new String[namespaces.length+1][];
    	
    	for (int i = 0; i < objects.length-1; i++) {
			objects[i] = new String[2];
			
			if(!namespaces[i].endsWith("]")){
				objects[i][0] = namespaces[i];
				objects[i][1] = "null";
			}else{
				objects[i][0] = namespaces[i].substring(0,namespaces[i].indexOf("["));
				objects[i][1] = namespaces[i].substring(namespaces[i].indexOf("[")+1,namespaces[i].indexOf("]"));
			}
		}
    	objects[objects.length-1] = new String[2];
    	objects[objects.length-1][0] = "end";
    	objects[objects.length-1][1] = "null";
    	    	
    	Vector<String> output = new Vector<String>();
    	JSONObject tobj = JsonObject;
    	if(tobj==null)
    		return null;

    	JSONArray tarray = null;
    	
    	int caseindex = getCase(objects);
    	int numindex1 = -1;
    	JSONArray jsonarraynum1 = null;
    	int allindex1 = -1;
    	JSONArray jsonarrayall1 = null;
    	@SuppressWarnings("unused")
		int allindex2 = -1;
    	
    	for (int i = 0; i < objects.length; i++) {
    		//debug("objects["+i+"]="+objects[i][0]+","+objects[i][1]);
    		//debug(tobj.names().optString(0));
			if(isArrayIndex(objects[i])){
				if(!isEnd(objects[i+1])){
				switch (caseindex) {
					case 2: //all num text
						numindex1 = i;
						//debug("allindex:"+allindex1+", numindex:"+numindex1);
						jsonarraynum1 = tobj.optJSONArray(objects[i][0]);
						break;
					case 3: //all num text
						numindex1 = i;
						//debug("allindex:"+allindex1+", numindex:"+numindex1);
						if(allindex1<numindex1) //num all
							if(allindex1<0)
								jsonarraynum1 = tobj.optJSONArray(objects[i][0]);
							else
								;
						else // all num
							jsonarraynum1 = tobj.optJSONArray(objects[i][0]);
						break;
					default:
						tarray = tobj.optJSONArray(objects[i][0]);
						tobj = tarray.optJSONObject(Integer.parseInt(objects[i][1]));
						break;
					}
				}
			}else if(isArrayAll(objects[i])){
				switch (caseindex) {
				case 2: // one all
					allindex1 = i;
					jsonarrayall1 = tobj.optJSONArray(objects[i][0]);
					break;
				case 3: // all num, num all
					allindex1 = i;
					//debug("allindex:"+allindex1+", numindex:"+numindex1);
					if(allindex1>numindex1) //num all
						if(numindex1<0)
							jsonarrayall1 = tobj.optJSONArray(objects[i][0]);
						else;
					else // all num
						jsonarrayall1 = tobj.optJSONArray(objects[i][0]);
					break;
				case 4: // two all
					if(allindex1==-1){
						allindex1 = i;
						jsonarrayall1 = tobj.optJSONArray(objects[i][0]);
					}else{
						allindex2 = i;
					}
					//debug("allindex1:"+allindex1+", allindex2:"+allindex2);
					break;
				}
			}else if(isEnd(objects[i])){
				if(isArrayIndex(objects[i-1])){
					//debug("allindex:"+allindex1+", numindex:"+numindex1);
					//array index
					switch (caseindex) {
					case 3: // all end[num]
						for (int j = 0; j < jsonarrayall1.length(); j++) {
							JsonParser jparser = new JsonParser(jsonarrayall1.optJSONObject(j));
							output.add(jparser.getArray(getNotation(objects,allindex1))[0]);
						}
						break;
					default:
						tarray = tobj.optJSONArray(objects[i-1][0]);
						output.add(tarray.optString(Integer.parseInt(objects[i-1][1])));
						break;
					}
					
				}else if(isArrayAll(objects[i-1])){
					//end with all, no all before
					switch (caseindex) { // 1 all, 0 num (all at the end)
					case 2: // text end[all] 
						tarray = tobj.optJSONArray(objects[i-1][0]);
						for (int j = 0; j < tarray.length(); j++) {
							output.add(tarray.optString(j));
						}
						break;
					case 3: // num end[all]
						//debug("allindex:"+allindex1+", numindex:"+numindex1);
						JsonParser jparser = new JsonParser(jsonarraynum1.optJSONObject(Integer.parseInt(objects[numindex1][1])));
						add(output,jparser.getArray(getNotation(objects,numindex1)));
						break;
					case 4:
						//debug("allindex1:"+allindex1+", allindex2:"+allindex2);
						for (int j = 0; j < jsonarrayall1.length(); j++) {
							JsonParser jparser4 = new JsonParser(jsonarrayall1.optJSONObject(j));
							add(output,jparser4.getArray(getNotation(objects,allindex1)));
						}
						break;
					default:
						break;
					}
					
				}else{
					//end with text
					//debug("allindex:"+allindex1+","+allindex2);
					switch (caseindex) {
					case 2: // all text
						for (int j = 0; j < jsonarrayall1.length(); j++) {
							JsonParser jparser = new JsonParser(jsonarrayall1.optJSONObject(j));
							String[] temp = jparser.getArray(getNotation(objects,allindex1));
							if(temp.length>0)
								output.add(jparser.getArray(getNotation(objects,allindex1))[0]);
						}
						break;
					case 3: // all num text , num all text
						//num all text
						if(allindex1>numindex1){
							//debug("allindex:"+allindex1+", numindex:"+numindex1);
							JsonParser jparser = new JsonParser(jsonarraynum1.optJSONObject(Integer.parseInt(objects[numindex1][1])));
							add(output,jparser.getArray(getNotation(objects,numindex1)));
						}else //all num text
							for (int j = 0; j < jsonarrayall1.length(); j++) {
								JsonParser jparser = new JsonParser(jsonarrayall1.optJSONObject(j));
								String[] temp = jparser.getArray(getNotation(objects,allindex1));
								if(temp.length>0)
									output.add(jparser.getArray(getNotation(objects,allindex1))[0]);
							}
						break;
					case 4: // all all text
						for (int j = 0; j < jsonarrayall1.length(); j++) {
							JsonParser jparser = new JsonParser(jsonarrayall1.optJSONObject(j));
							add(output,jparser.getArray(getNotation(objects,allindex1)));
						}
						break;
					default: // text.text , num text, num num text 
						output.add(tobj.optString(objects[i-1][0]));
					}
				}
			}else{
				//debug(tobj.names().optString(0));
				//just a name
				if(!isEnd(objects[i+1]))
					tobj = tobj.optJSONObject(objects[i][0]);
			}
		}
    	
    	return toArray(output);
    }
    
    public String getNotation(String[][] objects,int from){
    	StringBuffer temp = new StringBuffer();
    	//debug("objects.length:"+objects.length);
    	for (int i = from+1; i < objects.length; i++) {
    		if(objects[i][0].equals("end"))
    			continue;
    		if(objects[i][1].equals("null")){
    			temp.append(objects[i][0]);
    			temp.append(".");
    		}else{
    			temp.append(objects[i][0]);
    			temp.append("[");
    			temp.append(objects[i][1]);
    			temp.append("]");
    			temp.append(".");
    		}
		}
    	temp.deleteCharAt(temp.length()-1);
    	//debug("notation:"+temp.toString());
    	return temp.toString();
    }
    
    public int getCase(String[][] objects){
    	int countall = 0;
    	int countnum = 0;
    	
    	for (int i = 0; i < objects.length; i++) {
			if(isArrayAll(objects[i])){
				countall++;
			}else if(isArrayIndex(objects[i])){
				countnum++;
			}
		}
    	
    	if(countall==0&&countnum==0){
    		return 0;
    	}else if(countall==0&&countnum==1){
    		return 1;
    	}else if(countall==1&&countnum==0){
    		return 2; //OK
    	}else if(countall==1&&countnum==1){
    		return 3; //OK
    	}else if(countall==2&&countnum==0){
    		return 4;
    	}else if(countall==0&&countnum==2){
    		return 5;
    	}
		return -1;
    }
    
    public String getArrayString(String notation){
    	String[] temp = getArray(notation);
    	StringBuffer tbuffer = new StringBuffer();
    	tbuffer.append("[");
    	for (int i = 0; i < temp.length; i++) {
			tbuffer.append(temp[i]);
			tbuffer.append(",");
		}
    	tbuffer.deleteCharAt(tbuffer.length()-1);
    	tbuffer.append("]");
    	return tbuffer.toString();
    }
    
    public String getArrayString(String notation,int indent){
    	String[] temp = getArray(notation);
    	StringBuffer tbuffer = new StringBuffer();
    	tbuffer.append("[\n");
    	for (int i = 0; i < temp.length; i++) {
			tbuffer.append(tab(indent));
    		tbuffer.append(temp[i]);
			tbuffer.append(",");
		}
    	tbuffer.deleteCharAt(tbuffer.length()-1);
    	tbuffer.append("\n]");
    	return tbuffer.toString();
    }
    
    public String tab(int indent){
    	StringBuffer tbuffer = new StringBuffer();
    	for (int i = 0; i < indent; i++) {
			tbuffer.append(" ");
		}
    	return tbuffer.toString();
    }
    
    public boolean isArrayIndex(String[] notation){
    	if(notation[1].equals("null")||notation[1].equals("all")) 
    		return false;
    	else 
    		return true;
    }
    
    public boolean isEnd(String[] notation){
    	if(notation[0].equals("end")) 
    		return true;
    	else 
    		return false;
    }
    
    public boolean isArrayAll(String[] notation){
    	if(notation[1].equals("all"))
    		return true;
    	else
    		return false;
    }
    
    public void debugObjects(String[][] objects){
    	debug("debugObjects()");
    	for (int i = 0; i < objects.length; i++) {
			debug(objects[i][0]+" "+objects[i][1]);
		}
    }
    
    public String[] toArray(Vector<String> v){
    	for (int i = 0; i < v.size(); i++) {
			if(v.get(i)==null||v.get(i).equals(""))
				v.remove(i);
		}
    	
    	String[] temp = new String[v.size()];
    	for (int i = 0; i < v.size(); i++) {
			temp[i]=v.get(i);
		}
    	return temp;
    }
    
    public void add(Vector<String> v,String[] temp){
    	for (int i = 0; i < temp.length; i++) {
    		if(temp[i]!=null&&!temp[i].equals(""))
    			v.add(temp[i]);
		}
    }
}
