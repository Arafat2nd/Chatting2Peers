module com.example.chatting2peers {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens com.example.chatting2peers to javafx.fxml;
    exports com.example.chatting2peers;
}