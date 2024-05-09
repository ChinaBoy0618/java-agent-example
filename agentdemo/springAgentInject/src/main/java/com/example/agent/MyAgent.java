package com.example.agent;

//import com.example.popertieslauncher.TestController;

import com.example.agent.classloading.CustomClassLoader;
import com.example.agent.classloading.ShadeClassLoader;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.*;

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

    //    public static ClassLoader customerClassLoader;
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("premain started");
        //get class
        try {
            ClassLoader cl = MyAgent.class.getClassLoader();
//            if (cl != null) {
//                Annotation[] annotations = cl.loadClass("com.example.agent.template.TestController").getAnnotations();
//                if (annotations != null) {
//                    for (Annotation annotation : annotations) {
//                        System.out.println("TestController Annotation: " + annotation);
//                    }
//                } else {
//                    System.out.println("TestController Annotation is null ");
//                }
//            } else {
//                System.out.println("ClassLoader Annotation is null ");
//            }
            System.out.println("Agent classloader " + cl);
        } catch (Exception ex) {
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
                        declaresAnnotation(annotationType(named("org.springframework.boot.autoconfigure.SpringBootApplication")))
//                                .or(named("org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider"))
//                                .or(named("org.springframework.core.io.support.PathMatchingResourcePatternResolver"))
//                                .or(named("org.springframework.core.io.support.PathMatchingResourcePatternResolver"))
//                        named("org.springframework.core.io.support.PathMatchingResourcePatternResolver")
//                        named("org.springframework.core.io.DefaultResourceLoader")
//                        named("org.springframework.core.type.classreading.SimpleAnnotationMetadataReadingVisitor")
//                        named("sun.reflect.generics.factory.CoreReflectionFactory")
                )
//                .transform(new AgentBuilder.Transformer() {
//                    @Override
//                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
//                        System.out.println("SimpleAnnotationMetadataReadingVisitor transform installed:" + classLoader + "|TypeDescription:" + typeDescription);
//                        return builder.method(
////                                named("getClassLoader")
////                                named("getResource")
//                                named("getMetadata")
//                        )
////                                    .intercept(MethodDelegation.to(MyInterceptor.class));
//                                .intercept(Advice.to(AdviceClass6.class));
////                        .intercept(MethodDelegation.to(MyInterceptor.class));
//                    }
//                })
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

//                .transform(new AgentBuilder.Transformer() {
//                    @Override
//                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
//                        System.out.println("SpringBootApplication transformer installed:" + classLoader + "|TypeDescription:" + typeDescription);
//                        MyAgent.classLoader = classLoader;
//
////                        customerClassLoader=new CustomClassLoader()
//
//                        return builder.method(
//                                named("main")
//                        )
//                                .intercept(Advice.to(AdviceClass.class));
//                    }
//                })
//                .transform(new AgentBuilder.Transformer() {
//                    @Override
//                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
//                        System.out.println("SpringBootApplication transformer installed:" + classLoader + "|TypeDescription:" + typeDescription);
//                        MyAgent.classLoader = classLoader;
//
////                        customerClassLoader=new CustomClassLoader()
//
//                        return builder.method(
//                                named("doFindAllClassPathResources")
//                        )
//                                .intercept(Advice.to(AdviceClass9.class));
//                    }
//                })


                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                        System.out.println("SpringBootApplication transformer installed:" + classLoader + "|TypeDescription:" + typeDescription);
                        MyAgent.classLoader = classLoader;

