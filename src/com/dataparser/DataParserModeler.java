package com.dataparser;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

//This class takes data from the database and gives it to the controller.
public class DataParserModeler {


    Connection swingLeftDatabaseConnection = null;

    //starts up the database when the class is created.
    DataParserModeler(String jdbcDriver, String dbConnectStr, String dbUserid, String dbPassword) {
        try {
            Class.forName(jdbcDriver).newInstance();
            this.swingLeftDatabaseConnection = DriverManager.getConnection(dbConnectStr, dbUserid, dbPassword);
            this.swingLeftDatabaseConnection.setAutoCommit(false);
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    //Adds people to the person_table
    public boolean addPeople(List<HashMap<String, String>> peopleList) {
        try {
            PreparedStatement addPeoplePreparedStatement = null;
            String sqlStatementPreString = "INSERT INTO person_table (first_name, middle_name, last_name, post_nominal, state) VALUES (?,?,?,?,?)";
            addPeoplePreparedStatement = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementPreString);
            for (HashMap<String, String> personHashMap : peopleList) {
                addPeoplePreparedStatement.setString(1, personHashMap.get("first_name"));
                addPeoplePreparedStatement.setString(2, personHashMap.get("middle_name"));
                addPeoplePreparedStatement.setString(3, personHashMap.get("last_name"));
                addPeoplePreparedStatement.setString(4, personHashMap.get("post_nominal"));
                addPeoplePreparedStatement.setString(5, personHashMap.get("state"));
                addPeoplePreparedStatement.execute();
            }
            this.swingLeftDatabaseConnection.commit();
            addPeoplePreparedStatement.close();
            System.out.println("People added successfully.");
            return true;
        }
        catch (SQLException sqle){
            System.out.println("Attempt to insert people failed.");
            System.out.println(sqle);
            return false;
        }
    }

    //closes the database.
    public boolean closeConnection(){
        try{
            this.swingLeftDatabaseConnection.close();
        }
        catch(SQLException sqle){
            System.out.println(sqle);
            return false;
        }
        return true;
    }

    //this is used large scale updates that might be needed.
    public boolean updateTable(List<HashMap> updateList){
        try{
            PreparedStatement updatePreparedStatment = null;
            for(HashMap<String, String> updateHashMap: updateList) {
                String updateString = "UPDATE " + updateHashMap.get("tableName") + " SET " + updateHashMap.get("fieldName") + " WHERE " + updateHashMap.get("whereColumn") + " LIKE '?'";
                updatePreparedStatment = this.swingLeftDatabaseConnection.prepareCall(updateString);
                System.out.println(updateHashMap.get("whereValue"));
                updatePreparedStatment.setString(1, updateHashMap.get("whereValue"));
                updatePreparedStatment.executeUpdate();
            }
            this.swingLeftDatabaseConnection.commit();
            updatePreparedStatment.close();
        }
        catch (SQLException sqle){
            sqle.printStackTrace();
            System.out.println(sqle);
            return false;
        }
        return true;
    }

    //Takes the data about people and creates entries into the political_party table. If the party already exists, then it doesn't add it.
    public boolean addDataToPartyTable(List<String> politicalPartyList){

        ResultSet existingPoliticalPartiesResultsSet = getTableData("political_party_table");
        try {
            while(existingPoliticalPartiesResultsSet.next()){
                if(politicalPartyList.contains(existingPoliticalPartiesResultsSet.getString("party_name"))){
                    politicalPartyList.remove(existingPoliticalPartiesResultsSet.getString("party_name"));
                }
            }
            PreparedStatement politicalPartiesPreparedStatement = null;
            for (String party : politicalPartyList) {
                politicalPartiesPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement("insert into political_party_table (party_name) values (?)");
                politicalPartiesPreparedStatement.setString(1, party);
                politicalPartiesPreparedStatement.executeUpdate();
            }
            this.swingLeftDatabaseConnection.commit();
            politicalPartiesPreparedStatement.close();
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            return false;
        }
        return true;
    }


    //adds data to the candidate table.
    public boolean addDataToCandidateTable(List<HashMap<String, String>> candidateList) {
        try {
            PreparedStatement candidateTablePreparedStatment = null;
            for (HashMap candidate : candidateList) {
                candidateTablePreparedStatment = this.swingLeftDatabaseConnection.prepareStatement("INSERT INTO " + "candidate_table (party_id, result_percentage, result_votes, result, person_id, election_cycle_id, district_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
                candidateTablePreparedStatment.setInt(1, Integer.parseInt(candidate.get("party_id").toString()));
                candidateTablePreparedStatment.setDouble(2, Double.parseDouble(candidate.get("percentage").toString().trim()));
                candidateTablePreparedStatment.setInt(3, Integer.parseInt(candidate.get("votes").toString().trim()));
                candidateTablePreparedStatment.setString(4, candidate.get("result").toString().trim());
                candidateTablePreparedStatment.setInt(5, Integer.parseInt(candidate.get("person_id").toString().trim()));
                candidateTablePreparedStatment.setInt(6, Integer.parseInt(candidate.get("election_cycle_id").toString().trim()));
                candidateTablePreparedStatment.setInt(7, Integer.parseInt(candidate.get("district_id").toString().trim()));
                candidateTablePreparedStatment.executeUpdate();
            }
            this.swingLeftDatabaseConnection.commit();
            candidateTablePreparedStatment.close();

        } catch (SQLException sqle) {
            System.out.println("Adding candidates failed.");
            System.out.println(candidateList.toString());
            System.out.println(sqle);
            return false;
        }
        System.out.println("Adding candidates succeeded.");
        return true;
    }

    public ResultSet getTableData(String tableName){
        ResultSet tableDataResultSet = null;
        try{
            String sqlStatementString = "SELECT * FROM " + tableName;
            PreparedStatement tableDataPreparedStatement;
            tableDataPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementString);
            tableDataResultSet = tableDataPreparedStatement.executeQuery();
        }
        catch (SQLException sqle){
            System.out.println(sqle);
        }
        return tableDataResultSet;
    }

    //this gets data from the table when given a where string statement.
    public ResultSet getTableData(String tableName, String whereColumn, String whereValue){
        ResultSet tableDataResultSet = null;
        try{
            String sqlStatementString = "SELECT * FROM " + tableName + " WHERE " + whereColumn +" LIKE ?";
            PreparedStatement tableDataPreparedStatement;
            tableDataPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementString);
            tableDataPreparedStatement.setString(1, whereValue);
            tableDataResultSet = tableDataPreparedStatement.executeQuery();
        }
        catch (SQLException sqle){
            System.out.println(sqle);
        }
        return tableDataResultSet;
    }

