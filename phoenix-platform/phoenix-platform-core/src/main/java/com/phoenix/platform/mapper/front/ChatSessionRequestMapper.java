package com.phoenix.platform.mapper.front;

import com.phoenix.platform.dto.request.ChatSessionRequestQuery;
import com.phoenix.platform.vo.ChatSessionRequestVO;
import com.phoenix.platform.vo.ChatSessionAgentStatVO;
import com.phoenix.platform.vo.ChatSessionUserStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户对智能体的请求记录 Mapper
 *
 * <p>跨表查询 tbl_data_chat_session、tbl_data_chat_message、tbl_platform_account_info、tbl_data_agent</p>
 */
@Mapper
public interface ChatSessionRequestMapper {

	/**
	 * 分页查询请求记录明细（消息级）
	 *
	 * @param query  查询条件
	 * @param limit  每页条数
	 * @param offset 偏移量
	 * @return 本页请求记录列表
	 */
	List<ChatSessionRequestVO> selectPageByQuery(@Param("query") ChatSessionRequestQuery query,
												 @Param("limit") int limit,
												 @Param("offset") long offset);

	/**
	 * 统计满足条件的总记录数
	 *
	 * @param query 查询条件
	 * @return 总记录数
	 */
	long countByQuery(@Param("query") ChatSessionRequestQuery query);

	/**
	 * 按智能体维度统计（该智能体下有多少用户使用、多少请求）
	 *
	 * @param query 查询过滤条件（可传 agentId / keyword / startTime / endTime）
	 * @param limit 最多返回条数
	 * @return 智能体统计列表
	 */
	List<ChatSessionAgentStatVO> selectAgentStatsByQuery(@Param("query") ChatSessionRequestQuery query,
														@Param("limit") int limit);

	/**
	 * 按用户维度统计（该用户下使用多少智能体、多少请求）
	 *
	 * @param query 查询过滤条件（可传 keyword / startTime / endTime）
	 * @param limit 最多返回条数
	 * @return 用户统计列表
	 */
	List<ChatSessionUserStatVO> selectUserStatsByQuery(@Param("query") ChatSessionRequestQuery query,
													  @Param("limit") int limit);
}