//                        customerClassLoader=new CustomClassLoader()

                        return builder.method(
                                named("main")
                        )
                                .intercept(Advice.to(AdviceClass8.class));
                    }
                })
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
//        //        @Advice.OnMethodEnter
////        private static void onMethodEnter(@Advice.This Object obj) {
////            System.out.println(" addCandidateComponentsFromIndex:" + obj);
//////            print( getTypeField(obj, "includeFilters"),"includeFilters");
//////            print( getTypeField(obj, "excludeFilters"),"excludeFilters");
////        }
//        @Advice.OnMethodEnter
//        private static void onMethodEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] allArguments) {
//            System.out.println(method + " :" + allArguments[0]);
//            //get AppClassLoader
////            ClassLoader cl=classLoader.getParent();
////            System.out.println("main() inject start:" + classLoader);
////            new ClassInjector.UsingReflection(cl).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(TestController.class),
////                    ClassFileLocator.ForClassLoader.read(TestController.class)));
////            new ClassInjector.UsingReflection(cl).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(RestController.class),
////                    ClassFileLocator.ForClassLoader.read(RestController.class)));
////            new ClassInjector.UsingReflection(cl).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(RequestMapping.class),
////                    ClassFileLocator.ForClassLoader.read(RequestMapping.class)));
////            new ClassInjector.UsingReflection(cl).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(GetMapping.class),
////                    ClassFileLocator.ForClassLoader.read(GetMapping.class)));
////            new ClassInjector.UsingReflection(cl).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(ResponseBody.class),
////                    ClassFileLocator.ForClassLoader.read(ResponseBody.class)));
////            System.out.println(cl + " inject " + TestController.class + " succeed");
//        }
//        @Advice.OnMethodExit
//        @Advice.AssignReturned.ToThrown(index = 1)
//        static Type exit(@Advice.Origin Method method,@Advice.Argument(value = 0) String  name, @Advice.Thrown Throwable throwable) throws Throwable{
//            System.out.println(method+"thrown name:"+name+"|throwable:"+throwable);
//            if(throwable!=null&&name.contains("org.springframework.web.bind.annotation")){
//                System.out.println(method+"thrown name:"+name+"|throwable:"+throwable+" bingo");
//                try{
//                    return Class.forName(name,false,classLoader);
//                }catch (Exception ex){
//                    System.out.println(method+"Load Class error");
//                    ex.printStackTrace();
//                    throw  ex;
//                }
//            }
//            throw  throwable;
//        }
//
//    }


    public static class AdviceClass8 {
        @Advice.OnMethodEnter
        private static void onMethodEnter(@Advice.Origin Method method) {
            System.out.println(method + " enter Loader class");

            try {
//                ProtectionDomain protectionDomain = PrivilegedActionUtils.getProtectionDomain(MyAgent.class);
//
//                String path = protectionDomain.getCodeSource().getLocation().toURI().getSchemeSpecificPart();

                CustomClassLoader cl = new CustomClassLoader(new URL[]{
//                new File("/Users/zhanguowang/Desktop/project/github/java-agent-example/agent/springAgentInject/target/springAgentInject-1-SNAPSHOT.jar").toURL(),
                        new File("/Users/zhanguowang/Documents/popertiesLauncher-0.0.1-SNAPSHOT.jar").toURL()
                }, MyAgent.classLoader, "template","com/example/agentdemo/","com/example/popertieslauncher/","jar:file:/Users/zhanguowang/Documents/popertiesLauncher-0.0.1-SNAPSHOT.jar!/com/example/popertieslauncher/");
                Class<?> t = cl.loadClass("com.example.agent.template.TestController");
                byte[] clazz = cl.getShadedClassBytes("com.example.agent.template.TestController");
                System.out.println(method + " enter loaded class:" + t);
//
                URL[] appUrls = ((URLClassLoader) classLoader.getParent()).getURLs();
                //inject url to LaunchedURLClassLoader
                new com.example.agent.utils.ClassInjector().injectToURLClassLoader(Arrays.asList(appUrls) ,(URLClassLoader)cl);
                System.out.println(method + " inject url to CustomClassLoader:" + cl);
                URL[] urls = ((URLClassLoader) cl).getURLs();
                for (URL url:urls){
                    System.out.println(method + " inject url to CustomClassLoader:" + url);
                }

                Thread.currentThread().setContextClassLoader(new ShadeClassLoader(cl));
                System.out.println(method + " set thread class loader:" + cl);
//                new ClassInjector.UsingReflection(classLoader).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(t),
//                        clazz));
//                Class<?> t1 = classLoader.loadClass("com.example.agent.template.TestController");
//                System.out.println(method + " inject loaded class:" + t1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    //    public static class AdviceClass6 {
//        @Advice.OnMethodExit
//        private static void onMethodEnter(@Advice.Origin Method method, @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object obj) {
//            System.out.println(method + " enter:" + obj);
//            try {
//
//                AnnotationMetadata metadata = (AnnotationMetadata) obj;
//                Set<String> annotationTypes = metadata.getAnnotationTypes();
//                for (String v : annotationTypes) {
//                    System.out.println(metadata.getClassName() + "|annotation|" + v);
//                }
//            } catch (Exception ex) {
//                System.out.println(method+"Advice6 error");
//                ex.printStackTrace();
//            }
//        }
//
//
//    }
//    public static class AdviceClass5 {
//        @Advice.OnMethodEnter
//        private static void onMethodEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] allArguments) {
//            System.out.println(method+" :"+allArguments[0]);
//            //MetadataReader metadataReader
//        }
//
//
//    }
    public static class AdviceClass {

        @Advice.OnMethodEnter
        public static void onMethodEnter(@Advice.Origin Method method) {
            System.out.println(method + " onMethodEnter:" + classLoader);
            try {
//                PrivilegedActionUtils.action(new PrivilegedExceptionAction<Object>() {
//                    @Override
//                    public Object run() throws Exception {
//                        new ClassInjector().loaderAgentJarToClassLoader(MyAgent.classLoader);
//                        return null;
//                    }
//                });
                if (classLoader.equals(ClassLoader.getSystemClassLoader())) {
                    System.out.println("classLoader is AppClassLoader:" + classLoader);
                    return;
                }
                System.out.println("main() inject start:" + classLoader);
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
//                Annotation[] annotations = clazz.getAnnotations();
//                for (Annotation annotation : annotations) {
//                    System.out.println(classLoader + " inject " + TestController.class + " annotation:" + annotation);
//                }
                new com.example.agent.utils.ClassInjector().loaderAgentJarToClassLoader(classLoader);
//                new ClassInjector.UsingReflection(classLoader).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(TestController.class),
//                        ClassFileLocator.ForClassLoader.read(TestController.class)));
//            Class clazz=   classLoader.loadClass(TestController.class.getName());
//                System.out.println("transform ClassInjector.UsingReflection:"+clazz);
            } catch (Exception ex) {
                System.out.println("main() transform error");
                ex.printStackTrace();
            }
        }
    }

//    public static class AdviceClass9 {
//
//        @Advice.OnMethodExit
//        public static void OnMethodExit(@Advice.Origin Method method,@Advice.This Object object, @Advice.Return(readOnly = false,typing = Assigner.Typing.DYNAMIC) Object obj ) {
//            System.out.println(method + " OnMethodExit:" + obj);
//            try {
//                Object[] sources=   (Object[])obj;
//                Class<?> clazz= classLoader.loadClass("org.springframework.core.io.support.PathMatchingResourcePatternResolver");
//                Method method1= clazz.getMethod("addAllClassLoaderJarRoots");
//                method1.setAccessible(true);
//                method1.invoke(object,classLoader,obj);
//
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
