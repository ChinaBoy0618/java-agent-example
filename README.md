# 目录说明
``` markdown
- .
├── JavaAgent
│   ├── consoleAgent //简单的app
│   └── springAgentDemo//验证注入spring 的app demo
└── agentdemo
    ├── builds //公共的全部依赖打包项目-已废弃
    ├── consoleAgentInject//使用bytebuddy AgentBuidler创建的简单agent，不使用ByteBuddy Agent
    ├── popertiesLauncher //验证spring -Dload.path 的依赖包
    └── springAgentInject //验证spring 注入的agent demo
```

## 问题
目前agent包中如果定义spring的事件或者spring注解，会被spring所在的app 扫描到，但是并不会对里面注解进行代理类生成。原因还未深究
## 问题原因
在
ClassPathBeanDefinitionScanner doScan扫描出来 BeanDefinition{
    registerBeanDefinition(definitionHolder,DefaultListableBeanFactory registry)
}


DefaultListableBeanFactory 里的 preInstantiateSingletons {
    getMergedLocalBeanDefinition 创建 RootBeanDefinition，最终是调用 AbstractBeanDefinition 的构造函数，最终将meta信息抹除了
}

从根上来说，都是通过 LaunchedURLClassLoader 加载的，只是说通过ASM的ClassVisitor读取的时候，meta属性读取到了anntion，但是通过CL加载的ClassType并不能直接读取到
## 问题进一步追溯
最终发现是maven 的scope如果选择provided，则导致在 springAgentInject 这个包下没有spring的依赖，所以当我尝试将scope 恢复到 compile 的时候，确认在 目标的spring 程序生效。这主要原因有以下几点：
- 1.spring app 会扫描加载到agent 注入的jar包，把这个jar包添加到对应的classpath下
- 2.spring的 LaunchedURLClassLoader 加载 TestController class file的时候，会对里面的 org.springframework.web.bind.annotation.RequestMapping 符号引用进行替换为直接引用（我对比了不同打包方式class file 里面的符号引用，发现没有什么区别：使用 hexdump -C 进行16进制class file 的查看），这个时候加载 org.springframework.web.bind.annotation.RequestMapping 会由JVM（hotspot）委派给 LaunchedURLClassLoader 加载，这里目前现象是如果在 springAgentInject 中 打包了spring依赖，就可以正常进行解析，否则也不报错。这里我查看了spring 2.7 版本 org.springframework.boot.loader.LaunchedURLClassLoader的源码，发现 loadClass 的异常被吞掉了，没有进行任何处理。
- 3.依据第2点，待验证：将spring 2.7进行本地打包，并进行 org.springframework.boot.loader.LaunchedURLClassLoader 的异常处理捕获（可能需要修改spring的maven 插件，并在本地加载自己处理过的maven插件，主要是使用 修改后项目的 spring-boot-loader-tools 依赖）
- 4.第3点已经验证，为 sun.reflect.annotation.AnnotationParser#parseAnnotation2 报错，异常为 [TypeNotPresentException](https://docs.oracle.com/javase%2F8%2Fdocs%2Fapi%2F%2F/java/lang/TypeNotPresentException.html)
``` java
 private static Annotation parseAnnotation2(ByteBuffer var0, ConstantPool var1, Class<?> var2, boolean var3, Class<? extends Annotation>[] var4) {
        int var5 = var0.getShort() & '\uffff';
        Class var6 = null;
        String var7 = "[unknown]";

        try {
            try {
                var7 = var1.getUTF8At(var5);
                var6 = parseSig(var7, var2);//这里报错
            } catch (IllegalArgumentException var18) {
                var6 = var1.getClassAt(var5);
            }
        } catch (NoClassDefFoundError var19) {
            //并且这里会将异常吞噬
            if (var3) {
                throw new TypeNotPresentException(var7, var19);
            }

            skipAnnotation(var0, false);
            return null;
        } catch (TypeNotPresentException var20) {
            if (var3) {
                throw var20;
            }

            skipAnnotation(var0, false);
            return null;
        }

        //省略......

            return annotationForMap(var6, var10);
        }
    }

```
## TODO
- <del >下一步验证工作，只打包依赖的annotation class </del>(不可行)
- 屏蔽 spring app 的 cl 扫描（通过重命名class file 文件），创建自定义的agent ClassLoader，通过继承 目标类的ClassLoader，实现类加载
PS：仍然不建议将spring的依赖打包在 springAgentInject 这种agent 项目中，会带来版本不一致，不好维护，jar包庞大等很多问题，并且之后agent开发会除了premain对应的class外，全部使用 自定义的 ClassLoader 加载，会保证在 spring app 侧 不可见agent的 所有依赖和类，所以直接将依赖打进去的方式，始终不是我所想要的解决方案

## 附加调试
### 启动添加参数配置
``` bat
java -javaagent:/Users/zhanguowang/Desktop/project/github/java-agent-example/agentdemo/springAgentInject/target/springAgentInject-1-SNAPSHOT.jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9999 -jar springAgentDemo-0.0.1-SNAPSHOT.jar
```
### idea 增加远程调试
host：localhost 端口使用9999

