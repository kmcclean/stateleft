package com.dataparser;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;



//This class is used for importing text files into the database. Because the design of the data changes with each file,
// the methods in this class are designed to be specfic, and often have to be standardized into a CSV file format that
// can be used easily.
public class TextFileModel {

    //When data is to be imported into the database, this makes sure there are the correct number of CSV fields on each line.
    private boolean checkFile(Object[] fileObject) {
        for (Object lineObject : fileObject) {
            if (lineObject.toString().split(",").length != 11 || lineObject.toString().contains("%")) {
                System.out.println("Check File Error on line: " + lineObject.toString());
                return false;
            }
        }
        return true;
    }

    //this fetches a text file and returns it as an Object array for use by the program.
    private Object[] fetchTextFile(String filePathString){
        try (Stream<String> stream = Files.lines(Paths.get(filePathString))) {
            return stream.toArray();
        }
        catch (IOException ioe){
            System.out.println("Could not find file at " + filePathString);
            return null;
        }

    }

    //This gets the prepared text file, checks to make sure it is set up correctly, and then creates a HashMap that
    // the controller class and model class use to receive information.
    protected List<HashMap<String,String>> fetchTextFileForDatabase(String filePathString) {
        Object[] fileObject = fetchTextFile(filePathString);
        if(!checkFile(fileObject)){
            System.exit(0);
        }

        List<HashMap<String, String>> candidateHashMapList = new ArrayList<HashMap<String, String>>();

        for(Object candidateObject: fileObject){
            String[] candidateArray = candidateObject.toString().split(",");
            HashMap<String, String> candidateHashMap = new HashMap<String, String>();
            candidateHashMap.put("first_name", candidateArray[0]);
            candidateHashMap.put("middle_name", candidateArray[1]);
            candidateHashMap.put("last_name", candidateArray[2]);
            candidateHashMap.put("post_nominal", candidateArray[3]);
            candidateHashMap.put("party_id", candidateArray[4]);
            candidateHashMap.put("votes", candidateArray[5]);
            candidateHashMap.put("percentage", candidateArray[6]);
            candidateHashMap.put("district", candidateArray[7]);
            candidateHashMap.put("result", candidateArray[8]);
            candidateHashMap.put("election_cycle_id", candidateArray[9]);
            candidateHashMap.put("state", candidateArray[10]);
            candidateHashMapList.add(candidateHashMap);
        }

        return candidateHashMapList;
    }

