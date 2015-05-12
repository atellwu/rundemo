package com.yeahmobi.rundemo.utils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.yeahmobi.rundemo.config.Config;
import com.yeahmobi.rundemo.project.JavaCodeInfo;
import com.yeahmobi.rundemo.project.JavaFileInfo;

/**
 * TODO Comment of CodeUtils
 * 
 * @author wukezhu
 * @rundemo_name 代码解析工具的例子
 */
public class CodeUtils {

    private final static int NONE = -1;
    private static final Pattern PACKNAME = Pattern
            .compile("^package\\s+([a-zA-Z_][a-zA-Z_0-9]*([\\.][a-zA-Z_][a-zA-Z_0-9]*)*);.*$");
    private static final Pattern CLASSNAME = Pattern.compile("^public\\s+(class|enum)\\s+([a-zA-Z_][a-zA-Z_0-9]*)+\\s+.*$");

    private static final Pattern RUNDEMO_NAME = Pattern.compile("^\\s*\\*\\s*@rundemo_name\\s+(.*)$");
    private static final Pattern RUNDEMO_DESC = Pattern.compile("^\\s*\\*\\s*@rundemo_desc\\s+(.*)$");

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
                    String className = classNameMatch.group(2);
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

    public static JavaFileInfo getJavaFileInfo(File file, String app) throws IOException {
        JavaFileInfo javaFileInfo = new JavaFileInfo();
        //path
        String path = file.getPath().replace(Config.appprojectDir + app + "/", "");
        javaFileInfo.setPath(path);
        //name,desc
        String code = FileUtils.readFileToString(file, "UTF-8");
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
                Matcher nameMatch = RUNDEMO_NAME.matcher(line);
                if (nameMatch.matches()) {
                    String displayName = nameMatch.group(1);
                    javaFileInfo.setDisplayName(displayName);
                }
                Matcher descMatch = RUNDEMO_DESC.matcher(line);
                if (descMatch.matches()) {
                    String desc = descMatch.group(1);
                    javaFileInfo.setDesc(desc);
                }
            }
            if (javaFileInfo.getDisplayName() != null && javaFileInfo.getDesc() != null) {//都找到了
                return javaFileInfo;
            }
        }
        //找不到name，使用文件名
        if (javaFileInfo.getDisplayName() == null) {
            javaFileInfo.setDisplayName(file.getName());
        }
        return javaFileInfo;
    }

    public static void main(String[] args) throws IOException {
        //
        Matcher m2 = PACKNAME.matcher("package ass_.com;");
        System.out.println(m2.matches());
        System.out.println(m2.group(1));

        Matcher m = CLASSNAME.matcher("public class s ");
        System.out.println(m.matches());
        System.out.println(m.group(2));
        
        m = CLASSNAME.matcher("public enum s ");
        System.out.println(m.matches());
        System.out.println(m.group(2));

        System.out
                .println(CodeUtils
                        .getCodeInfo("package com.yeahmobi.rundemo.web;\n  public class Demo { \n    /**     * @param args     * @throws InterruptedException      */    public static void main(String[] args) throws InterruptedException {       int count = 0;       while (count++ < 50) {          System.out.println(count);          Thread.sleep(60);          System.out.println(\"--55555-----------\");       }    }  }"));

        //
        //      Pattern DEMO_DOC = Pattern.compile("^\\s*\\*\\s*@demo\\s+(.*)$");
        //      Matcher mm = DEMO_DOC.matcher(" * @demo: daiplayName");
        //      System.out.println(mm.matches());
        //      System.out.println(mm.group(1));
        //
        //      Matcher mm = DEMO_DOC.matcher(" * @demo_desc daiplayName");
        //      System.out.println(mm.matches());
        //      System.out.println(mm.group(1));
        System.out.println(CodeUtils.getJavaFileInfo(new File(Config.appprojectDir
                + "swallow-example/src/main/java/com/yeahmobi/swallow/example/consumer/DurableConsumerExample.java"),
                "swallow-example"));
    }
}
