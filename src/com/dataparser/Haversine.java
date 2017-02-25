package com.dataparser;

public class Haversine {

    public final int R = 6371;

    public void compareResults() {
        //Start Location: Latitude: 47.507315, Longitude: -94.864055
        //Closer location: Latitude: 47.40870, Longitude: -94.147081
        //Farther location: Latitude: 45.491837, Longitude: -94.203906

        System.out.println("Closer location is: " + getDistance(47.507315, -94.864055, 47.40870, -94.147081) + " kilometers away by getDistance().");
        System.out.println("Farther location is: " + getDistance(47.507315, -94.864055, 45.491837, -94.203906) + " kilometers away by getDistance().");
    }

    public Double getDistance(double lat1, double lon1, double lat2, double lon2) {

        //From https://bigdatanerd.wordpress.com/2011/11/03/java-implementation-of-haversine-formula-for-distance-calculation-between-two-points/
        Double latDistance = Math.toRadians(lat2-lat1);
        Double lonDistance = Math.toRadians(lon2-lon1);
        Double lon1Radians = Math.toRadians(lat1);
        Double lon2Radians = Math.toRadians(lat2);

        Double a = Math.pow(Math.sin(latDistance / 2), 2) + Math.pow(Math.sin(lonDistance/2), 2) * Math.cos(lon1Radians) * Math.cos(lon2Radians);
        Double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

}
