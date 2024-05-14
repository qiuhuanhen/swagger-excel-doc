package com.qiuhuanhen;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.qiuhuanhen.generate.CommonGenerator;
import com.qiuhuanhen.utils.AnnotationEnum;

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * @author qiuhuanhen
 */
public class CommonTool  extends AnAction {

    private AnnotationEnum annotationEnum;

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        // 获取当前的project对象
        Project project = anActionEvent.getProject();
        // 获取当前文件对象
        Editor editor = anActionEvent.getData(PlatformDataKeys.EDITOR);
        if (Objects.isNull(project) || Objects.isNull(editor)) {
            return;
        }
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiClass psiClass = PsiTreeUtil.findChildOfAnyType(psiFile, PsiClass.class);
        String selectionText = editor.getSelectionModel().getSelectedText();

        try {
            Constructor constructor = AnnotationEnum.generatorClassMap().get(annotationEnum).getConstructor(Project.class, PsiFile.class, PsiClass.class, String.class);
            CommonGenerator generator = (CommonGenerator) constructor.newInstance(project, psiFile, psiClass, selectionText);
            generator.doGenerate(annotationEnum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAnnotationEnum(AnnotationEnum annotationEnum) {
        this.annotationEnum = annotationEnum;
    }
}
