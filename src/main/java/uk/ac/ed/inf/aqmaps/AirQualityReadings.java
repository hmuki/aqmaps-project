package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class AirQualityReadings {

	public static ArrayList<SensorData> toSensorData(String body) {

		// de-serialise response body
		Gson gson = new Gson();
		var listType = new TypeToken<ArrayList<SensorData>>() {
		}.getType();
		ArrayList<SensorData> dataCollection = gson.fromJson(body, listType);

		return dataCollection;
	}

	public static void attachCoordinates(ArrayList<SensorData> dataCollection) {

		for (var data : dataCollection) {

			var location = data.location;
			String[] words = location.split("\\.");
			var filePath = "words/" + words[0] + "/" + words[1] + "/" + words[2] + "/details.json";
			var url = "http://localhost:80/" + filePath;

			var client = HttpClient.newHttpClient();
			var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
			HttpResponse<String> response = null;

			try {
				response = client.send(request, BodyHandlers.ofString());

				System.out.println(response.body());

			} catch (Exception e) {
				System.out.println(e);
				return;
			}
		}
	}
	
	public static void main(String[] args) {

		var day = args[0];
		var month = args[1];
		var year = args[2];

		System.out.println(day + " " + month + " " + year);

		var client = HttpClient.newHttpClient();
		var filePath = "maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";
		var url = "http://localhost:80/" + filePath;
		var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
		HttpResponse<String> response = null;

		try {
			response = client.send(request, BodyHandlers.ofString());

			attachCoordinates(toSensorData(response.body()));
			
		} catch (Exception e) {
			
			System.out.println(e);
			return;
		}

	}

}
