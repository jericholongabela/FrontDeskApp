module application {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.sql;
    requires sqlite.jdbc;

    opens com.example.application to javafx.fxml;
    exports com.example.application;
}