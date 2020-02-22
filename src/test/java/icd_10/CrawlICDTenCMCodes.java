package icd_10;

import core.files.writer.WriteExcelFile;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

/**
 * Created by ismail on 9/25/19
 */
public class CrawlICDTenCMCodes {

    // Define class variables
    WebDriver webDriver = null;
    WebDriverWait webDriverWait = null;
    String siteURL = "https://www.icd10data.com/ICD10CM/Codes/";
    String outputFolder = "src/test/resources/codes/";
    String outputExcel = outputFolder + "ki_codes_4.xlsx";

    @Test
    public void crawlCodes() {
        webDriver.navigate().to(siteURL);

        ArrayList<String> codesMainURL = new ArrayList<>();
        codesMainURL.add("https://www.icd10data.com/ICD10CM/Codes/I00-I99");
        codesMainURL.add("https://www.icd10data.com/ICD10CM/Codes/K00-K95");

        for (String codeMainURL : codesMainURL) {
            ArrayList<ArrayList<String>> codesGroup = crawlCodesGroup(codeMainURL);
            String sheetName = codeMainURL.replace("https://www.icd10data.com/ICD10CM/Codes/", "");
            for (ArrayList<String> codeGroup : codesGroup) {
                ArrayList<ArrayList<String>> secondLevelCodes = crawlSecondLevelCodes(codeGroup.get(1));
                writeExcelHeader(sheetName);
                for (ArrayList<String> row : secondLevelCodes) {
                    String codeDescription = row.get(2);
                    String codeURL = row.get(3);
                    // Check if code description is fully provided or need to extract from second level code page
                    if (codeDescription.endsWith("..."))
                        codeDescription = extractFullCodeDescription(codeURL);
                    row.set(2, codeDescription);
                    // Get first level code description
                    String firstCodeDescription = getFirstCodeLevelDescription(row.get(0));
                    row.add(1, firstCodeDescription);
                    WriteExcelFile.writeRowToExcelSheet(outputExcel, sheetName, row);
                }
            }
        }
    }

    @Test
    public void getRanges() {
        String rangesURL = siteURL;
        crawlCodesRanges(rangesURL);
    }

    @BeforeMethod
    public void setUp() {
        ChromeDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        // chromeOptions.addArguments("--headless");
        webDriver = new ChromeDriver(chromeOptions);
        webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
    }

    @AfterMethod
    public void tearDown() {
        webDriver.close();
    }

    // This method crawls codes group
    private ArrayList<ArrayList<String>> crawlCodesGroup(String codesURL) {
        ArrayList<ArrayList<String>> groupsInfo = new ArrayList<>();

        webDriver.navigate().to(codesURL);
        By groupCodesSelector = By.xpath("/html/body/div[3]/div/div[1]/div/ul/li/a");
        List<WebElement> groupsElements = webDriver.findElements(groupCodesSelector);

        for (WebElement groupElement : groupsElements) {
            ArrayList<String> groupInfo = new ArrayList<>();
            groupInfo.add(groupElement.getText());
            groupInfo.add(groupElement.getAttribute("href"));
            groupsInfo.add(groupInfo);
        }
        return groupsInfo;
    }

    // This method crawls first level of codes
    private ArrayList<ArrayList<String>> crawlFirstLevelCodes(String groupURL) {

        return null;
    }

