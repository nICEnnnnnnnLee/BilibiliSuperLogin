package top.nicelee;

import top.nicelee.login.bilibili.BiliLogin;

public class App {
	public static void main(String[] args) {
		BiliLogin login = new BiliLogin("userName", "passWord", "firefox");
		//BiliLogin login = new BiliLogin("userName", "passWord", "chrome");
		login.loginUtilSuccess();
		System.out.println(login.getCookie());
		login.quit();
	}
}
