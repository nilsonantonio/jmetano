package org.jmetano.driver;

import java.util.concurrent.TimeUnit;

import org.jmetano.parametrizacao.ParametroUtil;
import org.openqa.selenium.WebDriver;

public class GenericWebDriverSingleton extends Thread {

    protected static GenericWebDriverSingleton instancia;

    protected WebDriver driver;

    protected GenericWebDriverSingleton() {
        Runtime.getRuntime().addShutdownHook(this);
    }

    public static GenericWebDriverSingleton getInstance(WebDriver webDriver) {
        synchronized (GenericWebDriverSingleton.class) {
            if (instancia == null) {
                instancia = new GenericWebDriverSingleton();
                instancia.initWebDriver(webDriver);
            }
        }
        return instancia;
    }

    public void initWebDriver(WebDriver webDriver) {
        driver = webDriver;
        executarComandosDeInicializacao();
    }

    protected void executarComandosDeInicializacao() {
        driver.manage().timeouts().implicitlyWait(ParametroUtil.getValueAsInteger("esperaImplicita"), TimeUnit.SECONDS);
    }

    public static WebDriver getDriver() {
        return instancia.driver;
    }

    public void run() {
        try {
            driver.quit();
        } catch (NullPointerException npe) {
        }
    }

}
