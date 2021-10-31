package io.github.toohandsome.classassist.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hudcan
 */
public interface IClassPatch {

    /**
     * 返回 需要修改的方法
     *
     * @return
     */
    ArrayList<MethodMeta> getEditMethodList();

    /**
     * 返回 需要新增的方法
     * @return
     */
    ArrayList<MethodMeta> getAddMethodList();

    /**
     * 返回需要新增的字段
     * @return
     */
    List<String> getAddFieldList();

    /**
     * 返回需要导入的包
     * @return
     */
    List<String> getImprotPackages();
}
