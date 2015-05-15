package com.yeahmobi.rundemo.web;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.yeahmobi.rundemo.config.Config;
import com.yeahmobi.rundemo.utils.Constants;

@Controller
public class AdminController {
	private static final Logger LOG = LoggerFactory
			.getLogger(AdminController.class);

	@PostConstruct
	public void init() {
		try {
			Config.copyShellFile();
		} catch (IOException e) {
			LOG.info("copy shell files to target directory failed:"
					+ e.getMessage());
		} catch (InterruptedException e) {
			LOG.info("give shell files permission failed:" + e.getMessage());
		}
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String create(String app, String gitUrl, String branch,
			String subdir, String packageName,String mavenOpt, HttpServletRequest request,
			HttpServletResponse response) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		// (1)调用shell，下载git到本地，并且求出classpath
		if (StringUtils.isBlank(branch)) {
			branch = "master";
		}
		Git git = null;
		InputStream input = null;
		try {
			//（1）用JGit下载项目代码至本地(这样就不要求系统装git)
			git = this.cloneProjectFromGit(app, gitUrl, branch);
			//（2）下载完后cd 到pom文件目录下，使用mvn命令生成classpath文件（要求本地安装maven）
			input = this.generateClasspath(app, subdir, mavenOpt, map);
			//(3) copy git_temp下的src、pom.xml和classpath至appproject
			this.operateFiles(app, subdir);
			// (4) 持久化packageName至	Config.appprojectDir + app + "/app.properties"文件，便于后面读取展示该package下的文件
			Map<String, String> propertiesMap = new HashMap<String, String>();
			//TODO 以后可能还有其他属性值
			propertiesMap.put("packageName", packageName);
			this.persistentProperties2File(app, propertiesMap);
			
			map.put("app", app);
			map.put("success", true);
		} catch (Exception e) {
			map.put("success", false);
			map.put("errorMsg", e.toString());
		   LOG.error(e.getMessage());
		} finally {
			if(git!=null){
				git.close();
			}
			IOUtils.closeQuietly(input);
		}
		Gson gson = new Gson();
		return gson.toJson(map);
	}

	private InputStream generateClasspath(String app, String subdir,
			String mavenOpt, Map<String, Object> map) throws IOException,
			InterruptedException {
		InputStream input;
		Process proc = Runtime.getRuntime().exec(
				new String[] { Config.shellDir + "ouput_classpath.sh", Config.gitTempDir + app 	+ "/",
						subdir, mavenOpt });
		input = proc.getInputStream();
		LineIterator lineIterator = IOUtils
				.lineIterator(new InputStreamReader(input));
		while (lineIterator.hasNext()) {
			String line = lineIterator.next();
			LOG.info("ouput_classpath.sh output:" + line);
		}
		int exitVal = proc.waitFor();
		if(exitVal!=0){
			map.put("success", false);
			map.put("errorMsg", "mvn *** command ouput classpath failed! Please check run log!");
		}
		return input;
	}

	private Git cloneProjectFromGit(String app, String gitUrl, String branch)
			throws GitAPIException, InvalidRemoteException, TransportException {
		Git git;
		CloneCommand cloneCommand = Git.cloneRepository();
		cloneCommand.setDirectory(new File(Config.gitTempDir + app 	+ "/"));
		cloneCommand.setURI(gitUrl);
		cloneCommand.setBranch(branch);
		git = cloneCommand.call();
		return git;
	}

	private void operateFiles(String app, String subdir) throws IOException {
		if (!StringUtils.isBlank(subdir)) {
			subdir = "/" + subdir;
		} else {
			subdir = "";
		}
		//为避免应用重复，创建新应用时首先删除原来同名的应用
		FileUtils.deleteDirectory(new File(Config.appprojectDir + app));
		this.copyFile2AppProject(app, subdir);
		//copy完文件后，删除git_temp目录下的临时文件
		FileUtils.deleteDirectory(new File(Config.gitTempDir + app + "/"));
	}

	private void copyFile2AppProject(String app, String subdir)
			throws IOException {
		File file = new File(Config.gitTempDir + app + subdir + "/");
		File[] files = file.listFiles(new CopyFileFilter());
		for (File f : files) {
			File newFile = new File(Config.appprojectDir + app + "/"
					+ f.getName());
			if (f.isFile()) {
				FileUtils.copyFile(f, newFile);
			} else {
				FileUtils.copyDirectory(f, newFile);
			}
		}
	}
	
	private void persistentProperties2File(String app, Map<String, String> propertiesMap) throws IOException{
		FileWriter writer = null;
		File appPropertiesFile = new File(Config.appprojectDir + app + "/app.properties");
		try {
			writer = new FileWriter(appPropertiesFile);
			Set<Map.Entry<String, String>> entrySet = propertiesMap.entrySet();
			Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, String> entry = iterator.next();
				if(StringUtils.isNotBlank(entry.getValue())){
					writer.write(entry.toString());
				}
			}
		} catch (IOException e) {
		}finally{
			if(writer!=null){
				writer.flush();
			}
			IOUtils.closeQuietly(writer);
		}
	}
	
	public static void main(String[] args) {
		Map<String, String> propertiesMap = new HashMap<String, String>();
		propertiesMap.put("packagename", "okkkkkk");
		propertiesMap.put("packagename1", "okkkkkk2");
		
		Set<Map.Entry<String, String>> entrySet = propertiesMap.entrySet();
		Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			if(StringUtils.isNotBlank(entry.getValue())){
				System.out.println(entry.toString());
			}
		}
	}
	
}

class CopyFileFilter implements FileFilter {
	public boolean accept(File file) {
		if (Constants.SRC.equals(file.getName())
				|| Constants.POM.equals(file.getName())
				|| Constants.CLASSPATH.equals(file.getName())) {
			return true;
		}
		return false;
	}
}
