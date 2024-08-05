package project2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

/*
    The StudentFakebookOracle class is derived from the FakebookOracle class and implements
    the abstract query functions that investigate the database provided via the <connection>
    parameter of the constructor to discover specific information.
*/
public final class StudentFakebookOracle extends FakebookOracle {
    // [Constructor]
    // REQUIRES: <connection> is a valid JDBC connection
    public StudentFakebookOracle(Connection connection) {
        oracle = connection;
    }

    @Override
    // Query 0
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the total number of users for which a birth month is listed
    //        (B) Find the birth month in which the most users were born
    //        (C) Find the birth month in which the fewest users (at least one) were born
    //        (D) Find the IDs, first names, and last names of users born in the month
    //            identified in (B)
    //        (E) Find the IDs, first names, and last name of users born in the month
    //            identified in (C)
    //
    // This query is provided to you completed for reference. Below you will find the appropriate
    // mechanisms for opening up a statement, executing a query, walking through results, extracting
    // data, and more things that you will need to do for the remaining nine queries
    public BirthMonthInfo findMonthOfBirthInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            // Step 1
            // ------------
            // * Find the total number of users with birth month info
            // * Find the month in which the most users were born
            // * Find the month in which the fewest (but at least 1) users were born
            ResultSet rst = stmt.executeQuery(
                    "SELECT COUNT(*) AS Birthed, Month_of_Birth " + // select birth months and number of uses with that birth month
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth IS NOT NULL " + // for which a birth month is available
                            "GROUP BY Month_of_Birth " + // group into buckets by birth month
                            "ORDER BY Birthed DESC, Month_of_Birth ASC"); // sort by users born in that month, descending; break ties by birth month

            int mostMonth = 0;
            int leastMonth = 0;
            int total = 0;
            while (rst.next()) { // step through result rows/records one by one
                if (rst.isFirst()) { // if first record
                    mostMonth = rst.getInt(2); //   it is the month with the most
                }
                if (rst.isLast()) { // if last record
                    leastMonth = rst.getInt(2); //   it is the month with the least
                }
                total += rst.getInt(1); // get the first field's value as an integer
            }
            BirthMonthInfo info = new BirthMonthInfo(total, mostMonth, leastMonth);

            // Step 2
            // ------------
            // * Get the names of users born in the most popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + mostMonth + " " + // born in the most popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addMostPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 3
            // ------------
            // * Get the names of users born in the least popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + leastMonth + " " + // born in the least popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addLeastPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 4
            // ------------
            // * Close resources being used
            rst.close();
            stmt.close(); // if you close the statement first, the result set gets closed automatically

