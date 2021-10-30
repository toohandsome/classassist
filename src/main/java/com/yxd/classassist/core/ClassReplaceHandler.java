package com.yxd.classassist.core;

import javassist.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * @author hudcan
 */
class ClassReplaceHandler {

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

    void handler(ClassPool classPool, IClassPatch classPatch, CtClass ctClass) {
        try {
            // 导包
            final List<String> improtPackages = classPatch.getImprotPackages();
            if (improtPackages != null && !improtPackages.isEmpty()) {
                for (String improtPackage : improtPackages) {
                    classPool.importPackage(improtPackage);
                }
            }

            // 加字段
            final List<String> addFieldList = classPatch.getAddFieldList();
            addFiled(ctClass, addFieldList);

            // 加方法
            final ArrayList<MethodMeta> addMethodList = classPatch.getAddMethodList();
            addMethod(classPool, ctClass, addMethodList);

            // 修改方法
            final ArrayList<MethodMeta> editMethodList = classPatch.getEditMethodList();
            editMethod(classPool, ctClass, editMethodList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFiled(CtClass ctClass, List<String> addFieldList) throws CannotCompileException {
        if (addFieldList != null && !addFieldList.isEmpty()) {
            for (String fieldMate : addFieldList) {
                CtField make = CtField.make(fieldMate, ctClass);
                ctClass.addField(make);
            }
        }
    }

    private void addMethod(ClassPool classPool, CtClass ctClass, ArrayList<MethodMeta> addMethodList) throws NotFoundException, CannotCompileException {
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
    }

    private void editMethod(ClassPool classPool, CtClass ctClass, ArrayList<MethodMeta> editMethodList) throws NotFoundException, CannotCompileException {
        if (editMethodList != null && !editMethodList.isEmpty()) {
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
        }
    }

}