    //This takes the information from the file (usually used with the staging file) and puts it into the prepared file.
    // This is where the file that is taken and used by the database.
    public boolean prepareFile(String filePath) {
            Object[] fileObjectArray = fetchTextFile(filePath);
            String newFile = prepareText(fileObjectArray);
        try{
            PrintWriter writer = new PrintWriter("data/prepared_data/prepared_file.txt", "UTF-8");
            writer.append(newFile);
            writer.close();
            return true;
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
    }

    //This is used to test adjustmests to the prepareText method, to ensure it is working properly before moving the
    // data to the prepared_file.txt file, which is what is sent to the database.
    public void testChanges(String filePath) {
        Object[] fileObjectArray = fetchTextFile(filePath);
        String newFile = prepareText(fileObjectArray);
        System.out.println(newFile);
    }

    //This is where the data taken from a file is converted into a standardized CSV format in order to be loaded into the database.
    //This method is changed often in order to convert the staging_file.txt into the prepared_file.txt.
    private String prepareText(Object[] fileObjectArray) {
        String fullString = "";

        HashMap<String, Integer> totalVoteHashMap = new HashMap<>();
        List<String> districtList = new ArrayList();
        for(Object o: fileObjectArray){
            String districtName = createDistrictName(o.toString().split(",")[1] + o.toString().split(",")[2].replace("\"",""));

            if(!districtList.contains(districtName)){
                districtList.add(districtName);
                totalVoteHashMap.put(districtName, Integer.parseInt(o.toString().split(",")[23]));
            }
            else{
                totalVoteHashMap.put(districtName, totalVoteHashMap.get(districtName) + Integer.parseInt(o.toString().split(",")[23]));
            }
        }

        for (Object o : fileObjectArray) {
            String[] lineArray = o.toString().replace("\"","").split(",");
            String party;
            String votes;
            String percentage;
            String name;
            String cycleId = "";
            String result;
            String district;

            //reset this for each file.
            String cycle = "2016";

            name = createName(lineArray[16] + "," + lineArray[17]);
            district = createDistrictName(lineArray[1] + "," + lineArray[2]);
            party = setPartyId(lineArray[19]);
            votes = lineArray[23];
            percentage = createPercentage(district, votes, totalVoteHashMap);

            if (Double.parseDouble(percentage) >= 50) {
                result = "Won";
            } else {
                result = "Lost";
            }


            //this sets the election cycle with the id associated with it in the database.
            switch (cycle) {
                case "2016":
                    cycleId = "1";
                    break;
                case "2015":
                    cycleId = "2";
                    break;
                case "2014":
                    cycleId = "3";
                    break;
                case "2017":
                    cycleId = "4";
                    break;
                case "2013":
                    cycleId = "5";
                    break;
                case "2018":
                    cycleId = "6";
                    break;
                case "2019":
                    cycleId = "7";
                    break;
                case "2020":
                    cycleId = "8";
                    break;
            }

            StringJoiner stringJoiner = new StringJoiner(",");
            stringJoiner.add(name.trim());
            stringJoiner.add(party.trim());
            stringJoiner.add(votes.trim());
            stringJoiner.add(percentage.trim());
            stringJoiner.add(district.trim());
            stringJoiner.add(result.trim());
            stringJoiner.add(cycleId.trim());
            //reset this for each file.
            stringJoiner.add("HI");
            fullString += stringJoiner.toString() + "\n";
        }
        return fullString;
    }

    //This takes the district information and turns into a district name that is recognized by the system.
    private String createDistrictName(String districtLine){
        if(districtLine.contains("Representative")) {
            return "State House District " + districtLine.split(" ")[districtLine.split(" ").length-1];
        }
        else if(districtLine.contains("Senator")){
            return "State Senate District " + districtLine.split(" ")[districtLine.split(" ").length-1];
        }
        else{
            return null;
        }
    }

    //this sets the party_id that the database associates with each party.
    private String setPartyId(String partyName){

        if (partyName.contains("REP")) {
            return "6";
        }
        else if (partyName.contains("DEM")) {
            return "7";
        }
        else {
            return "16";
        }
    }

    //this method takes the data for percentage and converts it into a double for reading back into the file.
    private String createPercentage(String districtName, String line, HashMap<String, Integer> totalVotesHashMap){
        Integer total_votes = totalVotesHashMap.get(districtName);
        Integer votes = Integer.parseInt(line);

        return Double.toString(Math.round(Double.parseDouble(line) /
                Double.parseDouble(Integer.toString(totalVotesHashMap.get(districtName))) *
                10000.0) / 100.0);
    }

    //This takes the data related to the name and converts into a first name, middle name, last name, and post nominal.
    private String createName(String line) {
        line = line.replace("(L)","").replace("(D)","").replace("(R)","").replace("(I)","").replace("(","").replace(")","");
        String firstName;
        String middleName = "";
        String lastName;
        String postNominal = "";

        firstName = line.split(",")[1].trim();
        if (firstName.contains(" ")){
            middleName = firstName.split(" ")[1];
            firstName = firstName.split(" ")[0];
        }
        lastName = line.split(",")[0];
        if(lastName.split(" ")[lastName.split(" ").length-1].toLowerCase().equals("iii")
                || lastName.split(" ")[lastName.split(" ").length-1].toLowerCase().equals("ii")
                || lastName.split(" ")[lastName.split(" ").length-1].toLowerCase().equals("sr")
                || lastName.split(" ")[lastName.split(" ").length-1].toLowerCase().equals("jr")){
            postNominal = lastName.split(" ")[1];
            lastName = lastName.split(" ")[0];
        }

        return firstName.trim() + "," + middleName.trim() + "," + lastName.trim() + "," + postNominal.trim();
    }

    public String[] getSampleTables(String filePath){
        Object[] fileObjectArray = fetchTextFile(filePath);
        String fullString = "";
        for (Object fileObject: fileObjectArray){
            fullString += fileObject.toString();
        }
        return fullString.split(";");
    }

    public String[] getSampleData(String filePath){
        Object[] fileObjectArray = fetchTextFile(filePath);
        String fullString = "";
        for (Object fileObject: fileObjectArray){
            fullString += fileObject.toString()+"\n";
        }
        System.out.println(fullString);
        return fullString.split("\n");
    }

}
