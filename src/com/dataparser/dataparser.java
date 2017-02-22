package com.dataparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class dataparser {
    public static void main(String[] args) {
        dataParserModeler dataModel = startupDatabase();
        HashMap electionDataHashMap = getDataFromFile();

        if (dataModel.addDataToPartyTable((List) electionDataHashMap.get("politicalPartyList"))) {
            System.out.println("political_party_table update successful.");
        } else {
            System.out.println("political_party_table update failed.");
            System.exit(1);
        }
        if (dataModel.addDataToPersonTable((List) electionDataHashMap.get("personNamesList"))) {
            System.out.println("person_table update successful.");
        } else {
            System.out.println("person_table update failed.");
            System.exit(1);
        }
        if (dataModel.addDataToRaceTable()) {
            System.out.println("race_table update successful.");
        } else {
            System.out.println("person_table update failed.");
            System.exit(1);
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
            List<HashMap> candidateHMList = new ArrayList<>();


            for (Object houseResultLine : stringArray) {
                HashMap candidateHashMap = new HashMap();
                if (houseResultLine.toString().substring(houseResultLine.toString().length() - 1).equals("%") && !houseResultLine.toString().contains("WRITE-IN")) {
                    String[] resultsLine = houseResultLine.toString().split("\t");
                    if (!politicalParyList.contains(resultsLine[0])) {
                        politicalParyList.add(resultsLine[0]);
                    }
                    personNamesList.add(resultsLine[1]);
                    candidateHashMap.put("party", resultsLine[0]);
                    candidateHashMap.put("name", resultsLine[1]);
                    candidateHashMap.put("votes", resultsLine[2]);
                    candidateHashMap.put("percentage", resultsLine[3]);
                    if (Double.parseDouble(resultsLine[3]) >= 50) {
                        candidateHashMap.put("result", "Won");
                    } else {
                        candidateHashMap.put("result", "Lost");
                    }
                    candidateHMList.add(candidateHashMap);
                }
            }

            electionDataHashMap.put("personNamesList", personNamesList);
            electionDataHashMap.put("politicalPartyList", politicalParyList);
            electionDataHashMap.put("candidateHMList", candidateHMList);

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
