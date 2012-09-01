package com.dianping.rundemo.project;


public class JavaCodeInfo {
   private String               packageName;
   private String               className;

   public String getPackageName() {
      return packageName;
   }

   public void setPackageName(String packageName) {
      this.packageName = packageName;
   }

   public String getClassName() {
      return className;
   }

   public void setClassName(String className) {
      this.className = className;
   }

   @Override
   public String toString() {
      return "JavaCodeInfo [packageName=" + packageName + ", className=" + className + "]";
   }

}
