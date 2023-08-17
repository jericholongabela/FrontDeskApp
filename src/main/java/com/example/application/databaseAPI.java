package com.example.application;

import javafx.collections.ObservableList;
import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class databaseAPI {
    public static final String JDBC = "org.sqlite.JDBC";

    public static String defaultDatabase = "fdadatabase.db";
    public static String database = "jdbc:sqlite:"+defaultDatabase;
    private static String query = null;



    public static void addCustomer (String name, String address, String mobile_number, String email) throws SQLException, ClassNotFoundException {
        query = "INSERT into `customers` (`name`, `address`, `mobile_number`, `email`) VALUES (?, ?, ?, ?)";
        try (
                Connection connection = DriverManager.getConnection(database);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, address);
            preparedStatement.setString(3, mobile_number);
            preparedStatement.setString(4, email);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static void getCustomersData (ObservableList<Customer> CustomerList) throws SQLException {
        query = "SELECT * from customers";
        ResultSet resultSet = null;
        try (
                Connection connection = DriverManager.getConnection(database);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                CustomerList.add(new Customer(
                                resultSet.getInt("customer_id"),
                                resultSet.getString("name"),
                                resultSet.getString("address"),
                                resultSet.getString("mobile_number"),
                                resultSet.getString("email")));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void getTransactionsData (ObservableList<Transaction> TransactionsList) throws SQLException, ClassNotFoundException {
        query = "SELECT * from transactions";
        ResultSet resultSet = null;
        try (
                Connection connection = DriverManager.getConnection(database);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                TransactionsList.add(
                        new Transaction(
                                resultSet.getInt("transaction_id"),
                                resultSet.getString("date_stored"),
                                resultSet.getString("date_retrieved"),
                                resultSet.getString("status"),
                                resultSet.getInt("storage_id"),
                                resultSet.getInt("customer_id")
                        ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Integer> getSummary () throws SQLException{
        int smallCount = 0, mediumCount = 0, largeCount = 0;
        query = "SELECT " +
                "SUM(CASE WHEN id BETWEEN 1 AND 46 AND status = 0 THEN 1 ELSE 0 END) AS range_1_count, " +
                "SUM(CASE WHEN id BETWEEN 47 AND 60 AND status = 0 THEN 1 ELSE 0 END) AS range_2_count, " +
                "SUM(CASE WHEN id BETWEEN 61 AND 74 AND status = 0 THEN 1 ELSE 0 END) AS range_3_count " +
                "FROM storage_area";
        ArrayList<Integer> arrSummary = new ArrayList<>();
        try (
                Connection connection = DriverManager.getConnection(database);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();
                ){
            if (resultSet.next()) {
                smallCount = resultSet.getInt("range_1_count");
                mediumCount = resultSet.getInt("range_2_count");
                largeCount = resultSet.getInt("range_3_count");
            }
            arrSummary.add(smallCount);
            arrSummary.add(mediumCount);
            arrSummary.add(largeCount);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return arrSummary;
    }

    public static ArrayList<Integer> getActiveStorages () throws SQLException{
        int status = 0;
        ArrayList<Integer> arrSummary = new ArrayList<>();
        query = "SELECT id FROM storage_area WHERE status = ?";
        try (
                Connection connection = DriverManager.getConnection(database);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
             ){
            preparedStatement.setInt(1, status);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                arrSummary.add(resultSet.getInt("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrSummary;
    }

    public static int getStorageInformation (int storageId) throws SQLException {
        query = "SELECT status FROM storage_area WHERE id = ?";
        ResultSet returnResult = null;
        int returnStatus = -1;
        try (
                Connection connection = DriverManager.getConnection(database);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            preparedStatement.setInt(1, storageId);
            returnResult = preparedStatement.executeQuery();
            returnStatus = returnResult.getInt("status");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnStatus;
    }

    public static Customer searchCustomerData (String name) throws SQLException {
        Customer searchedCustomer = new Customer();
        query = "SELECT * FROM customers WHERE name = ?";
        ResultSet resultSet = null;
        try (
                Connection connection = DriverManager.getConnection(database);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            preparedStatement.setString(1, name);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                    searchedCustomer.setCustomer_id(resultSet.getInt("customer_id"));
                    searchedCustomer.setName(resultSet.getString("name"));
                    searchedCustomer.setAddress(resultSet.getString("address"));
                    searchedCustomer.setMobile_number(resultSet.getString("mobile_number"));
                    searchedCustomer.setEmail(resultSet.getString("email"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return searchedCustomer;
    }

    public static void addTransaction(String dateTime, String status, int storage_id, int customer_id) throws SQLException {
        System.out.println("I have entered addTransaction with status: " + status);
        if (status.contentEquals("Active")) {
            query = "INSERT into `transactions` (`date_stored`, `status`, `storage_id`, `customer_id`) VALUES (?, ?, ?, ?)";
            try (
                    Connection connection = DriverManager.getConnection(database);
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
            ) {
                preparedStatement.setString(1, dateTime);
                preparedStatement.setString(2, status);
                preparedStatement.setInt(3, storage_id);
                preparedStatement.setInt(4, customer_id);

                preparedStatement.executeUpdate();

                System.out.println("Successfully inserted a new data");

                updateStorageStatus(storage_id, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (status.contentEquals("Inactive")){
            System.out.println("I have entered the inactive function");
            query = "UPDATE transactions SET date_retrieved = ?, status = ? WHERE storage_id = ? AND customer_id = ?";
            try (
                    Connection connection = DriverManager.getConnection(database);
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
            ){
                preparedStatement.setString(1, dateTime);
                preparedStatement.setString(2, status);
                preparedStatement.setInt(3, storage_id);
                preparedStatement.setInt(4, customer_id);

                preparedStatement.executeUpdate();
                System.out.println("Successfully updated transactions!");
                System.out.println("Date retrieved: " + dateTime);
                System.out.println("Status: " + status);
                System.out.println("Storage ID: " + storage_id);
                System.out.println("CustomeR ID: " + customer_id);
                try{
                    updateStorageStatus(storage_id, 0);
                } catch (Exception e){
                    System.out.println("Failed to update storage status after retrieving.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int retrieveActiveStorage (int storage_id, String status) throws SQLException {
        query = "SELECT * from transactions WHERE storage_id = ? AND status = ?";
        ResultSet returnResult = null;
        PreparedStatement preppedS = null;
        int isActiveThenID = 0;
        try (
                Connection connection = DriverManager.getConnection(database);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            preparedStatement.setInt(1, storage_id);
            preparedStatement.setString(2, status);
            returnResult = preparedStatement.executeQuery();

            if (returnResult.next()){
                isActiveThenID = returnResult.getInt("customer_id");
            } else {
                isActiveThenID = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isActiveThenID;
    }

    public static Customer getCustomerData (int customer_id) throws SQLException {
        query = "SELECT * FROM customers WHERE customer_id = ?";
        ResultSet returnResult = null;
        Customer retrievedCustomer = new Customer();
        try(
                Connection connection = DriverManager.getConnection(database);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            preparedStatement.setInt(1, customer_id);
            returnResult = preparedStatement.executeQuery();

            if (returnResult.next()){
                retrievedCustomer.setCustomer_id(returnResult.getInt("customer_id"));
                retrievedCustomer.setName(returnResult.getString("name"));
                retrievedCustomer.setAddress(returnResult.getString("address"));
                retrievedCustomer.setMobile_number(returnResult.getString("mobile_number"));
                retrievedCustomer.setEmail(returnResult.getString("email"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retrievedCustomer;
    }

    public static void updateStorageStatus (int storage_id, int status) throws SQLException, ClassNotFoundException {
        query = "UPDATE storage_area SET status = ? WHERE id = ?";
        try (
                Connection connection = DriverManager.getConnection(database);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            preparedStatement.setInt(1, status);
            preparedStatement.setInt(2, storage_id);
            preparedStatement.executeUpdate();

            System.out.println("Successfully updated storage[" +storage_id+ "] to status[" +status+ "].");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Query: UPDATE storage_area SET status = " + status + " WHERE id = " + storage_id);
    }


    public static void setDefaultDatabase(String defaultDatabase) {
        databaseAPI.defaultDatabase = defaultDatabase;
    }

    public static String getDefaultDatabase() {
        return defaultDatabase;
    }

    public static void refreshSetDatabase () {
        databaseAPI.database = "jdbc:sqlite:"+ getDefaultDatabase();
    }
}
