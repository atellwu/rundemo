package com.dianping.rundemo.project;

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

}