    // This method crawls second level of codes
    private ArrayList<ArrayList<String>> crawlSecondLevelCodes(String firstCodeURL) {
        webDriver.navigate().to(firstCodeURL);
        // Define Array list to save all second level codes info
        ArrayList<ArrayList<String>> allCodesInfo = new ArrayList<>();
        // Point to all documents icons to displays all second level codes on the page
        By secondLevelCodesDocSelector = By.xpath("/html/body/div[3]/div/div[1]/div/ul/li/div");
        List<WebElement> secondLevelCodesDoc = webDriver.findElements(secondLevelCodesDocSelector);
        for (WebElement secondLevelCodeDoc : secondLevelCodesDoc) {
            Actions actions = new Actions(webDriver);
            actions.moveToElement(secondLevelCodeDoc).build().perform();
            try {
                Thread.sleep(1000);
            } catch (Throwable throwable) {
            }
        }

        // Extract all second level codes info
        By secondLevelCodesSelectors = By.xpath("//*[@class=\"qtip-content\"]/ul/li/ul/li/span");
        List<WebElement> secondLevelCodes = webDriver.findElements(secondLevelCodesSelectors);

        for (WebElement secondLevelCode : secondLevelCodes) {
            ArrayList<String> codeInfo = new ArrayList<>();
            // Extract code ID
            String codeId = secondLevelCode.getAttribute("id");
            // Extract code description from span(selenium has issues to retrieve text from span)
            String codeDescription = secondLevelCode.getAttribute("innerHTML").replaceAll("<a.*?</a>", "").trim();
            // Define href element for the code
            By hrefSecondLevelCodeSelector = By.xpath("//*[@class=\"qtip-content\"]/ul/li/ul/li/span[@id=\"" + codeId + "\"]/a");
            String codeURL = webDriver.findElement(hrefSecondLevelCodeSelector).getAttribute("href");
            // Add all code info to the list
            codeInfo.add(codeId.split("\\.")[0]);
            codeInfo.add(codeId);
            codeInfo.add(codeDescription);
            codeInfo.add(codeURL);
            // Add code info to main list
            allCodesInfo.add(codeInfo);
        }
        return allCodesInfo;
    }

    // This method extracts full code description if ends with three dots
    private String extractFullCodeDescription(String codeURL) {
        // Navigate to second level code page
        webDriver.navigate().to(codeURL);
        // Extract the full description
        By codeFullDescriptionSelector = By.xpath("/html/body/div[3]/div/div[1]/div/h2");
        WebElement codeFullDescriptionElement = null; //webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(codeFullDescriptionSelector));
        String codeFullDescription = codeFullDescriptionElement.getText();
        return codeFullDescription;
    }

    // This method writes excel headers
    private void writeExcelHeader(String sheetName) {
        ArrayList<String> headers = new ArrayList<>();
        headers.add("First Level Code ID");
        headers.add("First Level Code Description");
        headers.add("Second Level Code ID");
        headers.add("Second Level Code Description");
        headers.add("Second Level Code URL");
        WriteExcelFile.writeHeaderToExcelSheet(outputExcel, sheetName, headers);
    }

