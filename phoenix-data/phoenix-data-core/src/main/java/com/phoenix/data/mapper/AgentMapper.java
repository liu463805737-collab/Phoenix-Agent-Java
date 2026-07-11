package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.entity.Agent;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 智能体 Mapper 接口
 */
@Mapper
public interface AgentMapper extends BaseMapper<Agent> {
	/**
	 * 查询所有智能体
	 */
	@Select("""
			<script>
				SELECT * FROM tbl_data_agent
				WHERE id IN
				<foreach collection="ids" item="id" open="(" separator="," close=")">
					#{id}
				</foreach>
				<if test='status != null and status != ""'>
					AND status = #{status}
				</if>
				ORDER BY order_num DESC
			</script>
        """)
	List<Agent> findByIds(@Param("ids") List<Long> ids,@Param("status") String status);

	/**
	 * 查询所有智能体
	 */
	@Select("""
			SELECT * FROM tbl_data_agent ORDER BY order_num ASC, create_time DESC
			""")
	List<Agent> findAll();

	/**
	 * 根据ID查询智能体
	 */
	@Select("""
			SELECT * FROM tbl_data_agent WHERE id = #{id}
			""")
	Agent findById(Long id);

	/**
	 * 根据状态查询智能体列表
	 */
	@Select("""
			SELECT * FROM tbl_data_agent WHERE status = #{status} ORDER BY order_num ASC, create_time DESC
			""")
	List<Agent> findByStatus(String status);

	/**
	 * 根据SN查询智能体
	 */
	@Select("""
			SELECT * FROM tbl_data_agent WHERE sn = #{sn}
			""")
	Agent findBySn(String sn);

	/**
	 * 根据关键词搜索智能体
	 */
	@Select("""
			SELECT * FROM tbl_data_agent
			WHERE (name LIKE CONCAT('%', #{keyword}, '%')
				   OR description LIKE CONCAT('%', #{keyword}, '%')
				   OR tags LIKE CONCAT('%', #{keyword}, '%'))
			ORDER BY order_num ASC, create_time DESC
			""")
	List<Agent> searchByKeyword(@Param("keyword") String keyword);

	/**
	 * 根据条件筛选智能体
	 */
	@Select("""
			<script>
				SELECT * FROM tbl_data_agent
				<where>
					<if test='status != null and status != ""'>
						AND status = #{status}
					</if>
					<if test='keyword != null and keyword != ""'>
						AND (name LIKE CONCAT('%', #{keyword}, '%')
							 OR description LIKE CONCAT('%', #{keyword}, '%')
							 OR tags LIKE CONCAT('%', #{keyword}, '%'))
					</if>
				</where>
				ORDER BY order_num ASC, create_time DESC
			</script>
			""")
	List<Agent> findByConditions(@Param("status") String status, @Param("keyword") String keyword);

//	@Insert("""
//			INSERT INTO tbl_data_agent (name, description, avatar, status, type, sn, api_key, api_key_enabled, prompt, category, admin_id, tags, create_time, update_time)
//			VALUES (#{name}, #{description}, #{avatar}, #{status}, #{type}, #{sn}, #{apiKey}, #{apiKeyEnabled}, #{prompt}, #{category}, #{adminId}, #{tags}, #{createTime}, #{updateTime})
//			""")
//	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
//	int insert(Agent agent);

	/**
	 * 根据ID更新智能体（仅更新非空字段）
	 */
	@Update("""
			<script>
			          UPDATE tbl_data_agent
			          <trim prefix="SET" suffixOverrides=",">
			            <if test='name != null'>name = #{name},</if>
			            <if test='description != null'>description = #{description},</if>
			            <if test='avatar != null'>avatar = #{avatar},</if>
			            <if test='status != null'>status = #{status},</if>
			            <if test='apiKey != null'>api_key = #{apiKey},</if>
			            <if test='apiKeyEnabled != null'>api_key_enabled = #{apiKeyEnabled},</if>
			            <if test='prompt != null'>prompt = #{prompt},</if>
			            <if test='category != null'>category = #{category},</if>
			            <if test='categoryId != null'>category_id = #{categoryId},</if>
			            <if test='adminId != null'>admin_id = #{adminId},</if>
			            <if test='type != null'>type = #{type},</if>
			            <if test='sn != null'>sn = #{sn},</if>
			            <if test='tags != null'>tags = #{tags},</if>
			            <if test='orderNum != null'>order_num = #{orderNum},</if>
			            update_time = NOW()
			          </trim>
			          WHERE id = #{id}
			</script>
			""")
	int updateById(Agent agent);

	/**
	 * 更新智能体API密钥
	 */
	@Update("""
			UPDATE tbl_data_agent
			SET api_key = #{apiKey}, api_key_enabled = #{apiKeyEnabled}, update_time = NOW()
			WHERE id = #{id}
			""")
	int updateApiKey(@Param("id") Long id, @Param("apiKey") String apiKey,
			@Param("apiKeyEnabled") Integer apiKeyEnabled);

	/**
	 * 切换API密钥启用状态
	 */
	@Update("""
			UPDATE tbl_data_agent
			SET api_key_enabled = #{enabled}, update_time = NOW()
			WHERE id = #{id}
			""")
	int toggleApiKey(@Param("id") Long id, @Param("enabled") Integer enabled);

//	@Delete("""
//			DELETE FROM tbl_data_agent WHERE id = #{id}
//			""")
//	int deleteById(Long id);

}
