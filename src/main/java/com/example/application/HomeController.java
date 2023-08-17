package com.example.application;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    FileChooser fileChooser = new FileChooser();
    String query = null;
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    Customer searchedCustomer = new Customer();


    int intAreaID;

    @FXML
    private Label isConnected, lblAreaIDData, lblAreaSizeData, lblStatusData, lblCustomerDetails, lblCustomerID, lblCustomerIDData, lblName, lblNameData, lblMobile, lblMobileData, lblEmail, lblEmailData, lblCustomerNotFound;
    @FXML
    private Label lblFile;
    @FXML
    private Button btnCustomer, btnCloseBoxDetails, btnAssign, btnStore, btnSearch, btnRetrieve, btnLoadOtherDatabase;
    @FXML
    private TextField inputSmall, inputMedium, inputLarge, inputSearchName;

    @Override
    public void initialize (URL location, ResourceBundle resources) {
        lblFile.setText(databaseAPI.getDefaultDatabase());
        File initialDir = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(initialDir);
        btnStore.setDisable(true);
        btnRetrieve.setVisible(false);
        try (
                Connection connection = DriverManager.getConnection(databaseAPI.database);
        ){
            System.out.println("Getting connection from: " + databaseAPI.database);
            isConnected.setText("Connected");
            isConnected.setTextFill(Color.web("#38a63d"));
            updateSummary();
            getAvailableStorages();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void refresh (){
        lblFile.setText(databaseAPI.getDefaultDatabase());
        File initialDir = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(initialDir);
        btnStore.setDisable(true);
        btnRetrieve.setVisible(false);
        try (
                Connection connection = DriverManager.getConnection(databaseAPI.database);
        ){
            System.out.println("Getting connection from: " + databaseAPI.database);
            isConnected.setText("Connected");
            isConnected.setTextFill(Color.web("#38a63d"));
            updateSummary();
            getAvailableStorages();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void getDatabaseFile (MouseEvent event){
        try {
            File file = fileChooser.showOpenDialog(new Stage());
            System.out.println("Sucessfully set file: " + file);
            if (file != null){
                databaseAPI.setDefaultDatabase(file.toString());
                System.out.println("Updated default database: " + databaseAPI.getDefaultDatabase());
                databaseAPI.refreshSetDatabase();
                refresh();
            } else {
                System.out.println("Cancelled by the user");
            }
        } catch (Exception e){
            System.out.println("Cancelled by the user");
        }
    }

    public void updateSummary () throws SQLException{
        ArrayList<Integer> arrSummary = new ArrayList<>();
        arrSummary = databaseAPI.getSummary();
        inputSmall.setText(arrSummary.get(0).toString());
        inputMedium.setText(arrSummary.get(1).toString());
        inputLarge.setText(arrSummary.get(2).toString());
    }

    @FXML
    public void setSceneToCustomer(MouseEvent event) throws IOException {
        Stage stage;
        Scene scene;
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("customer.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(870);
        stage.setHeight(510);
        stage.show();
        centerStage(stage);
    }

    @FXML
    private void chooseBox (MouseEvent event) throws SQLException{
        btnAssign.setVisible(false);
        btnSearch.setVisible(true);
        btnStore.setVisible(false);
        btnRetrieve.setVisible(false);
        Stage stage;
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setWidth(1080);
        centerStage(stage);

        Button clickedButton = (Button)event.getSource();
        String areaID = clickedButton.getText();
        intAreaID = Integer.parseInt(areaID);
        try {
            clearAfterNewTransaction();
            int status = databaseAPI.getStorageInformation(intAreaID);
            lblAreaIDData.setText(areaID);
            System.out.println("Status: " + status);

            if (status != -1) {

                lblAreaSizeData.setText(processStorageSize(intAreaID));
                lblStatusData.setText(processAvailability(status));
                if (status == 0){
                    showCustomerDetails(false);
                    btnStore.setVisible(true);
                }
                if (status == 1){
                    showCustomerDetails(true);
                    String stats = "Active";
                    int isActiveThenID = databaseAPI.retrieveActiveStorage(intAreaID, stats);

                    if(isActiveThenID != 0){
                        Customer customer = databaseAPI.getCustomerData(isActiveThenID);

                        if (customer.getCustomer_id() != 0){
                            lblCustomerIDData.setText(Integer.toString(customer.getCustomer_id()));
                            lblNameData.setText(customer.getName());
                            lblMobileData.setText(customer.getMobile_number());
                            lblEmailData.setText(customer.getEmail());

                            btnRetrieve.setVisible(true);
                        }
                    }
                }
            } else {
                System.out.println("Storage not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void closeChooseBox (MouseEvent event){
        showCustomerDetails(false);
        Stage stage;
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setWidth(840);
        centerStage(stage);
        inputSearchName.setText("");
        btnStore.setDisable(true);
        intAreaID = 0;
    }

    @FXML
    private void search (MouseEvent event) throws SQLException{
        showCustomerDetails(false);
        lblCustomerNotFound.setText("Customer not found!");
        String name = inputSearchName.getText();
        if (!name.isEmpty() || !name.isBlank()){
            lblCustomerNotFound.setVisible(false);
            searchedCustomer = databaseAPI.searchCustomerData(name);

            if (searchedCustomer.getCustomer_id() != 0){
                lblCustomerIDData.setText(Integer.toString(searchedCustomer.getCustomer_id()));
                lblNameData.setText(searchedCustomer.getName());
                lblMobileData.setText(searchedCustomer.getMobile_number());
                lblEmailData.setText(searchedCustomer.getEmail());

                showCustomerDetails(true);
                btnSearch.setVisible(false);
                btnAssign.setVisible(true);

                btnStore.setDisable(false);
            } else {
                inputSearchName.setText("");
                lblCustomerNotFound.setVisible(true);
            }
        }else {
            inputSearchName.setText("");
            lblCustomerNotFound.setText("Type in a name!");
            lblCustomerNotFound.setVisible(true);
        }
    }

    @FXML
    private void storeItem (MouseEvent event) throws SQLException{
        LocalDateTime currDT = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatted_time = currDT.format(formatter);
        String status = "Active";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Alert");
        alert.setHeaderText("Do you want to store item?");
        alert.setContentText("Press OK to proceed or Cancel to cancel.");

        // Show the confirmation dialog and wait for the user's response
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    databaseAPI.addTransaction(formatted_time, status, intAreaID, searchedCustomer.getCustomer_id());
                    clearAfterNewTransaction();
                    closeChooseBox(event);
                    updateSummary();
                    getAvailableStorages();
                    showSuccess();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                // Perform the action you want to do if the user confirms
            } else {
                System.out.println("User clicked Cancel.");
                // Perform the action you want to do if the user cancels
            }
        });
    }

    @FXML
    private void retrieveItem (MouseEvent event) throws SQLException{
        LocalDateTime currDT = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatted_time = currDT.format(formatter);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Alert");
        alert.setHeaderText("Do you want to retrieve item?");
        alert.setContentText("Press OK to proceed or Cancel to cancel.");

        // Show the confirmation dialog and wait for the user's response
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    System.out.println("I have clicked ok!");
                    String status = "Inactive";
                    databaseAPI.addTransaction(formatted_time, status, intAreaID, Integer.parseInt(lblCustomerIDData.getText()));
                    clearAfterNewTransaction();
                    closeChooseBox(event);
                    updateSummary();
                    getAvailableStorages();
                    showSuccess();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                // Perform the action you want to do if the user confirms
            } else {
                System.out.println("User clicked Cancel.");
                // Perform the action you want to do if the user cancels
            }
        });
    }

    private void centerStage(Stage stage) {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = primaryScreenBounds.getMinX() + (primaryScreenBounds.getWidth() - stage.getWidth()) / 2;
        double centerY = primaryScreenBounds.getMinY() + (primaryScreenBounds.getHeight() - stage.getHeight()) / 2;
        stage.setX(centerX);
        stage.setY(centerY);
    }

    private void showCustomerDetails (Boolean show){
        lblCustomerDetails.setVisible(show);
        lblCustomerID.setVisible(show);
        lblCustomerIDData.setVisible(show);
        lblName.setVisible(show);
        lblNameData.setVisible(show);
        lblMobile.setVisible(show);
        lblMobileData.setVisible(show);
        lblEmail.setVisible(show);
        lblEmailData.setVisible(show);
    }

    public String processStorageSize (int id){
        String storageSize = "";
        if (id >= 1 && id <=46){
            storageSize = "Small";
        }
        if (id >= 47 && id <=60){
            storageSize = "Medium";
        }
        if (id >= 61 && id <=72){
            storageSize = "Large";
        }

        return storageSize;
    }

    public String processAvailability(int status){
        String availability = "";
        if (status == 0){
            availability = "Available";
            lblStatusData.setTextFill(Color.web("#2E8B57"));
        } else {
            availability = "Unavailable";
            lblStatusData.setTextFill(Color.web("#FF0000"));
        }

        return availability;
    }

    public void clearAfterNewTransaction(){
        lblCustomerIDData.setText("");
        lblNameData.setText("");
        lblEmailData.setText("");
        lblMobileData.setText("");
        inputSearchName.setText("");

        showCustomerDetails(false);
    }

    public void showSuccess () throws SQLException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Action Completed Successfully");
        alert.setContentText("Successfully updated data!");

        alert.showAndWait();

        getAvailableStorages();
    }

    public void getAvailableStorages () throws SQLException {
        ArrayList<Integer> availableStorages = databaseAPI.getActiveStorages();

        btn1.setDefaultButton(isAvailable(btn1.getText(), availableStorages));
        btn2.setDefaultButton(isAvailable(btn2.getText(), availableStorages));
        btn3.setDefaultButton(isAvailable(btn3.getText(), availableStorages));
        btn4.setDefaultButton(isAvailable(btn4.getText(), availableStorages));
        btn5.setDefaultButton(isAvailable(btn5.getText(), availableStorages));
        btn6.setDefaultButton(isAvailable(btn6.getText(), availableStorages));
        btn7.setDefaultButton(isAvailable(btn7.getText(), availableStorages));
        btn8.setDefaultButton(isAvailable(btn8.getText(), availableStorages));
        btn9.setDefaultButton(isAvailable(btn9.getText(), availableStorages));
        btn10.setDefaultButton(isAvailable(btn10.getText(), availableStorages));
        btn11.setDefaultButton(isAvailable(btn11.getText(), availableStorages));
        btn12.setDefaultButton(isAvailable(btn12.getText(), availableStorages));
        btn13.setDefaultButton(isAvailable(btn13.getText(), availableStorages));
        btn14.setDefaultButton(isAvailable(btn14.getText(), availableStorages));
        btn15.setDefaultButton(isAvailable(btn15.getText(), availableStorages));
        btn16.setDefaultButton(isAvailable(btn16.getText(), availableStorages));
        btn17.setDefaultButton(isAvailable(btn17.getText(), availableStorages));
        btn18.setDefaultButton(isAvailable(btn18.getText(), availableStorages));
        btn19.setDefaultButton(isAvailable(btn19.getText(), availableStorages));
        btn20.setDefaultButton(isAvailable(btn20.getText(), availableStorages));
        btn21.setDefaultButton(isAvailable(btn21.getText(), availableStorages));
        btn22.setDefaultButton(isAvailable(btn22.getText(), availableStorages));
        btn23.setDefaultButton(isAvailable(btn23.getText(), availableStorages));
        btn24.setDefaultButton(isAvailable(btn24.getText(), availableStorages));
        btn25.setDefaultButton(isAvailable(btn25.getText(), availableStorages));
        btn26.setDefaultButton(isAvailable(btn26.getText(), availableStorages));
        btn27.setDefaultButton(isAvailable(btn27.getText(), availableStorages));
        btn28.setDefaultButton(isAvailable(btn28.getText(), availableStorages));
        btn29.setDefaultButton(isAvailable(btn29.getText(), availableStorages));
        btn30.setDefaultButton(isAvailable(btn30.getText(), availableStorages));
        btn31.setDefaultButton(isAvailable(btn31.getText(), availableStorages));
        btn32.setDefaultButton(isAvailable(btn32.getText(), availableStorages));
        btn33.setDefaultButton(isAvailable(btn33.getText(), availableStorages));
        btn34.setDefaultButton(isAvailable(btn34.getText(), availableStorages));
        btn35.setDefaultButton(isAvailable(btn35.getText(), availableStorages));
        btn36.setDefaultButton(isAvailable(btn36.getText(), availableStorages));
        btn37.setDefaultButton(isAvailable(btn37.getText(), availableStorages));
        btn38.setDefaultButton(isAvailable(btn38.getText(), availableStorages));
        btn39.setDefaultButton(isAvailable(btn39.getText(), availableStorages));
        btn40.setDefaultButton(isAvailable(btn40.getText(), availableStorages));
        btn41.setDefaultButton(isAvailable(btn41.getText(), availableStorages));
        btn42.setDefaultButton(isAvailable(btn42.getText(), availableStorages));
        btn43.setDefaultButton(isAvailable(btn43.getText(), availableStorages));
        btn44.setDefaultButton(isAvailable(btn44.getText(), availableStorages));
        btn45.setDefaultButton(isAvailable(btn45.getText(), availableStorages));
        btn46.setDefaultButton(isAvailable(btn46.getText(), availableStorages));
        btn47.setDefaultButton(isAvailable(btn47.getText(), availableStorages));
        btn48.setDefaultButton(isAvailable(btn48.getText(), availableStorages));
        btn49.setDefaultButton(isAvailable(btn49.getText(), availableStorages));
        btn50.setDefaultButton(isAvailable(btn50.getText(), availableStorages));
        btn51.setDefaultButton(isAvailable(btn51.getText(), availableStorages));
        btn52.setDefaultButton(isAvailable(btn52.getText(), availableStorages));
        btn53.setDefaultButton(isAvailable(btn53.getText(), availableStorages));
        btn54.setDefaultButton(isAvailable(btn54.getText(), availableStorages));
        btn55.setDefaultButton(isAvailable(btn55.getText(), availableStorages));
        btn56.setDefaultButton(isAvailable(btn56.getText(), availableStorages));
        btn57.setDefaultButton(isAvailable(btn57.getText(), availableStorages));
        btn58.setDefaultButton(isAvailable(btn58.getText(), availableStorages));
        btn59.setDefaultButton(isAvailable(btn59.getText(), availableStorages));
        btn60.setDefaultButton(isAvailable(btn60.getText(), availableStorages));
        btn61.setDefaultButton(isAvailable(btn61.getText(), availableStorages));
        btn62.setDefaultButton(isAvailable(btn62.getText(), availableStorages));
        btn63.setDefaultButton(isAvailable(btn63.getText(), availableStorages));
        btn64.setDefaultButton(isAvailable(btn64.getText(), availableStorages));
        btn65.setDefaultButton(isAvailable(btn65.getText(), availableStorages));
        btn66.setDefaultButton(isAvailable(btn66.getText(), availableStorages));
        btn67.setDefaultButton(isAvailable(btn67.getText(), availableStorages));
        btn68.setDefaultButton(isAvailable(btn68.getText(), availableStorages));
        btn69.setDefaultButton(isAvailable(btn69.getText(), availableStorages));
        btn70.setDefaultButton(isAvailable(btn70.getText(), availableStorages));
        btn71.setDefaultButton(isAvailable(btn71.getText(), availableStorages));
        btn72.setDefaultButton(isAvailable(btn72.getText(), availableStorages));

    }


    public Boolean isAvailable(String buttonNumber, ArrayList<Integer> activeStorages){
        int intButtonNumber = Integer.parseInt(buttonNumber);
        boolean isActive = false;

        for (int id : activeStorages) {
            if (intButtonNumber == id) {
                isActive = true;
                break;
            }
        }
        System.out.println("Button: " + buttonNumber + " | Available?: " + isActive);
        return !isActive;
    }

    @FXML
    Button btn1;
    @FXML
    Button btn2;
    @FXML
    Button btn3;
    @FXML
    Button btn4;
    @FXML
    Button btn5;
    @FXML
    Button btn6;
    @FXML
    Button btn7;
    @FXML
    Button btn8;
    @FXML
    Button btn9;
    @FXML
    Button btn10;
    @FXML
    Button btn11;
    @FXML
    Button btn12;
    @FXML
    Button btn13;
    @FXML
    Button btn14;
    @FXML
    Button btn15;
    @FXML
    Button btn16;
    @FXML
    Button btn17;
    @FXML
    Button btn18;
    @FXML
    Button btn19;
    @FXML
    Button btn20;
    @FXML
    Button btn21;
    @FXML
    Button btn22;
    @FXML
    Button btn23;
    @FXML
    Button btn24;
    @FXML
    Button btn25;
    @FXML
    Button btn26;
    @FXML
    Button btn27;
    @FXML
    Button btn28;
    @FXML
    Button btn29;
    @FXML
    Button btn30;
    @FXML
    Button btn31;
    @FXML
    Button btn32;
    @FXML
    Button btn33;
    @FXML
    Button btn34;
    @FXML
    Button btn35;
    @FXML
    Button btn36;
    @FXML
    Button btn37;
    @FXML
    Button btn38;
    @FXML
    Button btn39;
    @FXML
    Button btn40;
    @FXML
    Button btn41;
    @FXML
    Button btn42;
    @FXML
    Button btn43;
    @FXML
    Button btn44;
    @FXML
    Button btn45;
    @FXML
    Button btn46;
    @FXML
    Button btn47;
    @FXML
    Button btn48;
    @FXML
    Button btn49;
    @FXML
    Button btn50;
    @FXML
    Button btn51;
    @FXML
    Button btn52;
    @FXML
    Button btn53;
    @FXML
    Button btn54;
    @FXML
    Button btn55;
    @FXML
    Button btn56;
    @FXML
    Button btn57;
    @FXML
    Button btn58;
    @FXML
    Button btn59;
    @FXML
    Button btn60;
    @FXML
    Button btn61;
    @FXML
    Button btn62;
    @FXML
    Button btn63;
    @FXML
    Button btn64;
    @FXML
    Button btn65;
    @FXML
    Button btn66;
    @FXML
    Button btn67;
    @FXML
    Button btn68;
    @FXML
    Button btn69;
    @FXML
    Button btn70;
    @FXML
    Button btn71;
    @FXML
    Button btn72;













}
