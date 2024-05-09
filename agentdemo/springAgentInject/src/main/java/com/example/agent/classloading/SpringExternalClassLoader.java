package com.example.agent.classloading;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.*;

/**
 *
 * 支持在 spring class path 扫描的时刻动态加载额外的class path jar包的CL
 * @author wzg
 * @date 2024/5/7 13:34
 */
public class SpringExternalClassLoader extends URLClassLoader {
    /**
     * 加载类文件的后缀
     */
    private static final String SUFFIX = ".declazz";
    private static final String CLASS_SUFFIX = ".class";
    private static final ProtectionDomain PROTECTION_DOMAIN;
    private static final String JAR_PREFIX="jar:";
    private static final String JAR_SUFFIX="!/";


    static {
        ClassLoader.registerAsParallelCapable();

        if (System.getSecurityManager() == null) {
            PROTECTION_DOMAIN = SpringExternalClassLoader.class.getProtectionDomain();
        } else {
            PROTECTION_DOMAIN = AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>() {
                @Override
                public ProtectionDomain run() {
                    return SpringExternalClassLoader.class.getProtectionDomain();
                }
            });
        }
    }

    private final String customPrefix;
    private final ThreadLocal<Set<String>> locallyNonAvailableResources = new ThreadLocal<Set<String>>() {
        @Override
        protected Set<String> initialValue() {
            return new HashSet<>();
        }
    };
    /**
     * spring app启动的class path
     */
    private final String springClassPath;
    /**
     * 扩展包的jar 地址，测试先按照spring的写
     */
    private final List<URL> externalJarPath;

    /**
     * Spring 扩展jar 包加载路径
     * @param externalUrls 扩展jar包的加载路径
     * @param parent  SpringExternalClassLoader的parent CL
     * @param prefix 自定义的资源前缀（实现类似spring 的BOOT_INF/lib）
     * @param springClassPath spring 的classpath路径，例如：示例项目的包名 com/example/agentdemo（Application类所在包名）
     */
    public SpringExternalClassLoader(URL[] externalUrls, ClassLoader parent, String prefix, String springClassPath) throws MalformedURLException{
        super(externalUrls, parent);
        this.customPrefix = prefix;
        this.springClassPath = springClassPath;
        this.externalJarPath=new ArrayList<>(getLength(externalUrls));
        initExternalJarPath(externalUrls);
    }

    /**
     * 通过  externalUrls 去 解析对应的地址,获取 扩展的jar包地址全路径
     * @param externalUrls
     */
    private URL[] initExternalJarPath(URL[] externalUrls) throws MalformedURLException {
        if(externalUrls!=null&&externalUrls.length>0){
            for (URL url:externalUrls){
                this.externalJarPath.add(new URL(JAR_PREFIX+url.toString()+JAR_SUFFIX));
            }
        }
        return externalJarPath.toArray(new URL[0]);
    }
    private int getLength(URL[] externalUrls){
         return externalUrls!=null?externalUrls.length:0;
    }

    /**
     * 获取扩展jar包列表
     * @return
     */
    public List<URL> getExternalJarPath(){
        return externalJarPath;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            try {
                // First, check if the class has already been loaded
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    c = findClass(name);
                    if (resolve) {
                        resolveClass(c);
                    }
                }
                return c;
            } catch (ClassNotFoundException e) {
                return super.loadClass(name, resolve);
            }
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = getShadedClassBytes(name);
        if (classBytes != null) {
            return defineClass(name, classBytes);
        }
        throw new ClassNotFoundException(name);
    }

    /**
     * 因为当前就一个jar包，且除了测试使用的template下类，其它类均正常加载，所以不进行 package 的 define 和 manifest 的define
     */
    private Class<?> defineClass(String name, byte[] classBytes) {
        return defineClass(name, classBytes, 0, classBytes.length, PROTECTION_DOMAIN);
    }

    public byte[] getShadedClassBytes(String name) throws ClassNotFoundException {
        //暂时去掉 customPrefix
//        try (InputStream is = getPrivilegedResourceAsStream(customPrefix + name.replace('.', '/') + PREFIX)) {
        try (InputStream is = getPrivilegedResourceAsStream(name.replace('.', '/') + SUFFIX)) {
            if (is != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int n;
                byte[] data = new byte[1024];
                while ((n = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, n);
                }
                return buffer.toByteArray();
            }
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
        return null;
    }

    private InputStream getPrivilegedResourceAsStream(final String name) {
        if (System.getSecurityManager() == null) {
            return getResourceAsStreamInternal(name);
        }

        return AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
            @Override
            public InputStream run() {
                return getResourceAsStreamInternal(name);
            }
        });
    }

    private InputStream getResourceAsStreamInternal(String name) {
        return super.getResourceAsStream(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    /**
     * This class loader should only see classes and resources that start with the custom prefix.
     * It still allows for classes and resources of the parent to be resolved via the getResource methods
     *
     * @param name the name of the resource
     * @return a {@code URL} for the resource, or {@code null}
     * if the resource could not be found, or if the loader is closed.
     */
    @Override
    public URL findResource(final String name) {
        if (locallyNonAvailableResources.get().contains(name)) {
            return null;
        }

        if (System.getSecurityManager() == null) {
            return findResourceInternal(getShadedResourceName(name));
        }

        // while most of the body of default 'findResource' in JDK implementation is in a privileged action
        // an extra URL check is performed just after it, hence we have to wrap the whole method call in a privileged
        // action otherwise the security manager will complain about lack of proper read privileges on the agent jar
        return AccessController.doPrivileged(new PrivilegedAction<URL>() {
            @Override
            public URL run() {
                return findResourceInternal(getShadedResourceName(name));
            }
        });
    }

    private URL findResourceInternal(String name) {
        return super.findResource(name);
    }

    /**
     * This class loader should only see classes and resources that start with the custom prefix.
     * It still allows for classes and resources of the parent to be resolved via the getResource methods
     *
     * @param name the name of the resource
     * @return a {@code URL} for the resource, or {@code null}
     * if the resource could not be found, or if the loader is closed.
     */
    @Override
    public Enumeration<URL> findResources(final String name) throws IOException {
        if (locallyNonAvailableResources.get().contains(name)) {
            return Collections.emptyEnumeration();
        }

        Enumeration<URL> result = super.findResources(getShadedResourceName(name));
        if (System.getSecurityManager() == null) {
            return result;
        }

        return new PrivilegedEnumeration<URL>(result);

    }

    /**
     * Implements a child-first resource lookup in the following order:
     *
     * <ol>
     *   <li><p> Look locally, which can only be done in the shaded form (see {@link #findResource(String)}). </p></li>
     *   <li><p> If not found, invoke the super's {@link URLClassLoader#getResource(String) getResource()}
     *   using the original form in a parent-first manner. This causes a duplicated lookup, but it's preferable over trying to
     *   deal with invoking lookup on the parent, where the parent may be {@code null} and not easily
     *   accessible from here. </p></li>
     * </ol>
     */
    @Override
    public URL getResource(String name) {

        // look locally first
        URL shadedResource = findResource(name);
        if (shadedResource != null) {
            return shadedResource;
        }
        // if not found locally, calling super's lookup, which does parent first and then local, so marking as not required for local lookup
        Set<String> locallyNonAvailableResources = this.locallyNonAvailableResources.get();
        try {
            locallyNonAvailableResources.add(name);
            return super.getResource(name);
        } finally {
            locallyNonAvailableResources.remove(name);
        }
    }

    /**
     * Implements a child-first resources lookup in the following order:
     *
     * <ol>
     *   <li><p> Look locally, which can only be done in the shaded form (see {@link #findResource(String)}). </p></li>
     *   <li><p> If not found, invoke the super's {@link URLClassLoader#getResources(String) getResources()}
     *   using the original form in a parent-first manner. This causes a duplicated lookup, but it's preferable over trying to
     *   deal with invoking lookup on the parent, where the parent may be {@code null} and not easily
     *   accessible from here. </p></li>
     * </ol>
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        System.out.println(this + "|getResources enter");
        // look locally first
        Enumeration<URL> shadedResources = findResources(name);
        if (shadedResources.hasMoreElements()) {
            // no need to compound results with parent lookup, we only want to return the shaded form if there is such
            if (isNotEmpty(name) && name.equals(springClassPath)) {
                return setClassLocator(name, shadedResources);
            }
            return shadedResources;
        }
        // if not found locally, calling super's lookup, which does parent first and then local, so marking as not required for local lookup
        Set<String> locallyNonAvailableResources = this.locallyNonAvailableResources.get();
        try {
            locallyNonAvailableResources.add(name);
//            System.out.println(this+"|name:"+name);
            if (isNotEmpty(name) && name.equals(springClassPath)) {
                System.out.println(this + "|name:" + name + "|setClassLocator");
                return setClassLocator(name, this.getParent().getResources(name));
            } else {
                //LaunchedURLClassLoader 获取
//                System.out.println(this+"|name:"+name+"|getParent:"+this.getParent());
                return this.getParent().getResources(name);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ClassLoader.getSystemClassLoader().getResources(name);
        } finally {
            locallyNonAvailableResources.remove(name);
        }
    }

    /**
     * getResourceAsStream 装饰方法
     *
     * @param name The resource name
     * @return An input stream for reading the resource, or <tt>null</tt>
     * if the resource could not be found
     * @since 1.1
     */
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    private boolean isNotEmpty(String s) {
        return s != null && s.length() > 0;
    }

    /**
     * 在 spring 启动 ClassPathBeanDefinitionScanner 扫描的时候，将要注入的资源注入进去
     *
     * @param name 资源名称
     * @return 返回资源列表
     */
    private Enumeration<URL> setClassLocator(String name, Enumeration<URL> oldSources) {
        try {
            //spring 加载 classpath 资源
            List<URL> urls = Collections.list(oldSources);
            for (URL url:externalJarPath){
                urls.add(url);
            }

            return Collections.enumeration(urls);
        } catch (Exception ex) {
            ex.printStackTrace();
            return oldSources;
        }
    }

    private String getShadedResourceName(String name) {
        if (name.contains(customPrefix) && name.endsWith(SUFFIX)) {
            return name;
        } else if (name.endsWith(SUFFIX)) {
            return name.substring(0, name.length() - SUFFIX.length()) + CLASS_SUFFIX;
        } else {
            return name;
        }
//        if (name.startsWith(customPrefix)) {
//            // already a lookup of the shaded form
//            return name;
//        } else if (name.endsWith(CLASS_SUFFIX)) {
//            return customPrefix + name.substring(0, name.length() - CLASS_SUFFIX.length()) + PREFIX;
//        }else if(name.endsWith(PREFIX)){
//            return name;
//        }
//
//        else {
//            return customPrefix + name;
//        }
    }

    /**
     * Wraps an {@link Enumeration} with privileged actions when executed with a security manager
     *
     * @param <E>
     */
    private static class PrivilegedEnumeration<E> implements Enumeration<E> {

        private final Enumeration<E> delegate;

        private PrivilegedEnumeration(Enumeration<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasMoreElements() {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return delegate.hasMoreElements();
                }
            });
        }

        @Override
        public E nextElement() {
            return AccessController.doPrivileged(new PrivilegedAction<E>() {
                @Override
                public E run() {
                    return delegate.nextElement();
                }
            });
        }
    }

    @Override
    public String toString() {
        return "CustomClassLoader{" +
                "parent=" + getParent() +
                ", customPrefix='" + customPrefix + '\'' +
                ",springClassPath='" + springClassPath + '\'' +
                '}';
    }
}
