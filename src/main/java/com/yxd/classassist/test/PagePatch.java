//package com.yxd.classassist.mp;
//
//import com.yxd.classassist.annotation.ClassAssist;
//import com.yxd.classassist.core.IClassPatch;
//import com.yxd.classassist.core.MethodMeta;
//
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//
///**
// * @author hudcan
// */
//@ClassAssist(className = "com.baomidou.mybatisplus.extension.plugins.pagination.dialects.SQLServerDialect1")
//public class PagePatch implements IClassPatch {
//
//
//	@Override
//	public ArrayList<MethodMeta> getEditMethodList() {
//		final MethodMeta methodMeta = new MethodMeta();
//
//
//		methodMeta.setBody("{System.out.println(\"3333333333333333333333333\");  String sqlWithOrderBy = appendOrderBy($1);\n" +
//				"        String sql = sqlWithOrderBy + \" OFFSET \" + FIRST_MARK + \" ROWS FETCH NEXT \" + SECOND_MARK + \" ROWS ONLY\";\n" +
//				"        return new com.baomidou.mybatisplus.extension.plugins.pagination.DialectModel(sql, $2, $3).setConsumerChain();}");
//		methodMeta.setName("buildPaginationSql");
//		final LinkedHashMap<String, Class> objectObjectHashMap = new LinkedHashMap<>();
//		objectObjectHashMap.put("originalSql", String.class);
//		objectObjectHashMap.put("offset", long.class);
//		objectObjectHashMap.put("limit", long.class);
//		methodMeta.setParams(objectObjectHashMap);
//		final ArrayList<MethodMeta> objects1 = new ArrayList<>();
//		objects1.add(methodMeta);
//
//		return objects1;
//	}
//
//	@Override
//	public ArrayList<MethodMeta> getAddMethodList() {
//		final ArrayList<MethodMeta> objects = new ArrayList<>();
//		final MethodMeta methodMeta = new MethodMeta();
//		methodMeta.setName("appendOrderBy");
//		String bodyStr = " if (p.matcher($1).find()) {" +
//				"return $1;\n" +
//				"} else {" +
//				"return $1 + \" ORDER BY CURRENT_TIMESTAMP\";" +
//				"}";
//		methodMeta.setBody(bodyStr);
//		final LinkedHashMap<String, Class> objectObjectLinkedHashMap = new LinkedHashMap<>();
//		objectObjectLinkedHashMap.put("sql", String.class);
//		methodMeta.setParams(objectObjectLinkedHashMap);
//		methodMeta.setReturnType(String.class);
//		objects.add(methodMeta);
//		return objects;
//	}
//
//	@Override
//	public List<String> getAddFieldList() {
//		final ArrayList<String> objects = new ArrayList<>();
//		objects.add("public final static Pattern p = Pattern.compile(\".*\\\\s+order\\\\s+by\\\\s+.*\", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);");
//		return objects;
//
//	}
//
//	@Override
//	public List<String> getImprotPackages() {
//		final ArrayList<String> objects = new ArrayList<>();
//		objects.add("java.util.regex.Pattern");
//		return objects;
//	}
//}
