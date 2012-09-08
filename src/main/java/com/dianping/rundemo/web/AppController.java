package com.dianping.rundemo.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.rundemo.project.AppProject;
import com.dianping.rundemo.project.JavaCodeInfo;
import com.dianping.rundemo.project.JavaFileInfo;
import com.dianping.rundemo.project.JavaProject;
import com.dianping.rundemo.project.ProjectContext;
import com.dianping.rundemo.utils.CodeUtils;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;

@Controller
public class AppController {
   @SuppressWarnings("unused")
   private static final Logger LOG = LoggerFactory.getLogger(AppController.class);

   @RequestMapping(value = "/")
   public ModelAndView allApps(HttpServletRequest request, HttpServletResponse response) {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("allAppNames", ProjectContext.getAllAppNames());
      return new ModelAndView("allApps", map);
   }

   @RequestMapping(value = "/{app}/loadCode", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
   @ResponseBody
   public Object loadJavaCode(@PathVariable String app, String javaFilePath) {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         AppProject appProject = ProjectContext.getAppProject(app);
         String code = appProject.loadCode(javaFilePath);
         map.put("code", code);
         map.put("success", true);
      } catch (Exception e) {
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

   @RequestMapping(value = "/{app}/loadRes", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
   @ResponseBody
   public Object loadRes(@PathVariable String app, String pageid, String resFileName) {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         JavaProject javaProject = ProjectContext.getJavaProject(app, pageid);
         String res = javaProject.loadRes(resFileName);
         map.put("res", res);
         map.put("success", true);
      } catch (Exception e) {
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

   @RequestMapping(value = "/{app}/saveRes", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
   @ResponseBody
   public Object saveRes(@PathVariable String app, String pageid, String resFileName, String res) {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         JavaProject javaProject = ProjectContext.getJavaProject(app, pageid);
         javaProject.saveRes(resFileName, res);
         map.put("res", res);
         map.put("success", true);
      } catch (Exception e) {
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

   @RequestMapping(value = "/{app}")
   public ModelAndView appIndex(@PathVariable String app, HttpServletRequest request, HttpServletResponse response)
         throws FileNotFoundException, IOException {
      Map<String, Object> map = new HashMap<String, Object>();

      //打开页面时，生成pageid，生成JavaProject
      String pageid = UUID.randomUUID().toString();
      JavaProject javaProject = new JavaProject(app, pageid);
      ProjectContext.putJavaProject(app, pageid, javaProject);

      AppProject appProject = ProjectContext.getAppProject(app);
      List<JavaFileInfo> javaFileInfoList = appProject.loadJavaFileNameList();
      String[] resFileNameList = javaProject.loadResFileNameList();
      String pom = appProject.loadPom();
      map.put("pom", (pom));
      map.put("javaFileInfoList", javaFileInfoList);
      map.put("resFileNameList", resFileNameList);
      map.put("app", app);
      map.put("allAppNames", ProjectContext.getAllAppNames());
      map.put("pageid", pageid);
      return new ModelAndView("app", map);
   }

   @RequestMapping(value = "/{app}/compile", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
   @ResponseBody
   public Object compile(@PathVariable String app, String pageid, String content) {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         //获取JavaProject，然后编译
         JavaProject javaProject = ProjectContext.getJavaProject(app, pageid);
         JavaCodeInfo javaCodeInfo = CodeUtils.getCodeInfo(content);
         String compileOutput = javaProject.compile(javaCodeInfo.getClassName() + ".java", content);
         map.put("content", compileOutput);
         String className;
         if (StringUtils.isBlank(javaCodeInfo.getPackageName())) {
            className = javaCodeInfo.getClassName();
         } else {
            className = javaCodeInfo.getPackageName() + '.' + javaCodeInfo.getClassName();
         }
         map.put("className", className);
         map.put("success", true);
      } catch (Exception e) {
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
    * @param app
    * @param pageid
    * @param className 指定要运行的完整的类名
    * @return
    * @throws JsonGenerationException
    * @throws JsonMappingException
    * @throws IOException
    */
   @RequestMapping(value = "/{app}/run", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
   @ResponseBody
   public Object run(@PathVariable String app, String pageid, String className) {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         //获取JavaProject，然后编译
         JavaProject javaProject = ProjectContext.getJavaProject(app, pageid);
         javaProject.run(className);
         map.put("success", true);
      } catch (Exception e) {
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
   public Object runConsole(@PathVariable String app, String pageid) {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         StringBuilder data = new StringBuilder();
         JavaProject javaProject = ProjectContext.getJavaProject(app, pageid);
         InputStream processInputStream = javaProject.getRunProcessInputStream();
         String status = "continue";
         if (processInputStream == null) {
            status = "done";
         } else {
            int count = 0;
            int available;
            try {
               while (count++ < 300) {
                  if (!javaProject.isRunning()) {//如果已经不在运行，则停止
                     data.append(IOUtils.toString(processInputStream));
                     status = "done";
                     javaProject.shutdown();
                     break;
                  } else {//如果已经还在运行，则尝试读一部分
                     available = processInputStream.available();
                     if (available > 0) {
                        byte[] bytes = new byte[available];
                        processInputStream.read(bytes);
                        data.append(new String(bytes));
                        break;
                     } else {//如果一直没有数据，最多会等待30s
                        try {
                           Thread.sleep(100);
                        } catch (InterruptedException e) {
                           status = "done";
                           javaProject.shutdown();
                           break;
                        }
                     }
                  }
               }
            } catch (IOException e) {//在任何异常时停止进程(如果已经不在运行，也会抛异常)
               status = "done";
               javaProject.shutdown();
            }

         }
         map.put("status", status);
         map.put("content", data.toString());
         map.put("success", true);

      } catch (Exception e) {
         StringBuilder error = new StringBuilder();
         error.append(e.getMessage()).append("\n");
         for (StackTraceElement element : e.getStackTrace()) {
            error.append(element.toString()).append("\n");
         }
         map.put("success", false);
         map.put("errorMsg", (error.toString()));
      }
      Gson gson = new Gson();
      return gson.toJson(map);

   }

   @RequestMapping(value = "/{app}/input", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
   @ResponseBody
   public Object input(@PathVariable String app, String pageid, String input) {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         //获取JavaProject，然后编译
         JavaProject javaProject = ProjectContext.getJavaProject(app, pageid);
         OutputStream output = javaProject.getRunProcessOutputStream();
         if (output != null) {
            IOUtils.write(input + IOUtils.LINE_SEPARATOR_UNIX, output, "UTF-8");
            output.flush();
         }
         map.put("success", true);
      } catch (IOException e) {
         map.put("success", true);
      } catch (Exception e) {
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
   public Object shutdown(@PathVariable String app, String pageid) {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         //获取JavaProject，然后编译
         JavaProject javaProject = ProjectContext.getJavaProject(app, pageid);
         javaProject.shutdown();
         map.put("success", true);
      } catch (Exception e) {
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

   @RequestMapping(value = "/{app}/deleteJavaProject", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
   @ResponseBody
   public Object deleteJavaProject(@PathVariable String app, String pageid) {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
         //获取JavaProject，然后编译
         JavaProject javaProject = ProjectContext.getJavaProject(app, pageid);
         javaProject.close();
         map.put("success", true);
      } catch (Exception e) {
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
      System.out.println("<p>中文");
   }

   static void print(byte[] bytes) {
      for (byte b : bytes) {
         System.out.println("--" + b);
      }
   }
}
