package com.qiuhuanhen;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.qiuhuanhen.utils.AnnotationEnum;

/**
 * @author qiuhuanhen
 */
public class SwaggerTool extends CommonTool {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        super.setAnnotationEnum(AnnotationEnum.SWAGGER);
        super.actionPerformed(anActionEvent);
    }

}
