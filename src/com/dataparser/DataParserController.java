package com.dataparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class DataParserController {
    DataParserModeler dataModel;

    DataParserController() {
        this.dataModel = startupDatabase();
    }

    public boolean zipCodeAddStates(){

        List zipCodesStatesList = new ArrayList();
        HashMap zipCodesStatesHashMap = new HashMap();

        return true;
    }

    public List<List> getZipCodeLatLong(String value){
        ResultSet tableDataResultSet = this.dataModel.getTableData("zip_code_table", "zip_code", value);
        List<List> tableResultsList = new ArrayList<>();
        try {
            while (tableDataResultSet.next()) {
                List<String> rowList = new ArrayList<>();
                for(int i = 1;  i <= tableDataResultSet.getMetaData().getColumnCount(); i++){
                    rowList.add(tableDataResultSet.getString(i));
                }
                tableResultsList.add(rowList);
            }
        }
        catch (SQLException sqle){
            sqle.printStackTrace();
            System.out.println(sqle);
        }

        return tableResultsList;
    }

    public HashMap getClosestCompetitiveSeat(List<String> zipCodeList){
        System.out.println("Longitude: " + zipCodeList.get(2));
        System.out.println("Latitude: " + zipCodeList.get(1));
        Haversine haversine = new Haversine();
        ResultSet tableDataResultSet = this.dataModel.getCompetitiveRaces();
        Double closestSeatDistance = Double.MAX_VALUE;
        Double zipLong = Double.parseDouble(zipCodeList.get(1));
        Double zipLat = Double.parseDouble(zipCodeList.get(2));
        HashMap closestSeatHashMap = new HashMap();
        try {
            while (tableDataResultSet.next()) {
                System.out.println(tableDataResultSet.getString("district_name"));
                System.out.println("Latitude: " + tableDataResultSet.getString("latitude"));
                System.out.println("Longitude: " + tableDataResultSet.getString("longitude"));
                Double seatLong = tableDataResultSet.getDouble("longitude");
                Double seatLat = tableDataResultSet.getDouble("latitude");
                Double distance = haversine.getDistance(zipLat, zipLong, seatLat, seatLong);
                System.out.println(distance.toString() + "\n");
                if (closestSeatDistance > distance){
                    closestSeatDistance = distance;
                    closestSeatHashMap.clear();
                    closestSeatHashMap.put("state", tableDataResultSet.getString("state"));
                    closestSeatHashMap.put("district_name", tableDataResultSet.getString("district_name"));

                }
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            System.out.println(sqle);
        }
        return closestSeatHashMap;
    }

    public HashMap getClosestHouseSeat(List<String> zipCodeList){
        Haversine haversine = new Haversine();
        ResultSet tableDataResultSet = this.dataModel.getTableData("district_table", "district_type", "Lower");
        Double closestSeatDistance = Double.MAX_VALUE;
        Double zipLong = Double.parseDouble(zipCodeList.get(2));
        Double zipLat = Double.parseDouble(zipCodeList.get(1));
        HashMap closestSeatHashMap = new HashMap();
        try {
            while (tableDataResultSet.next()) {
                Double seatLong = tableDataResultSet.getDouble("longitude");
                Double seatLat = tableDataResultSet.getDouble("latitude");
                Double distance = haversine.getDistance(zipLat, zipLong, seatLat, seatLong);
                if (closestSeatDistance > distance){
                    closestSeatDistance = distance;
                    closestSeatHashMap.clear();
                    closestSeatHashMap.put("state", tableDataResultSet.getString("state"));
                    closestSeatHashMap.put("district_name", tableDataResultSet.getString("district_name"));

                }
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            System.out.println(sqle);
        }
        return closestSeatHashMap;
    }

    public HashMap getClosestSenateSeat(List<String> zipCodeList){
        Haversine haversine = new Haversine();
        ResultSet tableDataResultSet = this.dataModel.getTableData("district_table", "district_type", "Upper");
        Double closestSeatDistance = Double.MAX_VALUE;
        Double zipLong = Double.parseDouble(zipCodeList.get(2));
        Double zipLat = Double.parseDouble(zipCodeList.get(1));
        HashMap closestSeatHashMap = new HashMap();
        try {
            while (tableDataResultSet.next()) {
                Double seatLong = tableDataResultSet.getDouble("longitude");
                Double seatLat = tableDataResultSet.getDouble("latitude");
                Double distance = haversine.getDistance(zipLat, zipLong, seatLat, seatLong);
                if (closestSeatDistance > distance){
                    closestSeatDistance = distance;
                    closestSeatHashMap.clear();
                    closestSeatHashMap.put("state", tableDataResultSet.getString("state"));
                    closestSeatHashMap.put("district_name", tableDataResultSet.getString("district_name"));

                }
            }
        }
        catch(SQLException sqle){
            sqle.printStackTrace();
            System.out.println(sqle);
        }
        return closestSeatHashMap;
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
}
