package org.cland.utils;

import lombok.SneakyThrows;

import java.io.InputStreamReader;
import java.util.Properties;

public class PropertiesUtil {

    @SneakyThrows
    public static Properties load(String propertyFileName) {
        InputStreamReader in = null;

        ClassLoader loder = Thread.currentThread().getContextClassLoader();

        in = new InputStreamReader(loder.getResourceAsStream(propertyFileName), "UTF-8");
        ;
        if (in != null) {
            Properties prop = new Properties();
            prop.load(in);
            return prop;
        }
        return null;
    }

}
