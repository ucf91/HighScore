package com.company.utils;

import java.io.IOException;
import java.util.Properties;

public class Util {
    private static final String PROPERTIES_PATH = "src/main/resources/";

    private Util() {
    }

    public static Properties getProperties() throws IOException {
        Properties props = new Properties();
        // load a properties file
        props.load(Util.class.getResourceAsStream("/application.properties"));
        return props;
    }
}
