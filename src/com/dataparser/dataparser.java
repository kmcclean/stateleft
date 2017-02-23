package com.dataparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class dataparser {
    public static void main(String[] args) {
        dataParserModeler dataModel = startupDatabase();
        HashMap electionDataHashMap = getDataFromFile();
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
        if(dataModel.addDataToCandidateTable((List) electionDataHashMap.get("candidateList"))){
            System.out.println("candidates successfully added.");
        }
        else{
            System.out.println("candidates not added.");
            System.exit(0);
        }
        if(dataModel.addDataToLegislatureSeatsTable()){
            System.out.println("legislature data successsfully added.");
        }
        else{
            System.out.println("legislature data addition failed.");
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

        try (Stream<String> stream = Files.lines(Paths.get("data/original/senateElectionResults.txt"))) {
            Object[] stringArray = stream.toArray();
            List<String> personNamesList = new ArrayList<>();
            List<String> politicalParyList = new ArrayList<>();
            List<Candidate> candidateList = new ArrayList<>();

            String senateDistrict = null;
            for (Object senateResultLine : stringArray) {

                if(senateResultLine.toString().contains("State Senator District")){
                    senateDistrict = "";
                    String[] senateDistrictArray = senateResultLine.toString().split(" ");
                    StringJoiner senateDistrictStringJoiner = new StringJoiner(" ");
                    for (int i = 0; i <4; i++){
                        senateDistrictStringJoiner.add(senateDistrictArray[i]);
                    }
                    senateDistrict = senateDistrictStringJoiner.toString();
                }
                else if (senateResultLine.toString().substring(senateResultLine.toString().length() - 1).equals("%") && !senateResultLine.toString().contains("WRITE-IN")) {
                    String[] resultsLine = senateResultLine.toString().split("\t");
                    if (!politicalParyList.contains(resultsLine[0])) {
                        politicalParyList.add(resultsLine[0]);
                    }
                    personNamesList.add(resultsLine[1]);
                    Candidate candidate = new Candidate(resultsLine[0], resultsLine[1], resultsLine[2], resultsLine[3], senateDistrict);
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
