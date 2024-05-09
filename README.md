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
- 3.依据第2点，待验证：将spring 2.7进行本地打包，并进行 org.springframework.boot.loader.LaunchedURLClassLoader 的异常处理捕获
- 4.第3点已经验证，为 sun.reflect.annotation.AnnotationParser#parseAnnotation2 报错，异常为 [TypeNotPresentException](https://docs.oracle.com/javase%2F8%2Fdocs%2Fapi%2F%2F/java/lang/TypeNotPresentException.html)

``` java 
ClassPathResource extends InputStreamSource{
	@Override
	public InputStream getInputStream() throws IOException {
		InputStream is;
		if (this.clazz != null) {
			is = this.clazz.getResourceAsStream(this.path);
		}
		else if (this.classLoader != null) {
		    //这里是最关键的点，所以我们必须在 SpringExternalClassLoader 和 ShadedClassLoader 中实现 getResourceAsStream
			is = this.classLoader.getResourceAsStream(this.path);
		}
		else {
			is = ClassLoader.getSystemResourceAsStream(this.path);
		}
		if (is == null) {
			throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
		}
		return is;
	}
}
```
```text
将jar包的URL直接注入到 LaunchedURLClassLoader 的方式，发现bean 正常生成，但是注解不生效，不mapping，以下为追溯的历程
RequestMappingHandlerMapping->AnnotatedElementUtils->MergedAnnotations->TypeMappedAnnotations->AnnotationsScanner->Class->AnnotationParser
PS:JDK的代码建议看JDK源码，反编译没有注释，很痛苦
```

``` java
public class RequestMappingHandlerMapping extends RequestMappingInfoHandlerMapping
        implements MatchableHandlerMapping, EmbeddedValueResolverAware {
	/**
	 * {@inheritDoc}
	 * <p>Expects a handler to have either a type-level @{@link Controller}
	 * annotation or a type-level @{@link RequestMapping} annotation.
	 */
	@Override
	protected boolean isHandler(Class<?> beanType) {
		return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
				AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
	}
}
```
```java
final class TypeMappedAnnotations implements MergedAnnotations {
    static MergedAnnotations from(AnnotatedElement element, SearchStrategy searchStrategy,
                                  RepeatableContainers repeatableContainers, AnnotationFilter annotationFilter) {

        if (AnnotationsScanner.isKnownEmpty(element, searchStrategy)) {
            return NONE;
        }
        return new TypeMappedAnnotations(element, searchStrategy, repeatableContainers, annotationFilter);
    }
}
```

