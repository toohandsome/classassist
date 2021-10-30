package com.yxd.classassist.core;

import cn.hutool.core.util.ClassUtil;
import com.yxd.classassist.annotation.ClassAssist;
import javassist.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

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
        ClassAssistClassLoad cloudOrmClassLoad1 = new ClassAssistClassLoad(classLoader);
        Thread.currentThread().setContextClassLoader(cloudOrmClassLoad1);

        try {

            ClassPool classPool = new ClassPool(true);
            classPool.appendClassPath(new LoaderClassPath(cloudOrmClassLoad1));
            boolean findit = false;
            String scanPath = "";
            for (String suffix : suffixList) {

                for (String config : configName) {

                    String fileName = config + "." + suffix;
                    System.out.println("fileName: " + fileName);
                    if (suffix.equals("properties")) {
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
                    if (!"null".equals(scanPath) && StringUtils.hasText(scanPath)) {
                        System.out.println("scanPath: " + scanPath);
                        findit = true;
                        break;
                    }

                }
                if (findit) {
                    break;
                }
            }


            Set<Class<?>> scanPackage = ClassUtil.scanPackageByAnnotation(scanPath, ClassAssist.class);

            for (Class<?> class1 : scanPackage) {
                final ClassAssist annotation = class1.getAnnotation(ClassAssist.class);
                final String className = annotation.className();
                CtClass ctClass = classPool.getCtClass(className);
                final IClassPatch classPatch = (IClassPatch) class1.newInstance();
                // 导包
                final List<String> improtPackages = classPatch.getImprotPackages();
                if (improtPackages != null && !improtPackages.isEmpty()) {
                    for (String improtPackage : improtPackages) {
                        classPool.importPackage(improtPackage);
                    }
                }

                // 加字段
                final List<String> addFieldList = classPatch.getAddFieldList();
                if (addFieldList != null && !addFieldList.isEmpty()) {
                    for (String fieldMate : addFieldList) {
                        CtField make = CtField.make(fieldMate, ctClass);
                        ctClass.addField(make);
                    }
                }

                // 加方法
                final ArrayList<MethodMeta> addMethodList = classPatch.getAddMethodList();
                if (addMethodList != null && !addMethodList.isEmpty()) {
                    for (MethodMeta methodMeta : addMethodList) {
                        final String name = methodMeta.getName();
                        final String body = methodMeta.getBody();
                        final Class returnType = methodMeta.getReturnType();
                        final LinkedHashMap<String, Class> paramsType = methodMeta.getParams();

                        final Set<String> strings = paramsType.keySet();
                        CtClass[] params = new CtClass[strings.size()];
                        final Object[] objects = strings.toArray();
                        for (int i = 0; i < objects.length; i++) {
                            String param = (String) objects[i];
                            final Class aClass = paramsType.get(param);
                            final String simpleName = aClass.getTypeName();
                            final CtClass ctClass1 = classPool.getCtClass(simpleName);
                            params[i] = ctClass1;
                        }
                        final CtClass ctClass1 = classPool.getCtClass(returnType.getTypeName());
                        CtMethod ctMethod = new CtMethod(ctClass1, name, params, ctClass);
                        ctClass.addMethod(ctMethod);
                        ctMethod.setBody(body);
                        ctMethod.setModifiers(Modifier.PUBLIC);
                        ctClass.setModifiers(ctClass.getModifiers() & ~Modifier.ABSTRACT);
                    }
                }

                // 修改方法
                final ArrayList<MethodMeta> editMethodList = classPatch.getEditMethodList();
                for (MethodMeta methodMeta : editMethodList) {
                    final LinkedHashMap<String, Class> paramsType = methodMeta.getParams();
                    final String body = methodMeta.getBody();
                    final String name = methodMeta.getName();
                    final int size = paramsType.size();
                    CtClass[] params = new CtClass[size];
                    final Set<String> strings = paramsType.keySet();
                    final Object[] objects = strings.toArray();
                    for (int i = 0; i < size; i++) {
                        String param = (String) objects[i];
                        final Class aClass = paramsType.get(param);
                        final String simpleName = aClass.getTypeName();
                        final CtClass ctClass1 = classPool.getCtClass(simpleName);
                        params[i] = ctClass1;
                    }
                    CtMethod method = ctClass.getDeclaredMethod(name, params);
                    method.setBody(body);
                }
                ctClass.toClass(cloudOrmClassLoad1, ctClass.getClass().getProtectionDomain());
                ctClass.detach();
            }

            System.out.println(Thread.currentThread().getName() + "  ====  " + Thread.currentThread().getContextClassLoader() + " ====   class-assist run success!");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public int getOrder() {
        return 0;
    }
}
