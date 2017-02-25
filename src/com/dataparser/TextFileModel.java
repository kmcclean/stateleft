package com.dataparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;


public class TextFileModel {

    public static HashMap getMinnesotaSenateElectionResults() {
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

    public void getZipCodeInfo() {

        String textFile = "";

        try (Stream<String> stream = Files.lines(Paths.get("data/new_data/2016_zip_code_info.txt"))) {
            Object[] stringArray = stream.toArray();
            for (Object zipCode : stringArray) {
                String zipCodeString = zipCode.toString();
                String[] zipCodeStringArray = zipCodeString.split("\t");
                if(isInteger(zipCodeStringArray[0])) {
                    StringJoiner zipCodeStringJoiner = new StringJoiner(",");
                    zipCodeStringJoiner.add(zipCodeStringArray[0]);
                    zipCodeStringJoiner.add(zipCodeStringArray[5]);
                    zipCodeStringJoiner.add(zipCodeStringArray[6]);
                    String zipCodeCSV = zipCodeStringJoiner.toString();
                    textFile = textFile + zipCodeCSV + "\n";
                }
            }
            Files.write(Paths.get("data/long_lat_files/zip_codes_long_lat.txt"), textFile.getBytes());
        } catch (IOException e) {
            System.out.println("Text file failure.");
        }
        System.out.println("Text file success.");
    }

    public void getUpperHouseInfo() {
        String textFile = "";
        try (Stream<String> stream = Files.lines(Paths.get("data/new_data/2016_upper_house_info.txt"))) {
            Object[] stringArray = stream.toArray();
            for (Object upperHouseSeat : stringArray) {
                String upperHouseString = upperHouseSeat.toString();
                String[] upperHouseStringArray = upperHouseString.split("\t");
                if(!upperHouseStringArray[0].equals("USPS")) {
                    StringJoiner upperHouseStringJoiner = new StringJoiner(",");
                    upperHouseStringJoiner.add(upperHouseStringArray[0]);
                    upperHouseStringJoiner.add(upperHouseStringArray[2]);
                    upperHouseStringJoiner.add("Upper");
                    upperHouseStringJoiner.add(upperHouseStringArray[7]);
                    upperHouseStringJoiner.add(upperHouseStringArray[8]);
                    String upperHouseCSV = upperHouseStringJoiner.toString();
                    textFile = textFile + upperHouseCSV + "\n";
                }
            }
            Files.write(Paths.get("data/district_info/upper_house_info.txt"), textFile.getBytes());
        } catch (IOException e) {
            System.out.println("Text file failure.");
        }
        System.out.println("Text file success.");
    }

    public void getLowerHouseInfo() {
        String textFile = "";
        try (Stream<String> stream = Files.lines(Paths.get("data/new_data/2016_lower_house_info.txt"))) {
            Object[] stringArray = stream.toArray();
            for (Object lowerHouseSeat : stringArray) {
                String lowerHouseString = lowerHouseSeat.toString();
                String[] lowerHouseStringArray = lowerHouseString.split("\t");
                if(!lowerHouseStringArray[0].equals("USPS")) {
                    StringJoiner lowerHouseStringJoiner = new StringJoiner(",");
                    lowerHouseStringJoiner.add(lowerHouseStringArray[0]);
                    lowerHouseStringJoiner.add(lowerHouseStringArray[2]);
                    lowerHouseStringJoiner.add("Lower");
                    lowerHouseStringJoiner.add(lowerHouseStringArray[7]);
                    lowerHouseStringJoiner.add(lowerHouseStringArray[8]);
                    String lowerHouseCSV = lowerHouseStringJoiner.toString();
                    textFile = textFile + lowerHouseCSV + "\n";
                }
            }
            Files.write(Paths.get("data/district_info/lower_house_info.txt"), textFile.getBytes());
        } catch (IOException e) {
            System.out.println("Text file failure.");
        }
        System.out.println("Text file success.");
    }

    private boolean isInteger(String integerCheckString){
        try{
            Integer.parseInt(integerCheckString);
        }
        catch (Exception e){
            return false;
        }
        return true;
    }
}
