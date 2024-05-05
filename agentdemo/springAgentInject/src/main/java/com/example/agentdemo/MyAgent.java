package com.example.agentdemo;

//import com.example.popertieslauncher.TestController;

import com.example.agentdemo.template.TestController;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.utility.JavaModule;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author wzg
 * @date 2024/4/11 15:22
 */
public class MyAgent {
    //    public static void main(String[] args) throws URISyntaxException, MalformedURLException {
//        new ClassInjector().loaderAgentJarToClassLoader();
//    }
    // transform's CL
    public static ClassLoader classLoader;

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("premain started");
        //get class
        try {
            ClassLoader cl= MyAgent.class.getClassLoader();
            if(cl!=null){
                Annotation[] annotations = cl.loadClass("com.example.agentdemo.template.TestController").getAnnotations();
                if(annotations!=null){
                    for(Annotation annotation:annotations){
                        System.out.println("TestController Annotation: "+annotation);
                    }
                }else{
                    System.out.println("TestController Annotation is null ");
                }
            }else{
                System.out.println("ClassLoader Annotation is null ");
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }



        class ErrorLoggingListener extends AgentBuilder.Listener.Adapter {

            @Override
            public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
                System.out.println("bytebuddy error typeName:" + typeName + "|classLoader:" + classLoader + "|JavaModule:" + module
                        + "|loaded:" + loaded + "|throwable:" + throwable.getMessage()
                );
                throwable.printStackTrace();
            }
        }


//        securityManagerCheck();
        new AgentBuilder.Default()
//                    .type(declaresMethod(isMain()))
//                .type(named("org.springframework.boot.loader.JarLauncher"))
                .type(
//                        declaresAnnotation(annotationType(named(Adapter"org.springframework.boot.autoconfigure.SpringBootApplication")))
//                                .or(named("org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider"))
//                                .or(named("org.springframework.core.io.support.PathMatchingResourcePatternResolver"))
//                        named("org.springframework.core.io.support.PathMatchingResourcePatternResolver")
//                        named("org.springframework.core.io.DefaultResourceLoader")
                        named("org.springframework.core.type.classreading.SimpleAnnotationMetadataReadingVisitor")
                )
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                        System.out.println("SimpleAnnotationMetadataReadingVisitor transform installed:" + classLoader + "|TypeDescription:" + typeDescription);
                        return builder.method(
//                                named("getClassLoader")
//                                named("getResource")
                                named("getMetadata")
                        )
//                                    .intercept(MethodDelegation.to(MyInterceptor.class));
                                .intercept(Advice.to(AdviceClass6.class));
//                        .intercept(MethodDelegation.to(MyInterceptor.class));
                    }
                })
//                .transform(new AgentBuilder.Transformer() {
//                    @Override
//                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
//                        System.out.println("SpringBootApplication transform installed:" + classLoader + "|TypeDescription:" + typeDescription);
//                        MyAgent.classLoader = classLoader;
//
//                        return builder.method(
//                                //isMain()
////                                named("launch")
////                                        .and(takesArgument(0, named("java.lang.ClassLoader")))
////                                        .and(takesArgument(1, named("java.lang.String")))
////                                        .and(takesArgument(2, named("java.lang.String[]")))
//                                named("main")
//                        )
////                                    .intercept(MethodDelegation.to(MyInterceptor.class));
//                                .intercept(Advice.to(AdviceClass.class));
//                    }
//                })
//                .transform(new AgentBuilder.Transformer() {
//                    @Override
//                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
//                        System.out.println("ClassPathScanningCandidateComponentProvider transformer "+classLoader);
//                        return builder.method(named("isCandidateComponent")
////                                .and(isPrivate())
////                                .and(takesArgument(0, named("org.springframework.context.index.CandidateComponentsIndex")))
////                                .and(takesArgument(1, named("java.lang.String")))
//                        )
//                                .intercept(Advice.to(AdviceClass.class));
//                    }
//                })
//                .transform(new AgentBuilder.Transformer() {
//                    @Override
//                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
//                        System.out.println("PathMatchingResourcePatternResolver transformer installed:" + classLoader + "|TypeDescription:" + typeDescription);
//                        return builder.method(
//                                named("findAllClassPathResources")
//                                        .and(isProtected())
//                                        .and(takesArgument(0, named("java.lang.String")))
//                        )
//                                .intercept(Advice.to(AdviceClass2.class));
//                    }
//                })
                .with(new ErrorLoggingListener())
                .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
//                .disableClassFormatChanges()
                .installOn(instrumentation);

        System.out.println("premain ended");


    }

//    static List<TypeFilter> getTypeField(Object obj, String name) {
//        try {
//            Class clazz = ClassPathScanningCandidateComponentProvider.class;
//            Field field = clazz.getField(name);
//            field.setAccessible(true);
//            return (List<TypeFilter>) field.get(obj);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return Collections.emptyList();
//    }
//
//    static void print(List<TypeFilter> filters, String fieldName) {
//        try {
//            for (TypeFilter filter : filters) {
//                System.out.println(fieldName + "|" + filter.toString());
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

//    public static class AdviceClass2 {
//        @Advice.OnMethodEnter
//        private static void onMethodEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] allArguments) {
//            System.out.println("findAllClassPathResources arg " + allArguments[0]);
//            System.out.println("findAllClassPathResources method " + method);
//        }
//
////        @Advice.OnMethodExit
////        public static void onMethodExit(@Advice.Return Object resources) {
////            try {
////                for (Resource resource : (Resource[])resources) {
////                    System.out.println("findAllClassPathResources URL:" + resource.getFilename());
////                }
////            } catch (Exception ex) {
////                ex.printStackTrace();
////            }
////        }
//    }

