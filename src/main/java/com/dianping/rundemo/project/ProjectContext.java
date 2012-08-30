package com.dianping.rundemo.project;

import java.util.concurrent.ConcurrentHashMap;

public class ProjectContext {

   private static ConcurrentHashMap<String, AppProject>                             appProjects       = new ConcurrentHashMap<String, AppProject>();

   private static ConcurrentHashMap<String, ConcurrentHashMap<String, JavaProject>> appToJavaProjects = new ConcurrentHashMap<String, ConcurrentHashMap<String, JavaProject>>();

   public static AppProject getAppProject(String app) {
      return appProjects.get(app);
   }

   public static void putAppProject(String app, AppProject appProject) {
      appProjects.put(app, appProject);
   }

   public static void removeAppProject(String app) {
      appProjects.remove(app);
   }

   public static JavaProject getJavaProject(String app, String pageid) {
      ConcurrentHashMap<String, JavaProject> javaProjects = appToJavaProjects.get(app);
      if (javaProjects != null) {
         return javaProjects.get(pageid);
      }
      return null;
   }

   public static void putJavaProject(String app, String pageid, JavaProject javaProject) {
      ConcurrentHashMap<String, JavaProject> javaProjects = appToJavaProjects.get(app);
      if (javaProjects == null) {
         synchronized (app.intern()) {
            javaProjects = new ConcurrentHashMap<String, JavaProject>();
            appToJavaProjects.put(app, javaProjects);
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
