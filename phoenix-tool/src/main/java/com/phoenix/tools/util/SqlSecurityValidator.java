package com.phoenix.tools.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SqlSecurityValidator {

    // 1. 定义绝对禁止的破坏性关键字
    private static final List<String> DANGEROUS_KEYWORDS = Arrays.asList(
            "DROP", "DELETE", "TRUNCATE", "ALTER",
            "CREATE", "UPDATE", "INSERT", "GRANT", "REVOKE"
    );

    // 2. 定义危险的正则模式（防范注释注入、嵌套查询等）
    private static final List<String> DANGEROUS_PATTERNS = Arrays.asList(
            "(--|#|/\\*|\\*/)",                         // 匹配 SQL 注释
            "(\\bUNION\\b\\s*\\bSELECT\\b)",             // 匹配 UNION 注入
            "(\\bOR\\b\\s+1\\s*=\\s*1\\b)",              // 匹配永真条件
            "(\\bAND\\b\\s+1\\s*=\\s*1\\b)"             // 匹配永真条件
    );

    /**
     * 校验 SQL 语句是否安全
     * @param sql 待校验的 SQL 语句
     * @return 校验结果
     */
    public static ValidationResult validate(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return new ValidationResult(false, "SQL 语句不能为空");
        }

        String upperSql = sql.toUpperCase().trim();

        // 3. 核心防线：强制只读（仅允许以 SELECT 或 WITH 开头）
        if (!upperSql.startsWith("SELECT") && !upperSql.startsWith("WITH")) {
            return new ValidationResult(false, "安全拦截：仅允许执行 SELECT 查询");
        }

        // 4. 关键字黑名单拦截
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                return new ValidationResult(false, "安全拦截：检测到危险关键字 [" + keyword + "]");
            }
        }

        // 5. 正则危险模式拦截
        for (String patternStr : DANGEROUS_PATTERNS) {
            if (Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE).matcher(sql).find()) {
                return new ValidationResult(false, "安全拦截：检测到危险注入模式");
            }
        }

        return new ValidationResult(true, "校验通过");
    }

    /**
     * 内部结果类，方便获取校验状态和错误信息
     */
    public static class ValidationResult {
        private final boolean safe;
        private final String message;

        public ValidationResult(boolean safe, String message) {
            this.safe = safe;
            this.message = message;
        }

        public boolean isSafe() { return safe; }
        public String getMessage() { return message; }
    }
}