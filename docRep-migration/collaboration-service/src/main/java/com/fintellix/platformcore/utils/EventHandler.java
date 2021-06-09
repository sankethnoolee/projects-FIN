/**
 * 
 */
package com.fintellix.platformcore.utils;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.rest.client.EventClient;
import com.fintellix.rest.client.PojoMapper;
import com.fintellix.rest.client.dto.Event;
import com.fintellix.rest.client.dto.SnapShot;

/**
 * @author rahul.rayan
 * 
 */
public class EventHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Event e;

	public EventHandler(Event e) {
		this.e = e;
	}

	public Event getEvent() {
		return this.e;
	}

	public void postEvent() {
		try {
			logger.info("posting event "+PojoMapper.toJson(this.e, true));
			EventClient.getInstance().addEvent(this.e);
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), e);
		}
	}

	public void setBeforeString(String before) {

		this.e.addSnapShot(createSnapShot(before));
	}

	public void setAfterString(String after) {
		this.e.addSnapShot(createSnapShot(after));
	}

	
	public void addSnapshot(String key,String value){
		SnapShot s = new SnapShot();
		s.addField(key, value);
		this.e.addSnapShot(s);
	}
	
	public void addSnapshot(Map<String, Object> snap){
		SnapShot s = new SnapShot();
		
		for(String key: snap.keySet()){
			s.addField(key, snap.get(key));
		}
		
		this.e.addSnapShot(s);
	}
	
	public void addSnapshots(Map<String, List<Object>> snap){
		SnapShot s = new SnapShot();
		
		for(String key: snap.keySet()){
			s.addField(key, snap.get(key));
		}
		
		this.e.addSnapShot(s);
	}
	
	private SnapShot createSnapShot(String str) {
		SnapShot snap = new SnapShot();
		int counter =0;
		for (String row : str.split("~~")) {
			counter++;
			row = row.replace(",,", "<br>");
			String[] rowData = row.split("::");
			String key = "";
			if(rowData.length == 2)
			{
				key = "<!--"+counter+"-->"+rowData[0].trim();
				snap.addField(key, rowData[1].trim());
			}
			
			else {
			   // we don't have key=value pair in the JSON
			   // data. Let's flag the condition as a warning
			   // and ignore the event.
			   logger.warn( row + " is invalid key/value pair");
			}
		}
		
		return snap;

	}

}
