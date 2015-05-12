package com.yeahmobi.rundemo.config;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class Config {

	private static Properties p;
	public static Object demoDir;
	public static String shellDir;
	public static String gitTempDir;
	public static String appprojectDir;
	public static String javaprojectDir;
	static {
		p = new Properties();
		try {
			p.load(Config.class.getResourceAsStream("/conf/config.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		// splice path
		demoDir = p.get("demoDir");
		shellDir = demoDir + "shell/";
		appprojectDir = demoDir + "appprojects/";
		javaprojectDir = demoDir + "javaprojects/";
		gitTempDir = demoDir + "git_temp/";
	}

	// Copy shell file to target Directory
	public static void copyShellFile() throws IOException, InterruptedException {
		File targetFile = new File(Config.shellDir);
		FileUtils.copyDirectory(
				new File(Config.class.getResource("/data-rundemo").getFile()),
				targetFile);
		//Give permission
		for(File shellFile: targetFile.listFiles()){
			shellFile.setExecutable(true);
		}
	}
}