package com.dianping.rundemo.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class JavaProject {

   private final AppProject appProject;

   private final String     pageid;

   private final String     dirPath;

   private final String     binPath;

   private final String     srcPath;

   private long             pid;

   private InputStream      compileProcessInputStream;

   private InputStream      runProcessInputStream;

   public JavaProject(String app, String pageid) {
      AppProject appProject = ProjectContext.getAppProject(app);
      if (appProject == null) {
         throw new IllegalArgumentException("app project not exsit:" + app);
      }
      this.appProject = appProject;
      this.pageid = pageid;
      this.dirPath = "/data/rundemo/javaprojects/" + app + "/" + pageid + "/";
      this.binPath = dirPath + "bin/";
      this.srcPath = dirPath + "src/";
      new File(binPath).mkdirs();
      new File(srcPath).mkdirs();

      //启动监测pid进程是否关闭的线程，如果关闭，则移除pid和processInputStream
   }

   /**
    * 创建java文件并且编译
    * 
    * @throws IOException
    */
   public void compile(String filename, String content) throws IOException {
      //创建java文件
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(srcPath + filename),
            "UTF-8"));
      writer.write(content);
      writer.close();
      //编译（javac -d <output_dir> -cp <classpath> xx.java）
      Process proc = Runtime.getRuntime()
            .exec(new String[] { "/data/rundemo/compile.sh", binPath, appProject.getClasspath(), srcPath + filename,
                  filename });
      this.compileProcessInputStream = proc.getInputStream();

      //debug信息
      //      BufferedReader stdInput = new BufferedReader(new InputStreamReader(compileProcessInputStream));
      //      System.out.println("Here is the standard output of the command:\n");
      //      String s;
      //      while ((s = stdInput.readLine()) != null) {
      //         System.out.println(s);
      //      }
   }

   /**
    * 运行java实例
    * 
    * @throws IOException
    */
   public void run(String filename) throws IOException {
      //编译（java -cp ${1}:${2} ${3} ）
      Process proc = Runtime.getRuntime().exec(
            new String[] { "/data/rundemo/run.sh", binPath, appProject.getClasspath(), filename });
      this.runProcessInputStream = proc.getInputStream();
   }

   public InputStream getCompileProcessInputStream() {
      return compileProcessInputStream;
   }

   public InputStream getRunProcessInputStream() {
      return runProcessInputStream;
   }

   public long getPid() {
      return pid;
   }

   public void setPid(long pid) {
      this.pid = pid;
   }

   public AppProject getAppProject() {
      return appProject;
   }

   public String getPageid() {
      return pageid;
   }

   public static void main(String[] args) throws IOException {
      JavaProject j = new JavaProject("app", "pageid");
      j.compile("file.java", "dasda");
   }
}
