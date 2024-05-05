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

从根上来说，都是通过LangtchURLCL 加载的，只是说通过ASM的ClassVisitor读取的时候，meta属性读取到了anntion，但是通过CL加载的ClassType并不能直接读取到
## 附加调试
### 启动添加参数配置
``` bat
java -javaagent:/Users/zhanguowang/Desktop/project/github/java-agent-example/agentdemo/springAgentInject/target/springAgentInject-1-SNAPSHOT.jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9999 -jar springAgentDemo-0.0.1-SNAPSHOT.jar
```
### idea 增加远程调试
host：localhost 端口使用9999

