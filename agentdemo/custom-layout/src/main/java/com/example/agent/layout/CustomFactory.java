package com.example.agent.layout;

import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LayoutFactory;
import org.springframework.boot.loader.tools.Layouts;

import java.io.File;

/**
 * 自定义打包LayoutFactory
 *
 * @author wzg
 * @date 2024/5/13 09:01
 */
public class CustomFactory implements LayoutFactory {

    @Override
    public Layout getLayout(File source) {
        return new AgentLoaderLayout(source);
    }

}