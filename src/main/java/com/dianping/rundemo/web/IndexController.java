package com.dianping.rundemo.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;

@Controller
public class IndexController {

   @RequestMapping(value = "/")
   public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
      return new ModelAndView("index");
   }

   @RequestMapping(value = "/producer/sendMsg", method = RequestMethod.GET, produces = "application/javascript; charset=utf-8")
   @ResponseBody
   public Object sendMsg(String topic, String content, String callback) throws JsonGenerationException,
         JsonMappingException, IOException {
      Map<String, Object> map = new HashMap<String, Object>();
      try {
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
      return callback + "(" + gson.toJson(map) + ");";

   }

}
