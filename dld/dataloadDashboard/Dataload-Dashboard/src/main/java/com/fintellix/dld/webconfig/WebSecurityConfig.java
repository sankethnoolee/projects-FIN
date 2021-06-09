package com.fintellix.dld.webconfig;


import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fintellix.dld.authentication.CustomAuthenticationProviderForBizscore;
import com.fintellix.dld.authentication.authhelper.DLDPasswordEncoder;
import com.fintellix.dld.authentication.authhelper.DldUserAuthenticationFromDbData;
import com.fintellix.dld.models.UserDetail;
import com.fintellix.dld.util.SolutionURLMappingPropertiesLoader;


@Component
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationProviderForBizscore.class);

	@Value("${dld.AllowedUser}")
	private String ALLOWEDUSERROLE;
	
	@Autowired
	CustomAuthenticationProviderForBizscore customAuthenticationProviderForBizscore;
	
	@Bean
    public UserDetailsService userDetails() {
        return new DldUserAuthenticationFromDbData();
    }
	
	@Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetails());
        authProvider.setPasswordEncoder(new DLDPasswordEncoder());
        return authProvider;
    }
	
	
	@Autowired
	public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
		LOGGER.debug("Registering authentication providers");

		auth.authenticationProvider(authProvider());
		auth.authenticationProvider(customAuthenticationProviderForBizscore);
	}	

	@Override
	protected void configure(HttpSecurity http) throws Exception {	
		
		
		http.csrf().ignoringAntMatchers("/dldwebapplication/API/**").disable();
		
		http.authorizeRequests()
		.antMatchers("/dldwebapplication/API/**")
		.permitAll();
		
		
		http
		.headers()
	    	.frameOptions()
	    	.sameOrigin()
	    .and()
			.authorizeRequests()
			.antMatchers("/dldwebapplication/css/**").permitAll()
			.antMatchers("/dldwebapplication/images/**").permitAll()
			.antMatchers("/dldwebapplication/js/**").permitAll()
			.anyRequest().authenticated()	
		.and()
			.formLogin()
				.loginPage("/dldwebapplication/login").permitAll()
				.usernameParameter("username").passwordParameter("password")
				.successHandler(loginSuccessHandler())
		.and()
			.logout()
				.logoutSuccessUrl("/dldwebapplication/logout")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				//.logoutSuccessHandler(dldLogoutSuccessHandler)
				
		.and()
			.exceptionHandling().accessDeniedPage("/dldwebapplication/notAuthorised")
		.and()
			.sessionManagement().sessionAuthenticationErrorUrl("/dldwebapplication/login").maximumSessions(10).expiredUrl("/dldwebapplication/logout")
		;
	}
	
	public AuthenticationSuccessHandler loginSuccessHandler() {
		LOGGER.info("Login Succesful Handler");
		return (request, response, authentication)-> {
			HttpSession session = request.getSession();
	        UserDetail authUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	        
	        SolutionURLMappingPropertiesLoader x = SolutionURLMappingPropertiesLoader.getInstance();
	        if(authUser.getClientCode().equalsIgnoreCase(x.getSolutions().get(request.getParameter("solutionName")).getClientCode())){
	            //set our response to OK status
	             session.setAttribute("username", authUser.getUsername());
	             session.setAttribute("clientCode", authUser.getClientCode());
	             session.setAttribute("solutionName",request.getParameter("solutionName"));
	             response.setStatus(HttpServletResponse.SC_OK);
	             response.sendRedirect("/dldwebapplication/dldLandingPage");
	        } else {
	              response.setStatus(HttpServletResponse.SC_OK);
		          response.sendRedirect("/dldwebapplication/notAuthorised");
	        }
	
	   };
	}
	
}
