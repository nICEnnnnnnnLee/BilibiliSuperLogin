package top.nicelee.login.bilibili;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;

public class BiliLogin {

	private String userName;
	private String pwd;
	private String explorer;
	private WebDriver driver;
	private JavascriptExecutor driver_js;
	private int failCount = 0;
	private int maxfailCount = 5;
	private int threshold = 10;

	private static ColorModel colorModel = ColorModel.getRGBdefault();
	
	public BiliLogin(String userName, String pwd) {
		this.userName = userName;
		this.pwd = pwd;
		this.explorer = "firefox";
	}

	public BiliLogin(String userName, String pwd, String explorer) {
		this.userName = userName;
		this.pwd = pwd;
		this.explorer = explorer;
	}

	public void init() {
		failCount = 0;
		driver = null;
	}

	public void quit() {
		driver.quit();
	}

	private void initDriver() {
		if (driver == null) {
			if(explorer.equalsIgnoreCase("firefox")) {
				FirefoxOptions options = new FirefoxOptions();
				options.addArguments("--headless");
				options.addArguments("--disable-gpu");
				driver = new FirefoxDriver(options);
				driver_js = ((JavascriptExecutor) driver);
			}else {
				ChromeOptions options = new ChromeOptions();
				options.addArguments("--headless");
				options.addArguments("--disable-gpu");
				driver = new ChromeDriver(options);
				driver_js = ((JavascriptExecutor) driver);
			}
		}
	}
	public String getCookie() {
		Iterator<Cookie> iter = driver.manage().getCookies().iterator();
		StringBuilder sb =new StringBuilder();
		while(iter.hasNext()) {
			Cookie cookie = iter.next();
			sb.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
		}
		return sb.toString();
	}

	public boolean loginUtilSuccess() {
		initDriver();
		driver.get("https://passport.bilibili.com/login");

		WebElement username = driver.findElement(By.id("login-username"));
		username.sendKeys(this.userName);
		WebElement password = driver.findElement(By.id("login-passwd"));
		password.clear();
		password.sendKeys(this.pwd);

		WebElement loginBtn = driver.findElement(By.cssSelector("a.btn.btn-login"));
		loginBtn.click();

		sleep(3000);
		try {
			loginBtn.click();
			sleep(3000);
		} catch (Exception e) {
		}
		System.out.println("账号密码输入成功");

		// 无阴影图片数据
		String js = "return document.getElementsByClassName('geetest_canvas_fullbg')[0].toDataURL('image/png');";
		String data = (String) driver_js.executeScript(js);
		// Base64Util.String2File(data.split(",")[1], "tmp/c_image.png");
		BufferedImage c_image = null;
		try {
			c_image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(data.split(",")[1])));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 有阴影图片数据
		js = "return document.getElementsByClassName('geetest_canvas_bg')[0].toDataURL('image/png');";
		data = (String) driver_js.executeScript(js);
		// Base64Util.String2File(data.split(",")[1], "tmp/ic_image.png");
		BufferedImage ic_image = null;
		try {
			ic_image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(data.split(",")[1])));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 计算缺口的偏移量
		int offsetX = this.getCorX(c_image, ic_image) - 5;
		System.out.printf("缺口的偏移量：%d\r\n", offsetX);

		// 拖拽
		this.drag(offsetX);

		sleep(5000);
		while (!isLoggedin()) {
			if (failCount == maxfailCount)
				return false;
			failCount++;
			System.out.printf("第 %d 次尝试登录\r\n", failCount);
			return loginUtilSuccess();
		}
		System.out.println("登录成功");
		return true;
	}

	boolean isLoggedin() {
		Cookie cookie = driver.manage().getCookieNamed("SESSDATA");
		if(cookie != null) {
			return true;
		}else {
			return false;
		}
	}

	private void sleep(long millisecs) {
		try {
			Thread.sleep(millisecs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 是否为同一像素
	 * 
	 * @param c_image
	 * @param ic_image
	 * @param x
	 * @param y
	 * @return
	 */
	boolean isPixelSimilar(BufferedImage c_image, BufferedImage ic_image, int x, int y) {
		// 【x,y】返回图片的像素信息
		int c_pixel = c_image.getRGB(x, y);
		int ic_pixel = ic_image.getRGB(x, y);
		int detaRed = colorModel.getRed(c_pixel) - colorModel.getRed(ic_pixel);
		int detaGreen = colorModel.getGreen(c_pixel) - colorModel.getGreen(ic_pixel);
		int detaBlue = colorModel.getBlue(c_pixel) - colorModel.getBlue(ic_pixel);
		if (Math.abs(detaRed) < threshold && Math.abs(detaGreen) < threshold && Math.abs(detaBlue) < threshold)
			return true;
		return false;
	}

	/**
	 * 获取边界x的值
	 * 
	 * @param c_image
	 * @param ic_image
	 * @return
	 */
	int getCorX(BufferedImage c_image, BufferedImage ic_image) {
		int width = c_image.getWidth();
		int height = c_image.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (!isPixelSimilar(c_image, ic_image, x, y))
					return x;
			}
		}
		return width;
	}

	/**
	 * 向右拖拽指定值
	 * 
	 * @param offsetX
	 * @throws InterruptedException
	 */
	void drag(int offsetX) {
		Actions actions = new Actions(driver);
		WebElement slider = driver.findElement(By.className("geetest_slider_button"));
		actions.clickAndHold(slider).perform();
		actions.moveByOffset(offsetX / 2, 0).perform();
		sleep(500);
		actions.moveByOffset(offsetX / 2, 0).perform();
		sleep(500);
		actions.release(slider).perform();
	}
}
