package uk.ac.ed.inf.aqmaps;

public class DroneFlight extends Flight {
	
	public DroneFlight(String day, String month, String year, int port, Point2D releasePoint, int seed) {
		super(day, month, year, port, releasePoint, seed);
	}
	
	@Override
	public void fly() {
		algorithm.moveDrone();
	}
	
	@Override
	public void generateResults() {
		var pathGenerator = new FlightPathGenerator(algorithm);
		pathGenerator.generateTextFile();
		pathGenerator.generateGeoJSONFile();
	}

}
