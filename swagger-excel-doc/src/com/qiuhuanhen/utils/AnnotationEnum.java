package com.qiuhuanhen.utils;

import com.qiuhuanhen.generate.EasyExcelGenerator;
import com.qiuhuanhen.generate.SwaggerGenerator;

import java.util.HashMap;
import java.util.Map;

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


    public static Map<AnnotationEnum,Class> generatorClassMap() {
        Map<AnnotationEnum,Class> map = new HashMap<>();
        map.put(SWAGGER, SwaggerGenerator.class);
        map.put(EASY_EXCEL, EasyExcelGenerator.class);
        return map;
    }

}
