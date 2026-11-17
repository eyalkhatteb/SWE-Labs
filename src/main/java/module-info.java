module com.example.swelabs {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.swelabs to javafx.fxml;
    exports com.example.swelabs;
}