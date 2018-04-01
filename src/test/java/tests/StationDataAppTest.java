package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import datacontroller.CassandraConnector;
import datacontroller.StationDataApp;

public class StationDataAppTest {

	@Test
	public void testInsertJson() {
		CassandraConnector ccon = new CassandraConnector();
		Session session = ccon.Connect();
		
		Cluster cluster;
	    cluster = Cluster.builder()
	            .addContactPoint("54.191.120.32")
	            .withPort(9042)
	            .withAuthProvider(new PlainTextAuthProvider("cassandra","cassandra"))
	            .build();

	    Session testSession = cluster.connect("demo");

	    String testJson ="{\"number\":999,\"name\":\"Test\",\"address\":\"Market Street South\",\"position\":{\"lat\":53.342296,\"lng\":-6.287661},\"banking\":false,\"bonus\":false,\"status\":\"OPEN\",\"bike_stands\":38,\"available_bike_stands\":22,\"available_bikes\":16,\"last_update\":1522560114000}";
	    StationDataApp stnDataApp = new StationDataApp();
	    stnDataApp.insertJson(testSession, testJson);
	    String cqlStatement = "select name from demo.dublin_bikes_json2 where number=999;";
		ResultSet results = testSession.execute(cqlStatement);
		String name=null;
		for ( com.datastax.driver.core.Row row : results ) {
			name=row.getString(0);
		}
		String cqlDelStatement = "delete from demo.dublin_bikes_json2 where number=999;";
		testSession.execute(cqlDelStatement);
		//System.out.println("PRINTING NAME"+name);
		assertEquals("Test",name);
		
	}

}
