package com.phoenix.data.aop;

import com.alibaba.cloud.ai.graph.OverAllState;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * AOP切面类，用于记录所有Node类的入口日志
 *
 *
 */
@Aspect
@Component
@Slf4j
public class NodeEntryLoggingAspect {

	/**
	 * 切入点：匹配 workflow.node 包下所有节点的 apply 方法。
	 */
	@Pointcut("execution(* com.phoenix.data.workflow.node..*.apply(com.alibaba.cloud.ai.graph.OverAllState))")
	public void nodeEntry() {
	}

	/**
	 * 在所有实现NodeAction接口的类的apply方法执行前记录日志
	 * @param joinPoint 连接点
	 */
	@Before("nodeEntry()")
	public void logNodeEntry(JoinPoint joinPoint) {
		String className = joinPoint.getTarget().getClass().getSimpleName();
		log.info("Entering {} node", className);

		// 获取方法参数并打印状态信息
		Object[] args = joinPoint.getArgs();
		if (args != null && args.length > 0 && args[0] instanceof OverAllState state) {
			log.debug("State: {}", state);
		}
	}

}
