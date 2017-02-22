package com.dataparser;
import com.sun.rowset.internal.Row;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class dataParserModeler {

    Connection swingLeftDatabaseConnection = null;

    dataParserModeler(String db_connect_str, String db_userid, String db_password) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            this.swingLeftDatabaseConnection = DriverManager.getConnection(db_connect_str, db_userid, db_password);
            this.swingLeftDatabaseConnection.setAutoCommit(false);
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

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

    //TODO Work on this next.
//    public boolean addDataToCandidateTable(List<String> candidateInformationList){
//        try {
//            //http://stackoverflow.com/questions/16882971/retrieve-entire-row-with-resultset
//            ResultSet raceTableResultsSet = getTableData("race_table", "election_cycle_id", "1");
//            HashMap raceTableHashMap = new HashMap();
//            Row row = null;
//            List<Row> raceTableTable = new ArrayList<Row>();
//            for (raceTableResultsSet.next()){
//                raceTableHashMap.put(raceTableResultsSet.getString("district_id"), raceTableResultsSet.);
//            }
//
//            PreparedStatement politicalPartiesPreparedStatement = null;
//            for (String party : politicalPartyList) {
//                politicalPartiesPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement("insert into political_party_table (party_name) values (?)");
//                politicalPartiesPreparedStatement.setString(1, party);
//                politicalPartiesPreparedStatement.addBatch();
//            }
//            politicalPartiesPreparedStatement.executeBatch();
//            this.swingLeftDatabaseConnection.commit();
//            politicalPartiesPreparedStatement.close();
//        }
//        catch (SQLException sqle){
//            System.out.println(sqle);
//            return false;
//        }
//        return true;
//    }


    //Takes the data about people and creates entries into the political_party table.
    public boolean addDataToPartyTable(List<String> politicalPartyList){
        try {
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

    //Takes the data about people and creates entries into the person_table.
    public boolean addDataToPersonTable(List<String> personNamesList) {
        try {
            PreparedStatement personNamesListPreparedStatement = null;
            for (String person : personNamesList) {
                String[] personNameStringSplit = person.split(" ");
                //System.out.println(personNameStringSplit.length);
                if (personNameStringSplit.length <= 3) {
                    personNamesListPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement("insert into person_table (first_name, last_name) values (?, ?)");
                    personNamesListPreparedStatement.setString(1, personNameStringSplit[1]);
                    personNamesListPreparedStatement.setString(2, personNameStringSplit[2]);
                    personNamesListPreparedStatement.executeUpdate();
                }
                else{
                    if(personNameStringSplit[3] != "."){
                        personNamesListPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement("insert into person_table (first_name, middle_name, last_name) values (?, ?, ?)");
                        personNamesListPreparedStatement.setString(1, personNameStringSplit[1]);
                        personNamesListPreparedStatement.setString(2, personNameStringSplit[2]);
                        personNamesListPreparedStatement.setString(3, personNameStringSplit[3]);
                        personNamesListPreparedStatement.executeUpdate();
                    }
                    else{
                        personNamesListPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement("insert into person_table (first_name, last_name, post_nominal) values (?, ?, ?)");
                        personNamesListPreparedStatement.setString(1, personNameStringSplit[1]);
                        personNamesListPreparedStatement.setString(2, personNameStringSplit[2]);
                        personNamesListPreparedStatement.setString(3, personNameStringSplit[3]);
                        personNamesListPreparedStatement.executeUpdate();
                    }
                }
            }
            this.swingLeftDatabaseConnection.commit();
            personNamesListPreparedStatement.close();
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            return false;
        }
        return true;
    }

    //add data to the races table.
    public boolean addDataToRaceTable (){
        try {
            PreparedStatement raceTablePreparedStatement;

            raceTablePreparedStatement = this.swingLeftDatabaseConnection.prepareStatement("SELECT * FROM district_table WHERE seat_type LIKE 'house'");
            ResultSet districtResultSet = raceTablePreparedStatement.executeQuery();

            raceTablePreparedStatement = this.swingLeftDatabaseConnection.prepareStatement("SELECT * FROM election_cycle_table WHERE cycle_year LIKE '2016'");
            ResultSet electionCycleResultSet = raceTablePreparedStatement.executeQuery();

            while(electionCycleResultSet.next()) {
                int election_id = electionCycleResultSet.getInt("election_cycle_id");
                while (districtResultSet.next()) {
                    int district_id = districtResultSet.getInt("district_id");
                    raceTablePreparedStatement = this.swingLeftDatabaseConnection.prepareStatement("INSERT INTO race_table (district_id, election_cycle_id) VALUES (?, ?)");
                    raceTablePreparedStatement.setInt(1, district_id);
                    raceTablePreparedStatement.setInt(2, election_id);
                    raceTablePreparedStatement.executeUpdate();
                }
            }
            this.swingLeftDatabaseConnection.commit();
            raceTablePreparedStatement.close();
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            return false;
        }
        return true;
    }

    public ResultSet getTableData(String tableName){

        ResultSet tableDataResultSet = null;
        try{
            PreparedStatement tableDataPreparedStatement;
            tableDataPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement("SELECT * FROM ?");
            tableDataPreparedStatement.setString(1, tableName);
            tableDataResultSet = tableDataPreparedStatement.executeQuery();
        }
        catch (SQLException sqle){
            System.out.println(sqle);
        }
        return tableDataResultSet;
    }

    public ResultSet getTableData(String tableName, String whereColumn, String whereValue){
        ResultSet tableDataResultSet = null;
        try{
            PreparedStatement tableDataPreparedStatement;
            tableDataPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement("SELECT * FROM ? WHERE ? LIKE ?");
            tableDataPreparedStatement.setString(1, tableName);
            tableDataPreparedStatement.setString(2, whereColumn);
            tableDataPreparedStatement.setString(3, whereValue);
            tableDataResultSet = tableDataPreparedStatement.executeQuery();
        }
        catch (SQLException sqle){
            System.out.println(sqle);
        }
        return tableDataResultSet;
    }

    public Connection addDataToDistrictTable(String db_connect_str, String db_userid, String db_password) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(db_connect_str, db_userid, db_password);
            conn.setAutoCommit(false);
            try (Stream<String> stream = Files.lines(Paths.get("data/altered/senateDistrictsMinnesota.txt"))) {
                Object[] stringArray = stream.toArray();
                preparedStatement = null;
                for (int i = 0; i < stringArray.length; i++) {
                    preparedStatement = conn.prepareStatement("insert into district_table(state, district_name, seat_type) values (?, ?, ?)");
                    preparedStatement.setString(1, "MN");
                    preparedStatement.setString(2, stringArray[i].toString());
                    preparedStatement.setString(3, "Senate");
                    preparedStatement.executeUpdate();
                    conn.commit();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            conn = null;
        } finally {
            try {
                preparedStatement.close();
                return conn;
            } catch (SQLException sqle) {
                System.out.println(sqle);
            }
        }
    return conn;
    }

    public Connection addDataToIssuesTable(String db_connect_str, String db_userid, String db_password) {
        Connection conn;
        PreparedStatement preparedStatement = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(db_connect_str, db_userid, db_password);
            conn.setAutoCommit(false);
            try (Stream<String> stream = Files.lines(Paths.get("data/original/issues.txt"))) {
                Object[] stringArray = stream.toArray();
                for (Object issue: stringArray) {
                    preparedStatement = conn.prepareStatement("INSERT INTO issues_table(issue_name) VALUES (?)");
                    preparedStatement.setString(1, issue.toString());
                    preparedStatement.executeUpdate();
                    conn.commit();
                }
            }
            preparedStatement.close();

        } catch (Exception e) {
            e.printStackTrace();
            conn = null;
        }

        return conn;
    }

    public Connection removeDataFromTable(String db_connect_str, String db_userid, String db_password, String tableName) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(db_connect_str, db_userid, db_password);
            conn.setAutoCommit(false);
            preparedStatement = conn.prepareStatement("DELETE FROM " + tableName);
            preparedStatement.executeUpdate();
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            conn = null;
        } finally {
            try {
                preparedStatement.close();
                return conn;
            } catch (SQLException sqle) {
                System.out.println(sqle);

            }
        }
        return conn;
    }

}
