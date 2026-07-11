package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.entity.Datasource;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * Data Source Mapper Interface
 *
 * @author Alibaba Cloud AI
 */
@Mapper
public interface DatasourceMapper extends BaseMapper<Datasource> {

	/**
	 * 根据ID查询数据源
	 */
	@Select("SELECT * FROM tbl_data_datasource WHERE id = #{id}")
	Datasource selectById(@Param("id") Integer id);

	/**
	 * 根据ID更新数据源，仅更新非空字段
	 */
	@Update("""
			<script>
			UPDATE tbl_data_datasource
			<set>
			    <if test="name != null">name = #{name},</if>
			    <if test="type != null">type = #{type},</if>
			    <if test="host != null">host = #{host},</if>
			    <if test="port != null">port = #{port},</if>
			    <if test="databaseName != null">database_name = #{databaseName},</if>
			    <if test="username != null">username = #{username},</if>
			    <if test="password != null">password = #{password},</if>
			    <if test="connectionUrl != null">connection_url = #{connectionUrl},</if>
			    <if test="status != null">status = #{status},</if>
			    <if test="testStatus != null">test_status = #{testStatus},</if>
			    <if test="description != null">description = #{description},</if>
			    <if test="creatorId != null">creator_id = #{creatorId},</if>
			    update_time = NOW()
			</set>
			WHERE id = #{id}
			</script>
			""")
	int updateById(Datasource datasource);

	/**
	 * 更新数据源测试状态
	 */
	@Update("UPDATE tbl_data_datasource SET test_status = #{testStatus} WHERE id = #{id}")
	int updateTestStatusById(@Param("id") Integer id, @Param("testStatus") String testStatus);

	/**
	 * 根据状态查询数据源列表
	 */
	@Select("SELECT * FROM tbl_data_datasource WHERE status = #{status} ORDER BY create_time DESC")
	List<Datasource> selectByStatus(@Param("status") String status);

	/**
	 * 根据类型查询数据源列表
	 */
	@Select("SELECT * FROM tbl_data_datasource WHERE type = #{type} ORDER BY create_time DESC")
	List<Datasource> selectByType(@Param("type") String type);

	/**
	 * 获取按状态统计的数据源数量
	 */
	@Select("SELECT status, COUNT(*) as count FROM tbl_data_datasource GROUP BY status")
	List<Map<String, Object>> selectStatusStats();

	/**
	 * 获取按类型统计的数据源数量
	 */
	@Select("SELECT type, COUNT(*) as count FROM tbl_data_datasource GROUP BY type")
	List<Map<String, Object>> selectTypeStats();

	/**
	 * 获取按测试状态统计的数据源数量
	 */
	@Select("SELECT test_status, COUNT(*) as count FROM tbl_data_datasource GROUP BY test_status")
	List<Map<String, Object>> selectTestStatusStats();

	/**
	 * 统计数据源总数
	 */
	@Select("SELECT COUNT(*) FROM tbl_data_datasource")
	Long selectCount();

}
