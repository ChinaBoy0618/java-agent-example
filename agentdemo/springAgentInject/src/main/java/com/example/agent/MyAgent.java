package com.example.agent;

import com.example.agent.classloading.ShadedClassLoader;
import com.example.agent.classloading.SpringExternalClassLoader;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.ProtectionDomain;

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

        class ErrorLoggingListener extends AgentBuilder.Listener.Adapter {

            @Override
            public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
                System.out.println("bytebuddy error typeName:" + typeName + "|classLoader:" + classLoader + "|JavaModule:" + module
                        + "|loaded:" + loaded + "|throwable:" + throwable.getMessage()
                );
                throwable.printStackTrace();
            }
        }


        new AgentBuilder.Default()
                .type(
                        declaresAnnotation(annotationType(named("org.springframework.boot.autoconfigure.SpringBootApplication")))
                )
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                        System.out.println("SpringBootApplication transformer installed:" + classLoader + "|TypeDescription:" + typeDescription);
                        MyAgent.classLoader = classLoader;
                        return builder.method(named("main"))
                               .intercept(Advice.to(AdviceClass8.class));
                    }
                })
                .with(new ErrorLoggingListener())
                .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
//                .disableClassFormatChanges()
                .installOn(instrumentation);

        System.out.println("premain ended");
    }

    public static class AdviceClass8 {
        @Advice.OnMethodEnter
        private static void onMethodEnter(@Advice.Origin Method method) {
            System.out.println(method + " enter Loader class");
            try {
                //TODO 将这里的参数变为配置文件，或者启动 agent 参数
                SpringExternalClassLoader cl = new SpringExternalClassLoader(
                        new URL[]{new File("/Users/zhanguowang/Documents/popertiesLauncher-0.0.1-SNAPSHOT.jar").toURL()}
                        , MyAgent.classLoader
                        , "template"
                        , "com/example/agentdemo/");

                Thread.currentThread().setContextClassLoader(new ShadedClassLoader(cl));
                System.out.println(method + " set thread class loader:" + cl);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
