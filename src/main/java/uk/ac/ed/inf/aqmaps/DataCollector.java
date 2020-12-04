package uk.ac.ed.inf.aqmaps;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;

import uk.ac.ed.inf.aqmaps.SensorData.Location;

public class DataCollector {

	protected final String day; // day on which readings were taken
	protected final String month; // month in which readings were taken
	protected final String year; // year in which readings were taken
	protected final int port; // port with which we communicate to the server
	protected final ArrayList<SensorData> sensorReadings;
	
	public DataCollector(String day, String month, String year, int port) {
		this.day = day;
		this.month = month;
		this.year = year;
		this.port = port;
		this.sensorReadings = getData();
	}
	
	public String getDay() { return day; }
	
	public String getMonth() { return month; }
	
	public String getYear() { return year; }
	
	public ArrayList<SensorData> getSensorReadings() { return sensorReadings; }
	
	// get pre-processed sensor data
	public ArrayList<SensorData> getData() {
				
		System.out.println(day + " " + month + " " + year);
		// Make a request to the web server for sensor data via Http
		var client = HttpClient.newHttpClient();
		var filePath = "maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";
		var url = "http://localhost:80/" + filePath;
		var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
		// deserialize the json response and catch any exceptions that may occur
		try {
			var response = client.send(request, BodyHandlers.ofString());
			var data = this.attachCoordinates(Utilities.deserialize(response.body()));
			return data;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}

	}
	
	// attach the latitude and longitude (sensor location) to the sensor readings
	public ArrayList<SensorData> attachCoordinates(ArrayList<SensorData> data) {

		if (data == null) {
			return data;
		}
		
		for (var datum : data) {
			// Issue request to the web server for location data via Http
			var location = datum.getLocation();
			String[] words = location.split("\\.");
			var filePath = "words/" + words[0] + "/" + words[1] + "/" + words[2] + "/details.json";
			var url = "http://localhost:" + port + "/" + filePath;

			var client = HttpClient.newHttpClient();
			var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
			// deserialize response, use Location class to store data and append to already existing sensor data
			try {
				var response = client.send(request, BodyHandlers.ofString());
				var locationCoordinates = new Gson().fromJson(response.body(), Location.class);
				datum.setLocationCoordinates(locationCoordinates);
			} catch (Exception e) {
				System.out.println(e);
				return null;
			}

		}
		return data;
	}
	
	// return the No Fly Zones as a list of lists of Point2D points
	public ArrayList<ArrayList<Point2D>> getNoFlyZones() {
		
		// Request for the coordinates of No Fly Zones from the web server using Http 
		var url = "http://localhost:" + port + "/buildings/no-fly-zones.geojson";
		var client = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
		
		try {
			var response = client.send(request, BodyHandlers.ofString());
			var featureCollection = FeatureCollection.fromJson(response.body());
			var featureList = new ArrayList<Feature>();
			featureList.addAll(featureCollection.features());
			var polygonList = new ArrayList<Polygon>();
			
			// Store No Fly Zones as a list of lists of Point2D points
			var polygons = new ArrayList<ArrayList<Point2D>>();
			
			for (var feature : featureList) {
				polygonList.add((Polygon)feature.geometry());
			}
			// iterate over the polygons, extract coordinates and insert in a list of lists
			for (int i = 0; i < polygonList.size(); i++) {
				
				var pointArray = polygonList.get(i).coordinates().get(0);
				polygons.add(new ArrayList<Point2D>());
				
				for (int j = 0; j < pointArray.size(); j++) {
					
					polygons.get(i).add(new Point2D(pointArray.get(j).longitude(),
							pointArray.get(j).latitude()));
				}
			}
			return polygons;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

}
