package com.dataparser;

import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

            Haversine haversine = new Haversine();
            haversine.compareResults();


        System.out.printf("Enter zip code: ");

        Scanner scanIn = new Scanner(System.in);
        String inputString = scanIn.nextLine();

        scanIn.close();

        DataParserController dataParserController = new DataParserController();


        List<List>zipCoordinates = dataParserController.getZipCodeLatLong(inputString);

        for (List<String> zipCodeList : zipCoordinates){
            HashMap closestSeatHashMap = dataParserController.getClosestCompetitiveSeat(zipCodeList);
            System.out.println("Closest competitive seat is " + closestSeatHashMap.get("district_name") + " in the state of " + closestSeatHashMap.get("state") + ".");
        }
    }

}
