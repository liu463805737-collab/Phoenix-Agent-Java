package com.phoenix.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * InEnum 注解的校验器，校验目标值是否在指定枚举的取值范围内。
 */
public class InEnumValidator implements ConstraintValidator<InEnum, Object> {

	private final Set<Object> allowedValues = new HashSet<>();

	/**
	 * 初始化校验器，解析注解中指定的枚举类和方法，收集所有允许的值。
	 * @param annotation InEnum 注解实例
	 */
	@Override
	public void initialize(InEnum annotation) {
		Class<? extends Enum<?>> enumClass = annotation.value();
		String methodName = annotation.method();

		Enum<?>[] enums = enumClass.getEnumConstants();

		for (Enum<?> enumVal : enums) {
			try {
				// 如果是默认的 "name"，直接获取 name()
				if ("name".equals(methodName)) {
					allowedValues.add(enumVal.name());
				}
				else {
					// 否则利用反射调用指定的方法（比如 getCode）获取值
					Method method = enumClass.getMethod(methodName);
					// 允许访问私有方法（视情况而定，一般public不需要）
					method.setAccessible(true);
					Object val = method.invoke(enumVal);
					allowedValues.add(val);
				}
			}
			catch (Exception e) {
				throw new RuntimeException("校验注解初始化失败，无法获取枚举方法: " + methodName, e);
			}
		}
	}

	/**
	 * 校验目标值是否在允许的枚举值集合中。
	 * @param value 待校验的值
	 * @param context 校验上下文
	 * @return 如果值为 null 或空字符串或包含在允许集合中返回 true，否则返回 false
	 */
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		// 允许为空（空值检查通常由 @NotNull 处理）
		if (value == null) {
			return true;
		}

		if (value instanceof String && ((String) value).isEmpty()) {
			return true;
		}

		// 核心校验：看 Set 里有没有这个值
		return allowedValues.contains(value);
	}

}
