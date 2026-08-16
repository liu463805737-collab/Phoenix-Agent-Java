package com.phoenix.platform.service.front;

import com.mybatisflex.core.paginate.Page;

import com.phoenix.platform.dto.request.ChatSessionRequestQuery;
import com.phoenix.platform.vo.ChatSessionRequestVO;
import com.phoenix.platform.vo.ChatSessionAgentStatVO;
import com.phoenix.platform.vo.ChatSessionUserStatVO;

import java.util.List;

/**
 * 用户对智能体的请求管理服务
 *
 * <p>只读查询，不涉及执行入口的修改。</p>
 */
public interface ChatSessionRequestService {

	/**
	 * 分页查询请求记录（消息级明细）
	 */
	Page<ChatSessionRequestVO> page(ChatSessionRequestQuery query);

	/**
	 * 统计总数
	 */
	long count(ChatSessionRequestQuery query);

	/**
	 * 按智能体维度统计
	 *
	 * @param query 可选过滤条件
	 * @param limit 最多返回条数，建议不设上限（传 Integer.MAX_VALUE）
	 * @return 智能体统计列表
	 */
	List<ChatSessionAgentStatVO> agentStats(ChatSessionRequestQuery query, int limit);

	/**
	 * 按用户维度统计
	 *
	 * @param query 可选过滤条件
	 * @param limit 最多返回条数
	 * @return 用户统计列表
	 */
	List<ChatSessionUserStatVO> userStats(ChatSessionRequestQuery query, int limit);
}