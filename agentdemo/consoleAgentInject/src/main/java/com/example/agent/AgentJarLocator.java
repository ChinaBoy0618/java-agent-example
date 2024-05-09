package com.example.agent;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * @author wzg
 * @date 2024/4/11 16:07
 */
public class AgentJarLocator {
    static File getAgentJarFile() throws URISyntaxException {
        ProtectionDomain protectionDomain = MyAgent.class.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) {
            throw new IllegalStateException(String.format("Unable to get agent location, protection domain = %s", protectionDomain));
        }
        URL location = codeSource.getLocation();
        if (location == null) {
            throw new IllegalStateException(String.format("Unable to get agent location, code source = %s", codeSource));
        }
        final File agentJar = new File(location.toURI());
        if (!agentJar.getName().endsWith(".jar")) {
            throw new IllegalStateException("Agent is not a jar file: " + agentJar);
        }
        return agentJar.getAbsoluteFile();
    }
    static File getAppJarFile(ProtectionDomain protectionDomain)throws URISyntaxException{
        if(protectionDomain==null){
            throw new IllegalStateException(String.format("Unable to get app location, protection domain = %s", protectionDomain));
        }
        CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) {
            throw new IllegalStateException(String.format("Unable to get app location, protection domain = %s", protectionDomain));
        }
        URL location = codeSource.getLocation();
        if (location == null) {
            throw new IllegalStateException(String.format("Unable to get app location, code source = %s", codeSource));
        }
        final File appJar = new File(location.toURI());
        if (!appJar.getName().endsWith(".jar")) {
            throw new IllegalStateException("App is not a jar file: " + appJar);
        }
        return appJar.getAbsoluteFile();
    }
}
