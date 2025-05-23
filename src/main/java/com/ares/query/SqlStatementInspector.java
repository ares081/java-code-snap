package com.ares.query;

import java.util.regex.Pattern;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置： spring.jps.properties.hibernate.session_factory.statement_inspector:
 * com.ares.domain.component.SqlStatementInspector
 */
public class SqlStatementInspector implements StatementInspector {

  private final Logger logger = LoggerFactory.getLogger("sql");
  private static final Pattern IGNORE_PATTERN = Pattern.compile(
      ".*(from|FROM)\\s+(hibernate_sequence|schema_version|flyway_schema_history).*",
      Pattern.CASE_INSENSITIVE);

  @Override
  public String inspect(String sql) {
    logger.info("sql:{}", sql);
    if (sql == null || sql.trim().isEmpty() || IGNORE_PATTERN.matcher(sql).matches()) {
      return sql;
    }

    String sqlUpperCase = sql.toUpperCase().trim();
    try {
      // 禁止TRUNCATE、DROP、ALTER、CREATE操作
      if (sqlUpperCase.startsWith("TRUNCATE") ||
          sqlUpperCase.startsWith("DROP") ||
          sqlUpperCase.startsWith("ALTER") ||
          sqlUpperCase.startsWith("CREATE")) {
        throw new SqlCheckException("禁止执行TRUNCATE、DROP、ALTER、CREATE操作: " + sql);
      }

      // UPDATE必须带WHERE条件
      if (sqlUpperCase.startsWith("UPDATE") && !sqlUpperCase.contains(" WHERE ")) {
        throw new SqlCheckException("UPDATE操作必须包含WHERE条件: " + sql);
      }

      // DELETE必须带WHERE条件
      if (sqlUpperCase.startsWith("DELETE") && !sqlUpperCase.contains(" WHERE ")) {
        throw new SqlCheckException("DELETE操作必须包含WHERE条件: " + sql);
      }

      // SELECT必须带LIMIT
      if (sqlUpperCase.startsWith("SELECT") &&
          !sqlUpperCase.contains(" LIMIT ") &&
          !isCountOrExistsQuery(sqlUpperCase) &&
          !isDistinctQuery(sqlUpperCase)) {
        throw new SqlCheckException("SELECT操作必须包含LIMIT条件: " + sql);
      }

      // INSERT必须带VALUES
      if (sqlUpperCase.startsWith("INSERT") && !sqlUpperCase.contains("VALUES")) {
        throw new SqlCheckException("INSERT操作必须包含VALUES: " + sql);
      }

      logger.debug("SQL验证通过: {}", sql);
      return sql;
    } catch (Exception e) {
      logger.error("SQL验证失败: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * 判断是否为COUNT查询或EXISTS查询，这类查询可以不需要LIMIT
   */
  private boolean isCountOrExistsQuery(String sql) {
    return sql.contains("COUNT(") || sql.contains("EXISTS(") || sql.contains("SELECT 1");
  }

  private boolean isDistinctQuery(String sql) {
    return sql.contains("DISTINCT");
  }
}
