package com.dataparser;

import java.sql.*;
import java.util.*;

//This class takes data from the database and gives it to the controller.
public class DataParserModeler {

    Connection swingLeftDatabaseConnection = null;

    //starts up the database when the class is created.
    DataParserModeler(String dbConnectStr, String dbUserid, String dbPassword) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            this.swingLeftDatabaseConnection = DriverManager.getConnection(dbConnectStr, dbUserid, dbPassword);
            this.swingLeftDatabaseConnection.setAutoCommit(false);
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    DataParserModeler(String jdbcDriver, String dbConnectStr, String dbUserid, String dbPassword, String[] sampleData){
        try {
            Class.forName(jdbcDriver).newInstance();
            Connection createDatabaseConnection = DriverManager.getConnection("jdbc:mysql://localhost/?user=" + dbUserid + "&password=" + dbPassword);
            Statement createDatabaseStatement = createDatabaseConnection.createStatement();
            if(createDatabaseStatement.executeUpdate("CREATE DATABASE IF NOT EXISTS sample_database")==0){
                System.out.println("Sample database created successfully.");
                if(createSampleTables(sampleData)){
                    System.out.println("Tables Added Successfully.");
                }
            }

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
            return false;
        }
    }

}
