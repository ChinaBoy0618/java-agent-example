package com.example.agent;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author wzg
 * @date 2024/4/27 23:36
 */
public class MyInterceptor {
    /***
     * 拦截方法
     * @param method：拦截的方法
     * @param callable：调用对象的代理对象
     * @return
     * @throws Exception
     */
    @RuntimeType // 声明为static
    public static Object intercept(@Origin Method method,
                                   @SuperCall Callable<?> callable) throws Exception {


        // 执行原函数
        Object result = callable.call();
        //执行时间统计
        System.out.println(method.getName() + ":classLoader:" +result);
        return result;
    }
}
