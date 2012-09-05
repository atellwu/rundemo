package com.dianping.rundemo.project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaProject {
   private static final Logger   LOG      = LoggerFactory.getLogger(JavaProject.class);

   private static final Executor executor = Executors.newCachedThreadPool();

   private final AppProject      appProject;

   private final String          pageid;

   private final String          dirPath;

   private final String          binPath;

   private final String          srcPath;

   private InputStream           runProcessInputStream;

   private boolean               isRunning;

   public JavaProject(String app, String pageid) throws IOException {
      this(app, pageid, true);
   }

   public JavaProject(String app, String pageid, boolean initDir) throws IOException {
      AppProject appProject = ProjectContext.getAppProject(app);
      if (appProject == null) {
         throw new IllegalArgumentException("app project not exsit:" + app);
      }
      this.appProject = appProject;
      this.pageid = pageid;
      this.dirPath = "/data/rundemo/javaprojects/" + app + "/" + pageid + "/";
      this.binPath = dirPath + "bin/";
      this.srcPath = dirPath + "src/";
      if (initDir) {
         //初始化javaprojects/{app}下的src和bin文件夹
         File binPathFileDir = new File(binPath);
         binPathFileDir.mkdirs();
         new File(srcPath).mkdirs();
         //复制resource文件到binPath
         File srcPathDir = new File("/data/rundemo/appprojects/" + app + "/src/main/resources/");
         srcPathDir.mkdirs();
         FileUtils.copyDirectory(srcPathDir, binPathFileDir, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
               return pathname.isFile();
            }
         });
         //         
         //         InputStream input = null;
         //         try {
         //            Process proc = Runtime.getRuntime().exec(new String[] { "/data/rundemo/copyRes.sh", app, pageid });
         //            input = proc.getInputStream();
         //            String output = IOUtils.toString(input);
         //            if (!StringUtils.isBlank(output)) {
         //               throw new IOException(output);
         //            }
         //         } finally {
         //            IOUtils.closeQuietly(input);
         //         }
      }
   }

   public String[] loadResFileNameList() {
      File binDir = new File(binPath);
      String[] fileNames = binDir.list(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return !name.endsWith(".class");
         }
      });
      Arrays.sort(fileNames);
      return fileNames;
   }

   public String loadRes(String resFileName) throws FileNotFoundException, IOException {
      File resFile = new File(binPath + resFileName);
      return IOUtils.toString(new FileInputStream(resFile), "UTF-8");
   }

   public void saveRes(String resFileName, String res) throws FileNotFoundException, IOException {
      File resFile = new File(binPath + resFileName);
      IOUtils.write(res, new FileOutputStream(resFile), "UTF-8");
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
      //编译
      InputStream input = null;
      try {
         Process proc = Runtime.getRuntime().exec(
               new String[] { "/data/rundemo/compile.sh", binPath, appProject.getClasspath(), appProject.getApp(),
                     pageid, filename });
         input = proc.getInputStream();
         return IOUtils.toString(input);
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
      //启动监测pid进程是否关闭的线程，如果关闭，则移除pid和processInputStream
      Runnable checkTask = new Runnable() {
         @Override
         public void run() {
            try {
               Process proc = Runtime.getRuntime().exec(new String[] { "/data/rundemo/check.sh", dirPath });
               //本想使用run.sh的proc.waitFor()来阻塞并且等待返回则认为run结束，但实际run使用&，导致run的终端一下子就结束的了，proc.waitFor()不会阻塞。
               proc.waitFor();//已经关闭(此处不能关闭runProcessInputStream，因为还需要读完，在读完时关闭)
               JavaProject.this.isRunning = false;
            } catch (IOException e) {
               LOG.error(e.getMessage(), e);
            } catch (InterruptedException e) {
               LOG.error(e.getMessage(), e);
            }
         }
      };
      executor.execute(checkTask);
   }

   public void shutdown() throws IOException {
      InputStream input = null;
      try {
         Process proc = Runtime.getRuntime().exec(new String[] { "/data/rundemo/shutdown.sh", dirPath });
         input = proc.getInputStream();
         String output = IOUtils.toString(input);
         if (!StringUtils.isBlank(output)) {
            throw new IOException(output);
         }
      } finally {
         IOUtils.closeQuietly(input);
      }
   }

   public void close() throws IOException {
      //shutdown
      this.shutdown();
      //delete
      InputStream input = null;
      try {
         Process proc = Runtime.getRuntime().exec(
               new String[] { "/data/rundemo/deleteJavaProject.sh", appProject.getApp(), pageid });
         input = proc.getInputStream();
         String output = IOUtils.toString(input);
         if (!StringUtils.isBlank(output)) {
            LOG.error(output);
         }
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
