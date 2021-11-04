package com.greg.interview_test;

import com.google.gson.Gson;

import java.io.*;
import java.sql.*;
import java.util.*;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class LogParser {

    static Connection con;
    static String connectionString = "jdbc:hsqldb:file:logdatabase";

    public static void main(String[] args) throws FileNotFoundException {

        List<String> result = new ArrayList<>();
        List<Keeper> keepers = new ArrayList<>();
        BufferedReader br = null;


        //   if (args.length!=1)
        //     throw new FileNotFoundException("Missing or incorrect number of source files!");

        //todo close with resources
        try {
            br = new BufferedReader(new FileReader("C:\\logfile.txt"));
            //   br = new BufferedReader(new FileReader(args[0]));

            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        for (int i = 0; i < result.size(); i++)
            keepers.add(gson.fromJson(result.get(i), Keeper.class));

        logValidation(keepers);

        sorting(keepers);

        List<FinalLog> normalizedlog = listForDBImport(keepers);


        System.out.println(keepers);


        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            con = DriverManager.getConnection(connectionString, "SA", "");

            con.createStatement().executeUpdate("CREATE TABLE if not EXISTS logs (ID varchar(50), duration int, type varchar(50), host varchar(50), alert varchar(20));");

            final String sql = "INSERT INTO logs (ID, duration, type, host, alert) Values(?, ?, ? ,? ,?)";

            PreparedStatement statement = con.prepareStatement(sql);

            for (int i=0; i<normalizedlog.size(); i++)
            {
                statement.setString(1, normalizedlog.get(i).getId());
                statement.setInt(2, normalizedlog.get(i).getDuration());
                statement.setString(3, normalizedlog.get(i).getType());
                statement.setString(4, normalizedlog.get(i).getHost());
                statement.setString(5, Boolean.toString(normalizedlog.get(i).getAlert()));
                statement.addBatch();
            }
            statement.executeBatch();


            PreparedStatement pst = con.prepareStatement("select * from logs");
            pst.clearParameters();
            ResultSet rs = pst.executeQuery();

            List<FinalLog> contacts = new ArrayList<>();
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


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void logValidation(List<Keeper> keepers) {

        for (Keeper k : keepers) {
            if (k.getId().isEmpty() || k.getState().isEmpty() || k.getTimestamp() == 0) {
                throw new RuntimeException("The logfile is corrupted");
            }
        }
    }


    public static List<FinalLog> listForDBImport(List<Keeper> keepers) {

        List<FinalLog> normalizedLog = new ArrayList<>();

        for (int i = 0; i < keepers.size() - 1; i += 2) {

            FinalLog finalLog = new FinalLog();

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

    //todo name change for this method
    private static void sorting(List<Keeper> keepers) {
        Collections.sort(keepers, new SortComparator());
    }
}

