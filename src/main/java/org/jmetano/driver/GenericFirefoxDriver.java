package org.jmetano.driver;

import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class GenericFirefoxDriver extends FirefoxDriver {

	protected WebElement findElement(String by, String using) {
		List<WebElement> elementos;
		elementos = super.findElements(by, using);
		if (elementos.isEmpty()) {
			throw new NoSuchElementException("Cannot locate an element using "
					+ toString());
		} else {
			return elementos.get(0);
		}
	}

}
