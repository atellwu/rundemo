package com.dianping.rundemo.project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaProject {
   private static final Logger LOG = LoggerFactory.getLogger(JavaProject.class);

   private final AppProject    appProject;

   private final String        pageid;

   private final String        dirPath;

   private final String        binPath;

   private final String        srcPath;

   private InputStream         runProcessInputStream;

   private boolean             isRunning;

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
      TimerTask task = new TimerTask() {
         @Override
         public void run() {
            InputStream input = null;
            try {
               Process proc = Runtime.getRuntime().exec(new String[] { "/data/rundemo/check.sh", dirPath });
               input = proc.getInputStream();
               String isRunning = IOUtils.toString(input);
               if ("false".equals(isRunning.trim())) {//已经关闭(此处不能关闭runProcessInputStream，因为还需要读完，在读完时关闭)
                  JavaProject.this.isRunning = false;
               }
            } catch (IOException e) {
               LOG.error(e.getMessage(), e);
            } finally {
               IOUtils.closeQuietly(input);
            }
         }
      };
      Timer t = new Timer();
      t.schedule(task, 0, 500);

   }

   /**
    * 创建java文件并且编译
    * 
    * @throws IOException
    */
   public String compile(String filename, String content) throws IOException {
      //创建java文件
      BufferedWriter writer = null;
      try {
         writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(srcPath + filename), "UTF-8"));
         writer.write(content);
      } finally {
         IOUtils.closeQuietly(writer);
      }
      //编译（javac -d <output_dir> -cp <classpath> xx.java）
      InputStream input = null;
      try {
         Process proc = Runtime.getRuntime().exec(
               new String[] { "/data/rundemo/compile.sh", binPath, appProject.getClasspath(), srcPath + filename,
                     filename });
         input = proc.getInputStream();
         return IOUtils.toString(input);//TODO encoding
      } finally {
         IOUtils.closeQuietly(input);
      }

   }

   /**
    * 运行java实例
    * 
    * @throws IOException
    */
   public void run(String filename) throws IOException {
      //（java -cp ${1}:${2} ${3} ）
      Process proc = Runtime.getRuntime().exec(
            new String[] { "/data/rundemo/run.sh", binPath, appProject.getClasspath(), filename, dirPath });
      if (this.runProcessInputStream != null) {
         IOUtils.closeQuietly(this.runProcessInputStream);
      }
      this.runProcessInputStream = proc.getInputStream();
      this.isRunning = true;
   }

   public void shutdown() throws IOException {
      InputStream input = null;
      try {
         Process proc = Runtime.getRuntime().exec(new String[] { "/data/rundemo/shutdown.sh", dirPath });
         input = proc.getInputStream();
         LOG.error(IOUtils.toString(input));
      } finally {
         IOUtils.closeQuietly(input);
      }
   }

   public InputStream getRunProcessInputStream() {
      return runProcessInputStream;
   }

   public AppProject getAppProject() {
      return appProject;
   }

   public String getPageid() {
      return pageid;
   }

   public boolean isRunning() {
      return isRunning;
   }

   @Override
   public String toString() {
      return "JavaProject [appProject=" + appProject + ", pageid=" + pageid + ", dirPath=" + dirPath + ", binPath="
            + binPath + ", srcPath=" + srcPath + ", isRunning=" + isRunning + "]";
   }

   public static void main(String[] args) throws IOException {
      JavaProject j = new JavaProject("app", "pageid");
      j.compile("file.java", "dasda");
   }

}
