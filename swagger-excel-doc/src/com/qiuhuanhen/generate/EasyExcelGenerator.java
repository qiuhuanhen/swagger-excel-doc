package com.qiuhuanhen.generate;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifierListOwner;
import com.qiuhuanhen.utils.AnnotationEnum;
import com.qiuhuanhen.utils.CommentUtils;

import java.util.Objects;


/**
 * @author qiuhuanhen
 */
public class EasyExcelGenerator extends CommonGenerator {


    public EasyExcelGenerator(Project project, PsiFile psiFile, PsiClass psiClass, String selectionText) {
        super(project, psiFile, psiClass, selectionText);
    }

    @Override
    public void doGenerate(AnnotationEnum generateType) {
        super.doGenerate(generateType);
    }

    /**
     * 写入到文件
     *
     * @param name                 注解名
     * @param qualifiedName        注解全包名
     * @param annotationText       生成注解文本
     * @param psiModifierListOwner 当前写入对象
     */
    @Override
    public void doWrite(String name, String qualifiedName, String annotationText, PsiModifierListOwner psiModifierListOwner) {
        super.doWrite(name, qualifiedName, annotationText, psiModifierListOwner);
    }


    /**
     * 生成类注解
     *
     * @param psiClass 类元素
     */
    @Override
    public void generateClassAnnotation(PsiClass psiClass, AnnotationEnum generateType) {
        for (PsiElement tmpEle : psiClass.getChildren()) {
            String annotationFromText;
            String annotation;
            String qualifiedName;
            if (tmpEle instanceof PsiComment) {

                String headAnnotation = "HeadRowHeight";
                String headQualifiedName = "com.alibaba.excel.annotation.write.style.HeadRowHeight";
                String headAnnotationFromText = "@HeadRowHeight(30)";
                this.doWrite(headAnnotation, headQualifiedName, headAnnotationFromText, psiClass);

                annotation = "ContentRowHeight";
                qualifiedName = "com.alibaba.excel.annotation.write.style.ContentRowHeight";
                annotationFromText = "@ContentRowHeight(20)";

                this.doWrite(annotation, qualifiedName, annotationFromText, psiClass);

            }
        }
    }


    /**
     * 生成属性注解
     *
     * @param psiField 类属性元素
     */
    @Override
    public void generateFieldAnnotation(PsiField psiField, AnnotationEnum generateType) {
        PsiComment classComment = null;
        for (PsiElement tmpEle : psiField.getChildren()) {
            if (tmpEle instanceof PsiComment) {
                classComment = (PsiComment) tmpEle;
                // 注释的内容
                String tmpText = classComment.getText();
                String commentDesc = CommentUtils.getCommentDesc(tmpText);
                String apiModelPropertyText = "";

                apiModelPropertyText = String.format("@ExcelProperty(value=\"%s\")", commentDesc);
                this.doWrite("ExcelProperty", "com.alibaba.excel.annotation.ExcelProperty", apiModelPropertyText, psiField);
                this.doWrite("ColumnWidth", "com.alibaba.excel.annotation.write.style.ColumnWidth", "@ColumnWidth(20)", psiField);
            }
        }
        // 如果没有文档注释
        if (Objects.isNull(classComment)) {

            this.doWrite("ExcelProperty", "com.alibaba.excel.annotation.ExcelProperty", "@ExcelProperty(\"\")", psiField);
            this.doWrite("ColumnWidth", "com.alibaba.excel.annotation.write.style.ColumnWidth", "@ColumnWidth(20)", psiField);
        }
    }

}
