package io.github.toohandsome.classassist.spi;

import io.github.toohandsome.classassist.core.ClassAssistApplicationContextInitializer;
import io.github.toohandsome.classassist.util.StringUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DefaultScanPath implements ScanPath {

    List<String> configName = new ArrayList<String>() {{
        add("bootstrap");
        add("application");
        add("config/application");
    }};
    List<String> suffixList = new ArrayList<String>() {{
        add("properties");
        add("yaml");
        add("yml");
    }};

    @Override
    public List<String> getScanPath() throws IOException {
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
                    properties.load(in);
                    scanPath = properties.getProperty("class-assist.scan");
                } else {
                    Yaml yaml = new Yaml();
                    InputStream in = ClassAssistApplicationContextInitializer.class.getClassLoader().getResourceAsStream(fileName);
                    if (in == null) {
                        continue;
                    }
                    Map<String, Map<String, Object>> properties = yaml.loadAs(in, HashMap.class);
                    Map<String, Object> scanPathMap = properties.get("class-assist");
                    if (scanPathMap != null) {
                        scanPath = scanPathMap.get("scan") + "";
                    }

                }
                if (StringUtil.isNotEmpty(scanPath)) {
                    System.out.println("class-assist  ===  scanPath: " + scanPath);
                    findit = true;
                    break;
                }
            }
            if (findit) {
                break;
            }
        }
        if (!findit) {
            System.err.println("class-assist  ===  " + this.getClass().getName() + " can't find scanPath");
            return new ArrayList<>();
        }
        return Arrays.asList(scanPath.split(","));
    }


}
