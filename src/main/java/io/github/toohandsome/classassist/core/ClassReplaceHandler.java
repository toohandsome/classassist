package io.github.toohandsome.classassist.core;

import io.github.toohandsome.classassist.util.JavaFormat;
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

    String replaceReg = "([\\.|\\s|\\|;|,)])";

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
                String bodyStr = JavaFormat.formJava(body);
                System.out.println("class-assist  ===  addMethodBodyStr: \r\n" + bodyStr);
                ctMethod.setBody(bodyStr);
                ctMethod.setModifiers(Modifier.PUBLIC);
                ctClass.setModifiers(ctClass.getModifiers() & ~Modifier.ABSTRACT);
            }
        }
    }

    private void editMethod(ClassPool classPool, CtClass ctClass, ArrayList<MethodMeta> editMethodList) throws NotFoundException, CannotCompileException {
        if (editMethodList != null && !editMethodList.isEmpty()) {
            for (MethodMeta methodMeta : editMethodList) {
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
                CtMethod method = ctClass.getDeclaredMethod(name, params);
                String bodyStr = JavaFormat.formJava(body);
                System.out.println("class-assist  ===  insertBefore: \r\n" + methodMeta.getInsertBefore());
                System.out.println("class-assist  ===  editMethodBodyStr: \r\n" + bodyStr);
                System.out.println("class-assist  ===  insertAfter: \r\n" + methodMeta.getInsertAfter());
                if (StringUtils.hasText(methodMeta.getBody())) {
                    method.setBody(bodyStr);
                }
                if (StringUtils.hasText(methodMeta.getInsertBefore())) {
                    method.insertBefore(methodMeta.getInsertBefore());
                }
                if (StringUtils.hasText(methodMeta.getInsertAfter())) {
                    method.insertAfter(methodMeta.getInsertAfter());
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
