package com.greg.interview_test;

import com.google.gson.Gson;

import java.io.*;
import java.sql.*;
import java.util.*;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class LogParserApp {

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

    public static List<DBImportData> normalizeForDBImport(List<LogData> logData) {

        List<DBImportData> listForDBImport = new ArrayList<>();

        for (int i = 0; i < logData.size() - 1; i += 2) {

            DBImportData parsedLog = new DBImportData();

            parsedLog.setId(logData.get(i).getId());
            parsedLog.setDuration((int) (logData.get(i + 1).getTimestamp() - logData.get(i).getTimestamp()));
            parsedLog.setType(logData.get(i).getType());
            parsedLog.setHost(logData.get(i).getHost());

            if ((parsedLog.getDuration() > 4)) {
                parsedLog.setAlert(TRUE);
            } else {
                parsedLog.setAlert(FALSE);
            }

            listForDBImport.add(parsedLog);
        }
        return listForDBImport;
    }

    private static void sorting(List<LogData> logData) {
        Collections.sort(logData, new SortComparator());
    }
}
