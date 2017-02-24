package com.dataparser;
import com.sun.rowset.internal.Row;

import javax.xml.transform.Result;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.Date;
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

    public boolean addLowerHouseDistricts() {
        PreparedStatement lowerHousePreparedStatment = null;
        int counter = 0;
        try (Stream<String> stream = Files.lines(Paths.get("data/district_info/lower_house_info.txt"))) {
            Object[] stringArray = stream.toArray();
            lowerHousePreparedStatment = null;
            for (Object lowerHouseObject : stringArray) {
                String lowerHouseString = lowerHouseObject.toString();
                String[] lowerHouseStringArray = lowerHouseString.split(",");
                if(!lowerHouseStringArray[0].equals("MN")) {
                    String prePreparedStatementString = "insert into district_table(state, district_name, district_type, longitude, latitude) values (?, ?, ?, ?, ?)";
                    lowerHousePreparedStatment = this.swingLeftDatabaseConnection.prepareStatement(prePreparedStatementString);
                    lowerHousePreparedStatment.setString(1, lowerHouseStringArray[0]);
                    lowerHousePreparedStatment.setString(2, lowerHouseStringArray[1]);
                    lowerHousePreparedStatment.setString(3, lowerHouseStringArray[2]);
                    lowerHousePreparedStatment.setDouble(4, Double.parseDouble(lowerHouseStringArray[3].trim()));
                    lowerHousePreparedStatment.setDouble(5, Double.parseDouble(lowerHouseStringArray[4].trim()));
                    lowerHousePreparedStatment.executeUpdate();
                    counter++;
                }
                else{
                    String prePreparedStatementString = "UPDATE district_table SET longitude = ?, latitude = ? WHERE district_name = ?";
                    lowerHousePreparedStatment = this.swingLeftDatabaseConnection.prepareCall(prePreparedStatementString);
                    lowerHousePreparedStatment.setDouble(1, Double.parseDouble(lowerHouseStringArray[3].trim()));
                    lowerHousePreparedStatment.setDouble(2, Double.parseDouble(lowerHouseStringArray[4].trim()));
                    lowerHousePreparedStatment.setString(3, lowerHouseStringArray[1]);
                    lowerHousePreparedStatment.executeUpdate();
                    counter++;
                }
            }
            this.swingLeftDatabaseConnection.commit();
            lowerHousePreparedStatment.close();
        } catch (Exception e) {
            System.out.println("Around line: " + counter);
            System.out.println(e);
            return false;
        }
        return true;
    }

    public boolean updateMinnesotaLocations() {
        PreparedStatement upperHousePreparedStatment = null;
        int counter = 0;
        try (Stream<String> stream = Files.lines(Paths.get("data/district_info/upper_house_info.txt"))) {
            Object[] stringArray = stream.toArray();
            upperHousePreparedStatment = null;
            for (Object upperHouseObject : stringArray) {
                String upperHouseString = upperHouseObject.toString();
                String[] upperHouseStringArray = upperHouseString.split(",");
                if (!upperHouseStringArray[0].equals("MN")) {
                    counter++;
                    String prePreparedStatementString = "insert into district_table(state, district_name, district_type, longitude, latitude) values (?, ?, ?, ?, ?)";
                    upperHousePreparedStatment = this.swingLeftDatabaseConnection.prepareStatement(prePreparedStatementString);
                    upperHousePreparedStatment.setString(1, upperHouseStringArray[0]);
                    upperHousePreparedStatment.setString(2, upperHouseStringArray[1]);
                    upperHousePreparedStatment.setString(3, upperHouseStringArray[2]);
                    upperHousePreparedStatment.setDouble(4, Double.parseDouble(upperHouseStringArray[3].trim()));
                    upperHousePreparedStatment.setDouble(5, Double.parseDouble(upperHouseStringArray[4].trim()));
                    upperHousePreparedStatment.executeUpdate();
                } else {
                    counter++;
                    String prePreparedStatementString = "UPDATE district_table SET longitude = ?, latitude = ? WHERE district_name = ?";
                    upperHousePreparedStatment = this.swingLeftDatabaseConnection.prepareCall(prePreparedStatementString);
                    upperHousePreparedStatment.setDouble(1, Double.parseDouble(upperHouseStringArray[3].trim()));
                    upperHousePreparedStatment.setDouble(2, Double.parseDouble(upperHouseStringArray[4].trim()));
                    upperHousePreparedStatment.setString(3, upperHouseStringArray[1]);
                    upperHousePreparedStatment.executeUpdate();
                }
            }
            this.swingLeftDatabaseConnection.commit();
            upperHousePreparedStatment.close();
        } catch (Exception e) {
            System.out.println("Fault on line " + counter + ".");
            System.out.println(e);
            return false;
        }
        return true;
    }


    public boolean addUpperHouseDistricts() {
        PreparedStatement upperHousePreparedStatment = null;
        int counter = 0;
        try (Stream<String> stream = Files.lines(Paths.get("data/district_info/upper_house_info.txt"))) {
            Object[] stringArray = stream.toArray();
            upperHousePreparedStatment = null;
            for (Object upperHouseObject : stringArray) {
                String upperHouseString = upperHouseObject.toString();
                String[] upperHouseStringArray = upperHouseString.split(",");
                if (!upperHouseStringArray[0].equals("MN")) {
                    counter++;
                    String prePreparedStatementString = "insert into district_table(state, district_name, district_type, longitude, latitude) values (?, ?, ?, ?, ?)";
                    upperHousePreparedStatment = this.swingLeftDatabaseConnection.prepareStatement(prePreparedStatementString);
                    upperHousePreparedStatment.setString(1, upperHouseStringArray[0]);
                    upperHousePreparedStatment.setString(2, upperHouseStringArray[1]);
                    upperHousePreparedStatment.setString(3, upperHouseStringArray[2]);
                    upperHousePreparedStatment.setDouble(4, Double.parseDouble(upperHouseStringArray[3].trim()));
                    upperHousePreparedStatment.setDouble(5, Double.parseDouble(upperHouseStringArray[4].trim()));
                    upperHousePreparedStatment.executeUpdate();
                } else {
                    counter++;
                    String prePreparedStatementString = "UPDATE district_table SET longitude = ?, latitude = ? WHERE district_name = ?";
                    upperHousePreparedStatment = this.swingLeftDatabaseConnection.prepareCall(prePreparedStatementString);
                    upperHousePreparedStatment.setDouble(1, Double.parseDouble(upperHouseStringArray[3].trim()));
                    upperHousePreparedStatment.setDouble(2, Double.parseDouble(upperHouseStringArray[4].trim()));
                    upperHousePreparedStatment.setString(3, upperHouseStringArray[1]);
                    upperHousePreparedStatment.executeUpdate();
                }
            }
            this.swingLeftDatabaseConnection.commit();
            upperHousePreparedStatment.close();
        } catch (Exception e) {
            System.out.println("Fault on line " + counter + ".");
            System.out.println(e);
            return false;
        }
        return true;
    }

    public boolean addZipCodeInfo() {
        PreparedStatement zipCodePreparedStatment = null;
        try (Stream<String> stream = Files.lines(Paths.get("data/zip_codes/zip_codes_long_lat.txt"))) {
            Object[] stringArray = stream.toArray();
            zipCodePreparedStatment = null;
            for (Object zipCodeObject : stringArray) {
                String zipCodeString = zipCodeObject.toString();
                String[] zipCodeStringArray = zipCodeString.split(",");
                String prePreparedStatementString = "insert into zip_code_table(zip_code, longitude, latitude) values (?, ?, ?)";
                zipCodePreparedStatment = this.swingLeftDatabaseConnection.prepareStatement(prePreparedStatementString);
                zipCodePreparedStatment.setString(1, zipCodeStringArray[0]);
                zipCodePreparedStatment.setDouble(2, Double.parseDouble(zipCodeStringArray[1].trim()));
                zipCodePreparedStatment.setDouble(3, Double.parseDouble(zipCodeStringArray[2].trim()));
                zipCodePreparedStatment.executeUpdate();
            }
            this.swingLeftDatabaseConnection.commit();
            zipCodePreparedStatment.close();
        } catch (Exception e) {
            System.out.println(e);
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


    public boolean addDataToCandidateTable(List<Candidate> candidateList){
            try {
                PreparedStatement candidateTablePreparedStatment = null;

                ResultSet districtTableResultSet = getTableData("district_table");
                ResultSet partyTableResultSet = getTableData("political_party_table");
                ResultSet personTableResultSet = getTableData("person_table");
                //ResultSet electionCycleTableResultsSet = getTableData("election_cycle_table", "cycle_year", 2016);

                HashMap districtTableHashMap = new HashMap();
                HashMap partyTableHashMap = new HashMap();
                HashMap personTableHashMap = new HashMap();
                //HashMap electionCycleTableHashMap = new HashMap();

                while(districtTableResultSet.next()){
                    districtTableHashMap.put(districtTableResultSet.getString("district_name").trim(), districtTableResultSet.getInt("district_id"));
                }
                while(partyTableResultSet.next()){
                    partyTableHashMap.put(partyTableResultSet.getString("party_name").trim(), partyTableResultSet.getInt("party_id"));
                }
                while(personTableResultSet.next()){
                    String[] nameArray =  {personTableResultSet.getString("first_name"), personTableResultSet.getString("middle_name"), personTableResultSet.getString("last_name"), personTableResultSet.getString("post_nominal")};
                    String nameString = "";
                    for (String namePiece: nameArray){
                        if(namePiece !=null){
                            nameString += namePiece + " ";
                        }
                    }
                    nameString.trim();
                    personTableHashMap.put(nameString.trim(), personTableResultSet.getInt("person_id"));
                }
                for(Candidate candidate: candidateList){
                    System.out.println(candidate.getCandidateNameString());
                    candidateTablePreparedStatment = this.swingLeftDatabaseConnection.prepareStatement("INSERT INTO candidate_table (party_id, result_percentage, result_votes, result, person_id, election_cycle_id, district_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    candidateTablePreparedStatment.setInt(1, (Integer) partyTableHashMap.get(candidate.getCandidatePartyString()));
                    candidateTablePreparedStatment.setDouble(2, candidate.getCandidatePercentageDouble());
                    candidateTablePreparedStatment.setInt(3, candidate.getCandidateVotesString());
                    candidateTablePreparedStatment.setString(4, candidate.getCandidateResultString());
                    candidateTablePreparedStatment.setInt(5, (Integer) personTableHashMap.get(candidate.getCandidateNameString()));
                    candidateTablePreparedStatment.setInt(6, 1);
                    candidateTablePreparedStatment.setInt(7, (Integer) districtTableHashMap.get(candidate.getCandidateDistrictString()));
                    candidateTablePreparedStatment.executeUpdate();
                }
                this.swingLeftDatabaseConnection.commit();
                candidateTablePreparedStatment.close();

            }
            catch (SQLException sqle){
                System.out.println(sqle);
                return false;
            }
            return true;
    }

    public boolean addDataToLegislatureSeatsTable(){
        List<String> selectList = new ArrayList<String>(Arrays.asList("person_id", "district_id", "party_id"));
        ResultSet electionResultsResultSet = getTableData(selectList,"candidate_table", "result", "won");
        try{
            PreparedStatement addDataToLegislatureSeatsTablePreparedStatement = null;
            String preStatementString = "INSERT INTO legislature_seats_table (person_id, district_id, elected_date, in_office, party_id) VALUES (?, ?, '2016-11-08', 'Yes', ?)";
            java.sql.Date electionDate = new java.sql.Date(2016);
            //System.out.println(electionDate.toString());
            while(electionResultsResultSet.next()){
                addDataToLegislatureSeatsTablePreparedStatement = this.swingLeftDatabaseConnection.prepareStatement(preStatementString);
                addDataToLegislatureSeatsTablePreparedStatement.setInt(1, electionResultsResultSet.getInt("person_id"));
                addDataToLegislatureSeatsTablePreparedStatement.setInt(2, electionResultsResultSet.getInt("district_id"));
                addDataToLegislatureSeatsTablePreparedStatement.setInt(3, electionResultsResultSet.getInt("party_id"));
                addDataToLegislatureSeatsTablePreparedStatement.executeUpdate();
            }
            this.swingLeftDatabaseConnection.commit();
            addDataToLegislatureSeatsTablePreparedStatement.close();
        }
        catch (SQLException sqle){
            System.out.println(sqle);
            return false;
        }
        return true;
    }

    public boolean outputTest(){
        ResultSet tableDataResultSet = null;
        String tableName = "candidate_table";
        String column = "result";
        String condition = "Won";
        String selection = "*";
        try{
            PreparedStatement tableDataPreparedStatement;
            String preStatement = "SELECT " + selection + " FROM " + tableName +" WHERE " + column + " LIKE ?";
            tableDataPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement(preStatement);
            tableDataPreparedStatement.setString(1, condition);
            tableDataResultSet = tableDataPreparedStatement.executeQuery();
            while(tableDataResultSet.next()){
                System.out.println("Id: " + tableDataResultSet.getInt("candidate_id"));
                System.out.println("Result: " + tableDataResultSet.getString("result"));
            }
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

    public ResultSet getTableData(String tableName, String whereColumn, String whereValue){
        ResultSet tableDataResultSet = null;
        try{
            String sqlStatementString = "SELECT * FROM " + tableName + " WHERE " + whereColumn +" LIKE ?";
            PreparedStatement tableDataPreparedStatement;
            tableDataPreparedStatement = this.swingLeftDatabaseConnection.prepareStatement(sqlStatementString);
            tableDataPreparedStatement.setString(1, whereValue);
            tableDataResultSet = tableDataPreparedStatement.executeQuery();
            while(tableDataResultSet.next()){
                System.out.println(tableDataResultSet.getString("district_name"));
            }
        }
        catch (SQLException sqle){
            System.out.println(sqle);
        }
        return tableDataResultSet;
    }

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
