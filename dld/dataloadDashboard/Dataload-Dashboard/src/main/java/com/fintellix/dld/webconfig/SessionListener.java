package com.fintellix.dld.webconfig;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
@Component
public class SessionListener implements HttpSessionListener {
	
	//ServerProperties server;
	
	private static Properties application;
	static{
		try {
			InputStream is  = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
			application = new Properties();
			application.load(is);
			
		}catch (Exception e) {
			throw new RuntimeException("Coudnt read application  properties from class path",e);
		}
	}
	
	private static final String sessiontime = application.getProperty("server.session-timeout");
 
	//private static final int sessiontime = 90;
	private static final Logger LOGGER = LoggerFactory.getLogger(SessionListener.class);
	
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		LOGGER.info("Session Created "+sdf.format(cal.getTime()));
		
	    se.getSession().setMaxInactiveInterval(Integer.parseInt(sessiontime));
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		LOGGER.info("Session Destroyed "+sdf.format(cal.getTime()));
		File index=new File(se.getSession().getServletContext().getRealPath("/tmp/uploader/"));
		removeTemporaryFiles(index);
    	
    	Cache uploaderCache = CacheManager.getInstance().getCache("uploadedFileListCache");
		if(uploaderCache != null) {
			@SuppressWarnings("unchecked")
			List<String> list = uploaderCache.getKeys();
			for (String string : list) {
				uploaderCache.remove(string);
			}
		}
		
		
	}
	
	private boolean removeTemporaryFiles(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = removeTemporaryFiles(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
	
}
