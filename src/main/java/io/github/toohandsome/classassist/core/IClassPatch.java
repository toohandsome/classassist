package io.github.toohandsome.classassist.core;

import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hudcan
 */
public interface IClassPatch {


    default Environment getEnv() {
        return ClassAssistEnvPostProcessor.environment;
    }

    /**
     *
     */
    default boolean condition() {
        return true;
    }

    /**
     * 返回 需要修改的方法
     */
    ArrayList<MethodMeta> getEditMethodList();

    /**
     * 返回 需要新增的方法
     */
    default ArrayList<MethodMeta> getAddMethodList() {
        return new ArrayList<>();
    }

    /**
     * 返回需要新增的字段
     */
    default List<String> getAddFieldList() {
        return new ArrayList<>();
    }

    /**
     * 返回需要导入的包
     */
    default List<String> getImprotPackages() {
        return new ArrayList<>();
    }

    /**
     * 返回需要修改的构造方法
     */
    default ArrayList<MethodMeta> getConstructorsMethodList() {
        return new ArrayList<>();
    }
}
