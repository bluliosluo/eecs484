import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONArray;

public class GetData {

    static String prefix = "project3.";

    // You must use the following variable as the JDBC connection
    Connection oracleConnection = null;

    // You must refer to the following variables for the corresponding 
    // tables in your database
    String userTableName = null;
    String friendsTableName = null;
    String cityTableName = null;
    String currentCityTableName = null;
    String hometownCityTableName = null;

    // DO NOT modify this constructor
    public GetData(String u, Connection c) {
        super();
        String dataType = u;
        oracleConnection = c;
        userTableName = prefix + dataType + "_USERS";
        friendsTableName = prefix + dataType + "_FRIENDS";
        cityTableName = prefix + dataType + "_CITIES";
        currentCityTableName = prefix + dataType + "_USER_CURRENT_CITIES";
        hometownCityTableName = prefix + dataType + "_USER_HOMETOWN_CITIES";
    }

    // TODO: Implement this function
    @SuppressWarnings("unchecked")
    public JSONArray toJSON() throws SQLException {

        // This is the data structure to store all users' information
        JSONArray users_info = new JSONArray();
        
        try (Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rst = stmt.executeQuery(
                    "SELECT * " + // select birth months and number of uses with that birth month
                    "FROM " + userTableName); // sort by users born in that month, descending; break ties by birth month

            int user_id = -1;
            String first_name = " " ;
            String last_name = " ";
            String gender = " ";
            int YOB =0;
            int MOB =0;
            int DOB =0;
            Statement st = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            while (rst.next()) { // step through result rows/records one by one
                user_id = rst.getInt(1); //   it is the month with the most
                first_name = rst.getString("FIRST_NAME");
                last_name = rst.getString("LAST_NAME");
                YOB = rst.getInt(4);
                MOB = rst.getInt(5);
                DOB = rst.getInt(6);
                gender = rst.getString(7);

                ResultSet rst1 = st.executeQuery(
                    "SELECT USER2_ID " + // select birth months and number of uses with that birth month
                    "FROM " + friendsTableName +
                    " WHERE USER1_ID = " + user_id); 
                JSONArray friends = new JSONArray();
                while (rst1.next()) { 
                    friends.put(rst1.getInt(1));
                    
                }

                JSONObject current = new JSONObject();
                rst1 = st.executeQuery(
                    "SELECT C.COUNTRY_NAME, C.CITY_NAME, C.STATE_NAME " + // select birth months and number of uses with that birth month
                    "FROM " + currentCityTableName + " U " + 
                    "JOIN " + cityTableName + " C " + 
                    "ON U.CURRENT_CITY_ID = C.CITY_ID " +
                    "WHERE U.USER_ID = " + user_id); 
                    while (rst1.next()) { 
                        current.put("country", rst1.getString(1));
                        current.put("city", rst1.getString(2));
                        current.put("state", rst1.getString(3));
                    }

                JSONObject hometown = new JSONObject();
                rst1 = st.executeQuery(
                    "SELECT C.COUNTRY_NAME, C.CITY_NAME, C.STATE_NAME " + // select birth months and number of uses with that birth month
                    "FROM " + hometownCityTableName + " U " + 
                    "JOIN " + cityTableName + " C " + 
                    "ON U.HOMETOWN_CITY_ID = C.CITY_ID " +
                    "WHERE U.USER_ID = " + user_id); 
                    while (rst1.next()) { 
                        hometown.put("country", rst1.getString(1));
                        hometown.put("city", rst1.getString(2));
                        hometown.put("state", rst1.getString(3));
                    }
                JSONObject user = new JSONObject();

                user.put("user_id", user_id);
                user.put("first_name",first_name);
                user.put("last_name", last_name);
                user.put("gender", gender);
                user.put("YOB", YOB);
                user.put("MOB", MOB);
                user.put("DOB", DOB);
                user.put("friends",friends);
                user.put("current", current);
                user.put("hometown", hometown);
                users_info.put(user);
            }
            
            
            rst.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return users_info;
    }

    // This outputs to a file "output.json"
    // DO NOT MODIFY this function
    public void writeJSON(JSONArray users_info) {
        try {
            FileWriter file = new FileWriter(System.getProperty("user.dir") + "/output.json");
            file.write(users_info.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
