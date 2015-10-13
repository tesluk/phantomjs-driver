package com.mi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by andrey.tesluk on 13.10.2015.
 */
public class Settings {

    public String url;
    public String username;
    public String password;
    public String userAgent;
    public String pathToPhantomJS;
    public String output;

    private Settings() {
    }

    public static Settings load(String path) {
        Properties props = new Properties();

        try {
            props.load(new FileInputStream(path));
        } catch (IOException e) {
            System.err.println("Could not load settings. Error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        Settings settings = new Settings();

        settings.url = props.getProperty("url");
        settings.username = props.getProperty("username");
        settings.password = props.getProperty("password");
        settings.userAgent = props.getProperty("useragent");
        settings.pathToPhantomJS = props.getProperty("path_to_phantom_js", "/local/phantomjs/phantomjs");
        settings.output = props.getProperty("output");

        return settings;
    }

}
