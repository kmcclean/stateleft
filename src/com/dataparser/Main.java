package com.dataparser;

import java.util.HashMap;
import java.util.Scanner;

//Runs the program.
public class Main {
    //this is designed to allow the user to pick the specific methods they need when running the program.
    public static void main(String[] args) {
        createSampleDatabase();
        //testSetup("data/staging_folder/staging_file.txt");
        //prepareFile("data/staging_folder/staging_file.txt");
        //addDataToDatabase("data/prepared_data/prepared_file.txt");
        //runProgram();
    }

    //This runs the program using a console, allowing the user to find the closest competitive state legislature race.
    public static void runProgram() {
        DataParserController dataParserController = new DataParserController();


        System.out.printf("Enter zip code: ");

        Scanner scanIn = new Scanner(System.in);
        String inputString = scanIn.nextLine();

        scanIn.close();
        HashMap closestSeatHashMap = dataParserController.getClosestCompetitiveSeat(inputString);
        System.out.println("Closest competitive seat is " + closestSeatHashMap.get("district_name") + " in the state of " + closestSeatHashMap.get("state") + ".");
        closeDatabase(dataParserController);

    }

        public static void closeDatabase(DataParserController dataParserController){

        if(dataParserController.closeDatabase()){
            System.out.println("Database successfully closed.");
        }
        else{
            System.out.println("Database did not close.");
        }
    }

    //adds new data to the database.
    public static void addDataToDatabase(String filePathString){
        TextFileModel textFileModel = new TextFileModel();
        DataParserController dataParserController = new DataParserController();
        if(dataParserController.addTextFile(textFileModel.fetchTextFileForDatabase(filePathString))){
            System.out.println("Data added successfully.");
        }
        else{
            System.out.println("Data not added.");
        }

        closeDatabase(dataParserController);
    }

    //prepares a file to be loaded into the database.
    public static void prepareFile(String filePath){
        TextFileModel textFileModel = new TextFileModel();
        if(textFileModel.prepareFile(filePath)){
            System.out.println("File Prepared successfully.");
        }
        else {
            System.out.println("File preparation failed.");
        }
    }

    //test the changes to the prepareText method in the TextFileModel.
    public static void testSetup(String filePath){
        TextFileModel textFileModel = new TextFileModel();
        textFileModel.testChanges(filePath);
    }

    public static void createSampleDatabase(){
        System.out.println(System.getProperty("os.name"));
    }
}
