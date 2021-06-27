import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeleniumTestWebApp
{
	static WebDriver driver;

	static WebElement startbutton, thirdServerEntry, secondStatusCode, stopbutton, secondServerEntry, thirdStatusCode, deletebutton, editbutton, addbutton;

	@BeforeAll
	public static void initTests() {
		System.out.println("initializing Webdriver ...");
		System.setProperty("webdriver.chrome.driver", "./drivers/chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		// options.addArguments("--headless"); // Headless kann er den Startbutton nicht klicken
		driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS); // Konfiguration der maximalen Wartezeit
		driver.get("http://localhost/servermonitoring-webapp-1/");
	}

	@BeforeEach
	public void arrange() {
		// Arrange
		startbutton = driver.findElement(By.cssSelector("input.btn-success"));
		stopbutton = driver.findElement(By.cssSelector("input.btn-warning"));
		stopbutton.click();
		stopbutton = driver.findElement(By.cssSelector("input.btn-warning"));
		addbutton = driver.findElement(By.id("form:j_idt37"));


	}

	@AfterEach
	public void stop() {
		try
		{
			stopbutton = driver.findElement(By.cssSelector("input.btn-warning"));
			stopbutton.click();
		}
		catch (Exception e)
		{
		}
	}

	@AfterAll
	public static void afterTests() {
		System.out.println("Tests done! Cleaning up ...");
		driver.close();
	}

	@Test
	@Order(6)
	public void editRow() {

		//Act
		Integer numberRows = driver.findElements(By.tagName("tr")).size() -1; // with Header
		addbutton.click();
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		editbutton = driver.findElement(By.xpath("/html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr["+(numberRows+1)+"]/td[4]/div/a[1]/span"));
		editbutton.click();
		// /html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr[3]/td[4]/div/a[1]/span
		// /html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr[3]/td[1]/div/div[1]
		// /html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr[3]/td[1]/div/div[2]/input
		WebElement inputField = driver.findElement(By.xpath("/html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr["+(numberRows+1)+"]/td[1]/div/div[2]/input"));
		inputField.clear();
		inputField.sendKeys("https://mailk8.me:443", Keys.RETURN);
		WebElement editedField = driver.findElement(By.id("form:requestdata:"+numberRows+":j_idt19"));
		String s = editedField.getText();

		//Assert
		assertEquals("https://mailk8.me:443", s);
	}

	@Test
	@Order(5)
	public void insertRow() {

		//Act
		int numberOfRowsBefore = driver.findElements(By.tagName("tr")).size() -1; // minus Header
		addbutton.click();
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		int numberOfRowsAfter = driver.findElements(By.tagName("tr")).size() -1; // minus Header
		//Assert
		assertEquals((numberOfRowsBefore + 1), numberOfRowsAfter);

	}


	@Test
	@Order(4)
	public void testDeleteRow() {

		int rowsBefore = driver.findElements(By.tagName("tr")).size();

		deletebutton = driver.findElement(By.xpath("/html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr[3]/td[6]/button/span[1]"));

		//Act
		deletebutton.click();

		int rowsAfter = driver.findElements(By.tagName("tr")).size();

		//Assert
		assertEquals((rowsBefore -1), rowsAfter);

	}

	@Test
	@Order(3)
	public void testNotReachable() {

		// Arrange
		secondServerEntry = driver.findElement(By.xpath("/html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr[2]/td[1]/div/div[1]"));
		thirdServerEntry = driver.findElement(By.id("form:requestdata:2:j_idt19"));

		secondStatusCode = driver.findElement(By.xpath("/html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr[2]/td[2]"));
		thirdStatusCode  = driver.findElement(By.xpath("/html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr[3]/td[2]"));

		//Act
		//System.out.println("Second Server Entry " + secondServerEntry.getText() + " equals " + secondServerEntry.getText().equals("http://8.8.8.8"));


		String s = secondServerEntry.getText();
		assertTrue(s.contains("8.8.8.8"));

		startbutton.click();
		WebDriverWait waitingDriver = new WebDriverWait(driver, 5);

		//Assert
		assertTrue(waitingDriver.until(ExpectedConditions
						.textMatches(By.xpath("/html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr[2]/td[2]"),
										Pattern.compile("Error or not reachable"))));

	}

	@Test
	@Order(2)
	public void testReachable() {

		// Arrange
		//thirdServerEntry = driver.findElement(By.id("form:requestdata:2:j_idt19"));
		thirdServerEntry = driver.findElement(By.xpath("/html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr[3]/td[1]/div/div[1]"));
		thirdStatusCode  = driver.findElement(By.xpath("/html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr[3]/td[2]"));

		//Act
		String s = thirdServerEntry.getText();

		assertEquals( "http://localhost:80", s);
		startbutton.click();
		WebDriverWait waitingDriver = new WebDriverWait(driver, 5);

		//Assert
		assertTrue(waitingDriver.until(ExpectedConditions
						.textMatches(By.xpath("/html/body/main/div/div/div[2]/form/div[1]/div/div/table/tbody/tr[3]/td[2]"),
										Pattern.compile("200 - OK"))));

	}

	@Test
	@Order(1)
	public void testTitle() {
		String excpect = "Check Server reachability";
		assertEquals(excpect, driver.getTitle());
	}



}
