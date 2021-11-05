package com.greg.interview_test;

import com.google.gson.Gson;

import java.io.*;
import java.sql.*;
import java.util.*;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class LogParser {

    static Connection connection;
    static String connectionString = "jdbc:hsqldb:file:logdatabase";

    public static void main(String[] args) throws FileNotFoundException {

        if (args.length != 1)
            throw new FileNotFoundException("Missing or incorrect number of source files!");

        List<String> rawDataFromFile = new ArrayList<>();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(args[0]));

            String line;
            while ((line = br.readLine()) != null) {
                rawDataFromFile.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<LogData> listOfJsonObjects = new ArrayList<>();

        Gson gson = new Gson();
        for (int i = 0; i < rawDataFromFile.size(); i++)
            listOfJsonObjects.add(gson.fromJson(rawDataFromFile.get(i), LogData.class));

        logValidation(listOfJsonObjects);

        sorting(listOfJsonObjects);

        List<DBImportData> listForDBImport = normalizeForDBImport(listOfJsonObjects);


        System.out.println(listOfJsonObjects);


        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(connectionString, "SA", "");

            connection.createStatement().executeUpdate("CREATE TABLE if not EXISTS logs (ID varchar(50), duration int, type varchar(50), host varchar(50), alert varchar(20));");

            final String sql = "INSERT INTO logs (ID, duration, type, host, alert) Values(?, ?, ? ,? ,?)";

            PreparedStatement statement = connection.prepareStatement(sql);

            for (DBImportData finalLog : listForDBImport) {
                statement.setString(1, finalLog.getId());
                statement.setInt(2, finalLog.getDuration());
                statement.setString(3, finalLog.getType());
                statement.setString(4, finalLog.getHost());
                statement.setString(5, Boolean.toString(finalLog.getAlert()));
                statement.addBatch();
            }
            statement.executeBatch();


//            PreparedStatement pst = connection.prepareStatement("select * from logs");
//            pst.clearParameters();
//            ResultSet rs = pst.executeQuery();
//
//            while (rs.next()) {
//                System.out.print(rs.getString(1));
//                System.out.print(" | ");
//                System.out.print(rs.getInt(2));
//                System.out.print(" | ");
//                System.out.print(rs.getString(3));
//                System.out.print(" | ");
//                System.out.print(rs.getString(4));
//                System.out.print(" | ");
//                System.out.println(rs.getString(5));
//            }


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // todo: find and discard unpaired entries
    private static void logValidation(List<LogData> keepers) {
        for (LogData k : keepers) {
            if (k.getId().isEmpty() || k.getState().isEmpty() || k.getTimestamp() == 0) {
                throw new RuntimeException("The logfile is corrupted.");
            }
        }
    }

    public static List<DBImportData> normalizeForDBImport(List<LogData> keepers) {

        List<DBImportData> normalizedLog = new ArrayList<>();

        for (int i = 0; i < keepers.size() - 1; i += 2) {

            DBImportData finalLog = new DBImportData();

            finalLog.setId(keepers.get(i).getId());
            finalLog.setDuration((int) (keepers.get(i + 1).getTimestamp() - keepers.get(i).getTimestamp()));
            finalLog.setType(keepers.get(i).getType());
            finalLog.setHost(keepers.get(i).getHost());

            if ((finalLog.getDuration() > 4)) {
                finalLog.setAlert(TRUE);
            } else {
                finalLog.setAlert(FALSE);
            }

            normalizedLog.add(finalLog);
        }
        return normalizedLog;
    }

    private static void sorting(List<LogData> keepers) {
        Collections.sort(keepers, new SortComparator());
    }
}

