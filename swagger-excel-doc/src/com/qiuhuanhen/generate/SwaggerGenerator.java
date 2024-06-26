package com.qiuhuanhen.generate;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.qiuhuanhen.utils.AnnotationEnum;
import com.qiuhuanhen.utils.CommentUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class SwaggerGenerator extends CommonGenerator {

    private static final String MAPPING_VALUE = "value";
    private static final String MAPPING_METHOD = "method";
    private static final String REQUEST_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.RequestMapping";
    private static final String POST_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.PostMapping";
    private static final String GET_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.GetMapping";
    private static final String DELETE_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.DeleteMapping";
    private static final String PATCH_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.PatchMapping";
    private static final String PUT_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.PutMapping";
    private static final String REQUEST_PARAM_TEXT = "org.springframework.web.bind.annotation.RequestParam";
    private static final String REQUEST_HEADER_TEXT = "org.springframework.web.bind.annotation.RequestHeader";
    private static final String PATH_VARIABLE_TEXT = "org.springframework.web.bind.annotation.PathVariable";
    private static final String REQUEST_BODY_TEXT = "org.springframework.web.bind.annotation.RequestBody";


    public SwaggerGenerator(Project project, PsiFile psiFile, PsiClass psiClass, String selectionText) {
        super(project, psiFile, psiClass, selectionText);
    }

    @Override
    public void doGenerate(AnnotationEnum generateType) {
        WriteCommandAction.runWriteCommandAction(this.getProject(), () -> {
            boolean selection = false;
            if (StringUtils.isNotEmpty(this.getSelectionText())) {
                selection = true;
            }
            // 遍历当前对象的所有属性
            boolean isController = this.isController(this.getPsiClass());

            if (Objects.equals(AnnotationEnum.EASY_EXCEL,generateType)) {
                isController = false;
            }

            if (selection) {
                this.generateSelection(this.getPsiClass(), this.getSelectionText(), isController, generateType);
                return;
            }

            // 获取注释
            this.generateClassAnnotation(this.getPsiClass(), isController, generateType);
            if (isController) {
                // 类方法列表
                PsiMethod[] methods = this.getPsiClass().getMethods();
                for (PsiMethod psiMethod : methods) {
                    this.generateMethodAnnotation(psiMethod);
                }
            } else {
                PsiClass[] innerClasses = this.getPsiClass().getInnerClasses();
                if (innerClasses.length > 0) {
                    for (PsiClass innerClass : innerClasses) {
                        this.generateClassAnnotation(innerClass, false, generateType);
                        // 类属性列表
                        PsiField[] field = innerClass.getAllFields();
                        for (PsiField psiField : field) {
                            this.generateFieldAnnotation(psiField, generateType);
                        }
                    }
                }
                // 类属性列表
                PsiField[] field = this.getPsiClass().getAllFields();
                for (PsiField psiField : field) {
                    this.generateFieldAnnotation(psiField, generateType);
                }
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
    @Override
    public void doWrite(String name, String qualifiedName, String annotationText, PsiModifierListOwner psiModifierListOwner) {
        super.doWrite(name, qualifiedName, annotationText, psiModifierListOwner);
    }

    /**
     * 类是否为controller
     *
     * @param psiClass 类元素
     * @return boolean
     */
    private void generateSelection(PsiClass psiClass, String selectionText, boolean isController, AnnotationEnum generateType) {
        if (Objects.equals(selectionText, psiClass.getName())) {
            this.generateClassAnnotation(psiClass, isController, generateType);
        }
        PsiMethod[] methods = psiClass.getMethods();
        for (PsiMethod psiMethod : methods) {
            if (Objects.equals(selectionText, psiMethod.getName())) {
                this.generateMethodAnnotation(psiMethod);
                return;
            }
        }
        PsiField[] field = psiClass.getAllFields();
        for (PsiField psiField : field) {
            if (Objects.equals(selectionText, psiField.getNameIdentifier().getText())) {
                this.generateFieldAnnotation(psiField, generateType);
                return;
            }
        }
    }

    /**
     * 类是否为controller
     *
     * @param psiClass 类元素
     * @return boolean
     */
    private boolean isController(PsiClass psiClass) {
        PsiAnnotation[] psiAnnotations = psiClass.getModifierList().getAnnotations();
        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            String controllerAnnotation = "org.springframework.stereotype.Controller";
            String restControllerAnnotation = "org.springframework.web.bind.annotation.RestController";
            if (controllerAnnotation.equals(psiAnnotation.getQualifiedName())
                    || restControllerAnnotation.equals(psiAnnotation.getQualifiedName())) {
                // controller
                return true;
            }
        }
        return false;
    }

    /**
     * 获取RequestMapping注解属性
     *
     * @param psiAnnotations 注解元素数组
     * @param attributeName  属性名
     * @return String 属性值
     */
    private String getMappingAttribute(PsiAnnotation[] psiAnnotations, String attributeName) {
        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            switch (Objects.requireNonNull(psiAnnotation.getQualifiedName())) {
                case REQUEST_MAPPING_ANNOTATION:
                    String attribute = getAttribute(psiAnnotation, attributeName, "");
                    if (Objects.equals("\"\"", attribute)) {
                        return "";
                    }
                    return attribute;
                case POST_MAPPING_ANNOTATION:
                    return "POST";
                case GET_MAPPING_ANNOTATION:
                    return "GET";
                case DELETE_MAPPING_ANNOTATION:
                    return "DELETE";
                case PATCH_MAPPING_ANNOTATION:
                    return "PATCH";
                case PUT_MAPPING_ANNOTATION:
                    return "PUT";
                default:
                    break;
            }
        }
        return "";
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

    @Override
    public void generateClassAnnotation(PsiClass psiClass, AnnotationEnum generateType) {

    }

    /**
     * 生成类注解
     *
     * @param psiClass     类元素
     * @param isController 是否为controller
     */
    private void generateClassAnnotation(PsiClass psiClass, boolean isController, AnnotationEnum generateType) {
        PsiComment classComment = null;
        for (PsiElement tmpEle : psiClass.getChildren()) {
            if (tmpEle instanceof PsiComment) {
                classComment = (PsiComment) tmpEle;
                // 注释的内容
                String tmpText = classComment.getText();
                String commentDesc = CommentUtils.getCommentDesc(tmpText);
                String annotationFromText;
                String annotation;
                String qualifiedName;
                if (isController) {
                    annotation = "Api";
                    qualifiedName = "io.swagger.annotations.Api";
                    String fieldValue = this.getMappingAttribute(psiClass.getModifierList().getAnnotations(), MAPPING_VALUE);
                    annotationFromText = String.format("@%s(value = %s, tags = {\"%s\"})", annotation, fieldValue, commentDesc);
                } else {
                    annotation = "ApiModel";
                    qualifiedName = "io.swagger.annotations.ApiModel";
                    annotationFromText = String.format("@%s(description = \"%s\")", annotation, commentDesc);

                }
                this.doWrite(annotation, qualifiedName, annotationFromText, psiClass);
            }
        }
        if (Objects.isNull(classComment)) {
            String annotationFromText;
            String annotation;
            String qualifiedName;
            if (isController) {
                annotation = "Api";
                qualifiedName = "io.swagger.annotations.Api";
                String fieldValue = this.getMappingAttribute(psiClass.getModifierList().getAnnotations(), MAPPING_VALUE);
                annotationFromText = String.format("@%s(value = %s)", annotation, fieldValue);
            } else {

                annotation = "ApiModel";
                qualifiedName = "io.swagger.annotations.ApiModel";
                annotationFromText = String.format("@%s", annotation);
            }
            this.doWrite(annotation, qualifiedName, annotationFromText, psiClass);
        }
    }

    /**
     * 生成方法注解
     *
     * @param psiMethod 类方法元素
     */
    private void generateMethodAnnotation(PsiMethod psiMethod) {
        String commentDesc = "";
        Map<String, String> methodParamCommentDesc = null;
        for (PsiElement tmpEle : psiMethod.getChildren()) {
            if (tmpEle instanceof PsiComment) {
                PsiComment classComment = (PsiComment) tmpEle;
                // 注释的内容
                String tmpText = classComment.getText();
                methodParamCommentDesc = CommentUtils.getCommentMethodParam(tmpText);
                commentDesc = CommentUtils.getCommentDesc(tmpText);
            }
        }

        PsiAnnotation[] psiAnnotations = psiMethod.getModifierList().getAnnotations();
        String methodValue = this.getMappingAttribute(psiAnnotations, MAPPING_METHOD);

        // 如果存在注解，获取注解原本的value和notes内容
        PsiAnnotation apiOperationExist = psiMethod.getModifierList().findAnnotation("io.swagger.annotations.ApiOperation");
        String apiOperationAttrValue = this.getAttribute(apiOperationExist, "value", commentDesc);
        String apiOperationAttrNotes = this.getAttribute(apiOperationExist, "notes", commentDesc);
        String apiOperationAnnotationText;
        if (StringUtils.isNotEmpty(methodValue)) {
            methodValue = methodValue.substring(methodValue.indexOf(".") + 1);
            apiOperationAnnotationText = String.format("@ApiOperation(value = %s, notes = %s, httpMethod = \"%s\")", apiOperationAttrValue, apiOperationAttrNotes, methodValue);
        } else {
            apiOperationAnnotationText = String.format("@ApiOperation(value = %s, notes = %s)", apiOperationAttrValue, apiOperationAttrNotes);
        }

        String apiImplicitParamsAnnotationText = null;
        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        List<String> apiImplicitParamList = new ArrayList<>(psiParameters.length);
        for (PsiParameter psiParameter : psiParameters) {
            PsiType psiType = psiParameter.getType();
            String dataType = CommentUtils.getDataType(psiType.getCanonicalText(), psiType);
            String paramType = "query";
            for (PsiAnnotation psiAnnotation : psiParameter.getModifierList().getAnnotations()) {
                if (StringUtils.isEmpty(psiAnnotation.getQualifiedName())) {
                    break;
                }
                switch (psiAnnotation.getQualifiedName()) {
                    case REQUEST_HEADER_TEXT:
                        paramType = "header";
                        break;
                    case REQUEST_PARAM_TEXT:
                        paramType = "query";
                        break;
                    case PATH_VARIABLE_TEXT:
                        paramType = "path";
                        break;
                    case REQUEST_BODY_TEXT:
                        paramType = "body";
                        break;
                    default:
                        break;
                }
            }
            if (Objects.equals(dataType, "file")) {
                paramType = "form";
            }
            String paramDesc = "";
            if (methodParamCommentDesc != null) {
                paramDesc = methodParamCommentDesc.get(psiParameter.getNameIdentifier().getText());
            }
            String apiImplicitParamText =
                    String.format("@ApiImplicitParam(paramType = \"%s\", dataType = \"%s\", name = \"%s\", value = \"%s\")",
                            paramType, dataType, psiParameter.getNameIdentifier().getText(), paramDesc == null ? "" : paramDesc);

            apiImplicitParamList.add(apiImplicitParamText);
        }
        if (apiImplicitParamList.size() != 0) {
            apiImplicitParamsAnnotationText = apiImplicitParamList.stream().collect(Collectors.joining(",\n", "@ApiImplicitParams({\n", "\n})"));
        }

        this.doWrite("ApiOperation", "io.swagger.annotations.ApiOperation", apiOperationAnnotationText, psiMethod);
        if (StringUtils.isNotEmpty(apiImplicitParamsAnnotationText)) {
            this.doWrite("ApiImplicitParams", "io.swagger.annotations.ApiImplicitParams", apiImplicitParamsAnnotationText, psiMethod);
        }
        addImport(this.getElementFactory(), this.getPsiFile(), "ApiImplicitParam");
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
                apiModelPropertyText = String.format("@ApiModelProperty(value=\"%s\")", commentDesc);
                this.doWrite("ApiModelProperty", "io.swagger.annotations.ApiModelProperty", apiModelPropertyText, psiField);

            }
        }
        // 如果没有文档注释
        if (Objects.isNull(classComment)) {
            this.doWrite("ApiModelProperty", "io.swagger.annotations.ApiModelProperty", "@ApiModelProperty(\"\")", psiField);
        }
    }

}
