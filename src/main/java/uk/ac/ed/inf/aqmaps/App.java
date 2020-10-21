package uk.ac.ed.inf.aqmaps;


public class App 
{
    public static void main(String[] args)  
    {
//    	AirQualityReadings airQualityData = new AirQualityReadings(args[0], args[1], args[2]);
//    	final var releasePoint = new SensorData.Location.Coordinates();
//    	releasePoint.lat = Double.parseDouble(args[3]);
//    	releasePoint.lng = Double.parseDouble(args[4]);
//    	FlightAlgorithm algorithm = new FlightAlgorithm(airQualityData.sensorReadings, releasePoint);
//    	algorithm.moveDrone();
    	Utilities.getNoFlyZones();
    }
}
