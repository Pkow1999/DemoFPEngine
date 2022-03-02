module fpengine.demofpengine {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.controlsfx.controls;

    opens fpengine.demofpengine to javafx.fxml;
    exports fpengine.demofpengine;
}