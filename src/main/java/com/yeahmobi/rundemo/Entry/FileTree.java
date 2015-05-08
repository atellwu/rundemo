package com.yeahmobi.rundemo.Entry;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class FileTree {
	public String name;
	public String type;
	public AdditionalParameters additionalParameters;
	
	public FileTree(String name, String type) {
		this.name = name;
		this.type = type;
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AdditionalParameters getAdditionalParameters() {
		return additionalParameters;
	}

	public void setAdditionalParameters(AdditionalParameters additionalParameters) {
		this.additionalParameters = additionalParameters;
	}

	public String toJsonString(){
		return JSONObject.toJSONString(this);
	}

	public static String getJsonFileList(File file) {
		List<FileTree> list = spliceJsonData(file.listFiles(), 0);
		JSONObject object = new JSONObject();
		for(FileTree fileTree: list){
			object.put(fileTree.name, fileTree);
		}
		return object.toJSONString();
	}
	
	private static List<FileTree> spliceJsonData(File[] files,int index){
		List<FileTree> mainlist = new ArrayList<FileTree>();
		for(File file : files){
			FileTree fileTree = null;
			if (file.isDirectory()) {
				fileTree = new FileTree(file.getName(), "folder");
				List<FileTree> list = spliceJsonData(file.listFiles(), (index++)*100);
				AdditionalParameters additionalParameters = new AdditionalParameters(list);
				fileTree.setAdditionalParameters(additionalParameters);
			}else {
				String name = "<a href=javascript:rundemo_app.changeJavaHash("+(index++)+"); filePath="+file.getAbsolutePath()+">"+file.getName()+"</a>";
				fileTree = new FileTree(name, "item");
			}
			mainlist.add(fileTree);
		}
		return mainlist;
	}
	
	public static void main(String[] args) {
		File file = new File("D:\\data\\rundemo\\appprojects\\test_abel\\src\\main\\java");
		System.out.println(getJsonFileList(file));
	}
}