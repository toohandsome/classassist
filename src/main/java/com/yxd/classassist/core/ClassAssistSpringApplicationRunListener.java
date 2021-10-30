package com.yxd.classassist.core;

import cn.hutool.core.util.ClassUtil;
import com.yxd.classassist.annotation.ClassAssist;
import com.yxd.classassist.util.StringUtil;
import javassist.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.Ordered;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author hudcan
 */
public class ClassAssistSpringApplicationRunListener implements SpringApplicationRunListener, Ordered {

    static boolean isFirst = true;
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

    public ClassAssistSpringApplicationRunListener(SpringApplication application, String[] args) {

        if (!isFirst) {
            return;
        }
        isFirst = false;

        final ClassLoader classLoader = this.getClass().getClassLoader();
        ClassAssistClassLoad classAssistClassLoad = new ClassAssistClassLoad(classLoader);
        Thread.currentThread().setContextClassLoader(classAssistClassLoad);
        ClassPool classPool = new ClassPool(true);
        classPool.appendClassPath(new LoaderClassPath(classLoader));
        ClassReplaceHandler classReplaceHandler = new ClassReplaceHandler();
        String currentClassName = "";
        try {
            String scanPath = getScanPath();

            if (!StringUtil.isNotEmpty(scanPath)) {
                System.out.println("class-assist not found scanPath , end of run");
                return;
            }
            Set<Class<?>> classSet = ClassUtil.scanPackageByAnnotation(scanPath, ClassAssist.class);
            for (Class<?> class1 : classSet) {
                final ClassAssist annotation = class1.getAnnotation(ClassAssist.class);
                final String className = annotation.className();
                System.out.println("class-assist found class " + class1.getTypeName());
                currentClassName = class1.getTypeName();
                CtClass ctClass = classPool.getCtClass(className);
                final IClassPatch classPatch = (IClassPatch) class1.newInstance();
                classReplaceHandler.handler(classPool, classPatch, ctClass);
                ctClass.toClass(classLoader, ctClass.getClass().getProtectionDomain());
                ctClass.detach();
            }

            System.out.println(Thread.currentThread().getName() + "  ===  " + Thread.currentThread().getContextClassLoader() + " ===   class-assist run success!");

        } catch (NotFoundException notFoundException) {
            System.err.println("class-assist === " + currentClassName + " not found , make sure class is exist.");
        } catch (InstantiationException instantiationException) {
            System.err.println("class-assist === " + currentClassName + " instantiationException . " + instantiationException.getMessage());
        } catch (IllegalAccessException illegalAccessException) {
            System.err.println("class-assist === " + currentClassName + " illegalAccessException . " + illegalAccessException.getMessage());
        } catch (CannotCompileException cannotCompileException) {
            System.err.println("class-assist === " + currentClassName + " cannotCompileException . " + cannotCompileException.getMessage());
        } catch (IOException ioException) {
            System.err.println("class-assist ===  getScanPath error . " + ioException.getMessage());
        }
    }


    private String getScanPath() throws IOException {
        boolean findit = false;
        String scanPath = "";
        for (String suffix : suffixList) {
            for (String config : configName) {
                String fileName = config + "." + suffix;
                if ("properties".equals(suffix)) {
                    InputStream in = ClassAssistSpringApplicationRunListener.class.getClassLoader().getResourceAsStream(fileName);
                    if (in == null) {
                        continue;
                    }
                    Properties properties = new Properties();
                    properties.load(in);
                    scanPath = properties.getProperty("class-assist.scan");
                } else {
                    Yaml yaml = new Yaml();
                    InputStream in = ClassAssistSpringApplicationRunListener.class.getClassLoader().getResourceAsStream(fileName);
                    if (in == null) {
                        continue;
                    }
                    Map<String, Map<String, Object>> properties = yaml.loadAs(in, HashMap.class);
                    Map<String, Object> scanPath1 = properties.get("class-assist");
                    scanPath = scanPath1.get("scan") + "";
                }
                if (StringUtil.isNotEmpty(scanPath)) {
                    System.out.println("class-assist scanPath: " + scanPath);
                    findit = true;
                    break;
                }
            }
            if (findit) {
                break;
            }
        }
        return scanPath;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
