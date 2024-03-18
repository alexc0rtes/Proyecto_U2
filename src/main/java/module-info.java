module com.example.web {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires javafx.web;

    opens com.example.web to javafx.fxml;
    exports com.example.web;
}