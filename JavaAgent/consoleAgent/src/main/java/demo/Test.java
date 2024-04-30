package demo;

import java.util.Collection;

/**
 * @author wzg
 * @date 2024/4/25 16:17
 */
public abstract class Test {
    public Test() {

    }
    public abstract Collection<String> pluginClassLoaderRootPackages();
}
