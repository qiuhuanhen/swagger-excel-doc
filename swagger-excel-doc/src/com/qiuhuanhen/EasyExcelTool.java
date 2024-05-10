package com.qiuhuanhen;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.qiuhuanhen.utils.AnnotationEnum;

/**
 * @author qiuhuanhen
 */
public class EasyExcelTool  extends CommonTool {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        super.setAnnotationEnum(AnnotationEnum.EASY_EXCEL);
        super.actionPerformed(anActionEvent);
    }

}