    //this gets all the data from a table when given a where integer statement.
    public ResultSet getTableData(String tableName, String whereColumn, int whereValue){
        ResultSet tableDataResultSet = null;
        try{
            String sqlStatementString = "SELECT * FROM " + tableName + " WHERE " + whereColumn +" LIKE ?";
            PreparedStatement tableDataPreparedStatement;
            tableDataPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementString);
            tableDataPreparedStatement.setInt(1, whereValue);
            tableDataResultSet = tableDataPreparedStatement.executeQuery();

        }
        catch (SQLException sqle){
            System.out.println(sqle);
        }
        return tableDataResultSet;
    }

    //Gets data from the table when given a list of specific fields to select.
    public ResultSet getTableData(List<String> selectList,String tableName, String whereColumn, String whereValue){
        ResultSet tableDataResultSet = null;
        StringJoiner selectionStringJoiner = new StringJoiner(", ");
        String selectionString;
        if(selectList.size() < 1){
            selectionString = "*";
        }
        else {
            for (String selection : selectList) {
                selectionStringJoiner.add(selection);
            }
            selectionString = selectionStringJoiner.toString();
        }
        try{
            PreparedStatement tableDataPreparedStatement;
            String preStatement = "SELECT " + selectionString + " FROM " + tableName +" WHERE " + whereColumn + " LIKE ?";
            tableDataPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement(preStatement);
            tableDataPreparedStatement.setString(1, whereValue);
            tableDataResultSet = tableDataPreparedStatement.executeQuery();

        }
        catch (SQLException sqle){
            System.out.println(sqle);
        }
        return tableDataResultSet;
    }

    //this finds the competitive races, as defined by me.
    public ResultSet getCompetitiveRaces(){
        ResultSet tableDataResultSet = null;
        try{
            String sqlStatementString = "SELECT * FROM district_table JOIN candidate_table ON district_table.district_id = candidate_table.district_id " +
                    "WHERE candidate_table.result like 'Won' AND candidate_table.result_percentage < 60 AND candidate_table.party_id = 6";
            PreparedStatement tableDataPreparedStatement;
            tableDataPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementString);
            tableDataResultSet = tableDataPreparedStatement.executeQuery();
        }
        catch (SQLException sqle){
            System.out.println(sqle);
        }
        return tableDataResultSet;
    }

    //this finds the competitive races for a specific chamber.
    public ResultSet getCompetitiveRaces(String chamber){
        ResultSet tableDataResultSet = null;
        try{
            String sqlStatementString = "SELECT * FROM district_table JOIN candidate_table ON district_table.district_id = candidate_table.district_id WHERE result like 'Won' AND candidate_table.result_percentage < 60 AND party_id = 6 AND district_type = ?";
            PreparedStatement tableDataPreparedStatement;
            tableDataPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementString);
            tableDataPreparedStatement.setString(1, chamber);
            tableDataResultSet = tableDataPreparedStatement.executeQuery();
        }
        catch (SQLException sqle){
            System.out.println(sqle);
        }
        return tableDataResultSet;
    }

    //this creates the sample tables that will be used.
    private boolean createSampleTables(String[] tableList){
        PreparedStatement createTablePS;
        try{
            for(String table : tableList) {
                createTablePS = this.swingLeftDatabaseConnection.prepareStatement(table);
                createTablePS.execute();
            }
            return true;
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            sqle.printStackTrace();
            return false;
        }
    }

    //adds candidate data to the Sample database.
    private boolean addSampleDataToCandidateTable(String[] candidateList){
        try {
            PreparedStatement addCandidateData = null;
            String sqlStatementPreString = "INSERT INTO candidate_table (" +
                    "candidate_id, party_id, website, result_percentage, result_votes, result, person_id, election_cycle_id, district_id) " +
                    "VALUES (?,?,?,?,?,?,?,?,?)";
            addCandidateData = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementPreString);
            for (String candidate : candidateList) {
                String[] candidateArray = candidate.split(",");
                addCandidateData.setInt(1, Integer.parseInt(candidateArray[0]));
                addCandidateData.setInt(2, Integer.parseInt(candidateArray[1]));
                addCandidateData.setString(3, candidateArray[2]);
                addCandidateData.setDouble(4, Double.parseDouble(candidateArray[3]));
                addCandidateData.setInt(5, Integer.parseInt(candidateArray[4]));
                addCandidateData.setString(6, candidateArray[5]);
                addCandidateData.setInt(7, Integer.parseInt(candidateArray[6]));
                addCandidateData.setInt(8, Integer.parseInt(candidateArray[7]));
                addCandidateData.setInt(9, Integer.parseInt(candidateArray[8]));
                addCandidateData.execute();
            }
            this.swingLeftDatabaseConnection.commit();
            addCandidateData.close();
            return true;
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            return false;
        }
    }

    //adds district data to the sample database.
    private boolean addSampleDataToDistrictTable(String[] districtList){
        try {
            PreparedStatement addDistrictDataPS = null;
            String sqlStatementPreString = "INSERT INTO district_table (" +
                    "district_id, state, district_name, district_type, latitude, longitude, next_election_year_fk, last_election_year_fk) " +
                    "VALUES (?,?,?,?,?,?,?,?)";
            addDistrictDataPS = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementPreString);
            for (String district : districtList) {
                String[] districtArray = district.split(",");
                addDistrictDataPS.setInt(1, Integer.parseInt(districtArray[0]));
                addDistrictDataPS.setString(2, districtArray[1]);
                addDistrictDataPS.setString(3, districtArray[2]);
                addDistrictDataPS.setString(4, districtArray[3]);
                addDistrictDataPS.setDouble(5, Double.parseDouble(districtArray[4]));
                addDistrictDataPS.setDouble(6, Double.parseDouble(districtArray[5]));
                addDistrictDataPS.setInt(7, Integer.parseInt(districtArray[6]));
                addDistrictDataPS.setInt(8, Integer.parseInt(districtArray[7]));
                addDistrictDataPS.execute();
            }
            this.swingLeftDatabaseConnection.commit();
            addDistrictDataPS.close();
            return true;
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            return false;
        }
    }

    //adds election cycle data to the sample database.
    private boolean addSampleDataToElectionCycleTable(String[] electionCycleList){
        try {
            PreparedStatement addElectionCycleDataPS = null;
            String sqlStatementPreString = "INSERT INTO election_cycle_table (" +
                    "election_cycle_id, cycle_year) " +
                    "VALUES (?,?)";
            addElectionCycleDataPS = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementPreString);
            for (String electionCycle : electionCycleList) {
                String[] electionCycleArray = electionCycle.split(",");
                addElectionCycleDataPS.setInt(1, Integer.parseInt(electionCycleArray[0]));
                addElectionCycleDataPS.setInt(2, Integer.parseInt(electionCycleArray[1]));
                addElectionCycleDataPS.execute();
            }
            this.swingLeftDatabaseConnection.commit();
            addElectionCycleDataPS.close();
            return true;
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            return false;
        }
    }


    //adds party data to the sample database.
    private boolean addSampleDataToPartyTable(String[] partyList){
        try {
            PreparedStatement addPartyDataPS = null;
            String sqlStatementPreString = "INSERT INTO national_political_party_table (" +
                    "party_id, party_name) " +
                    "VALUES (?,?)";
            addPartyDataPS = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementPreString);
            for (String party : partyList) {
                String[] partyArray = party.split(",");
                addPartyDataPS.setInt(1, Integer.parseInt(partyArray[0]));
                addPartyDataPS.setString(2, partyArray[1]);
                addPartyDataPS.execute();
            }
            this.swingLeftDatabaseConnection.commit();
            addPartyDataPS.close();
            return true;
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            return false;
        }
    }

    //adds district data to the sample database.
    private boolean addSampleDataToPersonTable(String[] personList){
        try {
            PreparedStatement addPersonPS = null;
            String sqlStatementPreString = "INSERT INTO person_table (" +
                    "person_id, first_name, middle_name, last_name, post_nominal, state) " +
                    "VALUES (?,?,?,?,?,?)";
            addPersonPS = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementPreString);
            for (String district : personList) {
                String[] districtArray = district.split(",");
                addPersonPS.setInt(1, Integer.parseInt(districtArray[0]));
                addPersonPS.setString(2, districtArray[1]);
                addPersonPS.setString(3, districtArray[2]);
                addPersonPS.setString(4, districtArray[3]);
                addPersonPS.setString(5, districtArray[4]);
                addPersonPS.setString(6, districtArray[5]);
                addPersonPS.execute();
            }
            this.swingLeftDatabaseConnection.commit();
            addPersonPS.close();
            return true;
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            return false;
        }
    }

    //adds zip code data to the Sample database.
    private boolean addSampleDataToZipCodeTable(String[] zipCodeList){
        try {
            PreparedStatement addZipCodeDataPS = null;
            String sqlStatementPreString = "INSERT INTO zip_code_table (" +
                    "zip_code, longitude, latitude) " +
                    "VALUES (?,?,?)";
            addZipCodeDataPS = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementPreString);
            for (String candidate : zipCodeList) {
                String[] candidateArray = candidate.split(",");
                addZipCodeDataPS.setInt(1, Integer.parseInt(candidateArray[0]));
                addZipCodeDataPS.setDouble(2, Double.parseDouble(candidateArray[1]));
                addZipCodeDataPS.setDouble(3, Double.parseDouble(candidateArray[2]));
                addZipCodeDataPS.execute();
            }
            this.swingLeftDatabaseConnection.commit();
            addZipCodeDataPS.close();
            return true;
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            return false;
        }
    }

    //This creates the sample database.
    public boolean createSampleDatabase(){
        try {
            if(!checkIfDatabaseExists()) {
                Statement createDatabaseStatement = this.swingLeftDatabaseConnection.createStatement();
                createDatabaseStatement.executeUpdate("CREATE DATABASE IF NOT EXISTS sample_database");
                return true;
            }
            else{
                return false;
            }
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            sqle.printStackTrace();
            return false;
        }
    }

    //This checks to see if the datbase exists. If it doesn't, it creates a new one.
    private boolean checkIfDatabaseExists(){
       try {
           ResultSet databasesResultSet = this.swingLeftDatabaseConnection.getMetaData().getCatalogs();
           while (databasesResultSet.next()) {
               if(databasesResultSet.getString(1).contains("sample_database")){
                   return true;
               }
           }
           return false;
       }
       catch (SQLException sqle){
           System.out.println(sqle);
           return true;
       }
    }

    //this adds all of the data into the database, one table at a time. It is nested if statements to avoid foreign key constraints.
    public boolean loadSampleData(String dbConnectStr, String dbUserid, String dbPassword, HashMap<String, String[]> dataHashMap) {
        try {
            this.swingLeftDatabaseConnection = DriverManager.getConnection(dbConnectStr, dbUserid, dbPassword);
            this.swingLeftDatabaseConnection.setAutoCommit(false);
            if (createSampleTables(dataHashMap.get("sample_create_tables.txt"))) {
                if (addSampleDataToZipCodeTable(dataHashMap.get("sample_zip_code_table_data.txt"))) {
                    if (addSampleDataToElectionCycleTable(dataHashMap.get("sample_election_cycle_table_data.txt"))) {
                        if (addSampleDataToPersonTable(dataHashMap.get("sample_person_table_data.txt"))) {
                            if (addSampleDataToPartyTable(dataHashMap.get("sample_national_political_parties_table_data.txt"))) {
                                if (addSampleDataToDistrictTable(dataHashMap.get("sample_district_table_data.txt"))) {
                                    if (addSampleDataToCandidateTable(dataHashMap.get("sample_candidate_table_data.txt"))) {
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            return false;
        }
    }
}
