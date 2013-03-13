package com.dianping.rundemo.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.dianping.rundemo.config.Config;
import com.dianping.rundemo.utils.CodeUtils;

public class AppProject {

    private final String app;
    private final String classpath;

    public AppProject(String app, String classpath) {
        super();
        this.app = app;
        this.classpath = classpath;
    }

    public String getApp() {
        return app;
    }

    public String getClasspath() {
        return classpath;
    }

    /**
     * 从本地load文件
     * 
     * @param javaFilePath
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    public String loadCode(String javaFilePath) throws FileNotFoundException, IOException {
        File javaFile = new File(Config.shellDir + "/appprojects/" + app + "/" + javaFilePath);
        return IOUtils.toString(new FileInputStream(javaFile), "UTF-8");
    }

    public String loadPom() throws FileNotFoundException, IOException {
        File pomFile = new File(Config.shellDir + "/appprojects/" + app + "/pom.xml");
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
        File appprojectsDir = new File(Config.shellDir + "/appprojects/" + app + "/src/main/java/");
        Collection<File> files = FileUtils.listFiles(appprojectsDir, null, true);
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
        return "AppProject [app=" + app + ", classpath=" + classpath + "]";
    }

    public static void main(String[] args) {

    }

}
