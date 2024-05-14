package com.qiuhuanhen.generate;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.qiuhuanhen.utils.AnnotationEnum;

import java.util.Objects;

/**
 * @author qiuhuanhen
 */
public abstract class CommonGenerator {


    private final Project project;
    private final PsiFile psiFile;
    private final PsiClass psiClass;
    private final PsiElementFactory elementFactory;
    private final String selectionText;

    public CommonGenerator(Project project, PsiFile psiFile, PsiClass psiClass, String selectionText) {
        this.project = project;
        this.psiFile = psiFile;
        this.psiClass = psiClass;
        this.selectionText = selectionText;
        this.elementFactory = JavaPsiFacade.getElementFactory(project);
    }

    public void doGenerate(AnnotationEnum generateType) {
        WriteCommandAction.runWriteCommandAction(project, () -> {

            // 获取注释
            this.generateClassAnnotation(psiClass, generateType);

            PsiClass[] innerClasses = psiClass.getInnerClasses();
            if (innerClasses.length > 0) {
                for (PsiClass innerClass : innerClasses) {
                    this.generateClassAnnotation(innerClass, generateType);
                    // 类属性列表
                    PsiField[] field = innerClass.getAllFields();
                    for (PsiField psiField : field) {
                        this.generateFieldAnnotation(psiField, generateType);
                    }
                }
            }
            // 类属性列表
            PsiField[] field = psiClass.getAllFields();
            for (PsiField psiField : field) {
                this.generateFieldAnnotation(psiField, generateType);
            }

        });
    }

    /**
     * 写入到文件
     *
     * @param name                 注解名
     * @param qualifiedName        注解全包名
     * @param annotationText       生成注解文本
     * @param psiModifierListOwner 当前写入对象
     */
    protected void doWrite(String name, String qualifiedName, String annotationText, PsiModifierListOwner psiModifierListOwner) {
        PsiAnnotation psiAnnotationDeclare = elementFactory.createAnnotationFromText(annotationText, psiModifierListOwner);
        final PsiNameValuePair[] attributes = psiAnnotationDeclare.getParameterList().getAttributes();
        PsiAnnotation existAnnotation = psiModifierListOwner.getModifierList().findAnnotation(qualifiedName);
        if (existAnnotation != null) {
            existAnnotation.delete();
        }
        addImport(elementFactory, psiFile, name);
        PsiAnnotation psiAnnotation = psiModifierListOwner.getModifierList().addAnnotation(name);
        for (PsiNameValuePair pair : attributes) {
            psiAnnotation.setDeclaredAttributeValue(pair.getName(), pair.getValue());
        }
    }



    /**
     * 获取注解属性
     *
     * @param psiAnnotation 注解全路径
     * @param attributeName 注解属性名
     * @return 属性值
     */
    private String getAttribute(PsiAnnotation psiAnnotation, String attributeName, String comment) {
        if (Objects.isNull(psiAnnotation)) {
            return "\"" + comment + "\"";
        }
        PsiAnnotationMemberValue psiAnnotationMemberValue = psiAnnotation.findDeclaredAttributeValue(attributeName);
        if (Objects.isNull(psiAnnotationMemberValue)) {
            return "\"" + comment + "\"";
        }
        return psiAnnotationMemberValue.getText();
    }

    /**
     * 生成类注解
     *
     * @param psiClass 类元素
     */
    public abstract void generateClassAnnotation(PsiClass psiClass, AnnotationEnum generateType);

    /**
     * 生成属性注解
     *
     * @param psiField 类属性元素
     */
    public abstract void generateFieldAnnotation(PsiField psiField, AnnotationEnum generateType) ;

    /**
     * 导入类依赖
     *
     * @param elementFactory 元素Factory
     * @param file           当前文件对象
     * @param className      类名
     */
    protected void addImport(PsiElementFactory elementFactory, PsiFile file, String className) {
        if (!(file instanceof PsiJavaFile)) {
            return;
        }
        final PsiJavaFile javaFile = (PsiJavaFile) file;
        // 获取所有导入的包
        final PsiImportList importList = javaFile.getImportList();
        if (importList == null) {
            return;
        }
        PsiClass[] psiClasses = PsiShortNamesCache.getInstance(project).getClassesByName(className, GlobalSearchScope.allScope(project));
        // 待导入类有多个同名类或没有时 让用户自行处理
        if (psiClasses.length != 1) {
            return;
        }
        PsiClass waiteImportClass = psiClasses[0];
        for (PsiImportStatementBase is : importList.getAllImportStatements()) {
            String impQualifiedName = Objects.requireNonNull(is.getImportReference()).getQualifiedName();
            if (Objects.equals(waiteImportClass.getQualifiedName(), impQualifiedName)) {
                // 已经导入
                return;
            }
        }
        importList.add(elementFactory.createImportStatement(waiteImportClass));
    }

    public Project getProject() {
        return project;
    }

    public PsiFile getPsiFile() {
        return psiFile;
    }

    public PsiClass getPsiClass() {
        return psiClass;
    }

    public PsiElementFactory getElementFactory() {
        return elementFactory;
    }

    public String getSelectionText() {
        return selectionText;
    }
}