    // Adding first level description
    private String getFirstCodeLevelDescription(String key) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("K00", "Disorders of tooth development and eruption");
        hashMap.put("K01", "Embedded and impacted teeth");
        hashMap.put("K02", "Dental caries");
        hashMap.put("K03", "Other diseases of hard tissues of teeth");
        hashMap.put("K04", "Diseases of pulp and periapical tissues");
        hashMap.put("K05", "Gingivitis and periodontal diseases");
        hashMap.put("K06", "Other disorders of gingiva and edentulous alveolar ridge");
        hashMap.put("K08", "Other disorders of teeth and supporting structures");
        hashMap.put("K09", "Cysts of oral region, not elsewhere classified");
        hashMap.put("K11", "Diseases of salivary glands");
        hashMap.put("K12", "Stomatitis and related lesions");
        hashMap.put("K13", "Other diseases of lip and oral mucosa");
        hashMap.put("K14", "Diseases of tongue");
        hashMap.put("K20", "Esophagitis");
        hashMap.put("K21", "Gastro-esophageal reflux disease");
        hashMap.put("K22", "Other diseases of esophagus");
        hashMap.put("K23", "Disorders of esophagus in diseases classified elsewhere");
        hashMap.put("K25", "Gastric ulcer");
        hashMap.put("K26", "Duodenal ulcer");
        hashMap.put("K27", "Peptic ulcer, site unspecified");
        hashMap.put("K28", "Gastrojejunal ulcer");
        hashMap.put("K29", "Gastritis and duodenitis");
        hashMap.put("K30", "Functional dyspepsia");
        hashMap.put("K31", "Other diseases of stomach and duodenum");
        hashMap.put("K35", "Acute appendicitis");
        hashMap.put("K36", "Other appendicitis");
        hashMap.put("K37", "Unspecified appendicitis");
        hashMap.put("K38", "Other diseases of appendix");
        hashMap.put("K40", "Inguinal hernia");
        hashMap.put("K41", "Femoral hernia");
        hashMap.put("K42", "Umbilical hernia");
        hashMap.put("K43", "Ventral hernia");
        hashMap.put("K44", "Diaphragmatic hernia");
        hashMap.put("K45", "Other abdominal hernia");
        hashMap.put("K46", "Unspecified abdominal hernia");
        hashMap.put("K50", "Crohn's disease [regional enteritis]");
        hashMap.put("K51", "Ulcerative colitis");
        hashMap.put("K52", "Other and unspecified noninfective gastroenteritis and colitis");
        hashMap.put("K55", "Vascular disorders of intestine");
        hashMap.put("K56", "Paralytic ileus and intestinal obstruction without hernia");
        hashMap.put("K57", "Diverticular disease of intestine");
        hashMap.put("K58", "Irritable bowel syndrome");
        hashMap.put("K59", "Other functional intestinal disorders");
        hashMap.put("K60", "Fissure and fistula of anal and rectal regions");
        hashMap.put("K61", "Abscess of anal and rectal regions");
        hashMap.put("K62", "Other diseases of anus and rectum");
        hashMap.put("K63", "Other diseases of intestine");
        hashMap.put("K64", "Hemorrhoids and perianal venous thrombosis");
        hashMap.put("K65", "Peritonitis");
        hashMap.put("K66", "Other disorders of peritoneum");
        hashMap.put("K67", "Disorders of peritoneum in infectious diseases classified elsewhere");
        hashMap.put("K68", "Disorders of retroperitoneum");
        hashMap.put("K70", "Alcoholic liver disease");
        hashMap.put("K71", "Toxic liver disease");
        hashMap.put("K72", "Hepatic failure, not elsewhere classified");
        hashMap.put("K73", "Chronic hepatitis, not elsewhere classified");
        hashMap.put("K74", "Fibrosis and cirrhosis of liver");
        hashMap.put("K75", "Other inflammatory liver diseases");
        hashMap.put("K76", "Other diseases of liver");
        hashMap.put("K77", "Liver disorders in diseases classified elsewhere");
        hashMap.put("K80", "Cholelithiasis");
        hashMap.put("K81", "Cholecystitis");
        hashMap.put("K82", "Other diseases of gallbladder");
        hashMap.put("K83", "Other diseases of biliary tract");
        hashMap.put("K85", "Acute pancreatitis");
        hashMap.put("K86", "Other diseases of pancreas");
        hashMap.put("K87", "Disorders of gallbladder, biliary tract and pancreas in diseases classified elsewhere");
        hashMap.put("K90", "Intestinal malabsorption");
        hashMap.put("K91", "Intraoperative and postprocedural complications and disorders of digestive system, not elsewhere classified");
        hashMap.put("K92", "Other diseases of digestive system");
        hashMap.put("K94", "Complications of artificial openings of the digestive system");
        hashMap.put("K95", "Complications of bariatric procedures");
        hashMap.put("I00", "Rheumatic fever without heart involvement");
        hashMap.put("I01", "Rheumatic fever with heart involvement");
        hashMap.put("I02", "Rheumatic chorea");
        hashMap.put("I05", "Rheumatic mitral valve diseases");
        hashMap.put("I06", "Rheumatic aortic valve diseases");
        hashMap.put("I07", "Rheumatic tricuspid valve diseases");
        hashMap.put("I08", "Multiple valve diseases");
        hashMap.put("I09", "Other rheumatic heart diseases");
        hashMap.put("I10", "Essential (primary) hypertension");
        hashMap.put("I11", "Hypertensive heart disease");
        hashMap.put("I12", "Hypertensive chronic kidney disease");
        hashMap.put("I13", "Hypertensive heart and chronic kidney disease");
        hashMap.put("I15", "Secondary hypertension");
        hashMap.put("I16", "Hypertensive crisis");
        hashMap.put("I20", "Angina pectoris");
        hashMap.put("I21", "Acute myocardial infarction");
        hashMap.put("I22", "Subsequent ST elevation (STEMI) and non-ST elevation (NSTEMI) myocardial infarction");
        hashMap.put("I23", "Certain current complications following ST elevation (STEMI) and non-ST elevation (NSTEMI) myocardial infarction (within the 28 day period)");
        hashMap.put("I24", "Other acute ischemic heart diseases");
        hashMap.put("I25", "Chronic ischemic heart disease");
        hashMap.put("I26", "Pulmonary embolism");
        hashMap.put("I27", "Other pulmonary heart diseases");
        hashMap.put("I28", "Other diseases of pulmonary vessels");
        hashMap.put("I30", "Acute pericarditis");
        hashMap.put("I31", "Other diseases of pericardium");
        hashMap.put("I32", "Pericarditis in diseases classified elsewhere");
        hashMap.put("I33", "Acute and subacute endocarditis");
        hashMap.put("I34", "Nonrheumatic mitral valve disorders");
        hashMap.put("I35", "Nonrheumatic aortic valve disorders");
        hashMap.put("I36", "Nonrheumatic tricuspid valve disorders");
        hashMap.put("I37", "Nonrheumatic pulmonary valve disorders");
        hashMap.put("I38", "Endocarditis, valve unspecified");
        hashMap.put("I39", "Endocarditis and heart valve disorders in diseases classified elsewhere");
        hashMap.put("I40", "Acute myocarditis");
        hashMap.put("I41", "Myocarditis in diseases classified elsewhere");
        hashMap.put("I42", "Cardiomyopathy");
        hashMap.put("I43", "Cardiomyopathy in diseases classified elsewhere");
        hashMap.put("I44", "Atrioventricular and left bundle-branch block");
        hashMap.put("I45", "Other conduction disorders");
        hashMap.put("I46", "Cardiac arrest");
        hashMap.put("I47", "Paroxysmal tachycardia");
        hashMap.put("I48", "Atrial fibrillation and flutter");
        hashMap.put("I49", "Other cardiac arrhythmias");
        hashMap.put("I50", "Heart failure");
        hashMap.put("I51", "Complications and ill-defined descriptions of heart disease");
        hashMap.put("I52", "Other heart disorders in diseases classified elsewhere");
        hashMap.put("I60", "Nontraumatic subarachnoid hemorrhage");
        hashMap.put("I61", "Nontraumatic intracerebral hemorrhage");
        hashMap.put("I62", "Other and unspecified nontraumatic intracranial hemorrhage");
        hashMap.put("I63", "Cerebral infarction");
        hashMap.put("I65", "Occlusion and stenosis of precerebral arteries, not resulting in cerebral infarction");
        hashMap.put("I66", "Occlusion and stenosis of cerebral arteries, not resulting in cerebral infarction");
        hashMap.put("I67", "Other cerebrovascular diseases");
        hashMap.put("I68", "Cerebrovascular disorders in diseases classified elsewhere");
        hashMap.put("I69", "Sequelae of cerebrovascular disease");
        hashMap.put("I70", "Atherosclerosis");
        hashMap.put("I71", "Aortic aneurysm and dissection");
        hashMap.put("I72", "Other aneurysm");
        hashMap.put("I73", "Other peripheral vascular diseases");
        hashMap.put("I74", "Arterial embolism and thrombosis");
        hashMap.put("I75", "Atheroembolism");
        hashMap.put("I76", "Septic arterial embolism");
        hashMap.put("I77", "Other disorders of arteries and arterioles");
        hashMap.put("I78", "Diseases of capillaries");
        hashMap.put("I79", "Disorders of arteries, arterioles and capillaries in diseases classified elsewhere");
        hashMap.put("I80", "Phlebitis and thrombophlebitis");
        hashMap.put("I81", "Portal vein thrombosis");
        hashMap.put("I82", "Other venous embolism and thrombosis");
        hashMap.put("I83", "Varicose veins of lower extremities");
        hashMap.put("I85", "Esophageal varices");
        hashMap.put("I86", "Varicose veins of other sites");
        hashMap.put("I87", "Other disorders of veins");
        hashMap.put("I88", "Nonspecific lymphadenitis");
        hashMap.put("I89", "Other noninfective disorders of lymphatic vessels and lymph nodes");
        hashMap.put("I95", "Hypotension");
        hashMap.put("I96", "Gangrene, not elsewhere classified");
        hashMap.put("I97", "Intraoperative and postprocedural complications and disorders of circulatory system, not elsewhere classified");
        hashMap.put("I99", "Other and unspecified disorders of circulatory system");
        return hashMap.containsKey(key) ? hashMap.get(key) : "";
    }

    // This method extracts all codes ranges and save them to an excel file
    private void crawlCodesRanges(String rangesURL) {
        // Navigate to diseases ranges site url
        webDriver.navigate().to(rangesURL);
        // Extract ranges title and save it as file name
        By rangesTitleSelector = By.xpath("/html/body/div[3]/div/div[1]/div/h1");
        String fileName = null; //webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(rangesTitleSelector)).getText();
        // Convert fileName for renaming standard
        fileName = fileName.toLowerCase().replace(" ", "_").replace("-", "_").concat(".xlsx");
        // Build full output excel path
        String outputExcel = outputFolder + (fileName.isEmpty() ? "ranges_file.xlsx" : fileName);
        // Build Excel File sheet
        String sheetName = getCurrentTime();

        // Extract all ranges of codes and add them to ArrayList to save them to outputExcel
        By rangesCodesSelector = By.xpath("/html/body/div[3]/div/div[1]/div/ul/li");
        // Define List of codes ranges webElements
        List<WebElement> rangesCodesElements = webDriver.findElements(rangesCodesSelector);
        // Define ArrayList to add all required info
        ArrayList<ArrayList<String>> rangesCodesInfo = new ArrayList<>();
        // Extract all code, description and URL from elements
        rangesCodesInfo = getElementsAttributes(rangesCodesElements);
        // Save all ranges to excel file
        writeExcelHeader(outputExcel, sheetName, "Range ID", "Range Description", "Range URL");
        for (ArrayList<String> row : rangesCodesInfo) {
            WriteExcelFile.writeRowToExcelSheet(outputExcel, sheetName, row);
        }
    }

    // This method to extract attributes from specific WebElement and return them as ArrayList
    private ArrayList<ArrayList<String>> getElementsAttributes(List<WebElement> elements) {
        // Define Return ArrayList
        ArrayList<ArrayList<String>> results = new ArrayList<>();
        // Loop through all elements and extract all attributes for each element
        for (WebElement element : elements) {
            // Define ArrayList to save all element attributes
            ArrayList<String> elementAttributes = new ArrayList<>();
            // Find element attributes and add them to the ArrayList
            String codeID = element.findElement(By.xpath(".//a")).getText();
            elementAttributes.add(codeID);
            String codeDescription = getText(element, "a", "div");
            elementAttributes.add(codeDescription);
            String codeURL = element.findElement(By.xpath(".//a")).getAttribute("href");
            elementAttributes.add(codeURL);
            // Add Element attributes to results ArrayList
            results.add(elementAttributes);
        }
        return results;
    }

    // This method extracts Text from innerHTML attribute as String
    private String getText(WebElement element, String... tags) {
        // Define regex Builder
        String regex = "";
        // Loop all tags and build regex for all of them
        for (String tag : tags) {
            // Check if the regex has previous regex to tag to add OR logic
            if (!regex.isEmpty())
                regex += "|";
            regex += "<" + tag + ".*?</" + tag + ">";
        }
        return element.getAttribute("innerHTML").replaceAll(regex, "").trim();
    }

    // This method return current time as string
    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    // This method writes excel headers
    private void writeExcelHeader(String outputExcel, String sheetName, String... headers) {
        WriteExcelFile.writeHeaderToExcelSheet(outputExcel, sheetName, headers);
    }
}
