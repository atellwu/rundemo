package com.dianping.rundemo.web;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.rundemo.project.AppProject;
import com.dianping.rundemo.project.JavaProject;
import com.dianping.rundemo.project.ProjectContext;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;

@Controller
public class AppController {
   private static final Logger LOG = LoggerFactory.getLogger(AppController.class);

   //初始化ProjectContext
   static {
      //扫描/data/rundemo/appprojects/目录，创建appprojects
      File appprojectsDir = new File("/data/rundemo/appprojects/");
      File[] appprojectDirs = appprojectsDir.listFiles();
      for (File appprojectDir : appprojectDirs) {
         String app = appprojectDir.getName();
         File[] classpathFiles = appprojectDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
               return "classpath".equals(name);
            }
         });
         try {
            File classpathFile = classpathFiles[0];
            String classpath = FileUtils.readFileToString(classpathFile, "ISO-8859-1").trim();
            AppProject appProject = new AppProject(app, classpath);
            ProjectContext.putAppProject(app, appProject);
         } catch (Exception e) {
            LOG.error("error when load from /data/rundemo/appprojects/" + app, e);
         }
      }
   }

   @RequestMapping(value = "/{app}")
   public ModelAndView java(@PathVariable String app, HttpServletRequest request, HttpServletResponse response) {
      String path = "java";
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("app", app);
      map.put("path", path);

      //打开页面时，生成pageid，生成JavaProject
      String pageid = UUID.randomUUID().toString();
      JavaProject javaProject = new JavaProject(app, pageid);
      ProjectContext.putJavaProject(app, pageid, javaProject);

      map.put("pageid", pageid);
      return new ModelAndView("app", map);
   }

   @RequestMapping(value = "/{app}/pom")
   public ModelAndView pom(@PathVariable String app, HttpServletRequest request, HttpServletResponse response) {
      String path = "pom";
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("app", app);
      map.put("path", path);
      return new ModelAndView("app", map);
   }

   @RequestMapping(value = "/{app}/res")
   public ModelAndView res(@PathVariable String app, HttpServletRequest request, HttpServletResponse response) {
      String path = "res";
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("app", app);
      map.put("path", path);
      return new ModelAndView("app", map);
   }

   @RequestMapping(value = "/{app}/compile", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
   @ResponseBody
   public Object compile(@PathVariable String app, String pageid, String content) throws JsonGenerationException,
         JsonMappingException, IOException {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         //获取JavaProject，然后编译
         JavaProject javaProject = ProjectContext.getJavaProject(app, pageid);
         String compileOutput = javaProject.compile("Demo.java", content);//TODO 先mock filename，后续应该从content中解析出来
         map.put("content", compileOutput);
         map.put("success", true);
      } catch (RuntimeException e) {
         StringBuilder error = new StringBuilder();
         error.append(e.getMessage()).append("\n");
         for (StackTraceElement element : e.getStackTrace()) {
            error.append(element.toString()).append("\n");
         }
         map.put("success", false);
         map.put("errorMsg", error.toString());
      }
      Gson gson = new Gson();
      return gson.toJson(map);

   }

   @RequestMapping(value = "/{app}/run", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
   @ResponseBody
   public Object run(@PathVariable String app, String pageid) throws JsonGenerationException, JsonMappingException,
         IOException {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         //获取JavaProject，然后编译
         JavaProject javaProject = ProjectContext.getJavaProject(app, pageid);
         javaProject.run("Demo");//TODO 先mock filename，后续应该从前端传递过来
         map.put("success", true);
      } catch (RuntimeException e) {
         StringBuilder error = new StringBuilder();
         error.append(e.getMessage()).append("\n");
         for (StackTraceElement element : e.getStackTrace()) {
            error.append(element.toString()).append("\n");
         }
         map.put("success", false);
         map.put("errorMsg", error.toString());
      }
      Gson gson = new Gson();
      return gson.toJson(map);

   }

   /**
    * js使用长polling不断调用console()。<br>
    * <br>
    * console()通过app和pageid获取对应的JavaProject对象,
    * 从JavaProject对象获取其正在运行的jvm的InputStream，<br>
    * 1 如果获取不到InputStream，说明没有正在运行的jvm，返回map.put("status", "done")，指示前端js停止轮询;<br>
    * 2 如果获取到InputStream，则尝试从InputStream读取数据:<br>
    * ---2.1 尝试available()+read() 10次，直到10次结束(注意，此处为了不阻塞，无法知道read()返回-1的情况) <br>
    * ---2.2 将读到的data(无论data是否有数据)，输出给前端<br>
    * 
    * @param app
    * @param pageid
    * @return
    * @throws JsonGenerationException
    * @throws JsonMappingException
    * @throws IOException
    */
   @RequestMapping(value = "/{app}/runConsole", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
   @ResponseBody
   public Object runConsole(@PathVariable String app, String pageid) throws JsonGenerationException,
         JsonMappingException, IOException {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         StringBuilder data = new StringBuilder();
         JavaProject javaProject = ProjectContext.getJavaProject(app, pageid);
         InputStream processInputStream = javaProject.getRunProcessInputStream();
         boolean isRunning = javaProject.isRunning();
         if (processInputStream == null) {
            map.put("status", "done");
         } else {
            if (!isRunning) {//如果已经不在运行，则读完直到-1
               data.append(IOUtils.toString(processInputStream));//TODO encoding
               map.put("status", "done");
            } else {//如果已经还在运行，则尝试读一部分
               int count = 0;
               int available;
               while (count++ < 10) {
                  available = processInputStream.available();
                  if (available > 0) {
                     byte[] bytes = new byte[available];
                     processInputStream.read(bytes);
                     data.append(new String(bytes));//TODO add encode
                  } else {
                     try {
                        Thread.sleep(200);
                     } catch (InterruptedException e) {
                        break;
                     }
                  }
               }
               map.put("status", "continue");
            }
         }
         map.put("content", data.toString());
         map.put("success", true);

      } catch (RuntimeException e) {
         StringBuilder error = new StringBuilder();
         error.append(e.getMessage()).append("\n");
         for (StackTraceElement element : e.getStackTrace()) {
            error.append(element.toString()).append("\n");
         }
         map.put("success", false);
         map.put("errorMsg", error.toString());
      }
      Gson gson = new Gson();
      return gson.toJson(map);

   }

   @RequestMapping(value = "/{app}/shutdown", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
   @ResponseBody
   public Object shutdown(@PathVariable String app, String pageid) throws JsonGenerationException,
         JsonMappingException, IOException {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         //获取JavaProject，然后编译
         JavaProject javaProject = ProjectContext.getJavaProject(app, pageid);
         javaProject.shutdown();
         map.put("success", true);
      } catch (RuntimeException e) {
         StringBuilder error = new StringBuilder();
         error.append(e.getMessage()).append("\n");
         for (StackTraceElement element : e.getStackTrace()) {
            error.append(element.toString()).append("\n");
         }
         map.put("success", false);
         map.put("errorMsg", error.toString());
      }
      Gson gson = new Gson();
      return gson.toJson(map);

   }

   public static void main(String[] args) throws IOException {
      System.out.println(UUID.randomUUID().toString());
   }

   static void print(byte[] bytes) {
      for (byte b : bytes) {
         System.out.println("--" + b);
      }
   }
}
