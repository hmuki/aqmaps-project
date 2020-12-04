package uk.ac.ed.inf.aqmaps;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class FlightPathGenerator {
	
	private FlightAlgorithm algorithm;
	
	public FlightPathGenerator(FlightAlgorithm algorithm) {
		this.algorithm = algorithm;
	}
	
	// generates the output text file
	public void generateTextFile() {

		int day, month, year;
		day = Integer.parseInt(algorithm.getDay());
		month = Integer.parseInt(algorithm.getMonth());
		year = Integer.parseInt(algorithm.getYear());
		
		var lines = new ArrayList<String>(); // will be used to create the entire text file
		var outputLines = ""; // stores each line of the text file

		outputLines += 1 + "," + algorithm.getReleasePoint().getX() + "," + algorithm.getReleasePoint().getY() + "," + algorithm.getDroneDirections().get(0) + ","
				+ algorithm.getDroneLocations().get(0).getX() + "," + algorithm.getDroneLocations().get(0).getY() + "," + algorithm.getSensorLocations().get(0)
				+ "\n";

		for (int i = 1; i < algorithm.getDroneLocations().size(); i++) {
			outputLines += (i + 1) + "," + algorithm.getDroneLocations().get(i-1).getX() + "," + algorithm.getDroneLocations().get(i-1).getY()
					+ "," + algorithm.getDroneDirections().get(i) + "," + algorithm.getDroneLocations().get(i).getX() + ","
					+ algorithm.getDroneLocations().get(i).getY() + "," + algorithm.getSensorLocations().get(i) + "\n";
		}

		lines.add(outputLines);

		var file = "/flightpath-";
		file += Utilities.formatDayAndMonth(day, month);
		file += "-" + year + ".txt";
		Path outputFilePath = Paths.get(new File("").getAbsolutePath().concat(file)); // initialise the text file's path

		try {
			Files.write(outputFilePath, lines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("The text file cannot be created");
		}
	}
	
	// generates the GeoJSON file
	public void generateGeoJSONFile() {
		
		int day, month, year;
		day = Integer.parseInt(algorithm.getDay());
		month = Integer.parseInt(algorithm.getMonth());
		year = Integer.parseInt(algorithm.getYear());

		final var size = algorithm.getPoints().length;
		var points = new ArrayList<Point>();
		double x, y; // longitude and latitude of the sensor's location respectively
		for (int i = 0; i < size; i++) {
			x = algorithm.getSensorReadings().get(algorithm.getPoints()[i].getIndex()).getLocationCoordinates().getCoordinates().getLng();
			y = algorithm.getSensorReadings().get(algorithm.getPoints()[i].getIndex()).getLocationCoordinates().getCoordinates().getLat();
			points.add(Point.fromLngLat(x, y));
		}
		// Instantiate features and add their respective properties
		var features = new ArrayList<Feature>();
		for (int i = 0; i < size; i++) {
			var feature = Feature.fromGeometry(points.get(i));
			var pair = Utilities
					.toRGBAndMarkerSymbol(algorithm.getSensorReadings().get(algorithm.getPoints()[i].getIndex()).getReading())
					.split(",");
			var location = algorithm.getSensorReadings().get(algorithm.getPoints()[i].getIndex()).getLocation();
			var rgbString = pair[0];
			var markerSymbol = "";
			if (pair.length == 2) {
				markerSymbol = pair[1];
			}
			feature.addStringProperty("location", location);
			feature.addStringProperty("rgb-string", rgbString);
			feature.addStringProperty("marker-color", rgbString);
			feature.addStringProperty("marker-symbol", markerSymbol);
			features.add(feature);
		}

		points.clear();
		// Add the LineString object used to trace the drone's flight path
		points.add(Point.fromLngLat(algorithm.getReleasePoint().getX(), algorithm.getReleasePoint().getY()));
		for (var point : algorithm.getDroneLocations()) {
			points.add(Point.fromLngLat(point.getX(), point.getY()));
		}
		var lineString = LineString.fromLngLats(points);
		features.add(Feature.fromGeometry(lineString));

		FeatureCollection featureCollection = FeatureCollection.fromFeatures(features);

		ArrayList<String> lines = new ArrayList<>();
		lines.add(featureCollection.toJson());

		// Write to the geojson output file
		var file = "/readings-";
		file += Utilities.formatDayAndMonth(day, month);
		file += "-" + year + ".geojson";
		Path outputFilePath = Paths.get(new File("").getAbsolutePath().concat(file)); // get the path of the geojson file
		
		try {
			Files.write(outputFilePath, lines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("The geojson file cannot be created");
		}
	}
	
}
