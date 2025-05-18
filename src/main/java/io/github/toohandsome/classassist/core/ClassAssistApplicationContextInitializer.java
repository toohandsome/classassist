package io.github.toohandsome.classassist.core;

//import cn.hutool.core.util.ClassUtil;

import io.github.toohandsome.classassist.annotation.ClassAssist;
import io.github.toohandsome.classassist.config.ClassAssistConfig;
import io.github.toohandsome.classassist.spi.ScanPath;
import io.github.toohandsome.classassist.util.ClassUtil;
import io.github.toohandsome.classassist.util.ConfigUtil;
import io.github.toohandsome.classassist.util.LogUtil;
import io.github.toohandsome.classassist.util.StringUtil;
import javassist.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hudcan
 */
@Component
public class ClassAssistApplicationContextInitializer implements ApplicationContextInitializer, Ordered {


    static boolean isFirst = true;

    static ClassLoader classLoader;
    static ClassPool classPool;
    static ClassReplaceHandler classReplaceHandler;
    //    static Vector<Class> todoList = new Vector<>();
    static ConcurrentHashMap<String, Class> classMap = new ConcurrentHashMap<>();

    {
        classPool = new ClassPool(true);
        classPool.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));
        classReplaceHandler = new ClassReplaceHandler();
    }

    public ClassAssistApplicationContextInitializer() {
        classLoader = this.getClass().getClassLoader();
        if (!isFirst) {
            return;
        }
        isFirst = false;

        if (handAnotation(false)) return;

        System.out.println(Thread.currentThread().getName() + "  ===  " + Thread.currentThread().getContextClassLoader() + " ===   class-assist run success!");
    }

    private boolean handAnotation(boolean handCondition) {
        final List enableList = ConfigUtil.getConfig("enable");
        if (!enableList.isEmpty()) {
            final String enableStr = enableList.get(0) + "";
            try {
                final boolean aBoolean = Boolean.valueOf(enableStr);
                if (!aBoolean) {
                    System.out.println("class-assist  ===  disabled!");
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final List logList = ConfigUtil.getConfig("log");
        if (!logList.isEmpty()) {
            final String enableStr = logList.get(0) + "";
            try {
                final boolean aBoolean = Boolean.valueOf(enableStr);
                if (aBoolean) {
                    ClassAssistConfig.log = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


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
                System.err.println("class-assist  ===  getScanPath error . " + " scanPathClass:" + o.getClass().getName()
                        + ", error: " + e.getMessage()
                        + ", stackTrace: " + e.getStackTrace()[0] + "\t" + e.getStackTrace()[1]);
            }
        }

        if (allPath.isEmpty()) {
            System.out.println("class-assist  ===  not found scanPath , end of run");
            return true;
        }

        for (int i = 0; i < allPath.size(); i++) {
            String scanPath = allPath.get(i);
            try {
                List<Class<?>> classSet = ClassUtil.getClassListByAnnotation(scanPath, ClassAssist.class);
                for (Class<?> class1 : classSet) {
//                    currentClassName = handler(class1);
                    final ClassAssist annotation = class1.getAnnotation(ClassAssist.class);
                    final String className = annotation.className();
                    LogUtil.info("class-assist  ===  found class " + class1.getTypeName());
                    currentClassName = class1.getTypeName();
                    if (annotation.useEnv()) {
//                        todoList.add(class1);
                        classMap.put(className, class1);
                        continue;
                    }
                    handler(className, class1);
                }
            } catch (Exception exception) {
                System.err.println("class-assist  ===  " + currentClassName + " getClassListByAnnotationException . " + exception.getMessage());
            }
        }
        return false;
    }

    static void handler(String className, Class class1) {
        String currentClassName = class1.getTypeName();
        try {
            CtClass ctClass = classPool.getCtClass(className);
            final IClassPatch classPatch = (IClassPatch) class1.newInstance();
            classReplaceHandler.handler(classLoader, classPool, classPatch, ctClass);
        } catch (NotFoundException notFoundException) {
            System.err.println("class-assist  ===  " + currentClassName + " not found , make sure class is exist.");
        } catch (InstantiationException instantiationException) {
            System.err.println("class-assist  ===  " + currentClassName + " instantiationException . " + instantiationException.getMessage());
        } catch (IllegalAccessException illegalAccessException) {
            System.err.println("class-assist  ===  " + currentClassName + " illegalAccessException . " + illegalAccessException.getMessage());
        } catch (CannotCompileException cannotCompileException) {
            System.err.println("class-assist  ===  " + currentClassName + " cannotCompileException . " + cannotCompileException.getMessage());
        } catch (Exception e) {
            System.err.println("class-assist  ===  " + currentClassName + " cannotCompileException . " + e.getMessage());
        }
//        return currentClassName;
    }


    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

    }

}
