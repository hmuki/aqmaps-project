package uk.ac.ed.inf.aqmaps;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Polygon;

import uk.ac.ed.inf.aqmaps.SensorData.Location;

public class Utilities implements java.util.Comparator<Point2D> {

	public static ArrayList<SensorData> JSONToSensorDataObjects(String json) {

		// de-serialise response body
		Gson gson = new Gson();
		var listType = new TypeToken<ArrayList<SensorData>>() {
		}.getType();
		ArrayList<SensorData> dataCollection = gson.fromJson(json, listType);

		return dataCollection;
	}

	public static ArrayList<SensorData> attachCoordinates(ArrayList<SensorData> dataCollection) {

		if (dataCollection == null) {
			return dataCollection;
		}
		
		for (var data : dataCollection) {

			var location = data.location;
			String[] words = location.split("\\.");
			var filePath = "words/" + words[0] + "/" + words[1] + "/" + words[2] + "/details.json";
			var url = "http://localhost:80/" + filePath;

			var client = HttpClient.newHttpClient();
			var request = HttpRequest.newBuilder().uri(URI.create(url)).build();

			try {
				var response = client.send(request, BodyHandlers.ofString());
				System.out.println(response.body());

				// attach location coordinates
				var locationCoordinates = new Gson().fromJson(response.body(), Location.class);
				data.locationCoordinates = locationCoordinates;

			} catch (Exception e) {
				System.out.println(e);
				return null;
			}

		}

		return dataCollection;
	}
	
	public static void getNoFlyZones() {
		
		var url = "http://localhost:80/buildings/no-fly-zones.geojson";
		var client = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
		
		try {
			var response = client.send(request, BodyHandlers.ofString());
			System.out.println(response.body());
			
			var featureCollection = FeatureCollection.fromJson(response.body());
			var featureList = new ArrayList<Feature>();
			featureList.addAll(featureCollection.features());
			var polygonList = new ArrayList<Polygon>();
			
			for (var feature : featureList) {
				polygonList.add((Polygon)feature.geometry());
			}
			
			for (var polygon : polygonList) {
				System.out.println(polygon.coordinates());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static double getAngleOfDeviation(double angle) {
		
		var angleInDegrees = angle * 180/Math.PI;
		var angleOfDeviation = 0.0;
		
		angleOfDeviation = Math.ceil(angleInDegrees/10) * 10;
		
		return angleOfDeviation * Math.PI/180;
	}
	
	public static int convertToDegrees(double angle) {
		
		return (int)Math.floor(angle * 180/Math.PI);

	}
	
	public static double rotateByTenDegrees(double angle) {
		
		return angle - (Math.PI/18); // subtract 10 degrees

	}
	
	@Override
	public int compare(Point2D point1, Point2D point2) {
		// TODO Auto-generated method stub
		
		if (point1.theta == point2.theta) {
			if (point1.r < point2.r)
				return -1;
			else if (point1.r > point2.r)
				return 1;
			else
				return 0;
		} else {
			if (point1.theta < point2.theta)
				return -1;
			else
				return 1;
		}
	}

}
