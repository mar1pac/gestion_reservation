module org.example.restaurant {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.restaurant to javafx.fxml;
    opens model to javafx.base;
    exports org.example.restaurant;
    exports application;
    opens controller to javafx.fxml;
}