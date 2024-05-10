package com.qiuhuanhen.utils;

/**
 * Description: 注解类型
 * date: 2024/5/9 9:35
 * @author qiuhuanhen
 */

public enum AnnotationEnum {

    /**
     * swagger注解 （@ApiModelProperty）
     */
    SWAGGER(1),

    /**
     * easy excel注解 (@ExcelProperty)
     */
    EASY_EXCEL(2);

    private Integer type;

    AnnotationEnum(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