//    public static class AdviceClass {
////        @Advice.OnMethodEnter
////        private static void onMethodEnter(@Advice.This Object obj) {
////            System.out.println(" addCandidateComponentsFromIndex:" + obj);
//////            print( getTypeField(obj, "includeFilters"),"includeFilters");
//////            print( getTypeField(obj, "excludeFilters"),"excludeFilters");
////        }
//        @Advice.OnMethodEnter
//        private static void onMethodEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] allArguments) {
//            System.out.println(method+" :"+allArguments[0]);
//            //MetadataReader metadataReader
//        }


    //    }
    public static class AdviceClass6 {
        @Advice.OnMethodExit
        private static void onMethodEnter(@Advice.Origin Method method, @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object obj) {
            System.out.println(method + " enter:" + obj);
            try {

                AnnotationMetadata metadata = (AnnotationMetadata) obj;
                Set<String> annotationTypes = metadata.getAnnotationTypes();
                for (String v : annotationTypes) {
                    System.out.println(metadata.getClassName() + "|annotation|" + v);
                }
            } catch (Exception ex) {
                System.out.println(method+"Advice6 error");
                ex.printStackTrace();
            }
        }


    }
//    public static class AdviceClass5 {
//        @Advice.OnMethodEnter
//        private static void onMethodEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] allArguments) {
//            System.out.println(method+" :"+allArguments[0]);
//            //MetadataReader metadataReader
//        }
//
//
//    }
//    public static class AdviceClass {
//
//        @Advice.OnMethodEnter
//        public static void onMethodEnter(@Advice.Origin Method method) {
//            System.out.println(method + " onMethodEnter:" + classLoader);
//            try {
////                PrivilegedActionUtils.action(new PrivilegedExceptionAction<Object>() {
////                    @Override
////                    public Object run() throws Exception {
////                        new ClassInjector().loaderAgentJarToClassLoader(MyAgent.classLoader);
////                        return null;
////                    }
////                });
//                if (classLoader.equals(ClassLoader.getSystemClassLoader())) {
//                    System.out.println("classLoader is AppClassLoader:" + classLoader);
//                    return;
//                }
//                System.out.println("main() inject start:" + classLoader);
//                new ClassInjector.UsingReflection(classLoader).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(TestController.class),
//                        ClassFileLocator.ForClassLoader.read(TestController.class)));
//                new ClassInjector.UsingReflection(classLoader).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(RestController.class),
//                        ClassFileLocator.ForClassLoader.read(RestController.class)));
//                new ClassInjector.UsingReflection(classLoader).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(RequestMapping.class),
//                        ClassFileLocator.ForClassLoader.read(RequestMapping.class)));
//                new ClassInjector.UsingReflection(classLoader).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(GetMapping.class),
//                        ClassFileLocator.ForClassLoader.read(GetMapping.class)));
//                new ClassInjector.UsingReflection(classLoader).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(ResponseBody.class),
//                        ClassFileLocator.ForClassLoader.read(ResponseBody.class)));
//                System.out.println(classLoader + " inject " + TestController.class + " succeed");
//                Class clazz = classLoader.loadClass(TestController.class.getName());
//                System.out.println(classLoader + " load " + clazz + " succeed");
////                new com.example.agentdemo.utils.ClassInjector().loaderAgentJarToClassLoader(classLoader);
////                new ClassInjector.UsingReflection(classLoader).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(TestController.class),
////                        ClassFileLocator.ForClassLoader.read(TestController.class)));
////            Class clazz=   classLoader.loadClass(TestController.class.getName());
////                System.out.println("transform ClassInjector.UsingReflection:"+clazz);
//            } catch (Exception ex) {
//                System.out.println("main() transform error");
//                ex.printStackTrace();
//            }
//        }
//    }


    //TODO 动态增强功能实现方式调研
    // 思路 1:在启动类加入 implements ApplicationContextAware ，然后在 setApplicationContext 触发时进行bean注入
    // 问题：CL问题，通过agent cl可能解决
    // 思路 2：在指定的spring 初始化类型中，把对应的类文件加进去
    // 问题：路径注入成功，未解析
    // 思路 3：通过切spring部分的类，进行类型注入
//    static  void injectClass( ProtectionDomain protectionDomain)throws URISyntaxException, IOException {
////        securityManagerCheck();
////        System.out.println(AgentJarLocator.getAppJarFile(protectionDomain).getAbsolutePath());
//        DynamicType.Unloaded unloaded= new ByteBuddy()
//                .with(new NamingStrategy.AbstractBase() {
//                    @Override
//                    protected String name(TypeDescription superClass) {
//                        return "i.love.ByteBuddy." + superClass.getSimpleName();
//                    }
//
//                })
//                .subclass(TestControllerParent.class)
//                .make();
//        unloaded.inject(AgentJarLocator.getAppJarFile(protectionDomain));
//    }
//    @SuppressWarnings("removal")
//    private static void securityManagerCheck() {
//        SecurityManager sm = System.getSecurityManager();
//        if (sm == null) {
//            return;
//        }
//        try {
//            sm.checkPermission(new AllPermission());
//        } catch (SecurityException e) {
//            // note: we can't get the actual path of the agent here as the Security Manager might prevent us from finding our own jar.
//            System.out.println("Security manager without agent grant-all permission, adding the following snippet to security policy is recommended:");
//            System.out.println("grant codeBase \"file:/path/to/elastic-apm-agent.jar\" {");
//            System.out.println("    permission java.security.AllPermission;");
//            System.out.println("};");
//            e.printStackTrace();
//        }
//    }
}
