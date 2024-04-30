package com.example.agentdemo;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;

import static net.bytebuddy.matcher.ElementMatchers.declaresMethod;
import static net.bytebuddy.matcher.ElementMatchers.isMain;

/**
 * @author wzg
 * @date 2024/4/11 15:22
 */
public class MyAgent {

    public static void  premain(String agentArgs, Instrumentation instrumentation){
        System.out.println("premain started");
            new AgentBuilder.Default()
                    .type(declaresMethod(isMain()))
                    .transform(new AgentBuilder.Transformer() {
                        @Override
                        public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                            try{
//                                System.out.println(AgentJarLocator.getAppJarFile(protectionDomain).getAbsolutePath());
                                injectClass(protectionDomain);
                            }catch (URISyntaxException uriSyntaxException){
                                uriSyntaxException.printStackTrace();
                            }
                            catch (Exception ex){
                                ex.printStackTrace();
                            }

                            return null;
                        }
                    })
                    .installOn(instrumentation);

        System.out.println("premain ended");


    }
    static  void injectClass( ProtectionDomain protectionDomain)throws URISyntaxException, IOException {
                DynamicType.Unloaded unloaded= new ByteBuddy()

                .with(new NamingStrategy.AbstractBase() {
                    @Override
                    protected String name(TypeDescription superClass) {
                        return "i.love.ByteBuddy." + superClass.getSimpleName();
                    }
                })
                .subclass(Object.class)
                .make();
        unloaded.inject(AgentJarLocator.getAppJarFile(protectionDomain));
    }

}
