package com.example.agent.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ProxySelector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.Map;

/**
 * Delegates calls with wrapping in privileged actions which is required when security manager is active
 */
public class PrivilegedActionUtils {

    public static String getEnv(final String key) {
        if (System.getSecurityManager() == null) {
            return System.getenv(key);
        }
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getenv(key);
            }
        });
    }

    public static Map<String, String> getEnv() {
        if (System.getSecurityManager() == null) {
            return System.getenv();
        }
        return AccessController.doPrivileged(new PrivilegedAction<Map<String, String>>() {
            @Override
            public Map<String, String> run() {
                return System.getenv();
            }
        });
    }

    public static String getProperty(final String name) {
        if (System.getSecurityManager() == null) {
            return System.getProperty(name);
        }

        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty(name);
            }
        });
    }

    public static ProxySelector getDefaultProxySelector() {
        if (System.getSecurityManager() == null) {
            return ProxySelector.getDefault();
        }

        return AccessController.doPrivileged(new PrivilegedAction<ProxySelector>() {
            @Override
            public ProxySelector run() {
                return ProxySelector.getDefault();
            }
        });
    }
    public static ClassLoader getClassLoader(final Class<?> type) {
        if (System.getSecurityManager() == null) {
            return type.getClassLoader();
        }
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return type.getClassLoader();
            }
        });
    }

    public static ProtectionDomain getProtectionDomain(final Class<?> type) {
        if (System.getSecurityManager() == null) {
            return type.getProtectionDomain();
        }
        return AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>() {
            @Override
            public ProtectionDomain run() {
                return type.getProtectionDomain();
            }
        });
    }

    public static ClassLoader getContextClassLoader(final Thread t) {
        if (System.getSecurityManager() == null) {
            return t.getContextClassLoader();
        }
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return t.getContextClassLoader();
            }
        });
    }

    public static void setContextClassLoader(final Thread t, final ClassLoader cl) {
        if (System.getSecurityManager() == null) {
            t.setContextClassLoader(cl);
        }
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                t.setContextClassLoader(cl);
                return null;
            }
        });
    }

    public static Thread newThread(final  Runnable r) {
        if (System.getSecurityManager() == null) {
            return new Thread(r);
        }
        return AccessController.doPrivileged(new PrivilegedAction<Thread>() {
            @Override
            public Thread run() {
                return new Thread(r);
            }
        });
    }

    public static FileInputStream newFileInputStream(final File file) throws FileNotFoundException {
        if (System.getSecurityManager() == null) {
            return new FileInputStream(file);
        }
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<FileInputStream>() {
                @Override
                public FileInputStream run() throws Exception {
                    return new FileInputStream(file);
                }
            });
        } catch (PrivilegedActionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FileNotFoundException) {
                throw (FileNotFoundException) cause;
            }
            throw new RuntimeException(cause);
        }
    }
    public  static <T extends Object> T action(PrivilegedExceptionAction<T> action) throws FileNotFoundException {
        if (System.getSecurityManager() == null) {
            return null;
        }
        try {
            return AccessController.doPrivileged(action);
        } catch (PrivilegedActionException e) {

           e.printStackTrace();
           return  null;
        }
    }
    /**
     * Creates directory and its parents when path does not exist
     *
     * @param path directory path to create
     * @throws IOException when there is an IO error
     */
    public static void createDirectories(final Path path) throws IOException {
        if (System.getSecurityManager() == null) {
            doCreateDirectories(path);
        }
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    doCreateDirectories(path);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new RuntimeException(cause);
        }
    }

    private static void doCreateDirectories(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

}

