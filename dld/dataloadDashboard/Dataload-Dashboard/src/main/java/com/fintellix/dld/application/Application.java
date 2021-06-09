package com.fintellix.dld.application;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@ComponentScan({"com.fintellix.dld.*"})
@EntityScan("com.fintellix.dld.domain")
@EnableNeo4jRepositories(basePackages = "com.fintellix.dld.repository")
@ServletComponentScan("com.fintellix.dld.application.licence")
public class Application {
	
	private static Properties applicationProperties;
	static{
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
			applicationProperties = new Properties();
			applicationProperties.load(is);

		}catch (Exception e) {
			throw new RuntimeException("Coudnt read application / data-dashboard-queries  properties from class path",e);
		}
	}
	
	private static final String FILE_PATH  = applicationProperties.getProperty("spring.data.neo4j.dbPath");
	private static final String LISTENER  = applicationProperties.getProperty("dld.graphDbListener");
	
    public static void main(String[] args) throws Throwable {
    	BoltConnector bolt = new BoltConnector();

		GraphDatabaseService graphDb = new GraphDatabaseFactory()
		        .newEmbeddedDatabaseBuilder( new File(FILE_PATH) )
		        .setConfig( bolt.type, "BOLT" )
		        .setConfig( bolt.enabled, "true" )
		        .setConfig( bolt.listen_address, LISTENER )

		        .newGraphDatabase();
		
		
		registerShutdownHook( graphDb );
    	SpringApplication app = new SpringApplication(Application.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
		
		
    } 
    
    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {

        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }
    
}