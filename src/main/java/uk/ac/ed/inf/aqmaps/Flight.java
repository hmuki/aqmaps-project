package uk.ac.ed.inf.aqmaps;

public abstract class Flight {
	
	protected FlightAlgorithm algorithm;
	
	public Flight(String day, String month, String year, int port, Point2D releasePoint, int seed) {
		algorithm = new FlightAlgorithm(day, month, year, port, releasePoint, seed);
	}
	
	public abstract void fly();
	public abstract void generateResults();
	
	public void execute() {
		fly();
		generateResults();
	}
	
}
