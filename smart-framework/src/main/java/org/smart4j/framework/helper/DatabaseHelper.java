package org.smart4j.framework.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.util.PropsUtil;

public class DatabaseHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);


  private static final QueryRunner QUERY_RUNNER = new QueryRunner();

  private static final ThreadLocal<Connection> CONNECTION_HOLDER = new ThreadLocal<>();

  private static final BasicDataSource DATA_SOURCE;

  static {
    Properties conf = PropsUtil.loadProps("smart.properties");
    Objects.requireNonNull(conf);
    String driver = conf.getProperty("smart.framework.jdbc.driver");
    String url = conf.getProperty("smart.framework.jdbc.url");
    String username = conf.getProperty("smart.framework.jdbc.username");
    String password = conf.getProperty("smart.framework.jdbc.password");

    DATA_SOURCE = new BasicDataSource();

    DATA_SOURCE.setDriverClassName(driver);
    DATA_SOURCE.setUrl(url);
    DATA_SOURCE.setUsername(username);
    DATA_SOURCE.setPassword(password);
  }

  public static Connection getConnection() {
    Connection conn = CONNECTION_HOLDER.get();
    if (Objects.nonNull(conn)) {
      return conn;
    }

    try {
      conn = DATA_SOURCE.getConnection();
      return conn;

    } catch (SQLException e) {
      LOGGER.error("get connection failure", e);
      throw new RuntimeException(e);

    } finally {
      CONNECTION_HOLDER.set(conn);
    }
  }

  public static void closeConnection() {
    Connection conn = CONNECTION_HOLDER.get();
    if (Objects.nonNull(conn)) {
      try{
        conn.close();
      } catch (SQLException e) {
        LOGGER.error("close connection failure.", e);
        throw new RuntimeException(e);
      } finally {
        CONNECTION_HOLDER.remove();
      }
    }
  }

  public static void beginTransaction() {
    Connection conn = getConnection();
    if (Objects.nonNull(conn)) {
      try {
        conn.setAutoCommit(false);
      } catch (SQLException e) {
        LOGGER.error("begin transaction failure", e);
        throw new RuntimeException(e);
      } finally {
        CONNECTION_HOLDER.set(conn);
      }
    }
  }

  public static void commitTransaction() {
    Connection conn = getConnection();
    if (Objects.nonNull(conn)) {
      try {
        conn.commit();
      } catch (SQLException e) {
        LOGGER.error("commit transaction failure", e);
        throw new RuntimeException(e);
      } finally {
        closeConnection();
      }
    }
  }
  public static void rollbackTransaction() {
    Connection conn = getConnection();
    if (Objects.nonNull(conn)) {
      try {
        conn.rollback();
      } catch (SQLException e) {
        LOGGER.error("rollback transaction failure", e);
        throw new RuntimeException(e);
      } finally {
        closeConnection();
      }
    }
  }
  public static <T> List<T> queryEntityList(Class<T> entityClass, String sql, Object ... params) {

    try {
      Connection conn = getConnection();
      return QUERY_RUNNER.query(conn, sql, new BeanListHandler<>(entityClass), params);
    } catch (SQLException e) {
      LOGGER.error("query entity list failure.", e);
      throw new RuntimeException(e);
    } finally {
      closeConnection();
    }

  }

  public static <T> T queryEntity(Class<T> entityClass, String sql, Object ... params) {

    try {
      Connection conn = getConnection();
      return QUERY_RUNNER.query(conn, sql, new BeanHandler<>(entityClass), params);
    } catch (SQLException e) {
      LOGGER.error("query entity failure.", e);
      throw new RuntimeException(e);
    } finally {
      closeConnection();
    }
  }

  public static List<Map<String, Object>> executeQuery(String sql, Object... params) {
    try {
      Connection conn = getConnection();
      return QUERY_RUNNER.query(conn, sql, new MapListHandler(), params);
    } catch (SQLException e) {
      LOGGER.error("execute query failure.", e);
      throw new RuntimeException(e);
    } finally {
      closeConnection();
    }
  }

  public static int executeUpdate(String sql, Object... params) {
    try {
      Connection conn = getConnection();
      return QUERY_RUNNER.update(conn, sql, params);
    } catch (SQLException e) {
      LOGGER.error("execute update failure.", e);
      throw new RuntimeException(e);
    } finally {
      closeConnection();
    }
  }

  public static <T> boolean insertEntity(Class<T> entityClass, Map<String, Object> fieldMap) {
    if (MapUtils.isEmpty(fieldMap)) {
      LOGGER.error("can not insert entity: fieldMap is empty.");
      return false;
    }

    String sql = "INSERT INTO " + getTableName(entityClass);

    StringBuilder columns = new StringBuilder("(");
    StringBuilder values = new StringBuilder("(");

    for (String fieldName : fieldMap.keySet()) {
      columns.append(fieldName).append(",");
      values.append("?, ");
    }
    columns.replace(columns.lastIndexOf(","), columns.length(), ")");
    values.replace(values.lastIndexOf(","), columns.length(), ")");
    sql += columns + " VALUES " + values;

    Object[] params = fieldMap.values().toArray();

    return executeUpdate(sql, params) == 1;
  }

  public static <T> boolean updateEntity(Class<T> entityClass, long id, Map<String, Object> fieldMap) {
    if (MapUtils.isEmpty(fieldMap)) {
      LOGGER.error("can not update entity : fieldMap is empty");
      return false;
    }

    String sql = "UPDATE " + getTableName(entityClass) + " SET ";

    StringBuilder columns = new StringBuilder();

    for (String fieldName : fieldMap.keySet()) {
      columns.append(fieldName).append("=?, ");
    }

    sql += columns.substring(0, columns.lastIndexOf(", ")) + " WHERE id = ?";

    List<Object> paramList = new ArrayList<>(fieldMap.values());
    paramList.add(id);

    Object[] params = paramList.toArray();

    return executeUpdate(sql, params) == 1;
  }

  public static <T> boolean deleteEntity(Class<T> entityClass, long id) {
    String sql = "DELETE FROM " + getTableName(entityClass) + " WHERE id = ?";

    return executeUpdate(sql, id) == 1;
  }

  public static void executeSqlFile(String filePath) {

    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);

    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    try {
      String sql;
      while ((sql = reader.readLine()) != null) {
        DatabaseHelper.executeUpdate(sql);
      }
    } catch (IOException e) {
      LOGGER.error("execute sql file failure", e);
      throw new RuntimeException(e);
    }
  }

  private static String getTableName(Class<?> entityClass) {
    return entityClass.getSimpleName();
  }

}
