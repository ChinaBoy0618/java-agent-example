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
## spring 注入思路一
- 通过在Lancher阶段，注入jar包URL进行注入。
  - 目前问题为Lancher阶段CL为AppClassLoader，不是spring 的LancherURLClassLoader，不具备注入条件，正在试验新的matcher 的type和method，方便在 LancherURLClassLoader 阶段注入。
  - 第二种为在Lancher AppClassLoader 阶段修改方法的出入参，添加被扫描的包
  - 以上两种，都成功注入了jar包，但是未解析
## spring注入思路二
- 通过在spring BeanFactory初始化完毕后，通过ByteClassLoader或者通过Bytebuddy的ClassInjector.UsingReflection注入类，然后在spring事件当中，重新刷新BeanDefinition。
## 问题
目前agent包中如果定义spring的事件或者spring注解，会被spring所在的app 扫描到，但是并不会对里面注解进行代理类生成。原因还未深究

## 下一步验证工作
- 熟读spring boot 源码，寻找 Inject的时机
- 找到 问题 小节中提到的问题原因
- 可能先放下注入，先把agent搭建起来，然后在完成agent搭建之后，通过解决 agent的class和app的class隔离问题之后，再次通过agent Helper 所对应的MutiParentClassLoader解决问题
