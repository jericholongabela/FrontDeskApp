package com.example.application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class CustomerController implements Initializable {

    Connection connection = null;
    ObservableList<Customer> CustomerList = FXCollections.observableArrayList();
    ObservableList<Transaction> TransactionList = FXCollections.observableArrayList();

    @FXML
    private Label isConnected;
    @FXML
    private TextField inputFirstName, inputLastName, inputMobile, inputEmail;
    @FXML
    private TextArea inputAddress;
    @FXML
    private Button btnAdd, btnHome;
    @FXML
    private TableView<Customer> tblCustomer;
    @FXML
    private TableView<Transaction> tblTransactions;
    @FXML
    private TableColumn<Customer, Integer> customer_id;
    @FXML
    private TableColumn<Customer, String> name;
    @FXML
    private TableColumn<Customer, String> address;
    @FXML
    private TableColumn<Customer, String> mobile_number;
    @FXML
    private TableColumn<Customer, String> email;

    @FXML
    private TableColumn<Customer, Integer> transaction_id;
    @FXML
    private TableColumn<Customer, Integer> transaction_customer_id;
    @FXML
    private TableColumn<Customer, Integer> storage_id;
    @FXML
    private TableColumn<Customer, String> status;
    @FXML
    private TableColumn<Customer, String> date_stored;
    @FXML
    private TableColumn<Customer, String> date_retrieved;


    @Override
    public void initialize (URL location, ResourceBundle resources) {
        try (
                Connection connection = DriverManager.getConnection(databaseAPI.database);
        ){
            isConnected.setText("Connected");
            isConnected.setTextFill(Color.web("#38a63d"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            getCustomersData();
            getTransactionsData();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void addCustomer (MouseEvent event) throws SQLException, ClassNotFoundException {
        String name = inputFirstName.getText() + " " + inputLastName.getText();
        String address = inputAddress.getText();
        String mobile_number = inputMobile.getText();
        String email = inputEmail.getText();

        if (name.isBlank() || address.isBlank() || mobile_number.isBlank() || email.isBlank()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Fill all data first!");
            alert.setTitle("Warning!");
            alert.showAndWait();
        } else {
            databaseAPI.addCustomer(name, address, mobile_number, email);
            clear();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Action Completed Successfully");
            alert.setContentText("Successfully added customer!");

            alert.showAndWait();

            try {
                getCustomersData();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void getCustomersData() throws SQLException {
        CustomerList.clear();
        databaseAPI.getCustomersData(CustomerList);
        tblCustomer.setItems(CustomerList);

        customer_id.setCellValueFactory(new PropertyValueFactory<>("customer_id"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        address.setCellValueFactory(new PropertyValueFactory<>("address"));
        mobile_number.setCellValueFactory(new PropertyValueFactory<>("mobile_number"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void getTransactionsData() throws SQLException, ClassNotFoundException {
        TransactionList.clear();
        databaseAPI.getTransactionsData(TransactionList);
        tblTransactions.setItems(TransactionList);

        transaction_id.setCellValueFactory(new PropertyValueFactory<>("transaction_id"));
        date_stored.setCellValueFactory(new PropertyValueFactory<>("date_stored"));
        date_retrieved.setCellValueFactory(new PropertyValueFactory<>("date_retrieved"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        storage_id.setCellValueFactory(new PropertyValueFactory<>("storage_id"));
        transaction_customer_id.setCellValueFactory(new PropertyValueFactory<>("customer_id"));
    }

    @FXML
    private void clear(){
        inputFirstName.setText("");
        inputLastName.setText("");
        inputAddress.setText("");
        inputMobile.setText("");
        inputEmail.setText("");
    }

    @FXML
    public void setSceneToHome(MouseEvent event) throws IOException {
        Stage stage;
        Scene scene;
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("home.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(850);
        stage.setHeight(510);
        stage.show();
        centerStage(stage);
    }

    private void centerStage(Stage stage) {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = primaryScreenBounds.getMinX() + (primaryScreenBounds.getWidth() - stage.getWidth()) / 2;
        double centerY = primaryScreenBounds.getMinY() + (primaryScreenBounds.getHeight() - stage.getHeight()) / 2;
        stage.setX(centerX);
        stage.setY(centerY);
    }
}
