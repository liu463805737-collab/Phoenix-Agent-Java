package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.entity.ChatSession;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话 Mapper 接口
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

	/**
	 * 根据智能体ID查询会话列表
	 */
	@Select("""
			SELECT * FROM tbl_data_chat_session
			WHERE agent_id = #{agentId} AND user_id = #{userId} AND status != 'deleted'
			ORDER BY is_pinned DESC, update_time DESC
			""")
	List<ChatSession> findByAgentIdAndUserId(@Param("agentId") Integer agentId, @Param("userId") String userId);

	/**
	 * 根据智能体ID查询会话列表
	 */
	@Select("""
			SELECT * FROM tbl_data_chat_session
			WHERE agent_id = #{agentId} AND status != 'deleted'
			ORDER BY is_pinned DESC, update_time DESC
			""")
	List<ChatSession> selectByAgentId(@Param("agentId") Integer agentId);

	/**
	 * 根据会话ID查询会话详情
	 */
	@Select("""
			SELECT * FROM tbl_data_chat_session
			WHERE id = #{sessionId} AND status != 'deleted'
			""")
	ChatSession selectBySessionId(@Param("sessionId") String sessionId);

	/**
	 * 更新会话信息
	 */
	@Update("""
			<script>
			UPDATE tbl_data_chat_session
			<set>
				<if test="title != null">title = #{title},</if>
				<if test="status != null">status = #{status},</if>
				<if test="isPinned != null">is_pinned = #{isPinned},</if>
				<if test="userId != null">user_id = #{userId},</if>
				update_time = NOW()
			</set>
			WHERE id = #{sessionId}
			</script>
			""")
	int updateById(ChatSession session);

	/**
	 * 软删除智能体下的所有会话
	 */
	@Update("""
			UPDATE tbl_data_chat_session
			SET status = 'deleted', update_time = #{updateTime}
			WHERE agent_id = #{agentId}
			""")
	int softDeleteByAgentId(@Param("agentId") Integer agentId, @Param("updateTime") LocalDateTime updateTime);

	/**
	 * 更新会话时间
	 */
	@Update("""
			UPDATE tbl_data_chat_session
			SET update_time = #{updateTime}
			WHERE id = #{sessionId}
			""")
	int updateSessionTime(@Param("sessionId") String sessionId, @Param("updateTime") LocalDateTime updateTime);

	/**
	 * 更新会话置顶状态
	 */
	@Update("""
			UPDATE tbl_data_chat_session SET
				is_pinned = #{isPinned},
				update_time = #{updateTime}
			WHERE id = #{sessionId}
			""")
	int updatePinStatus(@Param("sessionId") String sessionId, @Param("isPinned") boolean isPinned,
			@Param("updateTime") LocalDateTime updateTime);

	/**
	 * 更新会话标题
	 */
	@Update("""
			UPDATE tbl_data_chat_session SET
				title = #{title},
				update_time = #{updateTime}
			WHERE id = #{sessionId}
			""")
	int updateTitle(@Param("sessionId") String sessionId, @Param("title") String title,
			@Param("updateTime") LocalDateTime updateTime);

	/**
	 * 软删除会话
	 */
	@Update("""
			UPDATE tbl_data_chat_session
			SET status = 'deleted', update_time = #{updateTime}
			WHERE id = #{sessionId}
			""")
	int softDeleteById(@Param("sessionId") String sessionId, @Param("updateTime") LocalDateTime updateTime);

}
