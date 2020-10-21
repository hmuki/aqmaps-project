package uk.ac.ed.inf.aqmaps;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

public class AirQualityReadings {

	public String day;
	public String month;
	public String year;
	public ArrayList<SensorData> sensorReadings;
	
	public AirQualityReadings(String day, String month, String year) {
		this.day = day;
		this.month = month;
		this.year = year;
		this.sensorReadings = this.createSensorDataCollection();
	}
	
	public ArrayList<SensorData> createSensorDataCollection() {
				
		System.out.println(day + " " + month + " " + year);

		var client = HttpClient.newHttpClient();
		var filePath = "maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";
		var url = "http://localhost:80/" + filePath;
		var request = HttpRequest.newBuilder().uri(URI.create(url)).build();

		try {
			var response = client.send(request, BodyHandlers.ofString());

			var dataCollection = Utilities.attachCoordinates(Utilities.JSONToSensorDataObjects(response.body()));
			
			return dataCollection;
			
		} catch (Exception e) {

			System.out.println(e);
			return null;
		}

	}

}
