package com.phoenix.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Bean Validation 约束注解，校验字段或参数值是否在指定枚举范围内。
 */
@Documented
@Constraint(validatedBy = InEnumValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface InEnum {

	/**
	 * 指定枚举类
	 */
	Class<? extends Enum<?>> value();

	/**
	 * 指定判断的方法，默认是 name()，也可以是 getCode() 等
	 */
	String method() default "name";

	/**
	 * 校验失败时的提示消息
	 */
	String message() default "变量值必须是指定枚举值之一";

	/**
	 * 分组校验
	 */
	Class<?>[] groups() default {};

	/**
	 * 负载信息
	 */
	Class<? extends Payload>[] payload() default {};

}
