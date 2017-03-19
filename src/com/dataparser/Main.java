package com.dataparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

//Runs the program.
public class Main {
    //this is designed to allow the user to pick the specific methods they need when running the program.
    public static void main(String[] args) {
        //createSampleDatabaseAndRunProgram();
        //testSetup("data/staging_folder/staging_file.txt");
        //prepareFile("data/staging_folder/staging_file.txt");
        //addDataToDatabase("data/prepared_data/prepared_file.txt");
        //runProgram();
    }

    //This runs the program using a console, allowing the user to find the closest competitive state legislature race.
    public static void runProgram() {
        DataParserController dataParserController = new DataParserController("com.mysql.jdbc.Driver","jdbc:mysql://localhost:3306/swing_left_database", "keys/databaselogin.txt");
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
        String keyPath = "keys/databaselogin.txt";
        DataParserController dataParserController = new DataParserController("com.mysql.jdbc.Driver","jdbc:mysql://localhost:3306/swing_left_database", keyPath);
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

    //this will create a sample database and put all the necessary information into it so that it can be used as an example.
    public static void createSampleDatabaseAndRunProgram() {

        String keyPath = "sample_files/sample_keys.txt";
        if(System.getProperty("os_name").contains("Window")){
            keyPath = "sample_files\\sample_keys.txt";
        }
        DataParserController dataParserController = new DataParserController("com.mysql.jdbc.Driver", "jdbc:mysql://localhost", keyPath);
        if(dataParserController.createSampleDatabase()) {

            String createTablesPath;
            List<String> dataPathsList = new ArrayList<>();
            HashMap<String, String[]> dataHashMap = new HashMap<String, String[]>();

            if (System.getProperty("os.name").contains("Window")) {
                createTablesPath = "sample_files\\sample_create_tables.txt";

                dataPathsList.add("sample_files\\sample_candidate_table_data.txt");
                dataPathsList.add("sample_files\\sample_district_table_data.txt");
                dataPathsList.add("sample_files\\sample_election_cycle_table_data.txt");
                dataPathsList.add("sample_files\\sample_person_table_data.txt");
                dataPathsList.add("sample_files\\sample_national_political_parties_table_data.txt");
                dataPathsList.add("sample_files\\sample_zip_code_table_data.txt");

                TextFileModel textFileModel = new TextFileModel();
                dataHashMap.put(createTablesPath.split("\\\\")[createTablesPath.split("\\\\").length - 1], textFileModel.getSampleTables(createTablesPath));

                for (String dataPathString : dataPathsList) {
                    dataHashMap.put(dataPathString.split("\\\\")[dataPathString.split("\\\\").length - 1], textFileModel.getSampleData(dataPathString));
                }
            } else {
                createTablesPath = "sample_files/sample_create_tables.txt";

                dataPathsList.add("sample_files/sample_candidate_table_data.txt");
                dataPathsList.add("sample_files/sample_district_table_data.txt");
                dataPathsList.add("sample_files/sample_election_cycle_table_data.txt");
                dataPathsList.add("sample_files/sample_person_table_data.txt");
                dataPathsList.add("sample_files/sample_national_political_parties_table_data.txt");
                dataPathsList.add("sample_files/sample_zip_code_table_data.txt");

                TextFileModel textFileModel = new TextFileModel();
                dataHashMap.put(createTablesPath.split("/")[createTablesPath.split("/").length - 1], textFileModel.getSampleTables(createTablesPath));

                for (String dataPathString : dataPathsList) {
                    dataHashMap.put(dataPathString.split("/")[dataPathString.split("/").length - 1], textFileModel.getSampleData(dataPathString));
                }
            }
            dataParserController.loadSampleData("jdbc:mysql://localhost/sample_database", keyPath, dataHashMap);
        }

        else {
            dataParserController = new DataParserController("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/sample_database", keyPath);
        }

        System.out.printf("Enter zip code: ");

        Scanner scanIn = new Scanner(System.in);
        String inputString = scanIn.nextLine();

        scanIn.close();
        HashMap closestSeatHashMap = dataParserController.getClosestCompetitiveSeat(inputString);
        System.out.println("Closest competitive seat is " + closestSeatHashMap.get("district_name") + " in the state of " + closestSeatHashMap.get("state") + ".");
        closeDatabase(dataParserController);
    }
}
