package com.prach.mashup.wsconnector;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class XMLParser {
	private String TAG;
	//private String XmlMessage;
	private Document xmlDocument;
	private XPath xPath;
	
	public XMLParser(String xmlmessage) throws ParserConfigurationException, IOException, SAXException{
		//XmlMessage = xmlmessage;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		StringReader reader = new StringReader( xmlmessage );
		InputSource inputSource = new InputSource( reader );
		xmlDocument = docBuilder.parse(inputSource);
		XPathFactory xPathFactory = XPathFactory.newInstance();
		xPath = xPathFactory.newXPath();
		reader.close();
	}
	
	public String[] getArray(String xpathstring) throws XPathExpressionException{
		NodeList tempnodes = (NodeList) xPath.evaluate(xpathstring, xmlDocument, XPathConstants.NODESET);
		String[] results = new String[tempnodes.getLength()];
		for (int i = 0; i < tempnodes.getLength(); i++) {
			//if(i%2==1){
				String value = tempnodes.item(i).getChildNodes().item(0).getNodeValue();
				results[i] = value;
				System.out.println("results["+i+"]:"+results[i]);
			//}
		}
		return results;
	}
	
	public void setTAG(String tag){
		TAG = tag;
	}
	
	public void debug(String msg){
		//System.out.println(msg);
		Log.i(TAG,msg);
	}
}
