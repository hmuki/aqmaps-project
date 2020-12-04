package uk.ac.ed.inf.aqmaps;


public class SensorData {

	private String location; // what3words location of the sensor
	private double battery; // battery reading which lies between 0.0 and 100.0
	private String reading; // string representation of battery reading
	private Location locationCoordinates; // encapsulated location of the sensor
	
	public static class Location {
		
		private Coordinates coordinates;
		
		public static class Coordinates {
			
			private double lng; // longitude of the sensor location
			private double lat; // latitude of the sensor location
			
			public double getLng() {
				return lng;
			}
			
			public double getLat() {
				return lat;
			}
			
			public void setLng(double lng) {
				this.lng = lng;
			}
			
			public void setLat(double lat) {
				this.lat = lat;
			}
			
		}
		
		public Coordinates getCoordinates() {
			return coordinates;
		}
	}
	
	public String getLocation() { return location; }

	public double getBattery() { return battery; }

	public String getReading() { return reading; }

	public Location getLocationCoordinates() { return locationCoordinates; }
	
	public void setLocationCoordinates(Location locationCoordinates) {
		this.locationCoordinates = locationCoordinates;
	}

}
