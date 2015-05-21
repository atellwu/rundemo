package com.yeahmobi.rundemo.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeahmobi.rundemo.config.Config;
import com.yeahmobi.rundemo.utils.Constants;

/**
 * TODO Comment of ProjectContext
 * 
 * @author wukezhu
 */
public class ProjectContext {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectContext.class);

    private static ConcurrentHashMap<String, AppProject> appProjects = new ConcurrentHashMap<String, AppProject>();

    private static ConcurrentHashMap<String, ConcurrentHashMap<String, JavaProject>> appToJavaProjects = new ConcurrentHashMap<String, ConcurrentHashMap<String, JavaProject>>();
    
    //初始化ProjectContext
    static {
        //扫描Config.appprojectDir" + "appprojects/目录，创建appprojects
        File appprojectsDir = new File(Config.appprojectDir);
        File[] appprojectDirs = appprojectsDir.listFiles();
        if (appprojectDirs != null) {
            for (File appprojectDir : appprojectDirs) {
                String app = appprojectDir.getName();
                File[] eligibleFiles = appprojectDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return Constants.CLASSPATH.equals(name) || Constants.APPPROPERTIES.equals(name);
                    }
                });
                try {
                	String classpath = null;
                    for(File file : eligibleFiles){
                    	   if(Constants.CLASSPATH.equals(file.getName())){
                    		   classpath = FileUtils.readFileToString(file, "ISO-8859-1").trim();
                    	   }
                       }
                    AppProject appProject = ProjectContext.deserialize(Config.appprojectDir + app +"/app.properties");
                    appProject.setClasspath(classpath);
                    ProjectContext.putAppProject(app, appProject);
                } catch (Exception e) {
                    LOG.error("error when load from " + Config.appprojectDir + app, e);
                }
            }
        }
    }
    
    // 从文件反序列化到对象
 	public static AppProject deserialize(String filePath) {
 		AppProject appProject = null;
 		try {
 			// 创建一个对象输入流，从文件读取对象
 			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
 					filePath));
 			// 注意读对象时必须按照序列化对象顺序读，否则会出错
 			appProject = (AppProject) (in.readObject());
 			in.close();
 		} catch (Exception x) {
 			LOG.error("deserialize app.properties to AppProject object failed", x);
 		}
 		return appProject;
 	}

    public static List<String> getAllAppNames() {
        List<String> names = new ArrayList<String>();
        for (AppProject appProject : appProjects.values()) {
            names.add(appProject.getApp());
        }
        return names;
    }

    /**
     * 如果内存没有该app，尝试从本地文件加载。都没有则返回null
     */
    public static AppProject getAppProject(String app) {
        AppProject appProject = appProjects.get(app);
        if (appProject == null) {//从本地文件尝试加载
            synchronized (app.intern()) {
                appProject = appProjects.get(app);
                if (appProject == null) {
                    File appprojectsDir = new File(Config.appprojectDir);
                    File[] appprojectDirs = appprojectsDir.listFiles();
                    for (File appprojectDir : appprojectDirs) {
                        String app0 = appprojectDir.getName();
                        if (app0.equals(app)) {//从本地文件中找到这个app
                            LOG.info("loading AppProject from local filesystem, app=" + app);
                            File[] eligibleFiles = appprojectDir.listFiles(new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String name) {
                                	 return Constants.CLASSPATH.equals(name) || Constants.APPPROPERTIES.equals(name);
                                }
                            });
                            try {
                            	String classpath = null;
                                for(File file : eligibleFiles){
                                	   if(Constants.CLASSPATH.equals(file.getName())){
                                		   classpath = FileUtils.readFileToString(file, "ISO-8859-1").trim();
                                	   }
                                   }
                                appProject = ProjectContext.deserialize(Config.appprojectDir + app +"/app.properties");
                                appProject.setClasspath(classpath);
                                ProjectContext.putAppProject(app, appProject);
                            } catch (Exception e) {
                                LOG.error("error when load from " + Config.appprojectDir + app, e);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return appProject;
    }

    public static void putAppProject(String app, AppProject appProject) {
        appProjects.put(app, appProject);
    }

    public static void removeAppProject(String app) {
        appProjects.remove(app);
    }

    public static JavaProject getJavaProject(String app, String pageid) {
        JavaProject javaProject = null;
        ConcurrentHashMap<String, JavaProject> javaProjects = appToJavaProjects.get(app);
        if (javaProjects != null) {
            javaProject = javaProjects.get(pageid);
        }
        if (javaProject == null) {//尝试从本地文件加载
            synchronized (pageid.intern()) {
                javaProjects = appToJavaProjects.get(app);
                if (javaProjects != null) {
                    javaProject = javaProjects.get(pageid);
                }
                if (javaProject == null) {
                    File javaprojectsParentDir = new File(Config.appprojectDir + app);
                    File[] javaprojectDirs = javaprojectsParentDir.listFiles();
                    for (File appprojectDir : javaprojectDirs) {
                        String pageid0 = appprojectDir.getName();
                        if (pageid0.equals(pageid)) {//从本地文件中找到这个app
                            LOG.info("loading JavaProject from local filesystem, app=" + app + ",pageid=" + pageid);
                            try {
                                javaProject = new JavaProject(app, pageid, false);
                                javaProjects = appToJavaProjects.get(app);
                                if (javaProjects == null) {
                                    javaProjects = new ConcurrentHashMap<String, JavaProject>();
                                    appToJavaProjects.put(app, javaProjects);
                                }
                                javaProjects.put(pageid, javaProject);
                                LOG.info("loaded JavaProject from local filesystem, app=" + app + ",pageid=" + pageid);
                            } catch (IOException e) {
                                LOG.error(e.getMessage(), e);
                            }
                            break;
                        }
                    }
                }

            }
        }

        return javaProject;
    }

    public static void putJavaProject(String app, String pageid, JavaProject javaProject) {
        ConcurrentHashMap<String, JavaProject> javaProjects = appToJavaProjects.get(app);
        if (javaProjects == null) {
            synchronized (app.intern()) {
                javaProjects = appToJavaProjects.get(app);
                if (javaProjects == null) {
                    javaProjects = new ConcurrentHashMap<String, JavaProject>();
                    appToJavaProjects.put(app, javaProjects);
                }
            }
        }
        javaProjects.put(pageid, javaProject);
    }

    public static void removeJavaProject(String app, String pageid) {
        ConcurrentHashMap<String, JavaProject> javaProjects = appToJavaProjects.get(app);
        if (javaProjects != null) {
            javaProjects.remove(pageid);
        }
    }

}
