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

//This is designed to send and receive data from the database.
//It has two principal operations:
//1) It gets information from the text files and loads it into the database.
//2) It sends and receives data from the program.
public class DataParserController {
    DataParserModeler dataModel;

    //This is the standard constructor for the class.
    DataParserController(String jdbcDriver, String dbConnectStr, String keyPath) {
        HashMap<String, String> keys = getKeys(keyPath);
        this.dataModel = startupDatabase(jdbcDriver, dbConnectStr,keyPath);
    }

    //This runs the two methods dedicated to adding a new text file to the system.
    public boolean addTextFile(List<HashMap<String, String>> newDataHashMapList){
        if(addDataToPersonTable(newDataHashMapList)){
            return addCandidateData(newDataHashMapList);
        }
        else{
            return false;
        }
    }

    //This gets the latitude and longitude of the zipcode. These are from the 2016 U.S. Gazetteer.
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

    //this adds data from the text file as a candidate to the system. It fetches necessary information from the database
    //and uses it to complete the candidate information.
    public boolean addCandidateData(List<HashMap<String, String>> candidateHashMapList){
            try {
                ResultSet districtResultsSet = this.dataModel.getTableData("district_table", "state",
                        candidateHashMapList.get(0).get("state"));
                ResultSet peopleResultSet = this.dataModel.getTableData("person_table", "state",
                        candidateHashMapList.get(0).get("state"));
                HashMap<String, Integer> districtsHashMap = new HashMap<>();
                HashMap<String, Integer> peopleHashMap = new HashMap<>();

                while (districtResultsSet.next()) {
                    districtsHashMap.put(districtResultsSet.getString("district_name"), districtResultsSet.getInt("district_id"));
                }

                while (peopleResultSet.next()) {
                    peopleHashMap.put((peopleResultSet.getString("first_name") + " " + peopleResultSet.getString("middle_name")
                                    + " " + peopleResultSet.getString("last_name") + " "
                                    + peopleResultSet.getString("post_nominal")),
                            peopleResultSet.getInt("person_id"));
                }

                List<HashMap<String, String>> candidateList = completeCandidateList(candidateHashMapList, peopleHashMap, districtsHashMap);
                return this.dataModel.addDataToCandidateTable(candidateList);
            }
            catch (SQLException sqle) {
                sqle.printStackTrace();
                System.out.println(sqle);
                return false;
            }
    }


    //This checks to see if there are any people from the text file who are already in the database, and removes them is so.
    private List<HashMap<String, String>> removeDuplicatePersons(List<HashMap<String, String>> candidateHashMapList) {
        List<HashMap<String, String>> checkedList = new ArrayList<>();
        try {
            ResultSet peopleResultSet = this.dataModel.getTableData("person_table", "state", candidateHashMapList.get(0).get("state"));
            List existingPeopleList = new ArrayList();

            while (peopleResultSet.next()) {
                String existingName = peopleResultSet.getString("first_name") + " " + peopleResultSet.getString("middle_name") + " "
                        + " " + peopleResultSet.getString("last_Name") + " " + peopleResultSet.getString("post_nominal");
                existingPeopleList.add(existingName);
            }

            for(HashMap<String, String> candidateHashMap: candidateHashMapList){
                if(!existingPeopleList.contains(candidateHashMap.get("first_name") + " " + candidateHashMap.get("middle_name") + " "
                        + " " + candidateHashMap.get("last_Name") + " " + candidateHashMap.get("post_nominal"))){
                    checkedList.add(candidateHashMap);
                }
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println(sqle);
        }
        return checkedList;
    }

    //This takes finishes off the candidate list, which needs both a district_id and a person_id to be complete.
    private List<HashMap<String, String>> completeCandidateList(List<HashMap<String, String>> candidateHashMapList, HashMap<String, Integer> peopleHashMap, HashMap<String, Integer> districtHashMap){
        for(HashMap<String, String> candidate: candidateHashMapList) {
            candidate.put("district_id", districtHashMap.get(candidate.get("district")).toString());
            String candidateName = candidate.get("first_name") + " " + candidate.get("middle_name") + " " + candidate.get("last_name") + " " + candidate.get("post_nominal");
            candidate.put("person_id", peopleHashMap.get(candidateName).toString());
        }
        return candidateHashMapList;
    }

    //this method adds new people to the person table.
    public boolean addDataToPersonTable(List<HashMap<String, String>> personHashMapList){
        List<HashMap<String, String>> newPersonList = removeDuplicatePersons(personHashMapList);
        return this.dataModel.addPeople(newPersonList);
    }

    //This method finds the closest competitive seat for a zip code.
    public HashMap getClosestCompetitiveSeat(String zipCodeInputString){
        HashMap zipCoordinatesHashMap = getZipCodeLatLong(zipCodeInputString);
        ResultSet tableDataResultSet = this.dataModel.getCompetitiveRaces();
        return parseResults(tableDataResultSet, (Double) (zipCoordinatesHashMap.get("latitude")), (Double) (zipCoordinatesHashMap.get("longitude")));
    }

    //This gets the closest competitive seat for a given chamber.
    public HashMap getClosestCompetitiveSeat(String zipCodeInputString, String chamber){
        HashMap zipCoordinatesHashMap = getZipCodeLatLong(zipCodeInputString);
        ResultSet tableDataResultSet = this.dataModel.getCompetitiveRaces(chamber);
        return parseResults(tableDataResultSet, (Double) (zipCoordinatesHashMap.get("latitude")), (Double) (zipCoordinatesHashMap.get("longitude")));
    }

    //This takes the results and puts them in a format this readable as console output.
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

    //This get the haversine distrance (the distance between two points on a map) for a zip code and for a competitive seat.
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


    //This starts the database.
    public DataParserModeler startupDatabase(String jdbcDriver, String dbConnectStr, String keyPath) {
        HashMap<String, String> keysHashMap = getKeys(keyPath);
        return new DataParserModeler(jdbcDriver, dbConnectStr, keysHashMap.get("login"), keysHashMap.get("password"));
    }

    //creates the sample database.
    public boolean createSampleDatabase(){
        return this.dataModel.createSampleDatabase();
    }

    //loads the sample data.
    public boolean loadSampleData(String dbConnectStr, String keyPath, HashMap<String, String[]> dataHashMap){
        HashMap<String, String> keyHashMap = getKeys(keyPath);
        return this.dataModel.loadSampleData(dbConnectStr, keyHashMap.get("login"), keyHashMap.get("password"), dataHashMap);
    }

    //Gets the keys used by the database.
    public HashMap<String, String> getKeys(String keyPath) {
        HashMap keyHashMap = new HashMap();
        try (Stream<String> stream = Files.lines(Paths.get(keyPath))) {
            Object[] keyArray = stream.toArray();

            for (Object key : keyArray) {
                String[] keyLine = key.toString().split(",");
                keyHashMap.put(keyLine[0], keyLine[1]);
            }

        } catch (IOException e) {
            System.out.println(e);
        }
        return keyHashMap;
    }



   //closes the database.
    public boolean closeDatabase(){
        return this.dataModel.closeConnection();
    }
}
