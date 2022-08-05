package io.github.toohandsome.classassist.core;

import cn.hutool.core.util.ClassUtil;
import io.github.toohandsome.classassist.annotation.ClassAssist;
import io.github.toohandsome.classassist.spi.ScanPath;
import io.github.toohandsome.classassist.util.StringUtil;
import javassist.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author hudcan
 */
public class ClassAssistApplicationContextInitializer implements ApplicationContextInitializer, Ordered {


    static boolean isFirst = true;


    public ClassAssistApplicationContextInitializer() {

        if (!isFirst) {
            return;
        }
        isFirst = false;

        final ClassLoader classLoader = this.getClass().getClassLoader();
//        ClassAssistClassLoad classAssistClassLoad = new ClassAssistClassLoad(String.class.getClassLoader());
//        Thread.currentThread().setContextClassLoader(classAssistClassLoad);

        ClassPool classPool = new ClassPool(true);
        classPool.appendClassPath(new LoaderClassPath(classLoader));
        ClassReplaceHandler classReplaceHandler = new ClassReplaceHandler();
        String currentClassName = "";

        List<String> allPath = new ArrayList<>();


        ServiceLoader loader = ServiceLoader.load(ScanPath.class);
        for (Object o : loader) {

            try {
                if (o instanceof ScanPath) {
                    ScanPath scanPath = (ScanPath) o;
                    allPath.addAll(scanPath.getScanPath());
                }
            } catch (Exception e) {
                System.err.println("class-assist  ===  getScanPath error . " + " scanPathClass:" + o.getClass().getName() + ", error: " + e.getMessage());
            }
        }


        if (allPath.isEmpty()) {
            System.out.println("class-assist  ===  not found scanPath , end of run");
            return;
        }

        for (String scanPath : allPath) {
            Set<Class<?>> classSet = ClassUtil.scanPackageByAnnotation(scanPath, ClassAssist.class);
            for (Class<?> class1 : classSet) {
                try {
                    final ClassAssist annotation = class1.getAnnotation(ClassAssist.class);
                    final String className = annotation.className();
                    System.out.println("class-assist  ===  found class " + class1.getTypeName());
                    currentClassName = class1.getTypeName();
                    CtClass ctClass = classPool.getCtClass(className);
                    final IClassPatch classPatch = (IClassPatch) class1.newInstance();
                    classReplaceHandler.handler(classPool, classPatch, ctClass);
                    ctClass.toClass(classLoader, ctClass.getClass().getProtectionDomain());
                    ctClass.detach();
                } catch (NotFoundException notFoundException) {
                    System.err.println("class-assist  ===  " + currentClassName + " not found , make sure class is exist.");
                } catch (InstantiationException instantiationException) {
                    System.err.println("class-assist  ===  " + currentClassName + " instantiationException . " + instantiationException.getMessage());
                } catch (IllegalAccessException illegalAccessException) {
                    System.err.println("class-assist  ===  " + currentClassName + " illegalAccessException . " + illegalAccessException.getMessage());
                } catch (CannotCompileException cannotCompileException) {
                    System.err.println("class-assist  ===  " + currentClassName + " cannotCompileException . " + cannotCompileException.getMessage());
                }
            }
        }


        System.out.println(Thread.currentThread().getName() + "  ===  " + Thread.currentThread().getContextClassLoader() + " ===   class-assist run success!");
    }


    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
//        System.err.println("ApplicationContextInitializer1111");
    }
}
