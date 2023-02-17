package io.github.toohandsome.classassist.util;

import io.github.toohandsome.classassist.config.ClassAssistConfig;
import io.github.toohandsome.classassist.core.ClassAssistApplicationContextInitializer;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ConfigUtil {

    private static List<String> configName = new ArrayList<String>() {{
        add("bootstrap");
        add("application");
        add("config/application");
    }};
    private static List<String> suffixList = new ArrayList<String>() {{
        add("properties");
        add("yaml");
        add("yml");
    }};
    public static final String PROPPREFIX = "class-assist.";

    public static List getConfig(String propName) {
        boolean findit = false;
        String scanPath = "";
        for (String suffix : suffixList) {
            for (String config : configName) {
                String fileName = config + "." + suffix;
                if ("properties".equals(suffix)) {
                    InputStream in = ClassAssistApplicationContextInitializer.class.getClassLoader().getResourceAsStream(fileName);
                    if (in == null) {
                        continue;
                    }
                    Properties properties = new Properties();
                    try {
                        properties.load(in);
                        scanPath = properties.getProperty(PROPPREFIX + propName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    Yaml yaml = new Yaml();
                    InputStream in = ClassAssistApplicationContextInitializer.class.getClassLoader().getResourceAsStream(fileName);
                    if (in == null) {
                        continue;
                    }
                    Map<String, Map<String, Object>> properties = yaml.loadAs(in, HashMap.class);
                    Map<String, Object> scanPathMap = properties.get(PROPPREFIX);
                    if (scanPathMap != null) {
                        scanPath = scanPathMap.get(propName) + "";
                    }

                }
                if (StringUtil.isNotEmpty(scanPath)) {
                    LogUtil.info("class-assist  ===  " + propName + ": " + scanPath);
                    findit = true;
                    break;
                }
            }
            if (findit) {
                break;
            }
        }
        if (!findit) {
            LogUtil.error("class-assist  ===   can't find prop: " + propName);
            return new ArrayList<>();
        }
        return Arrays.asList(scanPath.split(","));
    }
}
