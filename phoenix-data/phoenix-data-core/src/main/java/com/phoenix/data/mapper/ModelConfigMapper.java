package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.entity.ModelConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 模型配置 Mapper 接口
 */
@Mapper
public interface ModelConfigMapper extends BaseMapper<ModelConfig> {

	/**
	 * 查询所有未删除的模型配置
	 */
	@Select("""
			SELECT id, provider, base_url, api_key, model_name, temperature, is_active, max_tokens,
			       model_type, completions_path, embeddings_path, created_time, updated_time, is_deleted,
			       proxy_enabled, proxy_host, proxy_port, proxy_username, proxy_password
			FROM tbl_data_model_config WHERE is_deleted = 0 ORDER BY created_time DESC
			""")
	List<ModelConfig> findAll();

	/**
	 * 根据ID查询模型配置
	 */
	@Select("""
			SELECT id, provider, base_url, api_key, model_name, temperature, is_active, max_tokens,
			       model_type, completions_path, embeddings_path, created_time, updated_time, is_deleted,
			       proxy_enabled, proxy_host, proxy_port, proxy_username, proxy_password
			FROM tbl_data_model_config WHERE id = #{id} AND is_deleted = 0
			""")
	ModelConfig findById(Integer id);

	/**
	 * 查询指定类型的已启用模型配置
	 */
	@Select("""
			SELECT id, provider, base_url, api_key, model_name, temperature, is_active, max_tokens,
			       model_type, completions_path, embeddings_path, created_time, updated_time, is_deleted,
			       proxy_enabled, proxy_host, proxy_port, proxy_username, proxy_password
			FROM tbl_data_model_config WHERE model_type = #{modelType} AND is_active = true AND is_deleted = 0 LIMIT 1
			""")
	ModelConfig selectActiveByType(@Param("modelType") String modelType);

	/**
	 * 将指定类型的其他模型配置设为非启用状态
	 */
	@Update("UPDATE tbl_data_model_config SET is_active = true WHERE model_type = #{modelType} AND id != #{currentId} AND is_deleted = 0")
	void deactivateOthers(@Param("modelType") String modelType, @Param("currentId") Integer currentId);

	/**
	 * 根据条件筛选模型配置
	 */
	@Select("""
			<script>
			   SELECT id, provider, base_url, api_key, model_name, temperature, is_active, max_tokens,
			          model_type, completions_path, embeddings_path, created_time, updated_time, is_deleted,
			          proxy_enabled, proxy_host, proxy_port, proxy_username, proxy_password
			   FROM tbl_data_model_config
			   <where>
			      is_deleted = 0
			      <if test='provider != null and provider != ""'>
			         AND provider = #{provider}
			      </if>
			      <if test='keyword != null and keyword != ""'>
			         AND (provider LIKE CONCAT('%', #{keyword}, '%')
			             OR base_url LIKE CONCAT('%', #{keyword}, '%')
			             OR model_name LIKE CONCAT('%', #{keyword}, '%'))
			      </if>
			      <if test='isActive != null'>
			         AND is_active = #{isActive}
			      </if>
			      <if test='maxTokens != null'>
			         AND max_tokens = #{maxTokens}
			      </if>
			      <if test='modelType != null'>
			         AND model_type = #{modelType}
			      </if>
			   </where>
			   ORDER BY created_time DESC
			</script>
			""")
	List<ModelConfig> findByConditions(@Param("provider") String provider, @Param("keyword") String keyword,
			@Param("isActive") Boolean isActive, @Param("maxTokens") Integer maxTokens,
			@Param("modelType") String modelType);

	/**
	 * 根据ID更新模型配置（仅更新非空字段）
	 */
	@Update("""
			<script>
			          UPDATE tbl_data_model_config
			          <trim prefix="SET" suffixOverrides=",">
			            <if test='provider != null'>provider = #{provider},</if>
			            <if test='baseUrl != null'>base_url = #{baseUrl},</if>
			            <if test='apiKey != null'>api_key = #{apiKey},</if>
			            <if test='modelName != null'>model_name = #{modelName},</if>
			            <if test='temperature != null'>temperature = #{temperature},</if>
			            <if test='isActive != null'>is_active = #{isActive},</if>
			            <if test='maxTokens != null'>max_tokens = #{maxTokens},</if>
			            <if test='modelType != null'>model_type = #{modelType},</if>
			            <if test='completionsPath != null'>completions_path = #{completionsPath},</if>
			            <if test='embeddingsPath != null'>embeddings_path = #{embeddingsPath},</if>
			            <if test='isDeleted != null'>is_deleted = #{isDeleted},</if>
			            <if test='proxyEnabled != null'>proxy_enabled = #{proxyEnabled},</if>
			            <if test='proxyHost != null'>proxy_host = #{proxyHost},</if>
			            <if test='proxyPort != null'>proxy_port = #{proxyPort},</if>
			            <if test='proxyUsername != null'>proxy_username = #{proxyUsername},</if>
			            <if test='proxyPassword != null'>proxy_password = #{proxyPassword},</if>
			            updated_time = NOW()
			          </trim>
			          WHERE id = #{id}
			</script>
			""")
	int updateById(ModelConfig modelConfig);

	/**
	 * 逻辑删除模型配置
	 */
	@Update("""
			UPDATE tbl_data_model_config SET is_deleted = 1 WHERE id = #{id}
			""")
	int logicalDeleteById(Integer id);

}
