package com.yeahmobi.rundemo.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.yeahmobi.rundemo.config.Config;
import com.yeahmobi.rundemo.utils.CodeUtils;

public class AppProject implements Serializable{

	private static final long serialVersionUID = 1L;
	private String app;
	private String gitUrl;
	private String branch;
	private String subdir;
	private String packageName;
	private String mavenOpt;
	private String classpath;
	private String fileTreeJsonData;

	public AppProject(String app, String gitUrl, String branch, String subdir,
			String packageName, String mavenOpt) {
		super();
		this.app = app;
		this.gitUrl = gitUrl;
		this.branch = branch;
		this.subdir = subdir;
		this.packageName = packageName;
		this.mavenOpt = mavenOpt;
	}

	public String getApp() {
		return app;
	}

	public String getClasspath() {
		return classpath;
	}

	public String getPackageName(){
		return packageName;
	}
	
	public String getGitUrl(){
		return gitUrl;
	}
	
	public String getBranch() {
		return branch;
	}

	public String getSubdir() {
		return subdir;
	}

	public String getMavenOpt() {
		return mavenOpt;
	}

	/**
	 * 从本地load文件
	 * 
	 * @param javaFilePath
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public String loadCode(String javaFilePath) throws FileNotFoundException,
			IOException {
		File javaFile = new File(javaFilePath);
		return IOUtils.toString(new FileInputStream(javaFile), "UTF-8");
	}

	public String loadPom() throws FileNotFoundException, IOException {
		File pomFile = new File(Config.appprojectDir + app
				+ "/pom.xml");
		return IOUtils.toString(new FileInputStream(pomFile), "UTF-8");
	}

	/**
	 * 从本地load文件名列表
	 * 
	 * @param javaFileName
	 * @return
	 * @throws IOException
	 */
	public List<JavaFileInfo> loadJavaFileNameList() throws IOException {
		List<JavaFileInfo> list = new ArrayList<JavaFileInfo>();
		File appprojectsDir = new File(Config.appprojectDir + app
				+ "/src/main/java/");
		Collection<File> files = FileUtils
				.listFiles(appprojectsDir, null, true);
		if (files.size() > 0) {
			for (File f : files) {
				JavaFileInfo javaFileInfo = parseJavaFileInfo(f);
				list.add(javaFileInfo);
			}
		}
		Collections.sort(list, new Comparator<JavaFileInfo>() {
			public int compare(JavaFileInfo o1, JavaFileInfo o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
		return list;
	}

	/**
	 * 从File中解析出path,displayName,desc
	 */
	private JavaFileInfo parseJavaFileInfo(File file) throws IOException {
		return CodeUtils.getJavaFileInfo(file, app);
	}

	@Override
	public String toString() {
		return "AppProject [app=" + app + ", packageName="+ packageName +", classpath=" + classpath + "]";
	}

	public void setApp(String app) {
		this.app = app;
	}

	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public void setSubdir(String subdir) {
		this.subdir = subdir;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setMavenOpt(String mavenOpt) {
		this.mavenOpt = mavenOpt;
	}

	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	public String getFileTreeJsonData() {
		return fileTreeJsonData;
	}

	public void setFileTreeJsonData(String fileTreeJsonData) {
		this.fileTreeJsonData = fileTreeJsonData;
	}

}
