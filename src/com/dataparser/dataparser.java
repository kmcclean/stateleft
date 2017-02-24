package com.dataparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

public class dataparser {
    public static void main(String[] args) {
        textFileModel textFileModel = new textFileModel();
        dataParserModeler dataModel = startupDatabase();
        //textFileModel.getZipCodeInfo();
        //textFileModel.getLowerHouseInfo();
        //textFileModel.getUpperHouseInfo();
        //HashMap electionDataHashMap = textFileModel.getMinnesotaSenateElectionResults();
//
//        if (dataModel.addDataToPartyTable((List) electionDataHashMap.get("politicalPartyList"))) {
//            System.out.println("political_party_table update successful.");
//        } else {
//            System.out.println("political_party_table update failed.");
//            System.exit(1);
//        }
//        if (dataModel.addDataToPersonTable((List) electionDataHashMap.get("personNamesList"))) {
//            System.out.println("person_table update successful.");
//        } else {
//            System.out.println("person_table update failed.");
//            System.exit(1);
//        }
//        if(dataModel.addDataToCandidateTable((List) electionDataHashMap.get("candidateList"))){
//            System.out.println("candidates successfully added.");
//        }
//        else{
//            System.out.println("candidates not added.");
//            System.exit(0);
//        }
//        if(dataModel.addDataToLegislatureSeatsTable()){
//            System.out.println("legislature data successsfully added.");
//        }
//        else{
//            System.out.println("legislature data addition failed.");
//            dataModel.closeConnection();
//            System.exit(1);
//        }
//        if (dataModel.addLowerHouseDistricts()) {
//            System.out.println("Lower House Info Added.");
//        } else {
//            System.out.println("Lower House Update Failed.");
//            System.exit(0);
//        }
//        if (dataModel.addUpperHouseDistricts()) {
//            System.out.println("Upper House Info Added.");
//        } else {
//            System.out.println("Upper House Update Failed.");
//            System.exit(0);
//        }
        if (dataModel.addZipCodeInfo()) {
            System.out.println("Zip Code Info Added.");
        } else {
            System.out.println("Zip Code Update Failed.");
            System.exit(0);
        }
        if (dataModel.closeConnection()) {
            System.out.println("Connection closed successfully.");
        } else {
            dataModel.closeConnection();
            System.out.println("Connection closure failed.");
            System.exit(0);
        }
    }

    public static dataParserModeler startupDatabase() {
        HashMap<String, String> keysHashMap = getKeys();
        dataParserModeler databaseModel = new dataParserModeler("jdbc:mysql://localhost:3306/swing_left_database", keysHashMap.get("login"), keysHashMap.get("password"));
        return databaseModel;
    }

    public static HashMap getKeys() {
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
