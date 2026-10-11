module minibrowser {
    requires javafx.controls;
    requires javafx.web;
    requires java.sql;
    requires mysql.connector.j;
    requires org.xerial.sqlitejdbc;

    opens minibrowser to javafx.graphics;
    opens minibrowser.ui to javafx.graphics;
    opens minibrowser.model to javafx.base;

    exports minibrowser;
    exports minibrowser.ui;
}
