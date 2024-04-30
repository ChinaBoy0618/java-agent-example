package com.example.agentdemo;

//import com.example.popertieslauncher.TestController;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Collections;
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
//        securityManagerCheck();
        new AgentBuilder.Default()
//                    .type(declaresMethod(isMain()))
//                .type(named("org.springframework.boot.loader.JarLauncher"))
                .type(declaresAnnotation(annotationType(named("org.springframework.boot.autoconfigure.SpringBootApplication"))))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                        System.out.println("transform started:" + classLoader);
                        MyAgent.classLoader = classLoader;
                        //                            try{
//
////                          0       System.out.println(AgentJarLocator.getAppJarFile(protectionDomain).getAbsolutePath());
////                                injectClass(protectionDomain);
//                            }catch (URISyntaxException uriSyntaxException){
//                                uriSyntaxException.printStackTrace();
//                            }
//                            catch (Exception ex){
//                                ex.printStackTrace();
//                            }

//                            return null;
                        return builder.method(
//                                named("launch")
//                                        .and(takesArgument(0, named("java.lang.ClassLoader")))
//                                        .and(takesArgument(1, named("java.lang.String")))
//                                        .and(takesArgument(2, named("java.lang.String[]")))
                                named("main")
                        )
//                                    .intercept(MethodDelegation.to(MyInterceptor.class));
                                .intercept(Advice.to(AdviceClass.class));
                    }
                })
                .installOn(instrumentation);

        System.out.println("premain ended");


    }

    public static class AdviceClass {
//        @Advice.OnMethodEnter()
//        public static void onMethodEnter( @Advice.Origin Method method, @Advice.AllArguments Object[] allArguments) {
////            System.out.println("setApplicationContext "+applicationContext);
//            System.out.println("setApplicationContext method enter");
//            System.out.println("setApplicationContext arg "+allArguments[0]);
//            System.out.println("setApplicationContext method "+method);
//        }

//        @Advice.OnMethodExit()
//        public static void onMethodExit(@Advice.Return(
//                readOnly = false) Set<URL> set) {
////            System.out.println("setApplicationContext "+applicationContext);
////            System.out.println("setApplicationContext arg "+allArguments[0]);
////            System.out.println("setApplicationContext method "+method);
////            System.out.println("setApplicationContext method end");
////            System.out.println("getClassPathUrls inject URL");
////            try{
////                set.add(new URL("jar:file:/Users/zhanguowang/Documents/popertiesLauncher-0.0.1-SNAPSHOT.jar!/"));
////            }catch (Exception ex){
////                //do nothing
////                ex.printStackTrace();
////            }
//        }

        @Advice.OnMethodEnter()
        public static void onMethodEnter() {
            try {
//                PrivilegedActionUtils.action(new PrivilegedExceptionAction<Object>() {
//                    @Override
//                    public Object run() throws Exception {
//                        new ClassInjector().loaderAgentJarToClassLoader(MyAgent.classLoader);
//                        return null;
//                    }
//                });
                System.out.println("onMethodEnter:"+classLoader);
                if (classLoader.equals(ClassLoader.getSystemClassLoader())) {
                    System.out.println("classLoader is AppClassLoader:"+classLoader);
                    return;
                }
                System.out.println("inject start:"+classLoader);
//                new ClassInjector.UsingReflection(classLoader).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(TestController.class),
//                        ClassFileLocator.ForClassLoader.read(TestController.class)));
//                System.out.println(classLoader+" inject "+TestController.class+" succeed");
//               Class clazz= classLoader.loadClass(TestController.class.getName());
//                System.out.println(classLoader+" load "+clazz+" succeed");
                new com.example.agentdemo.utils.ClassInjector().loaderAgentJarToClassLoader(classLoader);
//                new ClassInjector.UsingReflection(classLoader).inject(Collections.singletonMap(TypeDescription.ForLoadedType.of(TestController.class),
//                        ClassFileLocator.ForClassLoader.read(TestController.class)));
//            Class clazz=   classLoader.loadClass(TestController.class.getName());
//                System.out.println("transform ClassInjector.UsingReflection:"+clazz);
            } catch (Exception ex) {
                System.out.println("transform error");
                ex.printStackTrace();
            }
        }
    }


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
