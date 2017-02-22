package com.dataparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class dataparser {
    public static void main(String[] args) {
        dataParserModeler dataModel = startupDatabase();
        HashMap electionDataHashMap = getDataFromFile();

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
//        if (dataModel.addDataToRaceTable()) {
//            System.out.println("race_table update successful.");
//        } else {
//            System.out.println("race_table update failed.");
//            System.exit(1);
//        }
//        if(dataModel.addDataToCandidateTable((List) electionDataHashMap.get("candidateList"))){
//            System.out.println("candidates successfully added.");
//        }
//        else{
//            System.out.println("candidates not added.");
//            System.exit(0);
//        }
        if(dataModel.outputTest()){
            System.out.println("Test successsfully ran.");
        }
        else{
            dataModel.closeConnection();
            System.exit(1);
        }
        if(dataModel.closeConnection()){
            System.out.println("Connction successfully closed.");
        }
        else{
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


    public static HashMap getDataFromFile() {
        HashMap<String, List> electionDataHashMap = new HashMap();

        try (Stream<String> stream = Files.lines(Paths.get("data/original/houseElectionResults.txt"))) {
            Object[] stringArray = stream.toArray();
            List<String> personNamesList = new ArrayList<>();
            List<String> politicalParyList = new ArrayList<>();
            List<Candidate> candidateList = new ArrayList<>();

            String houseDistrict = null;
            for (Object houseResultLine : stringArray) {

                if(houseResultLine.toString().contains("State Representative District")){
                    houseDistrict = houseResultLine.toString();
                }
                else if (houseResultLine.toString().substring(houseResultLine.toString().length() - 1).equals("%") && !houseResultLine.toString().contains("WRITE-IN")) {
                    String[] resultsLine = houseResultLine.toString().split("\t");
                    if (!politicalParyList.contains(resultsLine[0])) {
                        politicalParyList.add(resultsLine[0]);
                    }
                    personNamesList.add(resultsLine[1]);
                    Candidate candidate = new Candidate(resultsLine[0], resultsLine[1], resultsLine[2], resultsLine[3], houseDistrict);
                    candidateList.add(candidate);
                }
            }

            electionDataHashMap.put("personNamesList", personNamesList);
            electionDataHashMap.put("politicalPartyList", politicalParyList);
            electionDataHashMap.put("candidateList", candidateList);

        } catch (IOException e) {
            System.out.println(e);
        }
        return electionDataHashMap;
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
