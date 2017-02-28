package com.dataparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class DataParserController {
    DataParserModeler dataModel;

    DataParserController() {
        this.dataModel = startupDatabase();
    }

    public HashMap getZipCodeLatLong(String zipCode){
        ResultSet tableDataResultSet = this.dataModel.getTableData("zip_code_table", "zip_code", zipCode);
        List<HashMap> tableResultsHashMapList = new ArrayList<>();
        HashMap tableResultsHashMap = new HashMap();
        try {
            while (tableDataResultSet.next()) {
                tableResultsHashMap.put("zip_code", tableDataResultSet.getString("zip_code"));
                tableResultsHashMap.put("latitude", tableDataResultSet.getDouble("latitude"));
                tableResultsHashMap.put("longitude", tableDataResultSet.getDouble("longitude"));
                tableResultsHashMapList.add(tableResultsHashMap);
            }
        }
        catch (SQLException sqle){
            sqle.printStackTrace();
            System.out.println(sqle);
        }

        return tableResultsHashMap;
    }

    public HashMap getClosestCompetitiveSeat(String zipCodeInputString){
        HashMap zipCoordinatesHashMap = getZipCodeLatLong(zipCodeInputString);
        ResultSet tableDataResultSet = this.dataModel.getCompetitiveRaces();
        HashMap closestSeatHashMap = parseResults(tableDataResultSet, (Double) (zipCoordinatesHashMap.get("latitude")), (Double) (zipCoordinatesHashMap.get("longitude")));
        return closestSeatHashMap;
    }

    public HashMap getClosestCompetitiveSeat(String zipCodeInputString, String chamber){
        HashMap zipCoordinatesHashMap = getZipCodeLatLong(zipCodeInputString);
        ResultSet tableDataResultSet = this.dataModel.getCompetitiveRaces(chamber);
        HashMap closestSeatHashMap = parseResults(tableDataResultSet, (Double) (zipCoordinatesHashMap.get("latitude")), (Double) (zipCoordinatesHashMap.get("longitude")));
        return closestSeatHashMap;
    }

    public HashMap parseResults(ResultSet tableDataResultSet, Double zipLat, Double zipLong){
        HashMap<String, String> competitiveSeat = new HashMap();
        Double closestSeatDistance = Double.MAX_VALUE;
        try {
            while (tableDataResultSet.next()) {
                Double distance = getHaversineDistance(zipLat, zipLong, tableDataResultSet.getDouble("latitude"), tableDataResultSet.getDouble("longitude"));
                if (closestSeatDistance > distance){
                    closestSeatDistance = distance;
                    competitiveSeat.clear();
                    competitiveSeat.put("state", tableDataResultSet.getString("state"));
                    competitiveSeat.put("district_name", tableDataResultSet.getString("district_name"));

                }
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            System.out.println(sqle);
        }
        return competitiveSeat;
    }

    //From https://bigdatanerd.wordpress.com/2011/11/03/java-implementation-of-haversine-formula-for-distance-calculation-between-two-points/
    public Double getHaversineDistance(double lat1, double lon1, double lat2, double lon2) {

        int R = 6371;
        Double latDistance = Math.toRadians(lat2-lat1);
        Double lonDistance = Math.toRadians(lon2-lon1);
        Double lon1Radians = Math.toRadians(lat1);
        Double lon2Radians = Math.toRadians(lat2);

        Double a = Math.pow(Math.sin(latDistance / 2), 2) + Math.pow(Math.sin(lonDistance/2), 2) * Math.cos(lon1Radians) * Math.cos(lon2Radians);
        Double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }


    public DataParserModeler startupDatabase() {
        HashMap<String, String> keysHashMap = getKeys();
        DataParserModeler databaseModel = new DataParserModeler("jdbc:mysql://localhost:3306/swing_left_database", keysHashMap.get("login"), keysHashMap.get("password"));
        return databaseModel;
    }

    public HashMap getKeys() {
        HashMap keyHashMap = new HashMap();
        try (Stream<String> stream = Files.lines(Paths.get("keys/databaselogin.txt"))) {
            Object[] keyArray = stream.toArray();

            for (Object key : keyArray) {
                String[] keyLine = key.toString().split(":");
                keyHashMap.put(keyLine[0], keyLine[1]);
            }

        } catch (IOException e) {
            System.out.println(e);
        }
        return keyHashMap;
    }

    public boolean closeDatabase(){
        if(this.dataModel.closeConnection()){
            return true;
        }
        else {
            return false;
        }
    }
}
