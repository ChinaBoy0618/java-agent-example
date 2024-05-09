import com.example.agent.MyAgent;
import com.example.agent.classloading.CustomClassLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author wzg
 * @date 2024/5/7 14:32
 */

public class CustomerClassLoaderTest {

//    @Nullable
    private ClassLoader customCL = null;
    private ClassLoader appCl;
    @Before
    void before(){
        appCl=getClass().getClassLoader();
    }
    @Test
    void testLoadTestClass() throws Exception {

        customCL = new CustomClassLoader(new URL[]{
//                new File("/Users/zhanguowang/Desktop/project/github/java-agent-example/agent/springAgentInject/target/springAgentInject-1-SNAPSHOT.jar").toURL(),
                new URL("/Users/zhanguowang/Documents/popertiesLauncher-0.0.1-SNAPSHOT.jar"),
        }, appCl, "template/","","","");
        Class<?> t = customCL.loadClass("com.example.agent.template.TestController");
        Assert.assertNotNull(t);
    }
    //

    /**
     * 测试加载资源
     */
    @Test
    void getSourcesTest() throws Exception {

        customCL = new CustomClassLoader(new URL[]{
//                new File("/Users/zhanguowang/Desktop/project/github/java-agent-example/agent/springAgentInject/target/springAgentInject-1-SNAPSHOT.jar").toURL(),
               new URL("/Users/zhanguowang/Documents/popertiesLauncher-0.0.1-SNAPSHOT.jar"),
        }, appCl, "template/","","","");
       Enumeration<URL> urlEnumeration = customCL.getResources("org/apache/logging/log4j/util/Strings");
        Assert.assertNotNull(urlEnumeration);
    }
    /**
     * 测试加载资源
     */
    @Test
    void getResourceAsStreamTest() throws Exception {

        customCL = new CustomClassLoader(
                new URL[]{
//                new File("/Users/zhanguowang/Desktop/project/github/java-agent-example/agent/springAgentInject/target/springAgentInject-1-SNAPSHOT.jar").toURL(),
                        new File("/Users/zhanguowang/Documents/popertiesLauncher-0.0.1-SNAPSHOT.jar").toURL(),
                }, MyAgent.classLoader,
                "template","com/example/agentdemo/","com/example/popertieslauncher/",
                "jar:file:/Users/zhanguowang/Documents/popertiesLauncher-0.0.1-SNAPSHOT.jar!/com/example/popertieslauncher/");
        InputStream stream = customCL.getResourceAsStream("com/example/popertieslauncher/TestController.class");
        Assert.assertNotNull(stream);
    }
}
