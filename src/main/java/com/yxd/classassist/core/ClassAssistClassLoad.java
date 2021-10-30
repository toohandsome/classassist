package com.yxd.classassist.core;

/**
 * @author hudcan
 */
public class ClassAssistClassLoad extends ClassLoader {

    public ClassAssistClassLoad(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}
