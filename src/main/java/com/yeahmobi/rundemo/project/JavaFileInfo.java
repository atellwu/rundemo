package com.yeahmobi.rundemo.project;

/**
 * TODO Comment of JavaFileInfo
 * 
 * @rundemo_name demo.text
 * @rundemo_desc 长江
 */
public class JavaFileInfo {

   private String path;       //相对于"${app}/"目录的path，比如swallow/src/main/java/xx.java

   private String displayName; //在页面上的显示名称

   private String desc;       //对demo的描述

   public String getPath() {
      return path;
   }

   public void setPath(String path) {
      this.path = path;
   }

   public String getDisplayName() {
      return displayName;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public String getDesc() {
      return desc;
   }

   public void setDesc(String desc) {
      this.desc = desc;
   }

   @Override
   public String toString() {
      return "JavaFileInfo [path=" + path + ", displayName=" + displayName + ", desc=" + desc + "]";
   }

}
