package io.github.toohandsome.classassist.core;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author hudcan
 */
public class ClassAssistClassLoad extends ClassLoader {

    public ClassAssistClassLoad(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
//        System.out.println("ClassAssistClassLoad  ===  " + name);
        if ("com.example.demo.controller.TestController".equals(name)) {
            return classAssistClassLoadFindClass();
        }
        return super.findClass(name);
    }

    private Class classAssistClassLoadFindClass() {

        byte[] cLassBytes = null;
        Path path;
        try {
            path = Paths.get(new URI("file:///C:\\Users\\Administrator\\Downloads\\demo\\target\\classes\\com\\example\\demo\\controller\\TestController.class"));
            cLassBytes = Files.readAllBytes(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Class cLass = defineClass("com.example.demo.controller.TestController", cLassBytes, 0, cLassBytes.length);
        return cLass;
    }
}
