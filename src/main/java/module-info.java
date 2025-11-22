module com.lucidia.dreamoracle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    // Add Jackson modules
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    opens com.lucidia.lucidia to javafx.fxml;
    opens com.lucidia.lucidia.controller to javafx.fxml;
    opens com.lucidia.lucidia.model to javafx.fxml;

    exports com.lucidia.lucidia;
    exports com.lucidia.lucidia.controller;
    exports com.lucidia.lucidia.model;
    exports com.lucidia.lucidia.service;
}