```java
abstract class AnnotationsScanner {
    static boolean isKnownEmpty(AnnotatedElement source, SearchStrategy searchStrategy) {
        if (hasPlainJavaAnnotationsOnly(source)) {
            return true;
        }
        if (searchStrategy == SearchStrategy.DIRECT || isWithoutHierarchy(source, searchStrategy)) {
            if (source instanceof Method && ((Method) source).isBridge()) {
                return false;
            }
            return getDeclaredAnnotations(source, false).length == 0;
        }
        return false;
    }

    static Annotation[] getDeclaredAnnotations(AnnotatedElement source, boolean defensive) {
        boolean cached = false;
        Annotation[] annotations = declaredAnnotationCache.get(source);
        if (annotations != null) {
            cached = true;
        }
        else {
            //这句代码是关键
            annotations = source.getDeclaredAnnotations();
            if (annotations.length != 0) {
                boolean allIgnored = true;
                for (int i = 0; i < annotations.length; i++) {
                    Annotation annotation = annotations[i];
                    if (isIgnorable(annotation.annotationType()) ||
                            !AttributeMethods.forAnnotationType(annotation.annotationType()).isValid(annotation)) {
                        annotations[i] = null;
                    }
                    else {
                        allIgnored = false;
                    }
                }
                annotations = (allIgnored ? NO_ANNOTATIONS : annotations);
                if (source instanceof Class || source instanceof Member) {
                    declaredAnnotationCache.put(source, annotations);
                    cached = true;
                }
            }
        }
        if (!defensive || annotations.length == 0 || !cached) {
            return annotations;
        }
        return annotations.clone();
    }
}
```
```java
public final class Class<T> implements java.io.Serializable,
                              GenericDeclaration,
                              Type,
                              AnnotatedElement {
    
    private AnnotationData annotationData() {
        while (true) { // retry loop
            AnnotationData annotationData = this.annotationData;
            int classRedefinedCount = this.classRedefinedCount;
            if (annotationData != null &&
                    annotationData.redefinedCount == classRedefinedCount) {
                return annotationData;
            }
            // null or stale annotationData -> optimistically create new instance
            AnnotationData newAnnotationData = createAnnotationData(classRedefinedCount);
            // try to install it
            if (Atomic.casAnnotationData(this, annotationData, newAnnotationData)) {
                // successfully installed new AnnotationData
                return newAnnotationData;
            }
        }
    }
    private AnnotationData createAnnotationData(int classRedefinedCount) {
        Map<Class<? extends Annotation>, Annotation> declaredAnnotations =
                AnnotationParser.parseAnnotations(getRawAnnotations(), getConstantPool(), this);
        Class<?> superClass = getSuperclass();
        Map<Class<? extends Annotation>, Annotation> annotations = null;
        if (superClass != null) {
            Map<Class<? extends Annotation>, Annotation> superAnnotations =
                    superClass.annotationData().annotations;
            for (Map.Entry<Class<? extends Annotation>, Annotation> e : superAnnotations.entrySet()) {
                Class<? extends Annotation> annotationClass = e.getKey();
                if (AnnotationType.getInstance(annotationClass).isInherited()) {
                    if (annotations == null) { // lazy construction
                        annotations = new LinkedHashMap<>((Math.max(
                                declaredAnnotations.size(),
                                Math.min(12, declaredAnnotations.size() + superAnnotations.size())
                        ) * 4 + 2) / 3
                        );
                    }
                    annotations.put(annotationClass, e.getValue());
                }
            }
        }
        if (annotations == null) {
            // no inherited annotations -> share the Map with declaredAnnotations
            annotations = declaredAnnotations;
        } else {
            // at least one inherited annotation -> declared may override inherited
            annotations.putAll(declaredAnnotations);
        }
        return new AnnotationData(annotations, declaredAnnotations, classRedefinedCount);
    }
    /**
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations()  {
        return AnnotationParser.toArray(annotationData().declaredAnnotations);
    }
    
}
```
```java
//AnnotationParser 在JDK中有个类，是这个包下的
package sun.reflect.annotation;

public class AnnotationParser {
    /*
     * This method converts the annotation map returned by the parseAnnotations()
     * method to an array.  It is called by Field.getDeclaredAnnotations(),
     * Method.getDeclaredAnnotations(), and Constructor.getDeclaredAnnotations().
     * This avoids the reflection classes to load the Annotation class until
     * it is needed.
     */
    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
    public static Annotation[] toArray(Map<Class<? extends Annotation>, Annotation> annotations) {
        return annotations.values().toArray(EMPTY_ANNOTATION_ARRAY);
    }
    /**
     * Parses the annotation at the current position in the specified
     * byte buffer, resolving constant references in the specified constant
     * pool.  The cursor of the byte buffer must point to an "annotation
     * structure" as described in the RuntimeVisibleAnnotations_attribute:
     *
     * annotation {
     *    u2    type_index;
     *       u2    num_member_value_pairs;
     *       {    u2    member_name_index;
     *             member_value value;
     *       }    member_value_pairs[num_member_value_pairs];
     *    }
     * }
     *
     * Returns the annotation, or null if the annotation's type cannot
     * be found by the VM, or is not a valid annotation type.
     *
     * @param exceptionOnMissingAnnotationClass if true, throw
     * TypeNotPresentException if a referenced annotation type is not
     * available at runtime
     */
    static Annotation parseAnnotation(ByteBuffer buf,
                                      ConstantPool constPool,
                                      Class<?> container,
                                      boolean exceptionOnMissingAnnotationClass) {
        return parseAnnotation2(buf, constPool, container, exceptionOnMissingAnnotationClass, null);
    }
    private static Map<Class<? extends Annotation>, Annotation> parseAnnotations2(
            byte[] rawAnnotations,
            ConstantPool constPool,
            Class<?> container,
            Class<? extends Annotation>[] selectAnnotationClasses) {
        Map<Class<? extends Annotation>, Annotation> result =
                new LinkedHashMap<Class<? extends Annotation>, Annotation>();
        ByteBuffer buf = ByteBuffer.wrap(rawAnnotations);
        int numAnnotations = buf.getShort() & 0xFFFF;
        for (int i = 0; i < numAnnotations; i++) {
            Annotation a = parseAnnotation2(buf, constPool, container, false, selectAnnotationClasses);
            if (a != null) {
                Class<? extends Annotation> klass = a.annotationType();
                if (AnnotationType.getInstance(klass).retention() == RetentionPolicy.RUNTIME &&
                        result.put(klass, a) != null) {
                    throw new AnnotationFormatError(
                            "Duplicate annotation for class: "+klass+": " + a);
                }
            }
        }
        return result;
    }
}
```
## 解决方案
通过创建自定义SpringExternalClassLoader和ShadedClassLoader，覆盖当前线程的上下文的spring 的 LaunchedURLClassLoader，解决不能加载引入包的问题；类的层级如下：
![类加载器层级](doc/images/img.png)


## 附加调试
### 启动添加参数配置
``` bat
java -javaagent:/Users/zhanguowang/Desktop/project/github/java-agent-example/agentdemo/springAgentInject/target/springAgentInject-1-SNAPSHOT.jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9999 -jar springAgentDemo-0.0.1-SNAPSHOT.jar
```
### idea 增加远程调试
host：localhost 端口使用9999
## 启动脚本
-run.sh


