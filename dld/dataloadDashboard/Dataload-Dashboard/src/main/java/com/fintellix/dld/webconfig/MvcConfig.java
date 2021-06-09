package com.fintellix.dld.webconfig;

import javax.servlet.MultipartConfigElement;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("login");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/403").setViewName("403");
        registry.addViewController("/dldLandingPage").setViewName("dldLandingPage");
        registry.addViewController("/logout").setViewName("logout");
        registry.addViewController("/notAuthorised").setViewName("notAuthorised");
        registry.addViewController("/uploadMetadataPage").setViewName("uploadMetadataPage");
        registry.addViewController("/lineage").setViewName("dataLineage");
        registry.addViewController("/handleuploadedfile.htm").setViewName("uploadMetadataPage");
    }

  @Bean
	public InternalResourceViewResolver viewResolver() {
	InternalResourceViewResolver resolver = new InternalResourceViewResolver();
	resolver.setPrefix("/WEB-INF/jsp/");
	resolver.setSuffix(".jsp");
	return resolver;
}
    
        
   
  @Bean
  public MultipartConfigElement multipartConfigElement() {
      return new MultipartConfigElement("");
  }

  @Bean
  public MultipartResolver multipartResolver() {
      org.springframework.web.multipart.commons.CommonsMultipartResolver multipartResolver = new org.springframework.web.multipart.commons.CommonsMultipartResolver();
      multipartResolver.setMaxUploadSize(1000000);
      return multipartResolver;
  }
     
}