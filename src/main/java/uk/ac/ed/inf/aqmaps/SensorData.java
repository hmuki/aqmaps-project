package uk.ac.ed.inf.aqmaps;

//import java.math.BigDecimal;

public class SensorData {

	public String location;
	public double battery;
	public String reading;
	
	public SensorData(String location, double battery, String reading) {
		this.location = location;
		this.battery = battery;
		this.reading = reading;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("hello world!");
	}

}
