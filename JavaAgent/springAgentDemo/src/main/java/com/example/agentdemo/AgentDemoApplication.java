package com.example.agentdemo;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;

@SpringBootApplication
public class AgentDemoApplication implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    public static void main(String[] args) {
        System.out.println("Spring Application started");

        ConfigurableApplicationContext c = SpringApplication.run(AgentDemoApplication.class, args);

        System.out.println("Spring Application ended");
//
//        try {
//            URI uri=  new URL("file:/Users/zhanguowang/Desktop/project/gitee/agentdemo/springAgentInject/target/springAgentInject-1-SNAPSHOT.jar").toURI();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String urlPath = applicationContext.getClassLoader().getResource("/").getPath();
        String path = Thread.currentThread().getContextClassLoader().getResource("/").getPath();
        System.out.println("classpath-Thread:" + path);
        System.out.println("classpath-urlPath:" + urlPath);
        printClassPathUrls("LaunchedClassLoader ", ((URLClassLoader) applicationContext.getClassLoader()).getURLs());
        printClassPathUrls("AppClassLoader ", ((URLClassLoader) applicationContext.getClassLoader().getParent()).getURLs());
//        Arrays.stream(new File(path).listFiles()).filter(e -> e.getPath().contains(".jar") && !e.getPath().contains("original") && !e.getPath().contains("sources")).forEach(e -> {
//            System.out.println("classpath-Thread:"+e.getName());
//        });
//        Arrays.stream(new File(urlPath).listFiles()).filter(e -> e.getPath().contains(".jar") && !e.getPath().contains("original") && !e.getPath().contains("sources")).forEach(e -> {
//            System.out.println("classpath-urlPath:"+e.getName());
//        });

//        String[] names = applicationContext.getBeanDefinitionNames();
//        for (String name : names) {
//            System.out.println(">>>>>>" + name);
//        }
//        System.out.println("------\nBean 总计:" + applicationContext.getBeanDefinitionCount());
//        org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext
//        AbstractHandlerMethodMapping
//        ScannedGenericBeanDefinition

//        System.out.println("BeanFactoryUtils#beanNamesForTypeIncludingAncestors>>>>>>");
//        String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, Object.class);
//        print(applicationContext, names);
//        System.out.println("ApplicationContext#getBeanNamesForType>>>>>>");
//        names = applicationContext.getBeanNamesForType(Object.class);
//        print(applicationContext, names);

    }

    private void print(ApplicationContext applicationContext, String[] names) {
        for (String name : names) {
            if (name.equals("testController") || name.equals("demoController")) {
                Object bean = applicationContext.getBean(name);
//                try {
//                    Class<?> clazz=  ((AbstractBeanDefinition) bean).resolveBeanClass(applicationContext.getClassLoader());
//                    Annotation[] annotations =   clazz.getAnnotations();
//                    System.out.println(name + ">>>>>>resolveBeanClass print annotations begin");
//                    for (Annotation a : annotations) {
//                        System.out.println(name + ">>>>>>" + a.getClass());
//                    }
//                    System.out.println(name + ">>>>>>resolveBeanClass print annotations end");
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
                System.out.println(name + ">>>>>>" + bean.getClass());
                System.out.println(name + ">>>>>>" + AnnotatedElementUtils.hasAnnotation(bean.getClass(), Controller.class));
                System.out.println(name + ">>>>>>" + AnnotatedElementUtils.hasAnnotation(bean.getClass(), RequestMapping.class));
                Annotation[] annotations = bean.getClass().getAnnotations();
                System.out.println(name + ">>>>>>annotations count:" + annotations != null ? annotations.length : 0);
                System.out.println(name + ">>>>>>print annotations begin");
                for (Annotation a : annotations) {
                    System.out.println(name + ">>>>>>" + a.getClass());
                }
                System.out.println(name + ">>>>>>print annotations end");
                System.out.println(name + ">>>>>>Controller.class" + Controller.class);
                System.out.println(name + ">>>>>>Controller.class" + RequestMapping.class);
                System.out.println(">>>>>>" + name);
                BeanDefinition definition = ((DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory()).getBeanDefinition(name);
                System.out.println(name + "BeanClass:" + definition.getClass());
            }
        }
    }

    void printClassPathUrls(String pre, URL[] urls) {
        for (URL url : urls) {
            System.out.println(pre + "getURLs:" + url);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("context refreshed event");
        ApplicationContext _context = event.getApplicationContext();
    }
//java -javaagent:/Users/zhanguowang/Desktop/project/gitee/agent\ demo/springAgentInject/target/springAgentInject-1-SNAPSHOT.jar -jar springAgentDemo-0.0.1-SNAPSHOT.jar
}
//    java -Dloader.path=/Users/zhanguowang/Desktop/project/gitee/agent\ demo/popertiesLauncher/target -jar springAgentDemo-0.0.1-SNAPSHOT.jar
//java -Dloader.path=/Users/zhanguowang/Desktop/project/gitee/agent\ demo/popertiesLauncher/target -cp myjar.jar org.springframework.boot.loader.PropertiesLauncher