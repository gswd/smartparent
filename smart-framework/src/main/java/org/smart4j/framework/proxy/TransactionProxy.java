package org.smart4j.framework.proxy;

import java.lang.reflect.Method;

import org.smart4j.framework.annotation.Transaction;
import org.smart4j.framework.helper.DatabaseHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionProxy implements Proxy {

  private static final ThreadLocal<Boolean> FLAG_HOLDER = ThreadLocal.withInitial(() -> false);

  @Override
  public Object doProxy(ProxyChain proxyChain) throws Throwable{
    Object result;
    boolean flag = FLAG_HOLDER.get();
    Method method = proxyChain.getTargetMethod();
    if (!flag && method.isAnnotationPresent(Transaction.class)) {
      FLAG_HOLDER.set(true);
      try {
        DatabaseHelper.beginTransaction();
        log.debug("begin transaction");
        result = proxyChain.doProxyChain();
        DatabaseHelper.commitTransaction();
        log.debug("commit transaction");
      } catch (Exception e) {
        DatabaseHelper.rollbackTransaction();
        log.debug("rollback transaction");
        throw e;
      } finally {
        FLAG_HOLDER.remove();
      }
    } else {
      result = proxyChain.doProxyChain();
    }
    return result;
  }
}
