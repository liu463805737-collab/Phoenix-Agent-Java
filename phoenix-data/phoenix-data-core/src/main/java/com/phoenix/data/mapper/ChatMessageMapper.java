package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.entity.ChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 聊天消息 Mapper 接口
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

	/**
	 * 根据会话ID查询消息列表
	 */
	@Select("""
			SELECT * FROM tbl_data_chat_message
			WHERE session_id = #{sessionId}
			ORDER BY create_time ASC
			""")
	List<ChatMessage> selectBySessionId(@Param("sessionId") String sessionId);

	/**
	 * 根据ID查询消息
	 */
	@Select("""
			SELECT * FROM tbl_data_chat_message
			WHERE id = #{id}
			""")
	ChatMessage selectById(@Param("id") Long id);

	/**
	 * 统计会话的消息数量
	 */
	@Select("""
			SELECT COUNT(*) FROM tbl_data_chat_message
			WHERE session_id = #{sessionId}
			""")
	int countBySessionId(@Param("sessionId") String sessionId);

	/**
	 * 根据会话ID和角色查询消息列表
	 */
	@Select("""
			SELECT * FROM tbl_data_chat_message
			WHERE session_id = #{sessionId}
			AND role = #{role}
			ORDER BY create_time ASC
			""")
	List<ChatMessage> selectBySessionIdAndRole(@Param("sessionId") String sessionId, @Param("role") String role);

}
