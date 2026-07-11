package com.phoenix.agent.react.bpm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.phoenix.tools.util.SqlSecurityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BpmToolSearch {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 搜索业务文档
     * 根据关键词和用户负责区域，在MongoDB向量数据库中查找最相关的文档
     *
     * @param sql 搜索关键词或问题描述
     * @return 格式化的相关文档列表（标题+摘要+相似度）
     */
    @Tool(name = "countDatas", description = """
            根据用户的中文问题，生成准确、高效的 PostgreSQL 查询语句，帮助用户分析流程实例和任务数据。
            生成的sql语句是返回多条数据的：如List<Map> datas = jdbcTemplate.queryForList(sql, Map.class);
            我的数据库是pg的，是flowable框架进行建表的，flowable表字段是大写的，处理flowable表字段查询大写的字段要加双引号
            生成的sql语句 数据最多10条 LIMIT 10
            """)
    public String countDatas(@ToolParam(description = "生成多条查询的sql语句") String sql) {
        sql = sql.trim();
        if(!SqlSecurityValidator.validate(sql).isSafe()) {
            return "生成的SQL包含不安全操作，已拦截。";
        }
        log.info("生成的sql为 {}", sql);
        List<Map<String, Object>> datas = jdbcTemplate.queryForList(sql);
        if (CollUtil.isEmpty(datas)) {
            return "未找到相关记录";
        }

        StringBuilder sb = new StringBuilder();
        for (Map document : datas) {
            document.forEach((s, o) -> sb.append(String.format("【%s】 %s ", s, o)));
            sb.append("\n---\n");
        }
        log.info("统计搜索返回{}条结果", datas.size());
        return "找到" + datas.size() + "条相关文档：\n" + sb.toString();
    }

    /**
     * 搜索业务文档
     * 根据关键词和用户负责区域，在MongoDB向量数据库中查找最相关的文档
     *
     * @param sql 搜索关键词或问题描述
     * @return 格式化的相关文档列表（标题+摘要+相似度）
     */
    @Tool(name = "countData", description = """
            根据用户的中文问题，生成准确、高效的 PostgreSQL 查询语句，帮助用户分析流程实例和任务数据。
            生成的sql语句是返回单条数据的：如Map<String, Object> document = jdbcTemplate.queryForMap(sql);
            我的数据库是pg的，是flowable框架进行建表的，flowable表字段是大写的，处理flowable表字段查询大写的字段要加双引号
            """)
    public String countData(@ToolParam(description = "生成单条查询的sql语句") String sql) {
        sql = sql.trim();
        if (sql.contains("delete") || sql.contains("update")) {
            return "生成的SQL包含不安全操作，已拦截。";
        }
        log.info("生成的sql为 {}", sql);
        Map<String, Object> document = jdbcTemplate.queryForMap(sql);
        if (MapUtil.isEmpty(document)) {
            return "未找到相关记录";
        }

        StringBuilder sb = new StringBuilder();
        document.forEach((s, o) -> sb.append(String.format("【%s】 %s ", s, o)));
        sb.append("\n---\n");
        return "为统计到数据\n" + sb.toString();
    }

    @Tool(name = "count", description = """
            根据用户的中文问题，生成准确、高效的 PostgreSQL 查询语句，帮助用户分析流程实例和任务数据。
            生成的sql语句是返回Long类型的总数的：如Long count = jdbcTemplate.queryForObject(sql, Long.class);
            我的数据库是pg的，是flowable框架进行建表的，flowable表字段是大写的，处理flowable表字段查询大写的字段要加双引号
            """)
    public String count(@ToolParam(description = "生成查询数据总数的sql语句") String sql) {
        sql = sql.trim();
        if (sql.contains("delete") || sql.contains("update")) {
            return "生成的SQL包含不安全操作，已拦截。";
        }
        log.info("生成的sql为 {}", sql);
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return "统计到数据总数:"+count+"\n";
    }
}
