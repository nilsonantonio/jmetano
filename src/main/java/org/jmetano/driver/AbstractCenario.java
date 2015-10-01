package org.jmetano.driver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmetano.parametrizacao.ParametroUtil;
import org.junit.BeforeClass;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

public abstract class AbstractCenario {

    protected static WebDriver webDriver;

    private static final Log LOG = LogFactory.getLog(AbstractCenario.class.getName());

    @BeforeClass
    public static void setUpClass() {
        synchronized (AbstractCenario.class) {
            WebDriver driver = webDriver;
            if (driver == null) {
                driver = criarWebDriver();
                GenericWebDriverSingleton.getInstance(driver);
            }
            webDriver = GenericWebDriverSingleton.getDriver();
        }
    }

    protected static WebDriver criarWebDriver() {
        if ("chrome".equals(ParametroUtil.getValueAsString("browser"))) {
            return new ChromeDriver();
        } else if ("ie".equals(ParametroUtil.getValueAsString("browser"))) {
            return new InternetExplorerDriver();
        } else {
            return new GenericFirefoxDriver();
        }
    }

    protected static void mouseOver(String idElemento) {
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        String script =
                "var elem = document.getElementById('" + idElemento + "');" + "if( document.createEvent) {"
                        + "var evObj = document.createEvent('MouseEvents');"
                        + "evObj.initEvent( 'mouseover', true, false );" + "elem.dispatchEvent(evObj);"
                        + "} else if( document.createEventObject ) {" + "elem.fireEvent('onmouseover');" + "}";
        js.executeScript(script);
    }

    protected void delay() {
        delay(ParametroUtil.getValueAsInteger("esperaImplicita") * 1000);
    }

    public static void delay(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            LOG.error("falha no delay;", e);
        }
    }
}
