# classassist

一款帮助修改第三方包中class的工具,不需要agent,不需要添加启动参数, 只需添加一个注解和实现一个接口即可,降低用户对 javassist 的使用成本

项目起源 : mybatis-plus 3.4.+ 版本中 sqlserver分页查询必须带上 order by 条件

本人在公司负责框架升级,为了解决mp某个版本逻辑删除处的bug,升级3.4.+版本,发现分页报错

github上查询后得知需带上 order by 条件, 但是公司现有业务系统很多查询并没有带上此条件,因为推广升级阻力很大,由此产生该项目

原理是借助 javassist 修改 mp 中 sqlserver 对应的分页逻辑 class 中的字节码

经过拓展现在可以修改任意class,如 某些历史遗留代码已经被很多业务系统引用,此时业务系统肯定是不愿意配合升级,可以使用该项目将里面的逻辑替换为最新的逻辑,实现无缝升级

## 1. 增加依赖

```
<dependency>
    <groupId>io.github.toohandsome</groupId>
    <artifactId>class-assist</artifactId>
    <version>1.1.0</version>
</dependency>
```

## 2. 在配置文件中配置扫描包路径

### properties 配置

```
# 控制扫描包
class-assist.scan = com.xxx.xxx
# 控制是否开启功能
class-assist.enable = true
# 控制日志打印
class-assist.log = true
```

### yml/yaml 配置

```
class-assist:
  scan: com.xxx.xxx
```

配置读取顺序 resources 目录下的

```
1: bootstrap.properties
2: application.properties
3: config/application.properties
4: bootstrap.yaml
5: application.yaml
6: config/application.yaml
7: bootstrap.yml
8: application.yml
```

## 3. 新建一个patch类

### 添加注解

```
@ClassAssist(className = "com.xxx.TestServiceImpl") 
```

1. className 为需要修改的class全路径
2. 注解写在 class 上, 示例:

```aspectj

@ClassAssist(className = "TestApp")
public class TestPatch implements IClassPatch {
}
```

## 4. 实现 IClassPatch 接口

IClassPatch 中目前有四个需要重写的方法，后期会增加，以支持注解等功能

#### getEditMethodList

```
/**
 * 返回 需要修改的方法
 *
 * @return
 */
ArrayList<MethodMeta> getEditMethodList();

```

#### getConstructorsMethodList

```
/**
 * 返回 需要修改的构造方法
 *
 * @return
 */
ArrayList<MethodMeta> getConstructorsMethodList();

```

#### getAddMethodList

```
/**
 * 返回 需要新增的方法
 * @return
 */
ArrayList<MethodMeta> getAddMethodList();

```

#### getAddFieldList

```
/**
 * 返回需要新增的字段
 * @return
 */
List<String> getAddFieldList();

```

#### getImprotPackages

```
/**
 * 返回需要导入的包
 * @return
 */
List<String> getImprotPackages();

```

## 5. 重写对应的方法
此处以mp为示例,此处逻辑修改来源于 https://github.com/baomidou/mybatis-plus/issues/3969
```
package com.example.demo.mp;

import ClassAssist;
import IClassPatch;
import MethodMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author hudcan
 */
@ClassAssist(className = "com.baomidou.mybatisplus.extension.plugins.pagination.dialects.SQLServerDialect")
public class PagePatch implements IClassPatch {


    @Override
    public ArrayList<MethodMeta> getEditMethodList() {
        final MethodMeta methodMeta = new MethodMeta();


        methodMeta.setBody("System.out.println(\"3333333333333333333333333\");  String sqlWithOrderBy = appendOrderBy(originalSql);\n" +
                "        String sql = sqlWithOrderBy + \" OFFSET \" + FIRST_MARK + \" ROWS FETCH NEXT \" + SECOND_MARK + \" ROWS ONLY\";\n" +
                "        return new com.baomidou.mybatisplus.extension.plugins.pagination.DialectModel(sql,offset, limit).setConsumerChain();");
        methodMeta.setName("buildPaginationSql");
        final LinkedHashMap<String, Class> objectObjectHashMap = new LinkedHashMap<>();
        objectObjectHashMap.put("originalSql", String.class);
        objectObjectHashMap.put("offset", long.class);
        objectObjectHashMap.put("limit", long.class);
        methodMeta.setParams(objectObjectHashMap);
        final ArrayList<MethodMeta> objects1 = new ArrayList<>();
        objects1.add(methodMeta);

        return objects1;
    }

    @Override
    public ArrayList<MethodMeta> getAddMethodList() {

        final ArrayList<MethodMeta> objects = new ArrayList<>();
        final MethodMeta methodMeta = new MethodMeta();
        methodMeta.setName("appendOrderBy");
        String bodyStr = " if (p.matcher(sql).find()) {" +
                "return sql;\n" +
                "} else {" +
                "return sql + \" ORDER BY CURRENT_TIMESTAMP\";}";
        methodMeta.setBody(bodyStr);
        final LinkedHashMap<String, Class> objectObjectLinkedHashMap = new LinkedHashMap<>();
        objectObjectLinkedHashMap.put("sql", String.class);
        methodMeta.setParams(objectObjectLinkedHashMap);
        methodMeta.setReturnType(String.class);
        objects.add(methodMeta);
        return objects;
    }

    @Override
    public List<String> getAddFieldList() {
        final ArrayList<String> objects = new ArrayList<>();
        objects.add("public final static Pattern p = Pattern.compile(\".*\\\\s+order\\\\s+by\\\\s+.*\", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);");
        return objects;

    }

    @Override
    public List<String> getImprotPackages() {
        final ArrayList<String> objects = new ArrayList<>();
        objects.add("java.util.regex.Pattern");
        return objects;
    }
}

```
