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

	protected final String day;
	protected final String month;
	protected final String year;
	protected final int port;
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

	public ArrayList<SensorData> getData() {
				
		System.out.println(day + " " + month + " " + year);

		var client = HttpClient.newHttpClient();
		var filePath = "maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";
		var url = "http://localhost:80/" + filePath;
		var request = HttpRequest.newBuilder().uri(URI.create(url)).build();

		try {
			var response = client.send(request, BodyHandlers.ofString());
			System.out.println(response.body());
			var data = this.attachCoordinates(Utilities.deserialize(response.body()));
			return data;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}

	}
	
	public ArrayList<SensorData> attachCoordinates(ArrayList<SensorData> data) {

		if (data == null) {
			return data;
		}
		
		for (var datum : data) {

			var location = datum.location;
			String[] words = location.split("\\.");
			var filePath = "words/" + words[0] + "/" + words[1] + "/" + words[2] + "/details.json";
			var url = "http://localhost:" + port + "/" + filePath;

			var client = HttpClient.newHttpClient();
			var request = HttpRequest.newBuilder().uri(URI.create(url)).build();

			try {
				var response = client.send(request, BodyHandlers.ofString());
				// attach location coordinates
				var locationCoordinates = new Gson().fromJson(response.body(), Location.class);
				datum.locationCoordinates = locationCoordinates;
			} catch (Exception e) {
				System.out.println(e);
				return null;
			}

		}
		return data;
	}
	
	public ArrayList<ArrayList<Point2D>> getNoFlyZones() {
		
		var url = "http://localhost:" + port + "/buildings/no-fly-zones.geojson";
		var client = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
		
		try {
			var response = client.send(request, BodyHandlers.ofString());
			var featureCollection = FeatureCollection.fromJson(response.body());
			var featureList = new ArrayList<Feature>();
			featureList.addAll(featureCollection.features());
			var polygonList = new ArrayList<Polygon>();
			
			var polygons = new ArrayList<ArrayList<Point2D>>();
			
			for (var feature : featureList) {
				polygonList.add((Polygon)feature.geometry());
			}
			
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
