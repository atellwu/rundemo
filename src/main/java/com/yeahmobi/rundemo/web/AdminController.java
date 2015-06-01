package com.yeahmobi.rundemo.web;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.yeahmobi.rundemo.config.Config;
import com.yeahmobi.rundemo.project.AppProject;
import com.yeahmobi.rundemo.project.ProjectContext;
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
			String subdir, String packageName, String mavenOpt,
			HttpServletRequest request, HttpServletResponse response) {

		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();

		File appDir = new File(Config.localGitDir + app + "/");
		if (appDir.exists() && appDir.list().length > 0) {
			map.put("success", false);
			map.put("errorMsg", app + "应用名称已存在，不可重复！");
		} else {
			app = app.trim();
			gitUrl = gitUrl.trim();
			branch = StringUtils.isNotBlank(branch) ? branch.trim() : "master";
			subdir = StringUtils.isNotBlank(subdir) ? subdir.trim() : "";
			packageName = StringUtils.isNotBlank(packageName) ? packageName
					.trim() : "";

			Git git = null;
			InputStream input = null;
			try {
				// (1) 用JGit下载项目代码至本地(这样就不要求系统装git)
				git = this.cloneProjectFromGit(app, gitUrl, branch);
				// （2）下载完后cd 到pom文件目录下，使用mvn命令生成classpath文件（要求本地安装maven）
				input = this.generateClasspath(app, subdir, mavenOpt, map);
				// (3) copy git_temp下的src、pom.xml和classpath至appproject
				this.operateFiles(app, subdir);
				// (4) 序列化Appproject对象至 Config.appprojectDir + app +
				// "/app.properties"文件，便于后面读取展示该package下的文件
				this.serialize(Config.appprojectDir + app + "/app.properties",
						new AppProject(app, gitUrl, branch, subdir,
								packageName, mavenOpt));

				map.put("app", app);
				map.put("success", true);
			} catch (Exception e) {
				map.put("success", false);
				map.put("errorMsg", e.getMessage());
				LOG.error(e.getMessage());
				// 创建应用发生异常时，应删除git_temp目录下的临时文件，否则下次再创建相同名称的应用时会报错：Destination
				// path
				// TODO "test" already exists and is not an empty directory
				try {
					FileUtils.deleteDirectory(new File(Config.localGitDir + app
							+ "/"));
				} catch (IOException e1) {
					LOG.error(e1.getMessage());
				}
			} finally {
				if (git != null) {
					git.close();
				}
				IOUtils.closeQuietly(input);
			}
		}
		return gson.toJson(map);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String update(String app) {

		Map<String, Object> map = new HashMap<String, Object>();
		AppProject appProject = ProjectContext.getAppProject(app);

		String gitUrl = appProject.getGitUrl();
		String branch = appProject.getBranch();
		String subdir = StringUtils.isNotBlank(appProject.getSubdir()) ? appProject
				.getSubdir() : "";
		String packageName = StringUtils
				.isNotBlank(appProject.getPackageName()) ? appProject
				.getPackageName() : "";
		String mavenOpt = StringUtils.isNotBlank(appProject.getMavenOpt()) ? appProject
				.getMavenOpt() : "";

		Git git = null;
		InputStream input = null;
		try {
			//更新前先判断pom文件是否被更改过，若没有更改过则无须mvn重新生成classpath文件
			String beforeContent = IOUtils.toString(new FileInputStream(new File(Config.localGitDir + app + "/" + Constants.POM))).trim();
			git = this.pullProjectFromGit(app, gitUrl, branch);
			String afterContent = IOUtils.toString(new FileInputStream(new File(Config.localGitDir + app + "/" + Constants.POM))).trim();
			if (!beforeContent.equals(afterContent)) {
				input = this.generateClasspath(app, subdir, mavenOpt, map);
			}
			this.operateFiles(app, subdir);
			this.serialize(Config.appprojectDir + app + "/app.properties",
					new AppProject(app, gitUrl, branch, subdir, packageName,
							mavenOpt));
			//TODO 更新后清除缓存，重新加载
			ProjectContext.removeAppProject(app);
			map.put("app", app);
			map.put("success", true);
		} catch (Exception e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
			LOG.error(e.getMessage());
		} finally {
			if (git != null) {
				git.close();
			}
			IOUtils.closeQuietly(input);
		}
		Gson gson = new Gson();
		return gson.toJson(map);
	}

	// 序列化对象到文件
	public void serialize(String fileName, Object appProject) {
		try {
			// 创建一个对象输出流，讲对象输出到文件
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(fileName));
			out.writeObject(appProject); // 序列化一个会员对象
			out.close();
		} catch (Exception x) {
			LOG.error("serialize appProject object to app.properties failed", x);
		}

	}

	private InputStream generateClasspath(String app, String subdir,
			String mavenOpt, Map<String, Object> map) throws IOException,
			InterruptedException {
		InputStream input;
		Process proc = Runtime.getRuntime().exec(
				new String[] { Config.shellDir + "ouput_classpath.sh",
						Config.localGitDir + app + "/", subdir, mavenOpt });
		input = proc.getInputStream();
		LineIterator lineIterator = IOUtils.lineIterator(new InputStreamReader(
				input));
		while (lineIterator.hasNext()) {
			String line = lineIterator.next();
			LOG.info("ouput_classpath.sh output:" + line);
		}
		int exitVal = proc.waitFor();
		if (exitVal != 0) {
			map.put("success", false);
			map.put("errorMsg",
					"mvn *** command ouput classpath failed! Please check run log!");
		}
		return input;
	}

	private Git cloneProjectFromGit(String app, String gitUrl, String branch)
			throws GitAPIException, InvalidRemoteException, TransportException {
		CloneCommand cloneCommand = Git.cloneRepository();
		cloneCommand.setDirectory(new File(Config.localGitDir + app + "/"));
		cloneCommand.setURI(gitUrl);
		cloneCommand.setBranch(branch);
		Git git = cloneCommand.call();
		return git;
	}

	// 更新应用
	private Git pullProjectFromGit(String app, String gitUrl, String branch)
			throws GitAPIException, InvalidRemoteException, TransportException,
			IOException {
		Git git = Git.open(new File(Config.localGitDir + app + "/.git"));
		PullCommand pullCommand = git.pull();
		pullCommand.call();
		return git;
	}

	private void operateFiles(String app, String subdir) throws IOException {
		if (!StringUtils.isNotBlank(subdir)) {
			subdir = "/" + subdir;
		} else {
			subdir = "";
		}
		// 为避免应用重复，copy应用时首先删除原来同名的应用
		FileUtils.deleteDirectory(new File(Config.appprojectDir + app));
		this.copyFile2AppProject(app, subdir);
		// copy完文件后，删除git_temp目录下的临时文件
		// TODO FileUtils.deleteDirectory(new File(Config.localGitDir + app +
		// "/"));
	}

	private void copyFile2AppProject(String app, String subdir)
			throws IOException {
		File file = new File(Config.localGitDir + app + subdir + "/");
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

	public static void main(String[] args) throws Exception {
		/*Git git = Git.open(new File(Config.localGitDir + "/guavaexample/.git"));
		PullCommand pullCommand = git.pull();
		PullResult pullResult = pullCommand.call();
		System.out.println(pullResult.getFetchedFrom());*/
		
		String remoteContent = getContentWithFile(Config.localGitDir + "/guavaexample/.git", "master", "pom.xml").trim();
		System.out.println(remoteContent);
		String localContent = IOUtils.toString(new FileInputStream(new File(Config.localGitDir + "/guavaexample/pom.xml"))).trim();
		if (localContent.equals(remoteContent)) {
			System.out.println("ok");
		}
	}
	
	/**
	 * 获取指定分支、指定文件的内容
	 * 
	 * @param gitRoot git仓库目录
	 * @param branchName 分支名称
	 * @param fileName 文件名称
	 * @return 返回remote文件内容
	 * @throws IOException 
	 */
	public static String getContentWithFile(String gitRoot,
			final String branchName, String fileName) throws IOException {
		final Git git = Git.open(new File(gitRoot));
		Repository repository = git.getRepository();

		repository = git.getRepository();
		RevWalk walk = new RevWalk(repository);
		Ref ref = repository.getRef(branchName);
		/*if (ref == null) {
			// 获取远程分支
			ref = repository.getRef(REF_REMOTES + branchName);
		}*/
		// 异步pull
		ExecutorService executor = Executors.newCachedThreadPool();
		FutureTask<Boolean> task = new FutureTask<Boolean>(
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						/*
						 * //创建分支 CreateBranchCommand createBranchCmd =
						 * git.branchCreate();
						 * createBranchCmd.setStartPoint(REF_REMOTES +
						 * branchName).setName(branchName).call();
						 */
						return git.pull().call().isSuccessful();
					}
				});
		executor.execute(task);

		ObjectId objId = ref.getObjectId();
		RevCommit revCommit = walk.parseCommit(objId);
		RevTree revTree = revCommit.getTree();

		TreeWalk treeWalk = TreeWalk.forPath(repository, fileName, revTree);
		// 文件名错误
		if (treeWalk == null)
			return null;

		ObjectId blobId = treeWalk.getObjectId(0);
		ObjectLoader loader = repository.open(blobId);
		byte[] bytes = loader.getBytes();
		if (bytes != null)
			return new String(bytes);
		return null;
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
