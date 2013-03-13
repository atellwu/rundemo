package com.dianping.rundemo.config;

import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Properties p;
    public static Object shellDir;
    static {
        p = new Properties();
        try {
            p.load(Config.class.getResourceAsStream("/rundemo.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        //sendPostThreadCount
        shellDir = p.get("shellDir");
    }

}
