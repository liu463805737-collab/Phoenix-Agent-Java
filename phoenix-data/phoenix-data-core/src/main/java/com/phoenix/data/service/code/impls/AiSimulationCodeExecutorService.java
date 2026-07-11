package com.phoenix.data.service.code.impls;

import com.phoenix.data.service.code.CodePoolExecutorService;
import com.phoenix.data.service.llm.LlmService;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用AI模拟运行Python代码（便于在无Docker环境测试）
 *
 * @author vlsmb
 * @since 2025/7/30
 */
@Slf4j
public class AiSimulationCodeExecutorService implements CodePoolExecutorService {

	private static final String SYSTEM_PROMPT = """
			你将模拟Python的执行，根据我提供的代码和输入数据，并给出最终的数据结果。
			在模拟运行时，请按照以下要求操作：
			1. 仔细理解代码和输入数据的内容。
			2. 输出模拟运行结果。
			**要求**：仅输出模拟运行结果，禁止包含任何额外说明或自然语言。
			""";

	private final LlmService llmService;

	/**
	 * 构造 AI 模拟代码执行器
	 */
	public AiSimulationCodeExecutorService(LlmService llmService) {
		this.llmService = llmService;
	}

	/**
	 * 使用 AI 模拟运行 Python 代码并返回结果
	 */
	@Override
	public TaskResponse runTask(TaskRequest request) {
		String userPrompt = String.format("""
				【代码】
				```python
				%s
				```
				【标准输入】
				```json
				%s
				```
				""", request.code(), request.input());
		String output = llmService.toStringFlux(llmService.call(SYSTEM_PROMPT, userPrompt))
			.collect(StringBuilder::new, StringBuilder::append)
			.map(StringBuilder::toString)
			.block();
		return TaskResponse.success(output);
	}

}
