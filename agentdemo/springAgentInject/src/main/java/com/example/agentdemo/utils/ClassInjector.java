package com.example.agentdemo.utils;

import java.awt.*;
import java.awt.image.DirectColorModel;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author wzg
 * @date 2024/4/28 10:33
 */
public class ClassInjector {


    //
//    public boolean injectToURLClassLoader(URL[] urls, URLClassLoader classLoader) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
//        if (urls != null) {
//            for (URL url : urls) {
//                if (!URL_SET.contains(url)) {
//                    ADD_URL.invoke(classLoader, url);
//                    URL_SET.add(url);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
    public boolean injectToURLClassLoader(List<URL> urls, URLClassLoader classLoader) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        if (urls != null) {
            Method ADD_URL;
            try {
                ADD_URL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                ADD_URL.setAccessible(true);
                Set<URL> URL_SET = new HashSet<>();
                for (URL url : urls) {
                    if (!URL_SET.contains(url)) {
                        ADD_URL.invoke(classLoader, url);
                        URL_SET.add(url);
                        System.out.println(classLoader + "classLoader addURL succeed");
                        return true;
                    }
                }
            } catch (Exception e) {
                System.out.println(classLoader + " classLoader get addURL method");
                e.printStackTrace();
                try {
                    ADD_URL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    ADD_URL.setAccessible(true);
                    Set<URL> URL_SET = new HashSet<>();
                    for (URL url : urls) {
                        if (!URL_SET.contains(url)) {
                            ADD_URL.invoke(classLoader, url);
                            URL_SET.add(url);
                            System.out.println(classLoader + "classLoader parent addURL succeed");
                            return true;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println(classLoader + "classLoader parent get addURL method");
                    return false;
                }
            }

        }
        return false;
    }

    public boolean loaderAgentJarToClassLoader(ClassLoader classLoader) throws URISyntaxException, MalformedURLException, IOException {
//        ProtectionDomain protectionDomain = PrivilegedActionUtils.getProtectionDomain(getClass());
//        List<URL> urlList = Collections.emptyList();
//        String path;
//        if (protectionDomain != null) {
//            path = protectionDomain.getCodeSource().getLocation().toURI().getSchemeSpecificPart();
//            if (!path.contains(".jar")) {//说明是ide启动
//                Path newPath = Paths.get(path).getParent();
//                File file = newPath.toFile();
//                urlList = Arrays.stream(file.listFiles()).filter(e -> e.getPath().contains(".jar") && !e.getPath().contains("original") && !e.getPath().contains("sources")).map(e -> {
//                    try {
//                        return new File(e.getPath()).toURL();
//                    } catch (MalformedURLException malformedURLException) {
//                        //throw malformedURLException;
//                        malformedURLException.printStackTrace();
//                        return null;
//                    }
//                }).filter(e -> e != null).collect(Collectors.toList());
//
//            }else{
//                urlList=new ArrayList<>();
//                urlList.add(new File(path).toURL());
//            }
//        }
        List<URL> urlList=new ArrayList<>();
        urlList.add(new URL("jar:file:/Users/zhanguowang/Documents/popertiesLauncher-0.0.1-SNAPSHOT.jar!/"));
//        URL[] urls = JarLoader.loadMyAgentCoreLib();
//        if (urls == null) {
//            System.err.println("can not find my agent urls");
//            return false;
//        }
        System.out.println("urlList:" + urlList);
        try {
            System.out.println(classLoader + "--------" + classLoader.toString());
            System.out.println(classLoader + "|parent--------" + classLoader.getParent());
            injectToURLClassLoader(urlList, (URLClassLoader) classLoader);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new LoadMyAgentException("inject url to class loader exception", e);
        }
        return true;
    }
}
