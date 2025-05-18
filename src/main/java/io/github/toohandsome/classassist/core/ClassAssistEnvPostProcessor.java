package io.github.toohandsome.classassist.core;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hudcan
 */
@Component
public class ClassAssistEnvPostProcessor implements EnvironmentPostProcessor {

    static Environment environment;

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment env, SpringApplication app) {
//        log.error("This should be printed");
        System.out.println("ClassAssistEnvPostProcessor.postProcessEnvironment");
        environment = env;
//        ConcurrentHashMap.KeySetView<IClassPatch, CtClass> iClassPatches = ClassAssistApplicationContextInitializer.todoMap.keySet();
//        for (IClassPatch iClassPatch : iClassPatches) {
//            try {
//                ClassAssistApplicationContextInitializer.classReplaceHandler.handler(
//                        ClassAssistApplicationContextInitializer.classLoader,
//                        ClassAssistApplicationContextInitializer.classPool,
//                        iClassPatch,
//                        ClassAssistApplicationContextInitializer.todoMap.get(iClassPatch));
//            } catch (CannotCompileException e) {
//                throw new RuntimeException(e);
//            } catch (NotFoundException e) {
//                throw new RuntimeException(e);
//            }
//        }

        ConcurrentHashMap<String, Class> classMap = ClassAssistApplicationContextInitializer.classMap;
        ConcurrentHashMap.KeySetView<String, Class> strings = classMap.keySet();
        for (String string : strings) {
            ClassAssistApplicationContextInitializer.handler(string, classMap.get(string));
        }

//        for (Class aClass : ClassAssistApplicationContextInitializer.todoList) {
//            ClassAssistApplicationContextInitializer.handler(aClass);
//        }

//        for (IClassPatch classPatch : ClassAssistApplicationContextInitializer.todoList) {
//
//            classReplaceHandler.handler(ClassAssistApplicationContextInitializer.classPool,classPatch,);
//        }
    }
}