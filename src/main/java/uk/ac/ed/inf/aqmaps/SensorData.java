package uk.ac.ed.inf.aqmaps;

//import java.math.BigDecimal;

public class SensorData {

	public String location;
	public double battery;
	public String reading;
	public Location locationCoordinates;
	
	public static class Location {
		
		public Coordinates coordinates;
		
		public static class Coordinates {
			
			public double lng;
			public double lat;
			
		}
	}
	
	public String getLocation() { return location; }

	public double getBattery() { return battery; }

	public String getReading() { return reading; }

	public Location getLocationCoordinates() { return locationCoordinates; }

}
