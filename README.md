##rundemo是什么
rundemo是一个基于GitHub和Maven的在线demo演示平台，您可将自己的demo项目(github) 添加到rundemo平台上。

rundemo为你的demo提供在线运行的功能：可以在rundemo上查看应用的demo的同时，执行这些demo，看到运行结果。同时还可以在界面上直接修改代码，并且可以马上看到修改后的运行效果。

###部署和运行
1.rundemo只能运行在linux环境
2.下载rundemo源码,mvn clean package之后将生成war包
3.修改war中的config/config.properties的demoDir配置项，默认值是/data/rundemo/，这是rundemo用于存放项目和运行时临时文件的目录，请确保你配置的目录存在并且有可读写权限；
4.将war包放到容器中，运行容器，访问首页即可。

###rundemo功能使用说明
**1) 创建应用**：为您的demo起一个应用名称(是唯一的)，输入所在GitHub的仓库地址，包括一些可选项，如分支branch(默认为master分支)、pom文件所在目录(默认在主目录下，不在主目录下的需要填入相对路径)、需要显示的package(您可以选择显示指定包下的应用代码文件，默认显示/src/main/java/下所有包文件)以及maven的一些命令参数；例如有个guavaexample的项目在github的地址为：https://github.com/Corsair007/guavaexample.git，分支为master，pom文件在主目录下，需要显示com/yeahmobi/rundemo/guavaexample包下的代码文件。

 ![image](https://github.com/atellwu/rundemo/raw/develop/webapp/images/readme/1.png)

**2) 浏览代码**：代码文件以Annotation(@rundemo_name)后的功能描述命名并按树形列表层级显示，选中某个文件，右边Code View编辑框内就会显示对应代码。

![image](https://github.com/atellwu/rundemo/raw/develop/webapp/images/readme/2.png)


**3) demo运行**：代码显示后可以在线修改，并可运行，Run Result将实时显示控制台。

![image](https://github.com/atellwu/rundemo/raw/develop/webapp/images/readme/3.png)
