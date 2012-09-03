package com.dianping.rundemo.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dianping.rundemo.project.JavaCodeInfo;

public class CodeUtils {

   private final static int     NONE      = -1;
   private static final Pattern PACKNAME  = Pattern
                                                .compile("^package\\s+([a-zA-Z_][a-zA-Z_0-9]*([\\.][a-zA-Z_][a-zA-Z_0-9]*)*);.*$");
   private static final Pattern CLASSNAME = Pattern.compile("^public\\s+class\\s+([a-zA-Z_][a-zA-Z_0-9]*)+\\s+.*$");

   public static JavaCodeInfo getCodeInfo(String code) {
      JavaCodeInfo javaCodeInfo = new JavaCodeInfo();
      //packageName
      boolean waitingStart = true;
      int lineStart = NONE, lineEnd = NONE;
      for (int i = 0; i < code.length(); i++) {
         char c = code.charAt(i);
         if (waitingStart && c != '\r' && c != '\n') {//找到行首
            lineStart = i;
            waitingStart = false;
         } else if (!waitingStart && (c == '\r' || c == '\n')) {//找到行尾
            lineEnd = i;
            waitingStart = true;
         }
         if (lineStart != NONE && lineEnd != NONE) {//找到一行
            String line = code.substring(lineStart, lineEnd);
            lineStart = NONE;
            lineEnd = NONE;
            line = line.trim();
            Matcher packegeMatch = PACKNAME.matcher(line);
            if (packegeMatch.matches()) {
               String packageName = packegeMatch.group(1);
               javaCodeInfo.setPackageName(packageName);
            }
            Matcher classNameMatch = CLASSNAME.matcher(line);
            if (classNameMatch.matches()) {
               String className = classNameMatch.group(1);
               javaCodeInfo.setClassName(className);
            }
         }
         if (javaCodeInfo.getClassName() != null && javaCodeInfo.getPackageName() != null) {//都找到了
            return javaCodeInfo;
         }
      }
      //找不到className，报错
      if (javaCodeInfo.getClassName() == null) {
         String errorMsg = "code error!";
         errorMsg += "\r\n\tcorrect public class name definition not find - this line must match: ^public\\s+class\\s+([a-zA-Z_][a-zA-Z_0-9]*)+\\s+.*$";
         throw new IllegalArgumentException(errorMsg);
      }
      //找不到packageName，设置为空字符串
      if (javaCodeInfo.getPackageName() == null) {
         javaCodeInfo.setPackageName("");
      }
      return javaCodeInfo;
   }

   public static void main(String[] args) {
      Matcher m2 = PACKNAME.matcher("package ass_.com;");
      System.out.println(m2.matches());
      System.out.println(m2.group(1));

      Matcher m = CLASSNAME.matcher("public class s ");
      System.out.println(m.matches());
      System.out.println(m.group(1));

      System.out
            .println(CodeUtils
                  .getCodeInfo("package com.dianping.rundemo.web;\n  public class Demo { \n    /**     * @param args     * @throws InterruptedException      */    public static void main(String[] args) throws InterruptedException {       int count = 0;       while (count++ < 50) {          System.out.println(count);          Thread.sleep(60);          System.out.println(\"--55555-----------\");       }    }  }"));
   }
}
