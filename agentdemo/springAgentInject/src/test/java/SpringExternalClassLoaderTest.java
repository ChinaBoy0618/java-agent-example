import com.example.agent.MyAgent;
import com.example.agent.classloading.SpringExternalClassLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

/**
 * @author wzg
 * @date 2024/5/7 14:32
 */

public class SpringExternalClassLoaderTest {

//    @Nullable
    private static ClassLoader customCL = null;
    private static ClassLoader appCl;
    private static URL[] externalJarURL;
    private static final String JAR_PREFIX="jar:";
    private static final String JAR_SUFFIX="!/";
    @BeforeAll
   static void before() throws MalformedURLException {

        appCl=SpringExternalClassLoaderTest.class.getClassLoader();
        externalJarURL=new URL[]{
                new File("/Users/zhanguowang/Documents/popertiesLauncher-0.0.1-SNAPSHOT.jar").toURL()
        };
        customCL = new SpringExternalClassLoader(externalJarURL, appCl,"template","com/example/agentdemo/");
    }
    @Test
    void testLoadTestClass()  throws ClassNotFoundException{

        Class<?> t = customCL.loadClass("com.example.agent.template.TestController");
        Assert.assertNotNull(t);
    }
    //

    /**
     * 测试加载资源
     */
    @Test
    void getSourcesTest() throws IOException {

       Enumeration<URL> urlEnumeration = customCL.getResources("org/apache/logging/log4j/util/Strings");
        Assert.assertNotNull(urlEnumeration);
    }
    /**
     * 测试加载资源
     */
    @Test
    void getResourceAsStreamTest() throws Exception {

        InputStream stream = customCL.getResourceAsStream("com/example/popertieslauncher/TestController.class");
        Assert.assertNotNull(stream);
    }
    /**
     * 测试 external jar url
     */
    @Test
    void externalJarURlTest() throws Exception {

        List<URL> urls= ((SpringExternalClassLoader)customCL).getExternalJarPath();
        for (URL url:urls){
            String path=url.toString();
            System.out.println(path);
            Assert.assertTrue(path.substring(0,JAR_PREFIX.length()).equals(JAR_PREFIX));
            Assert.assertTrue(path.substring(path.indexOf(JAR_SUFFIX)).equals(JAR_SUFFIX));

        }
    }

}
