package hello;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;

import datacontroller.CassandraConnector;

@SpringBootApplication
public class StationDataApp {

	private static final Logger log = LoggerFactory.getLogger(StationDataApp.class);

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
			Station[] stndata = restTemplate.getForObject(
					"https://api.jcdecaux.com/vls/v1/stations?contract=Dublin&apiKey=abf9e3b328f8d505771cd2d3c85ddef36e995451", Station[].class);
			//log.info(stndata[1].toString());
			ObjectMapper mapper = new ObjectMapper();
			CassandraConnector ccon = new CassandraConnector();
			for (Station stn : stndata) {
				String jsonInString = mapper.writeValueAsString(stn);
				jsonInString=jsonInString.replaceAll("'","''");
				log.info(jsonInString);
				ccon.Connect(jsonInString);
			}
			/*String jsonInString = mapper.writeValueAsString(stndata[0]);
			CassandraConnector ccon = new CassandraConnector();
			log.info(jsonInString);
			ccon.Connect(jsonInString);*/
			TimeUnit.MINUTES.sleep(5);
			}
		};
	}
}