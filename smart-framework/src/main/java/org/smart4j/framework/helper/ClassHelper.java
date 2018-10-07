package org.smart4j.framework.helper;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.smart4j.framework.annotation.Controller;
import org.smart4j.framework.annotation.Service;
import org.smart4j.framework.util.ClassUtil;

public final class ClassHelper {
  private static final Set<Class<?>> CLASS_SET;

  static {
    String basePackage = ConfigHelper.getAppBasePackage();
    CLASS_SET = ClassUtil.getClassSet(basePackage);
  }

  public static Set<Class<?>> getClassSet() {
    return CLASS_SET;
  }

  public static Set<Class<?>> getServiceClassSet() {
    return CLASS_SET.stream().filter(c -> c.isAnnotationPresent(Service.class)).collect(Collectors.toSet());
  }

  public static Set<Class<?>> getControllerClassSet() {
    return CLASS_SET.stream().filter(c -> c.isAnnotationPresent(Controller.class)).collect(Collectors.toSet());
  }

  public static Set<Class<?>> getBeanClassSet() {
    return Stream.concat(getServiceClassSet().stream(), getControllerClassSet().stream()).collect(Collectors.toSet());
  }
}
