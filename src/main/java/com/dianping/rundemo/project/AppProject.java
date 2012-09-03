package com.dianping.rundemo.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

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
    * @param javaFileName
    * @return
    * @throws IOException
    * @throws FileNotFoundException
    */
   public String loadCode(String javaFileName) throws FileNotFoundException, IOException {
      File javaFile = new File("/data/rundemo/appprojects/" + app + "/java/" + javaFileName);
      return IOUtils.toString(new FileInputStream(javaFile), "UTF-8");
   }
   
   public String loadPom() throws FileNotFoundException, IOException {
      File pomFile = new File("/data/rundemo/appprojects/" + app + "/pom.xml");
      return IOUtils.toString(new FileInputStream(pomFile), "UTF-8");
   }

   /**
    * 从本地load文件名列表
    * 
    * @param javaFileName
    * @return
    */
   public String[] loadJavaFileNameList() {
      File appprojectsDir = new File("/data/rundemo/appprojects/" + app + "/java/");
      String[] fileNames = appprojectsDir.list(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return !name.endsWith(".desc");
         }
      });
      Arrays.sort(fileNames);
      return fileNames;
   }

   @Override
   public String toString() {
      return "AppProject [app=" + app + ", classpath=" + classpath + "]";
   }

}
