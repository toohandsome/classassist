package io.github.toohandsome.classassist.core;

import javassist.Modifier;
import lombok.Data;

import java.util.LinkedHashMap;

/**
 * @author hudcan
 */
@Data
public class MethodMeta {

	private Modifier accessModifier;
	private boolean isFinal;
	private boolean isStatic;
	private String name;
	private String body;
	private LinkedHashMap<String, Class> params = new LinkedHashMap<>();
	private Class returnType;

}
