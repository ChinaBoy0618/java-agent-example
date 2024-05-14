package com.example.agent.layout;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.springframework.boot.loader.tools.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * @author wzg
 * @date 2024/5/12 22:46
 */
public class AgentLoaderLayout extends Layouts.Jar implements CustomLoaderLayout {
    private final static String NESTED_LOADER_JAR = "lib/springAgentInject-1-SNAPSHOT.jar";
    private static final int BUFFER_SIZE = 32 * 1024;
    private File source;
    private Method method;

    public AgentLoaderLayout(File source) {
        this.source = source;
        method=getWriteEntry();
    }

    @Override
    public String getLauncherClassName() {
        return "com.example.agent.MyAgent";
    }

    @Override
    public void writeLoadedClasses(LoaderClassesWriter writer) throws IOException {
        System.out.println("source file name:" + source.getName());
        URL loaderJar = getClass().getClassLoader().getResource(NESTED_LOADER_JAR);
        try (JarInputStream inputStream = new JarInputStream(new BufferedInputStream(loaderJar.openStream()))) {
            JarEntry entry;
            //写入自定义jar包
            while ((entry = inputStream.getNextJarEntry()) != null) {
                if (isClassEntry(entry)) {
                    if(method!=null){
                        try {
                            method.invoke(writer,new JarArchiveEntry(entry), new InputStreamEntryWriter(inputStream));
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }

                    }
                }
            }
        }
        /**
         Manifest-Version: 1.0
         Archiver-Version: Plexus Archiver
         Built-By: zhanguowang
         Created-By: Apache Maven 3.6.3
         Build-Jdk: 1.8.0_221
         */
        //重写 manifest。TODO 之后改为获取 agent 包.使用spring loader 的Handler进行处理
        // spring-boot-maven-plugin 内部做了限制，这里写入 Manifest 不生效，需要自定义writer
//        Manifest manifest=new Manifest();
//        Attributes attributes= manifest.getMainAttributes();
//        attributes.putValue("Manifest-Version", "1.0");
//        attributes.putValue("Premain-Class", "com.example.agent.MyAgent");
//        attributes.putValue("Agent-Class", "com.example.agent.MyAgent");
////        attributes.putValue(" Archiver-Version", "Plexus Archiver");
//        attributes.putValue("Built-By", "zhanguowang");
//        attributes.putValue("Created-By", "Apache Maven 3.6.3");
//        attributes.putValue("Build-Jdk", "1.8.0_221");
//
//        ((AbstractJarWriter)writer).writeManifest(manifest);
    }

    private Method getWriteEntry() {
        try {
            Method method= AbstractJarWriter.class.getDeclaredMethod("writeEntry", JarArchiveEntry.class, EntryWriter.class);
            method.setAccessible(true);
            return method;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private boolean isClassEntry(JarEntry entry) {
        return entry.getName().endsWith(".class");
    }

    /**
     * {@link EntryWriter} that writes content from an {@link InputStream}.
     */
    private static class InputStreamEntryWriter implements EntryWriter {

        private final InputStream inputStream;

        InputStreamEntryWriter(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = this.inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }

    }

//    public URL getUrl(String file) throws MalformedURLException {
//        file = file.replace("file:////", "file://"); // Fix UNC paths
//        return new URL("jar", "", -1, file, new org.springframework.boot.loader.jar.Handler(this));
//
//    }
}