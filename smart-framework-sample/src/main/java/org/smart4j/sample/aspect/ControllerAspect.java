package org.smart4j.sample.aspect;

import java.lang.reflect.Method;

import org.smart4j.framework.annotation.Aspect;
import org.smart4j.framework.annotation.Controller;
import org.smart4j.framework.proxy.AspectProxy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect(Controller.class)
public class ControllerAspect extends AspectProxy {
  private long begin;

  @Override
  public void before(Class<?> cls, Method method, Object[] params) {
    log.debug("------------------ begin -----------------");
    log.debug("class : {}", cls.getName());
    log.debug("method : {}", method.getName());
    begin = System.currentTimeMillis();
  }

  @Override
  public void after(Class<?> cls, Method method, Object[] params) {
    log.debug("time : {}ms", System.currentTimeMillis() - begin);

    log.debug("------------------ end -----------------");

  }
}
