package com.greg.log_parser;

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
                if (br != null) {
                    br.close();
                }
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

            for (DBImportData el : listForDBImport) {
                statement.setString(1, el.getId());
                statement.setInt(2, el.getDuration());
                statement.setString(3, el.getType());
                statement.setString(4, el.getHost());
                statement.setString(5, Boolean.toString(el.getAlert()));
                statement.addBatch();
            }
            statement.executeBatch();


            showDbContents();


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

    private static void showDbContents() throws SQLException {

        PreparedStatement pst = connection.prepareStatement("select * from logs");
        pst.clearParameters();
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            System.out.print(rs.getString(1));
            System.out.print(" | ");
            System.out.print(rs.getInt(2));
            System.out.print(" | ");
            System.out.print(rs.getString(3));
            System.out.print(" | ");
            System.out.print(rs.getString(4));
            System.out.print(" | ");
            System.out.println(rs.getString(5));
        }
    }

    // todo: find and discard unpaired entries
    public static void logValidation(List<LogData> logData) {
        for (LogData k : logData) {
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
