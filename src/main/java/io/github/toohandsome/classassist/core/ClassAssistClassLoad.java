package io.github.toohandsome.classassist.core;

import org.springframework.boot.SpringApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author hudcan
 */
public class ClassAssistClassLoad extends ClassLoader {
    //AppClassLoader的父类加载器
    private ClassLoader extClassLoader;
    private static final String CLASS_FILE_SUFFIX = ".class";

    public ClassAssistClassLoad(ClassLoader parent) {
        if (parent == null) {
            parent = getSystemClassLoader();
            while (parent.getParent() != null) {
                parent = parent.getParent();
            }
        }
        this.extClassLoader = parent;
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        Class cls = null;
        cls = findLoadedClass(name);
        if (cls != null) {
            return cls;
        }
        //获取ExtClassLoader
        ClassLoader extClassLoader = getExtClassLoader();
        //确保自定义的类不会覆盖Java的核心类
        try {
            cls = extClassLoader.loadClass(name);
            if (cls != null) {
                return cls;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        cls = findClass(name);
        return cls;
    }

    @Override
    public Class<?> findClass(String name) {
        byte[] bt = loadClassData(name);
        return defineClass(name, bt, 0, bt.length);
    }

    private byte[] loadClassData(String className) {
        // 读取Class文件呢
        InputStream is = getClass().getClassLoader().getResourceAsStream(className.replace(".", "/") + CLASS_FILE_SUFFIX);
        ByteArrayOutputStream byteSt = new ByteArrayOutputStream();
        // 写入byteStream
        int len = 0;
        try {
            while ((len = is.read()) != -1) {
                byteSt.write(len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 转换为数组
        return byteSt.toByteArray();
    }

    public ClassLoader getExtClassLoader() {
        return extClassLoader;
    }

//    @Override
//    protected Class<?> findClass(String name) throws ClassNotFoundException {
////        System.out.println("ClassAssistClassLoad  ===  " + name);
//        if ("io.github.toohandsome.classassist.test.TestApp".equals(name)) {
//            return classAssistClassLoadFindClass();
//        }
//        return super.findClass(name);
//    }
//
//    private Class classAssistClassLoadFindClass() {
//
//        byte[] cLassBytes = null;
//        Path path;
//        try {
//            path = Paths.get(new URI("file:///D:\\java\\git\\classassist\\target\\classes\\io\\github\\toohandsome\\classassist\\test\\TestApp.class"));
//            cLassBytes = Files.readAllBytes(path);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Class cLass = defineClass("io.github.toohandsome.classassist.test.TestApp", cLassBytes, 0, cLassBytes.length);
//        return cLass;
//    }

}
