package datacontroller;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.Station;

import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Abhimanyu Hazarika
 * 
 */
@SpringBootApplication
public class StationDataApp {

	private static final Logger log = LoggerFactory.getLogger(StationDataApp.class);
	Properties prop = new Properties();
	InputStream input = null;

	public static void main(String args[]) {
		SpringApplication.run(StationDataApp.class);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {
			while(true)
			{	
				try {
					
					input = new FileInputStream("config.properties");
					// load the properties file
					prop.load(input);
					Station[] stndata = restTemplate.getForObject(
							prop.getProperty("dBikesApi"), Station[].class);
					ObjectMapper mapper = new ObjectMapper();
					CassandraConnector ccon = new CassandraConnector();
					Session session =ccon.Connect();
					
					for (Station stn : stndata) {
						String jsonInString = mapper.writeValueAsString(stn);
						//Replace the quotes in Station Names with double quotes before storing in database
						jsonInString=jsonInString.replaceAll("'","''");
						log.info(jsonInString);
						insertJson(session,jsonInString);
					}
					TimeUnit.MINUTES.sleep(5);
				
				} catch (IOException ex) {
					ex.printStackTrace();
				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
	}
	
	public void insertJson(Session session, String jsonData) {
	    //Inserts the raw data from Dublin Bikes API to Cassandra database
		String cqlStatement = "INSERT INTO demo.dublin_bikes_json2 JSON '"+jsonData+"';";
		session.execute(cqlStatement);
		log.info("JSON object inserted into database\n");
	}
}