package com.example.agent.classloading;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;

/**
 * 为了解决 spring 框架底层 getResource 问题，再加一层CL，作为 {@link ClassLoader}
 * 类中的 getResource 调用的解决方案
 */
public class ShadeClassLoader extends URLClassLoader {
    public ShadeClassLoader(ClassLoader parent) {
        super(new URL[]{}, parent);
    }

    /**
     * getResourceAsStream 装饰方法
     *
     * @param  name
     *         The resource name
     *
     * @return  An input stream for reading the resource, or <tt>null</tt>
     *          if the resource could not be found
     *
     * @since  1.1
     */
    public InputStream getResourceAsStream(String name) {
       return this.getParent().getResourceAsStream(name);
    }
}
