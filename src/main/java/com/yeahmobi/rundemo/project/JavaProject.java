package com.yeahmobi.rundemo.project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeahmobi.rundemo.config.Config;

public class JavaProject {
	private static final Logger LOG = LoggerFactory
			.getLogger(JavaProject.class);

	private static final Executor executor = Executors.newCachedThreadPool();

	private final AppProject appProject;

	private final String pageid;

	private final String dirPath;

	private final String binPath;

	private final String srcPath;

	private Process proc;
	private boolean isRunning;

	public JavaProject(String app, String pageid) throws IOException {
		this(app, pageid, true);
	}

	public JavaProject(String app, String pageid, boolean initDir)
			throws IOException {
		AppProject appProject = ProjectContext.getAppProject(app);
		if (appProject == null) {
			throw new IllegalArgumentException("app project not exsit:" + app);
		}
		this.appProject = appProject;
		this.pageid = pageid;
		this.dirPath = Config.javaprojectDir + app + "/" + pageid + "/";
		this.binPath = dirPath + "bin/";
		this.srcPath = dirPath + "src/";
		if (initDir) {
			// 初始化javaprojects/{app}下的src和bin文件夹
			File binPathFileDir = new File(binPath);
			binPathFileDir.mkdirs();
			new File(srcPath).mkdirs();
			// 复制resource文件到binPath
			File srcPathDir = new File(Config.appprojectDir + app
					+ "/src/main/resources/");
			srcPathDir.mkdirs();
			FileUtils.copyDirectory(srcPathDir, binPathFileDir,
					new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							// return pathname.isFile();
							return true;// 文件夹也可以
						}
					});
		}
	}

	public String[] loadResFileNameList() {
		File binDir = new File(binPath);
		String[] fileNames = binDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return !name.endsWith(".class");
			}
		});
		Arrays.sort(fileNames);
		return fileNames;
	}

	public String loadRes(String resFileName) throws FileNotFoundException,
			IOException {
		File resFile = new File(binPath + resFileName);
		if (resFile.isFile()) {
			return IOUtils.toString(new FileInputStream(resFile), "UTF-8");
		}
		return "";
	}

	public void saveRes(String resFileName, String res)
			throws FileNotFoundException, IOException {
		File resFile = new File(binPath + resFileName);
		IOUtils.write(res, new FileOutputStream(resFile), "UTF-8");
	}

	/**
	 * 创建java文件并且编译
	 * 
	 * @throws IOException
	 */
	public String compile(String filename, String content) throws IOException {
		// 创建java文件
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(srcPath + filename), "UTF-8"));
			writer.write(content);
		} finally {
			IOUtils.closeQuietly(writer);
		}
		// 编译
		InputStream input = null;
		try {
			Process proc = Runtime.getRuntime().exec(
					new String[] { Config.shellDir + "/compile.sh", binPath,
							appProject.getClasspath(), appProject.getApp(),
							pageid, filename });
			input = proc.getInputStream();
			return IOUtils.toString(input);
		} finally {
			IOUtils.closeQuietly(input);
		}

	}

	/**
	 * 运行java实例
	 * 
	 * @throws IOException
	 */
	public void run(String filename) throws IOException {
		// 关闭当前进程
		this.shutdown();
		// （java -cp ${1}:${2} ${3} ）
		this.proc = Runtime.getRuntime().exec(
				new String[] { Config.shellDir + "/run.sh", binPath,
						appProject.getClasspath(), filename, this.pageid,
						dirPath });
		this.isRunning = true;
		// 启动监测pid进程是否关闭的线程，如果关闭，则移除pid和processInputStream
		Runnable checkRunTask = new Runnable() {
			@Override
			public void run() {
				// java终端在前台运行的方案
				try {
					proc.waitFor();
					JavaProject.this.isRunning = false;
					LOG.info("proc[app=" + appProject.getApp() + ",pageid="
							+ pageid + "] done");
				} catch (InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		};
		executor.execute(checkRunTask);
	}

	/**
	 * @deprecated proc.destroy()不可用的
	 */
	public void shutdownOld() throws IOException {
		// 执行destroy，进行关闭
		if (this.proc != null) {
			System.out.println(proc);
			this.proc.destroy();

		}
	}

	public void shutdown() throws IOException {
		// 执行kill脚本，正式关闭
		Process p = Runtime.getRuntime().exec(
				new String[] { Config.shellDir + "/shutdownByProcessName.sh",
						this.pageid });
		System.out.println("------------"
				+ IOUtils.toString(p.getInputStream()));
		// 执行destroy，进行关闭
		if (this.proc != null) {
			this.proc.destroy();
		}
		//
		JavaProject.this.isRunning = false;
	}

	public void close() throws IOException {
		// 关闭当前进程
		this.shutdown();
		// delete
		InputStream input = null;
		try {
			Process proc = Runtime.getRuntime().exec(
					new String[] { Config.shellDir + "/deleteJavaProject.sh",
							appProject.getApp(), pageid });
			input = proc.getInputStream();
			String output = IOUtils.toString(input);
			if (!StringUtils.isBlank(output)) {
				LOG.error(output);
			}
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	public InputStream getRunProcessInputStream() {
		if (this.proc != null) {
			return this.proc.getInputStream();
		}
		return null;
	}

	public OutputStream getRunProcessOutputStream() {
		if (this.proc != null) {
			return this.proc.getOutputStream();
		}
		return null;
	}

	public AppProject getAppProject() {
		return appProject;
	}

	public String getPageid() {
		return pageid;
	}

	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public String toString() {
		return "JavaProject [appProject=" + appProject + ", pageid=" + pageid
				+ ", dirPath=" + dirPath + ", binPath=" + binPath
				+ ", srcPath=" + srcPath + ", isRunning=" + isRunning + "]";
	}

	public static void main(String[] args) throws IOException {
		JavaProject j = new JavaProject("app", "pageid");
		j.compile("file.java", "dasda");
	}

}
