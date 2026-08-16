package com.phoenix.platform.controller;

import com.mybatisflex.core.paginate.Page;
import com.phoenix.platform.dto.request.ChatSessionRequestQuery;
import com.phoenix.platform.service.front.ChatSessionRequestService;
import com.phoenix.platform.vo.ChatSessionRequestVO;
import com.phoenix.platform.vo.ChatSessionAgentStatVO;
import com.phoenix.platform.vo.ChatSessionUserStatVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户对智能体的请求管理控制器
 *流水明细和两个维度的统计查询。
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/monitoring/agent-request")
@RequiredArgsConstructor
public class ChatSessionController {

	private final ChatSessionRequestService chatSessionRequestService;

	/**
	 * 分页查询请求记录（消息级明细）
	 */
	@GetMapping("/page")
	public ReturnVo<Page<ChatSessionRequestVO>> page(ChatSessionRequestQuery query) {
		Page<ChatSessionRequestVO> page = chatSessionRequestService.page(query);
		return ReturnVo.ok(page);
	}

	/**
	 * 按智能体维度统计
	 */
	@GetMapping("/agent-stats")
	public ReturnVo<List<ChatSessionAgentStatVO>> agentStats(ChatSessionRequestQuery query,
															  @RequestParam(defaultValue = "50") int limit) {
		List<ChatSessionAgentStatVO> stats = chatSessionRequestService.agentStats(query, limit);
		return ReturnVo.ok(stats);
	}

	/**
	 * 按用户维度统计，查询用户智能体使用数、请求总数、最后请求时间
	 */
	@GetMapping("/user-stats")
	public ReturnVo<List<ChatSessionUserStatVO>> userStats(ChatSessionRequestQuery query,
															@RequestParam(defaultValue = "50") int limit) {
		List<ChatSessionUserStatVO> stats = chatSessionRequestService.userStats(query, limit);
		return ReturnVo.ok(stats);
	}

}