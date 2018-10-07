package org.smart4j.sample.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.annotation.Service;
import org.smart4j.sample.helper.DatabaseHelper;
import org.smart4j.sample.model.Customer;

@Service
public class CustomerService {

  private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

  /**
   * 获取客户列表
   */
  public List<Customer> getCustomerList() {

    String sql = "select * from customer";
    return DatabaseHelper.queryEntityList(Customer.class, sql);
  }

  /**
   * 获取客户
   */
  public Customer getCustomer(long id) {
    String sql = "select * from customer where id = ?";
    return  DatabaseHelper.queryEntity(Customer.class, sql, id);
  }

  /**
   * 创建客户
   */
  public boolean createCustomer(Map<String, Object> fieldMap) {
    return DatabaseHelper.insertEntity(Customer.class, fieldMap);
  }

  /**
   * 更新客户
   */
  public boolean updateCustomer(long id, Map<String, Object> fieldMap) {
    return DatabaseHelper.updateEntity(Customer.class, id, fieldMap);
  }

  /**
   * 删除客户
   */
  public boolean deleteCustomer(long id) {
    return DatabaseHelper.deleteEntity(Customer.class, id);

  }


}
