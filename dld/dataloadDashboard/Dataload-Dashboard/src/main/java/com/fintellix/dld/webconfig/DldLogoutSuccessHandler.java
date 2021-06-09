package com.fintellix.dld.webconfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.fintellix.dld.authentication.CustomAuthenticationProviderForBizscore;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
@Component
public class DldLogoutSuccessHandler implements LogoutSuccessHandler{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationProviderForBizscore.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public void onLogoutSuccess(HttpServletRequest request,HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		 
		LOGGER.info("DldLogoutSuccessHandler-> onLogoutSuccess");
		File index=new File(request.getSession().getServletContext().getRealPath("/tmp/uploader/"));
		removeTemporaryFiles(index);
    	
    	Cache uploaderCache = CacheManager.getInstance().getCache("uploadedFileListCache");
		if(uploaderCache != null) {
			List<String> list = uploaderCache.getKeys();
			for (String string : list) {
				uploaderCache.remove(string);
			}
		}
		
		
		String URL = request.getContextPath() +"/dldwebapplication/logout";
		response.setStatus(HttpStatus.OK.value());
		response.sendRedirect(URL);
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
