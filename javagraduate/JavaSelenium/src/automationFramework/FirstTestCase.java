package automationFramework;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
public class FirstTestCase {

	public static void main(String[] args) {
		String exePath = "/Users/joyeongmin/Downloads/chromedriver";
		System.setProperty("webdriver.chrome.driver", exePath);
		WebDriver driver = new ChromeDriver();
		driver.get("http://203.249.126.126:9090/servlets/jsp/timetable/frame.jsp");
		//school class search
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		WebElement frame_element=driver.findElement(By.xpath("/html/frameset/frame[1]"));
		driver.switchTo().frame(frame_element);
		////*[@id="opt2"]/table/tbody/tr[1]/td/select/option[5] -> college choice 
		driver.findElement(By.xpath("//*[@id='opt2']/table/tbody/tr[2]/td/select/option[3]")).click();
		//click health&care management or any major you want by changing last option number
		
		driver.findElement(By.xpath("/html/body/form/table[4]/tbody/tr/td[7]/img")).click();
		//when user click 'ask' image button

		driver.switchTo().defaultContent();
		
		WebElement frame_element2=driver.findElement(By.xpath("/html/frameset/frame[2]"));
		driver.switchTo().frame(frame_element2);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		WebDriverWait wait = new WebDriverWait(driver,10);
		
		//WebElement element=driver.findElement(By.xpath("/html/body/form/table[4]"));
		WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/form/table[3]")));
		
		String element_text=element.getText();
		System.out.println(element_text);
	}
	/*
	private static String testeddd(Exception exception) {
		if exception instanceof InterruptedException {
			return "dasdas";
		}
		return "";
	}
	*/
} 
