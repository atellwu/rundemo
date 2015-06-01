package com.yeahmobi.rundemo.web;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

public class Test {

	// 序列化对象到文件
	public static void serialize(String fileName) {
		try {
			// 创建一个对象输出流，讲对象输出到文件
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(fileName));
			UserInfo user = new UserInfo("renyanwei", "888888", 20);
			out.writeObject(user); // 序列化一个会员对象
			out.close();
		} catch (Exception x) {
			System.out.println(x.toString());
		}

	}

	// 从文件反序列化到对象
	public static void deserialize(String fileName) {
		try {
			// 创建一个对象输入流，从文件读取对象
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					fileName));

			// 注意读对象时必须按照序列化对象顺序读，否则会出错
			// 读取字符串
			String today = (String) (in.readObject());
			System.out.println(today);

			// 读取日期对象
			Date date = (Date) (in.readObject());
			System.out.println(date.toString());

			// 读取UserInfo对象并调用它的toString()方法
			UserInfo user = (UserInfo) (in.readObject());
			System.out.println(user.toString());

			in.close();
		} catch (Exception x) {
			System.out.println(x.toString());
		}

	}

	public static void main(String[] args) {
		serialize("/home/test.txt");
		System.out.println("序列化完毕");

		deserialize("/home/test.txt");
		System.out.println("反序列化完毕");
	}

}

// 一定要实现Serializable接口才能被序列化
class UserInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	public String userName;
	public String userPass;
	// 注意，userAge变量前面的transient
	public transient int userAge;

	public UserInfo() {
	}

	public UserInfo(String username, String userpass, int userage) {
		this.userName = username;
		this.userPass = userpass;
		this.userAge = userage;
	}

	public String toString() {
		return "用户名: " + this.userName + ";密码：" + this.userPass + ";年龄："
				+ this.userAge;
	}
}
