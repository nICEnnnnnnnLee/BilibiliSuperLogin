B站账户名密码登录(java+selenium)

## 描述  
通过滑动块验证码的反爬，以账户名密码方式登录B站，获取Cookies

## 前置需求
+ 安装Chrome/FireFox浏览器(这一步通常可以省略,谁还没个浏览器呢...)
+ 下载浏览器对应的webdriver，并设置相关环境变量
+ 各大浏览器webdriver地址可参见：<https://docs.seleniumhq.org/download/>
    + Firefox：<https://github.com/mozilla/geckodriver/releases/>
    + Chrome：<https://sites.google.com/a/chromium.org/chromedriver/> 或者  
    <http://chromedriver.storage.googleapis.com/index.html>

## Demo
```
public class App {
	public static void main(String[] args) {
		BiliLogin login = new BiliLogin("userName", "passWord", "firefox");
		//BiliLogin login = new BiliLogin("userName", "passWord", "chrome");
		login.loginUtilSuccess();
		System.out.println(login.getCookie());
		login.quit();
	}
}
```

## 其它
Python版[传送门](https://github.com/nICEnnnnnnnLee/bilibiliLogin),原理自<https://github.com/LgHuan/bilibili->