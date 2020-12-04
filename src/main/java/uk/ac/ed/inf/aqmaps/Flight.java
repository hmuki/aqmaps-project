package uk.ac.ed.inf.aqmaps;

public abstract class Flight {
	
	protected FlightAlgorithm algorithm; // The algorithm used to control the drone's flight
	
	public Flight(String day, String month, String year, int port, Point2D releasePoint, int seed) {
		algorithm = new FlightAlgorithm(day, month, year, port, releasePoint, seed);
	}
	
	public abstract void fly(); // executes the drone's flight algorithm
	public abstract void generateResults(); // generates the necessary output files
	
	// Template method
	public final void execute() {
		fly();
		generateResults();
	}
	
}
