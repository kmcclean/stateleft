package com.dataparser;

import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {


        DataParserController dataParserController = new DataParserController();


        System.out.printf("Enter zip code: ");

        Scanner scanIn = new Scanner(System.in);
        String inputString = scanIn.nextLine();

        scanIn.close();
        HashMap closestSeatHashMap = dataParserController.getClosestCompetitiveSeat(inputString);
        System.out.println("Closest competitive seat is " + closestSeatHashMap.get("district_name") + " in the state of " + closestSeatHashMap.get("state") + ".");
        HashMap closestHouseSeatHashMap = dataParserController.getClosestCompetitiveSeat(inputString, "Lower");
        System.out.println("Closest competitive House seat is " + closestHouseSeatHashMap.get("district_name") + " in the state of " + closestHouseSeatHashMap.get("state") + ".");
        HashMap closestUpperSeatHashMap = dataParserController.getClosestCompetitiveSeat(inputString, "Upper");
        System.out.println("Closest competitive Senate seat is " + closestUpperSeatHashMap.get("district_name") + " in the state of " + closestUpperSeatHashMap.get("state") + ".");




        if(dataParserController.closeDatabase()){
            System.out.println("Database successfully closed.");
        }
        else{
            System.out.println("Database did not close.");
        }
    }

}