            return info;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new BirthMonthInfo(-1, -1, -1);
        }
    }

    @Override
    // Query 1
    // -----------------------------------------------------------------------------------
    // GOALS: (A) The first name(s) with the most letters
    //        (B) The first name(s) with the fewest letters
    //        (C) The first name held by the most users
    //        (D) The number of users whose first name is that identified in (C)
    public FirstNameInfo findNameInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*   
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                FirstNameInfo info = new FirstNameInfo();
                info.addLongName("Aristophanes");
                info.addLongName("Michelangelo");
                info.addLongName("Peisistratos");
                info.addShortName("Bob");
                info.addShortName("Sue");
                info.addCommonName("Harold");
                info.addCommonName("Jessica");
                info.setCommonNameCount(42);
                return info;
            */
            ResultSet rst = stmt.executeQuery(
                "SELECT LENGTH(First_Name) AS Longest, First_Name " + 
                        "FROM " + UsersTable + " " + 
                        "GROUP BY First_Name " +
                        "ORDER BY Longest DESC, First_Name ASC"
            );  
 
            FirstNameInfo info = new FirstNameInfo(); 
            String longest; 
            String shortest; 
            String common;  
            int longest_length = 0;
            int shortest_length = 0; 
            int count_common = 0; 

            while(rst.next()) {
                if (rst.isFirst()) { 
                    longest_length = rst.getInt(1);  
                    longest = rst.getString(2); 
                    info.addLongName(longest);
                } 
                else {
                    if (rst.getInt(1) == longest_length) {
                        longest = rst.getString(2); 
                        info.addLongName(longest); 
                    } 
                    else {
                        break; 
                    }
                }
            }
            
            rst = stmt.executeQuery(
                "SELECT LENGTH(First_Name) AS Shortest, First_Name " + 
                        "FROM " + UsersTable + " " + 
                        "GROUP BY First_Name " +
                        "ORDER BY Shortest, First_Name ASC"
            );
            
            while(rst.next()) {
                if (rst.isFirst()) { 
                    shortest_length = rst.getInt(1);  
                    shortest = rst.getString(2); 
                    info.addShortName(shortest);
                } 
                else {
                    if (rst.getInt(1) == shortest_length) {
                        shortest = rst.getString(2); 
                        info.addShortName(shortest); 
                    }
                    else {
                        break; 
                    }
                }
            }

            rst = stmt.executeQuery(
                "SELECT COUNT(*) AS Common, First_Name " + 
                        "FROM " + UsersTable + " " + 
                        "GROUP BY First_Name " +
                        "ORDER BY Common DESC, First_Name ASC"
            ); 

            while(rst.next()) {
                if (rst.isFirst()) { 
                    count_common = rst.getInt(1); 
                    info.setCommonNameCount(count_common); 
                    common = rst.getString(2); 
                    info.addCommonName(common); 
                } 
                else {
                    if (rst.getInt(1) == count_common) {
                        common = rst.getString(2); 
                        info.addCommonName(common); 
                    }
                    else {
                        break; 
                    }
                }
            }

            rst.close(); 
            stmt.close(); 

            return info; 
          //  return new FirstNameInfo(); // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new FirstNameInfo();
        }
    }

    @Override
    // Query 2
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users without any friends
    //
    // Be careful! Remember that if two users are friends, the Friends table only contains
    // the one entry (U1, U2) where U1 < U2.
    public FakebookArrayList<UserInfo> lonelyUsers() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(15, "Abraham", "Lincoln");
                UserInfo u2 = new UserInfo(39, "Margaret", "Thatcher");
                results.add(u1); 
                results.add(u2); 
            */
            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                        "FROM " + UsersTable + " " +
                        "WHERE User_ID NOT IN " + 
                        "(SELECT F1.User1_ID FROM " + FriendsTable + " F1) AND User_ID NOT IN " +
                        "(SELECT F2.User2_ID FROM " + FriendsTable + " F2) " +
                        "ORDER BY User_ID"
            );  


            while (rst.next()) {
                UserInfo u = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)); 
                results.add(u); 
            }

            rst.close(); 
            stmt.close(); 

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return results;
    }

    @Override
    // Query 3
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users who no longer live
    //            in their hometown (i.e. their current city and their hometown are different)
    public FakebookArrayList<UserInfo> liveAwayFromHome() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(9, "Meryl", "Streep");
                UserInfo u2 = new UserInfo(104, "Tom", "Hanks");
                results.add(u1);
                results.add(u2);
            */
            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT U.User_ID, U.First_Name, U.Last_Name " + // select ID, first name, and last name
                        "FROM " + UsersTable + " U, " + CurrentCitiesTable + " C, " + HometownCitiesTable + " H " +
                        "WHERE U.User_ID = C.User_ID AND U.User_ID = H.User_ID " + 
                        "AND C.Current_City_ID != H.Hometown_City_ID " + 
                        "ORDER BY U.User_ID"
            );  

            while (rst.next()) {
                UserInfo u = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)); 
                results.add(u); 
            }

            rst.close(); 
            stmt.close(); 

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 4
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, links, and IDs and names of the containing album of the top
    //            <num> photos with the most tagged users
    //        (B) For each photo identified in (A), find the IDs, first names, and last names
    //            of the users therein tagged
    public FakebookArrayList<TaggedPhotoInfo> findPhotosWithMostTags(int num) throws SQLException {
        FakebookArrayList<TaggedPhotoInfo> results = new FakebookArrayList<TaggedPhotoInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                PhotoInfo p = new PhotoInfo(80, 5, "www.photolink.net", "Winterfell S1");
                UserInfo u1 = new UserInfo(3901, "Jon", "Snow");
                UserInfo u2 = new UserInfo(3902, "Arya", "Stark");
                UserInfo u3 = new UserInfo(3903, "Sansa", "Stark");
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                tp.addTaggedUser(u1);
                tp.addTaggedUser(u2);
                tp.addTaggedUser(u3);
                results.add(tp);
      */   

            ResultSet rst = stmt.executeQuery(
                "SELECT PID, AID, Plink, AName " +
                        "FROM " +
                        "(SELECT T.Tag_Photo_ID AS PID, A.Album_ID AS AID, P.Photo_Link AS Plink, A.Album_Name AS AName " +
                        "FROM " + TagsTable + " T, " + PhotosTable + " P, " + AlbumsTable + " A " + 
                        "WHERE T.Tag_Photo_ID = P.Photo_ID AND P.Album_ID = A.Album_ID " + 
                        "GROUP BY T.Tag_Photo_ID, A.Album_ID, P.Photo_Link, A.Album_Name " + 
                        "ORDER BY count(*) DESC, PID ASC) "+
                        "WHERE ROWNUM <= " + num 
            ); 

            Statement st = oracle.createStatement(FakebookOracleConstants.AllScroll,
            FakebookOracleConstants.ReadOnly); 
   
            while (rst.next()) {
                // long photo_id = rst.getLong(1); 
                PhotoInfo p = new PhotoInfo(rst.getLong(1), rst.getLong(2), rst.getString(3), rst.getString(4)); 
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p); 

                ResultSet rs = st.executeQuery(
                    "SELECT U.User_ID, U.First_Name, U.Last_Name " +
                            "FROM " + TagsTable + " T, " + UsersTable + " U, " + PhotosTable + " P, " + AlbumsTable + " A " + 
                            "WHERE T.Tag_Photo_ID = " + rst.getLong(1) + " AND U.User_ID = T.Tag_Subject_ID AND P.Photo_ID = T.Tag_Photo_ID AND P.Album_ID = A.Album_ID " + 
                            "ORDER BY U.User_ID ASC"
                ); 

                while (rs.next()) {
                    UserInfo u = new UserInfo(rs.getLong(1), rs.getString(2), rs.getString(3)); 
                    tp.addTaggedUser(u);
                }

                results.add(tp); 
            } 

            st.close(); 
            rst.close(); 
            stmt.close(); 

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 5
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, last names, and birth years of each of the two
    //            users in the top <num> pairs of users that meet each of the following
    //            criteria:
    //              (i) same gender
    //              (ii) tagged in at least one common photo
    //              (iii) difference in birth years is no more than <yearDiff>
    //              (iv) not friends
    //        (B) For each pair identified in (A), find the IDs, links, and IDs and names of
    //            the containing album of each photo in which they are tagged together
    public FakebookArrayList<MatchPair> matchMaker(int num, int yearDiff) throws SQLException {
        FakebookArrayList<MatchPair> results = new FakebookArrayList<MatchPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(93103, "Romeo", "Montague");
                UserInfo u2 = new UserInfo(93113, "Juliet", "Capulet");
                MatchPair mp = new MatchPair(u1, 1597, u2, 1597);
                PhotoInfo p = new PhotoInfo(167, 309, "www.photolink.net", "Tragedy");
                mp.addSharedPhoto(p);
                results.add(mp);
            */

            ResultSet rst = stmt.executeQuery(
                "SELECT ID1, ID2, FN1, FN2, LN1, LN2, YOB1, YOB2 " + 
                        "FROM " + 
                        "(SELECT U1.User_ID AS ID1, U2.User_ID AS ID2, U1.First_Name AS FN1, U2.First_Name AS FN2, U1.Last_Name AS LN1, U2.Last_Name AS LN2, U1.Year_Of_Birth AS YOB1, U2.Year_Of_Birth AS YOB2 " +
                        "FROM " + UsersTable + " U1, " + UsersTable + " U2, " + TagsTable + " T1, " + TagsTable + " T2 "+
                        "WHERE U1.gender = U2.gender AND U1.User_ID < U2.User_ID AND ABS(U1.Year_Of_Birth - U2.Year_Of_Birth) <= " + yearDiff + " AND " +
                        "T1.Tag_Subject_ID = U1.User_ID AND T2.Tag_Subject_ID = U2.User_ID AND T1.Tag_Photo_Id = T2.Tag_Photo_Id AND NOT EXISTS " + 
                        "(SELECT * FROM " + FriendsTable + " F " + 
                        "WHERE F.User1_ID = U1.User_ID AND F.User2_ID = U2.User_ID) " + 
                        "GROUP BY U1.User_ID, U2.User_ID, U1.First_Name, U2.First_Name, U1.Last_Name, U2.Last_Name, U1.Year_Of_Birth, U2.Year_Of_Birth " + 
                        "ORDER BY COUNT(*) DESC, U1.User_ID, U2.User_ID) " + 
                        "WHERE ROWNUM <= " + num 
            ); 

            Statement st = oracle.createStatement(FakebookOracleConstants.AllScroll,
            FakebookOracleConstants.ReadOnly); 
   
            while (rst.next()) {
                UserInfo u1 = new UserInfo(rst.getLong(1), rst.getString(3), rst.getString(5)); 
                UserInfo u2 = new UserInfo(rst.getLong(2), rst.getString(4), rst.getString(6)); 
                MatchPair mp = new MatchPair(u1, rst.getLong(7), u2, rst.getLong(8)); 

                ResultSet rs = st.executeQuery( 
                    "SELECT P.Photo_ID, P.Photo_Link, A.Album_ID, A.Album_Name " +
                            "FROM " + PhotosTable + " P, " + AlbumsTable + " A, " + TagsTable + " T1, " + TagsTable + " T2 " +
                            "WHERE T1.Tag_Subject_ID = " + rst.getLong(1) + " AND T2.Tag_Subject_ID = " + rst.getLong(2) + " " + 
                            "AND T1.Tag_Photo_ID = T2.Tag_Photo_ID AND T1.Tag_Photo_ID = P.Photo_ID AND P.Album_ID = A.Album_ID " + 
                            "ORDER BY P.Photo_ID"
                ); 

                while (rs.next()) {
                    PhotoInfo p = new PhotoInfo(rs.getLong(1), rs.getLong(3), rs.getString(2), rs.getString(4)); 
                    mp.addSharedPhoto(p);
                }
                
                results.add(mp); 
            }
            
            st.close(); 
            rst.close(); 
            stmt.close(); 

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 6
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of each of the two users in
    //            the top <num> pairs of users who are not friends but have a lot of
    //            common friends
    //        (B) For each pair identified in (A), find the IDs, first names, and last names
    //            of all the two users' common friends
    public FakebookArrayList<UsersPair> suggestFriends(int num) throws SQLException {
        FakebookArrayList<UsersPair> results = new FakebookArrayList<UsersPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {

            stmt.executeUpdate(
                "CREATE VIEW F AS " +
                "SELECT * FROM " + FriendsTable + " UNION SELECT USER2_ID, USER1_ID FROM " + FriendsTable
            );
                    
            ResultSet rst = stmt.executeQuery(
                    "SELECT U1ID,U2ID, U1F, U2F, U1L,U2L, U1B, U2B " +
                    "FROM " +
                        "(SELECT DISTINCT U1.USER_ID AS U1ID, U2.USER_ID AS U2ID, U1.FIRST_NAME AS U1F, U2.FIRST_NAME AS U2F, U1.LAST_NAME AS U1L, U2.LAST_NAME AS U2L, U1.YEAR_OF_BIRTH AS U1B, U2.YEAR_OF_BIRTH AS U2B " + 
                        "FROM " + UsersTable + " " + "U1" + " "+ 
                        "JOIN " +UsersTable + " " + "U2" + " " +
                        "ON U1.User_ID < U2.USER_ID " + 
                        "JOIN F " + 
                        "ON U1.USER_ID = F.USER1_ID " +
                        "WHERE F.USER2_ID IN ( SELECT F1.USER2_ID FROM F F1" +" WHERE F1.USER1_ID = U2.USER_ID)"+ 
                        " AND NOT EXISTS " + 
                            "(SELECT * FROM " + FriendsTable + " F2 " + "WHERE F2.User1_ID = U1.User_ID AND F2.User2_ID = U2.User_ID) " + 
                        "GROUP BY U1.USER_ID, U2.USER_ID, U1.FIRST_NAME, U2.FIRST_NAME, U1.LAST_NAME, U2.LAST_NAME, U1.YEAR_OF_BIRTH, U2.YEAR_OF_BIRTH " + 
                        "ORDER BY COUNT(DISTINCT F.USER2_ID) DESC, U1.USER_ID, U2.USER_ID)" + " " +
                    "WHERE ROWNUM <= " + num); 

            Statement st = oracle.createStatement(FakebookOracleConstants.AllScroll,FakebookOracleConstants.ReadOnly); 

            while (rst.next()) { 
                UserInfo u1 = new UserInfo(rst.getInt(1), rst.getString(3), rst.getString(5));
                UserInfo u2 = new UserInfo(rst.getInt(2), rst.getString(4), rst.getString(6));
                UsersPair up = new UsersPair(u1, u2);
                ResultSet rst1 = st.executeQuery(
                    "SELECT F.USER2_ID AS U2ID, U.FIRST_NAME AS FN, U.LAST_NAME AS LN FROM F " +
                    "JOIN " + UsersTable + " U " +
                    "ON U.USER_ID = F.USER2_ID " + 
                    "WHERE F.USER1_ID = " + rst.getInt(1) + " AND F.USER2_ID IN ( SELECT F1.USER2_ID FROM F F1 " +" WHERE F1.USER1_ID = " + rst.getInt(2) + " )" +
                    " ORDER BY F.USER2_ID" 
                    ); 
                    while (rst1.next()) { 
                        UserInfo u3 = new UserInfo(rst1.getInt(1), rst1.getString(2), rst1.getString(3));
                        up.addSharedFriend(u3);
                    }
                results.add(up);
            }
            stmt.executeUpdate("DROP VIEW F"); 
            rst.close();
            stmt.close(); 
            st.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }


    @Override
    // Query 7
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the name of the state or states in which the most events are held
    //        (B) Find the number of events held in the states identified in (A)
    public EventStateInfo findEventStates() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                EventStateInfo info = new EventStateInfo(50);
                info.addState("Kentucky");
                info.addState("Hawaii");
                info.addState("New Hampshire");
                return info;
            */
        
            // stmt.executeUpdate("DROP VIEW view_name"); 
            
            ResultSet rst = stmt.executeQuery(
                "SELECT C.State_Name, COUNT(*) " +
                "FROM " + CitiesTable + " C, " + EventsTable + " E " + 
                "WHERE E.Event_City_ID = C.City_ID " + 
                "GROUP BY C.State_Name " + 
                "HAVING COUNT(*) = " + 
                    "(SELECT MAX(COUNT(*)) " + 
                    "FROM " + CitiesTable + " C1, " + EventsTable + " E1 " + 
                    "WHERE E1.Event_City_ID = C1.City_ID " + 
                    "GROUP BY C1.State_Name)" 
            );

        
            int count = 0; 
            String state = ""; 
            while(rst.next()) {
                count = rst.getInt(2); 
                state = rst.getString(1); 
                break; 
            }

            EventStateInfo info = new EventStateInfo(count);
            info.addState(state); 

            //rst.beforeFirst();
            while(rst.next()) {
                info.addState(rst.getString(1)); 
            } 

        //    stmt.executeUpdate("DROP VIEW most_events");
            rst.close(); 
            stmt.close(); 

            return info;
        //    return new EventStateInfo(-1); // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new EventStateInfo(-1);
        }
    }

    @Override
    // Query 8
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the ID, first name, and last name of the oldest friend of the user
    //            with User ID <userID>
    //        (B) Find the ID, first name, and last name of the youngest friend of the user
    //            with User ID <userID>
    public AgeInfo findAgeInfo(long userID) throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo old = new UserInfo(12000000, "Galileo", "Galilei");
                UserInfo young = new UserInfo(80000000, "Neil", "deGrasse Tyson");
                return new AgeInfo(old, young);

            */

            // stmt.executeUpdate("CREATE VIEW view_name AS ..."); 
            // stmt.executeUpdate("DROP VIEW view_name"); 

            stmt.executeQuery(
                "CREATE VIEW V_Friends AS " + 
                    "SELECT F1.User2_ID AS User_ID " +
                    "FROM " + FriendsTable + " F1 " + 
                    "WHERE F1.User1_ID = " + userID + " " +
                    "UNION " + 
                    "SELECT F2.User1_ID AS User_ID " + 
                    "FROM " + FriendsTable + " F2 " + 
                    "WHERE F2.User2_ID = " + userID + " "
            ); 
            
            ResultSet rst = stmt.executeQuery(
                "SELECT * " +
                    "FROM " + 
                        "(SELECT U.User_ID, U.First_Name, U.Last_Name " + 
                        "FROM V_Friends V, " + UsersTable + " U " +
                        "WHERE V.User_ID = U.User_ID " + 
                        "ORDER BY U.year_of_birth ASC, U.month_of_birth ASC, U.day_of_birth ASC, U.User_ID DESC) " +
                    "WHERE ROWNUM = 1"
            );

            long id_oldest = -1 ; 
            long id_youngest = -1; 
            String FN_oldest = ""; 
            String FN_youngest = ""; 
            String LN_oldest = "";
            String LN_youngest = ""; 

            while(rst.next()) {
                
                id_oldest = rst.getLong(1); 
                FN_oldest = rst.getString(2); 
                LN_oldest = rst.getString(3); 
            } 

            UserInfo old = new UserInfo(id_oldest, FN_oldest, LN_oldest);    

            rst = stmt.executeQuery(
                "SELECT * " +
                    "FROM " + 
                        "(SELECT U.User_ID, U.First_Name, U.Last_Name " + 
                        "FROM V_Friends V, " + UsersTable + " U " +
                        "WHERE V.User_ID = U.User_ID " + 
                        "ORDER BY U.year_of_birth DESC, U.month_of_birth DESC, U.day_of_birth DESC, U.User_ID DESC) " +
                    "WHERE ROWNUM = 1"
            ); 

            while(rst.next()) {
                id_youngest = rst.getLong(1); 
                FN_youngest = rst.getString(2); 
                LN_youngest = rst.getString(3); 
            }; 

            UserInfo young = new UserInfo(id_youngest, FN_youngest, LN_youngest); 

            stmt.executeUpdate("DROP VIEW V_Friends"); 

            rst.close(); 
            stmt.close(); 

            return new AgeInfo(old, young);
            // return new AgeInfo(new UserInfo(-1, "UNWRITTEN", "UNWRITTEN"), new UserInfo(-1, "UNWRITTEN", "UNWRITTEN")); // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new AgeInfo(new UserInfo(-1, "ERROR", "ERROR"), new UserInfo(-1, "ERROR", "ERROR"));
        }
    }

    @Override
    // Query 9
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find all pairs of users that meet each of the following criteria
    //              (i) same last name
    //              (ii) same hometown
    //              (iii) are friends
    //              (iv) less than 10 birth years apart
    public FakebookArrayList<SiblingInfo> findPotentialSiblings() throws SQLException {
        FakebookArrayList<SiblingInfo> results = new FakebookArrayList<SiblingInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(81023, "Kim", "Kardashian");
                UserInfo u2 = new UserInfo(17231, "Kourtney", "Kardashian");
                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);
            */
            ResultSet rst = stmt.executeQuery(
                "SELECT U1.User_ID, U2.User_ID, U1.First_Name, U2.First_Name, U1.Last_Name, U2.Last_Name " +
                    "FROM " + UsersTable + " U1, " + UsersTable + " U2, " + HometownCitiesTable + " HC1, " + HometownCitiesTable + " HC2, " + FriendsTable + " F " + 
                    "WHERE U1.User_ID < U2.User_ID AND U1.User_ID = HC1.User_ID AND U2.User_ID = HC2.User_ID AND U1.Last_Name = U2.Last_Name AND HC1.Hometown_City_ID = HC2.Hometown_City_ID " + 
                    "AND ABS(U1.Year_Of_Birth - U2.Year_Of_Birth) < 10 " + 
                    "AND F.User1_ID = U1.User_ID AND F.User2_ID = U2.User_ID " + 
                    "ORDER BY U1.User_ID ASC, U2.User_ID ASC" 
            ); 

            while(rst.next()) {
                UserInfo s1 = new UserInfo(rst.getLong(1), rst.getString(3), rst.getString(5));
                UserInfo s2 = new UserInfo(rst.getLong(2), rst.getString(4), rst.getString(6));
                SiblingInfo s = new SiblingInfo(s1, s2);
                results.add(s);
            }

            rst.close(); 
            stmt.close(); 

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    // Member Variables
    private Connection oracle;
    private final String UsersTable = FakebookOracleConstants.UsersTable;
    private final String CitiesTable = FakebookOracleConstants.CitiesTable;
    private final String FriendsTable = FakebookOracleConstants.FriendsTable;
    private final String CurrentCitiesTable = FakebookOracleConstants.CurrentCitiesTable;
    private final String HometownCitiesTable = FakebookOracleConstants.HometownCitiesTable;
    private final String ProgramsTable = FakebookOracleConstants.ProgramsTable;
    private final String EducationTable = FakebookOracleConstants.EducationTable;
    private final String EventsTable = FakebookOracleConstants.EventsTable;
    private final String AlbumsTable = FakebookOracleConstants.AlbumsTable;
    private final String PhotosTable = FakebookOracleConstants.PhotosTable;
    private final String TagsTable = FakebookOracleConstants.TagsTable;
}
