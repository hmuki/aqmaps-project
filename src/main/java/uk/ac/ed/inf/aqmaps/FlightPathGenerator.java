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
	
	// generate Text file
	public void generateTextFile() {

		int day, month, year;
		day = Integer.parseInt(algorithm.getDay());
		month = Integer.parseInt(algorithm.getMonth());
		year = Integer.parseInt(algorithm.getYear());
		
		var lines = new ArrayList<String>();
		var outputLines = "";

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
		Path outputFilePath = Paths.get(new File("").getAbsolutePath().concat(file));

		try {
			Files.write(outputFilePath, lines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("The text file cannot be created");
		}
	}
	
	// generate GeoJSON file
	public void generateGeoJSONFile() {
		
		int day, month, year;
		day = Integer.parseInt(algorithm.getDay());
		month = Integer.parseInt(algorithm.getMonth());
		year = Integer.parseInt(algorithm.getYear());

		final var size = algorithm.getPoints().length;
		var points = new ArrayList<Point>();
		double x, y;
		for (int i = 0; i < size; i++) {
			x = algorithm.getSensorReadings().get(algorithm.getPoints()[i].getIndex()).getLocationCoordinates().coordinates.lng;
			y = algorithm.getSensorReadings().get(algorithm.getPoints()[i].getIndex()).getLocationCoordinates().coordinates.lat;
			points.add(Point.fromLngLat(x, y));
		}

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
		points.add(Point.fromLngLat(algorithm.getReleasePoint().getX(), algorithm.getReleasePoint().getY()));
		for (var point : algorithm.getDroneLocations()) {
			points.add(Point.fromLngLat(point.getX(), point.getY()));
		}
		var lineString = LineString.fromLngLats(points);
		features.add(Feature.fromGeometry(lineString));

		FeatureCollection featureCollection = FeatureCollection.fromFeatures(features);

		ArrayList<String> lines = new ArrayList<>();
		lines.add(featureCollection.toJson());

		var file = "/readings-";
		file += Utilities.formatDayAndMonth(day, month);
		file += "-" + year + ".geojson";
		Path outputFilePath = Paths.get(new File("").getAbsolutePath().concat(file));
		
		try {
			Files.write(outputFilePath, lines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("The geojson file cannot be created");
		}
	}
	
}
