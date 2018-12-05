package org.smart4j.sample.controller;

import java.util.List;
import java.util.Map;

import org.smart4j.framework.annotation.Action;
import org.smart4j.framework.annotation.Controller;
import org.smart4j.framework.annotation.Inject;
import org.smart4j.framework.bean.Data;
import org.smart4j.framework.bean.Param;
import org.smart4j.framework.bean.View;
import org.smart4j.sample.model.Customer;
import org.smart4j.sample.service.CustomerService;

@Controller
public class CustomerController {

  @Inject
  private CustomerService customerService;

  @Action("get:/customer")
  public View index() {
    List<Customer> customerList = customerService.getCustomerList();
    return new View("customer.jsp").addModel("customerList", customerList);
  }

  @Action("get:/customer_create")
  public View create(Param param) {
    return new View("customer_create.jsp");
  }

  @Action("post:/customer_create")
  public Data createSubmit(Param param) {
    Map<String, Object> fieldMap = param.getParamMap();
    boolean result = customerService.createCustomer(fieldMap);
    return new Data(result);
  }
}
