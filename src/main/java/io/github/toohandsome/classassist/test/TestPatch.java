//package io.github.toohandsome.classassist.test;
//
//
//import io.github.toohandsome.classassist.annotation.ClassAssist;
//import io.github.toohandsome.classassist.core.IClassPatch;
//import io.github.toohandsome.classassist.core.MethodMeta;
//
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//
///**
// * @author hudcan
// */
//@ClassAssist(className = "io.github.toohandsome.classassist.test.TestApp")
//public class TestPatch implements IClassPatch {
//
//
//	@Override
//	public ArrayList<MethodMeta> getEditMethodList() {
//		final MethodMeta methodMeta = new MethodMeta();
//
//
//		methodMeta.setBody("System.out.println(\"test22222222222222221\"); return \"abc\";");
//		methodMeta.setName("retString");
//		final LinkedHashMap<String, Class> objectObjectHashMap = new LinkedHashMap<>();
//
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
