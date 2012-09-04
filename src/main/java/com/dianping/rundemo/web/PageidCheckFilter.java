package com.dianping.rundemo.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PageidCheckFilter extends HttpServlet implements Filter {
   private FilterConfig filterConfig;

   // Handle the passed-in FilterConfig
   public void init(FilterConfig filterConfig) throws ServletException {
      this.filterConfig = filterConfig;
   }

   // Process the request/response pair
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
      try {

         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpServletResponse httpResponse = (HttpServletResponse) response;
         boolean isValid = true;
         String uriStr = httpRequest.getRequestURI();
         if (uriStr.indexOf(".jsp") == -1 && uriStr.indexOf(".do") == -1) {
            isValid = true;
         } else if (uriStr.indexOf("login.jsp") == -1 && uriStr.indexOf("login.do") == -1
               && httpRequest.getSession().getAttribute("UserWraper") == null) {
            isValid = false;
         }

         if (isValid) {
            request.setCharacterEncoding("GBK");
            filterChain.doFilter(request, response);
         }

         else {
            request.setCharacterEncoding("GBK");
            PrintWriter out = httpResponse.getWriter();
            if (uriStr.indexOf("index.jsp") == -1) {
               out.write("<script>window.parent.parent.location.href='../../login.jsp'</script>");
            } else {
               out.write("<script>window.parent.parent.location.href='../login.jsp'</script>");
            }

         }

      } catch (ServletException sx) {
         filterConfig.getServletContext().log(sx.getMessage());
      } catch (IOException iox) {
         filterConfig.getServletContext().log(iox.getMessage());
      }
   }

   // Clean up resources
   public void destroy() {
   }

}
