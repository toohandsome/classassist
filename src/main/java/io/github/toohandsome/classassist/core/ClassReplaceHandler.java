package io.github.toohandsome.classassist.core;

import io.github.toohandsome.classassist.config.ClassAssistConfig;
import io.github.toohandsome.classassist.util.JavaFormat;
import io.github.toohandsome.classassist.util.LogUtil;
import javassist.*;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * @author hudcan
 */
class ClassReplaceHandler {

    String replaceReg = "([\\.|\\s|\\|;|,)])";

    void handler(ClassLoader classLoader, ClassPool classPool, IClassPatch classPatch, CtClass ctClass) throws CannotCompileException, NotFoundException {


        // 不满足条件 不处理
        if (!classPatch.condition()) {
            return;
        }

        // 导包
        final List<String> importPackages = classPatch.getImprotPackages();
        if (importPackages != null && !importPackages.isEmpty()) {
            for (String importPackage : importPackages) {
                classPool.importPackage(importPackage);
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
        editMethod(classPool, ctClass, editMethodList, false);

        // 修改构造方法
        final ArrayList<MethodMeta> constructorsMethodList = classPatch.getConstructorsMethodList();
        editMethod(classPool, ctClass, constructorsMethodList, true);

        ctClass.toClass(classLoader, ctClass.getClass().getProtectionDomain());
        ctClass.detach();

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
                String body = "{" + methodMeta.getBody() + "}";
                final Class returnType = methodMeta.getReturnType();
                final LinkedHashMap<String, Class> paramsType = methodMeta.getParams();

                final Set<String> paramsNameSet = paramsType.keySet();
                CtClass[] params = new CtClass[paramsNameSet.size()];
                final Object[] paramsNameArr = paramsNameSet.toArray();
                for (int i = 0; i < paramsNameArr.length; i++) {
                    body = replaceMethodBody(classPool, paramsType, body, params, paramsNameArr, i);
                }
                final CtClass ctClass1 = classPool.getCtClass(returnType.getTypeName());
                CtMethod ctMethod = new CtMethod(ctClass1, name, params, ctClass);
                ctClass.addMethod(ctMethod);
                String bodyStr = JavaFormat.formatJava(body);
                LogUtil.info("class-assist  ===  " + "methodName: " + name + "\t,addMethodBodyStr : \r\n" + bodyStr);
                ctMethod.setBody(bodyStr);
                ctMethod.setModifiers(Modifier.PUBLIC);
                ctClass.setModifiers(ctClass.getModifiers() & ~Modifier.ABSTRACT);
            }
        }
    }

    private void editMethod(ClassPool classPool, CtClass ctClass, ArrayList<MethodMeta> editMethodList, boolean isConstructor) throws NotFoundException, CannotCompileException {
        if (editMethodList != null && !editMethodList.isEmpty()) {
            for (MethodMeta methodMeta : editMethodList) {
                try {
                    final LinkedHashMap<String, Class> paramsType = methodMeta.getParams();
                    String body = "{" + methodMeta.getBody() + "}";
                    final String name = methodMeta.getName();
                    final int size = paramsType.size();
                    CtClass[] params = new CtClass[size];
                    final Set<String> paramsNameSet = paramsType.keySet();
                    final Object[] paramsNameArr = paramsNameSet.toArray();
                    for (int i = 0; i < size; i++) {
                        body = replaceMethodBody(classPool, paramsType, body, params, paramsNameArr, i);
                    }
                    body = body.replaceAll("\\sthis\\.", " \\$0");

                    String bodyStr = JavaFormat.formatJava(body);
                    CtBehavior ctBehavior = null;
                    if (isConstructor) {
                        ctBehavior = ctClass.getDeclaredConstructor(params);
                    } else {
                        ctBehavior = ctClass.getDeclaredMethod(name, params);
                    }
                    if (StringUtils.hasText(methodMeta.getBody())) {
                        ctBehavior.setBody(bodyStr);
                        LogUtil.info("class-assist  ===  " + "methodName: " + name + "\t,editMethodBodyStr: \r\n" + bodyStr);
                    }
                    if (StringUtils.hasText(methodMeta.getInsertBefore())) {
                        LogUtil.info("class-assist  ===  " + "methodName: " + name + "\t,insertBefore: \r\n" + methodMeta.getInsertBefore());
                        ctBehavior.insertBefore(methodMeta.getInsertBefore());
                    }
                    if (StringUtils.hasText(methodMeta.getInsertAfter())) {
                        LogUtil.info("class-assist  ===  " + "methodName: " + name + "\t,insertAfter: \r\n" + methodMeta.getInsertAfter());
                        ctBehavior.insertAfter(methodMeta.getInsertAfter());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private String replaceMethodBody(ClassPool classPool, LinkedHashMap<String, Class> paramsType, String body, CtClass[] params, Object[] paramsNameArr, int i) throws NotFoundException {
        String paramName = (String) paramsNameArr[i];
        body = body.replaceAll(paramName + replaceReg, "\\$" + (i + 1) + "$1");
        final Class paramType = paramsType.get(paramName);
        final String paramTypeStr = paramType.getTypeName();
        final CtClass paramTypeClass = classPool.getCtClass(paramTypeStr);
        params[i] = paramTypeClass;
        return body;
    }

}
