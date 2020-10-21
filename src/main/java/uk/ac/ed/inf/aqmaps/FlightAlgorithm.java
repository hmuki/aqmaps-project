package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FlightAlgorithm {

	public ArrayList<SensorData> sensorReadings;
	public Point2D[] points;
	public Point2D releasePoint;
	public final double moveLength = 0.0003;
	public final double readingRange = 0.0002;
	public ArrayList<Point2D> droneLocations = new ArrayList<>();
	public ArrayList<Integer> droneDirections = new ArrayList<>();
	public ArrayList<String> sensorLocations = new ArrayList<>();
	public final double leftLongitude = -3.192473;
	public final double rightLongitude = -3.184319;
	public final double bottomLatitude = 55.942617;
	public final double topLatitude = 55.946233;
	
	public FlightAlgorithm(ArrayList<SensorData> sensorReadings, SensorData.Location.Coordinates releasePoint) {
		this.sensorReadings = sensorReadings;
		this.releasePoint = new Point2D(releasePoint);
		this.points = this.orderPoints(this.getPoints());
	}
	
	public ArrayList<SensorData.Location.Coordinates> getPoints() {
		
		var points = new ArrayList<SensorData.Location.Coordinates>();
		
		for (var reading : sensorReadings) {
			points.add(reading.locationCoordinates.coordinates);
		}
		
		return points;
	}
	
	public Point2D[] orderPoints(ArrayList<SensorData.Location.Coordinates> points) {
		
		SensorData.Location.Coordinates centre = new SensorData.Location.Coordinates();
		int size = points.size();
		
		centre.lng = 0;
		centre.lat = 0;
		
		for (var point : points) {
			centre.lng += point.lng;
			centre.lat += point.lat;
		}
		
		centre.lng = centre.lng/size;
		centre.lat = centre.lat/size;
		
		System.out.println("Centre is : " + centre.lng + " " + centre.lat);
		
		// array containing 33 sensors
		var Point2DList = new Point2D[size];
		
		for (int i = 0; i < size; i++) {
			Point2DList[i] = new Point2D(points.get(i), centre, i);
		}
		
		var comparator = new Utilities();
		Arrays.sort(Point2DList, comparator);

		// get the closest sensor to the release point
		var distanceMap = new HashMap<Integer, Double>();
		var distances = new double[size];
		var nearest = 0;
		
		for (int i = 0; i < size; i++) {
			distances[i] = releasePoint.getDistanceFrom(Point2DList[i]);
		}
				
		for (int i = 0; i < size; i++) {
			distanceMap.put(i, distances[i]);
		}
		
		// get nearest point
		Arrays.sort(distances);
		
		for (int i = 0; i < size; i++) {
			if (distances[0] == distanceMap.get(i)) {
				nearest = i;
			}
		}
		
		var orderedPoint2DList = new Point2D[size];
		
		for (int i = 0; i < size; i++) {
			orderedPoint2DList[i] = Point2DList[(nearest + i) % size];
		}
		
		for (int i = 0; i < size; i++) {
			System.out.print(orderedPoint2DList[i].index + " ");
		}
		System.out.println();
//		for (int i = 0; i < size; i++) {
//			System.out.print(orderedPoint2DList[i].x + ",");
//		}
//		System.out.println();
//		for (int i = 0; i < size; i++) {
//			System.out.print(orderedPoint2DList[i].y + ",");
//		}
//		System.out.println();
		return orderedPoint2DList;
	}
	
	public void moveDrone() {
		
		final var size = this.points.length;
		
		var i = 0;
		var initialPoint = releasePoint;
		// while drone has not finished reading from all sensors
		while (i < size) {	
			// compute corrected angle to next sensor from current location
			var angle = points[i].getAngleFrom(initialPoint);
			var correctedAngle = Utilities.getAngleOfDeviation(angle);
			var nextMovePoint = move(initialPoint, correctedAngle); // make a move
			
			// make sure it is a valid location
			if (isWithinConfinementArea(nextMovePoint)) {
				// add location and direction to logs
				droneLocations.add(nextMovePoint);
				droneDirections.add(Utilities.convertToDegrees(correctedAngle));
			} else {
				while(!isWithinConfinementArea(nextMovePoint)) {
					correctedAngle = Utilities.rotateByTenDegrees(correctedAngle);
					nextMovePoint = move(initialPoint, correctedAngle);
				}
				// add location and direction to logs
				droneLocations.add(nextMovePoint);
				droneDirections.add(Utilities.convertToDegrees(correctedAngle));
			}
			
			var d = nextMovePoint.getDistanceFrom(points[i]); // compute remaining distance to target
			if (d > readingRange) {
				sensorLocations.add("null");
			} else {
				sensorLocations.add(sensorReadings.get(points[i].index).location);
			}
			Point2D startPoint;
			while (d > readingRange) {
				
				angle = points[i].getAngleFrom(nextMovePoint); // compute corrected angle to next sensor from current location
				correctedAngle = Utilities.getAngleOfDeviation(angle);
				nextMovePoint = move(nextMovePoint, correctedAngle); // make a move
				startPoint = new Point2D(nextMovePoint); // make a copy
				
				// make sure it is a valid location
				if (isWithinConfinementArea(startPoint)) {
					// add location and direction to logs
					droneLocations.add(startPoint);
					droneDirections.add(Utilities.convertToDegrees(correctedAngle));
				} else {
					while(!isWithinConfinementArea(nextMovePoint)) {
						correctedAngle = Utilities.rotateByTenDegrees(correctedAngle);
						nextMovePoint = move(startPoint, correctedAngle);
					}
					// add location and direction to logs
					droneLocations.add(nextMovePoint);
					droneDirections.add(Utilities.convertToDegrees(correctedAngle));
				}
				
				d = nextMovePoint.getDistanceFrom(points[i]);
				if (d > readingRange) {
					sensorLocations.add("null");
				} else {
					sensorLocations.add(sensorReadings.get(points[i].index).location);
				}
			}
			// upon getting close...
			// read information from sensor
			// update initial and target points
			i += 1;
			initialPoint = nextMovePoint;
//			for (var location : sensorLocations) {
//				System.out.print(location + ",");
//			}
//			System.out.println();
		}
//		for (var location : droneLocations) {
//			System.out.print(location.x + ",");
//		}
//		System.out.println();
//		for (var location : droneLocations) {
//			System.out.print(location.y + ",");
//		}
//		System.out.println();
//		for (var direction : droneDirections) {
//			System.out.print(direction + ",");
//		}
//		System.out.println();
//		for (var location : sensorLocations) {
//			System.out.print(location + ",");
//		}
//		System.out.println();
		System.out.println(droneLocations.size() + " i:" + i);
	}
	
	public Point2D move(Point2D initialPoint, double correctedAngle) {
		
		var dx = moveLength * Math.cos(correctedAngle);
		var dy = moveLength * Math.sin(correctedAngle);
		
		var nextMoveCoordinates = new SensorData.Location.Coordinates();
		nextMoveCoordinates.lng = initialPoint.x + dx;
		nextMoveCoordinates.lat = initialPoint.y + dy;
		
		return new Point2D(nextMoveCoordinates);
	}
	
	public boolean isWithinConfinementArea(Point2D point) {
		
		return point.x > leftLongitude && point.x < rightLongitude && 
				point.y > bottomLatitude && point.y < topLatitude;		
	}
}

