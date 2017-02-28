package com.dataparser;

public class Haversine {

    public final int R = 6371;

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
