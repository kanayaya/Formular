module PharmNameToClass {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires json.simple;
    requires org.apache.poi.ooxml;

    opens com.kanayaya.pharmnametoclass to javafx.fxml;
    exports com.kanayaya.pharmnametoclass;
}