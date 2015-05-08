package com.yeahmobi.rundemo.config;

import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Properties p;
    public static Object demoDir;
    public static String shellDir;
    public static String appprojectDir;
    public static String javaprojectDir;
    static {
        p = new Properties();
        try {
            p.load(Config.class.getResourceAsStream("/conf/config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        //sendPostThreadCount
        demoDir = p.get("demoDir");
        shellDir = demoDir + "shell/";
        appprojectDir = demoDir + "appprojects/";
        javaprojectDir = demoDir + "javaprojects/";
    }

}
