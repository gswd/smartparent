package org.smart4j.framework;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.smart4j.framework.bean.Data;
import org.smart4j.framework.bean.Handler;
import org.smart4j.framework.bean.Param;
import org.smart4j.framework.bean.View;
import org.smart4j.framework.helper.BeanHelper;
import org.smart4j.framework.helper.ConfigHelper;
import org.smart4j.framework.helper.ControllerHelper;
import org.smart4j.framework.util.CodecUtil;
import org.smart4j.framework.util.JsonUtil;
import org.smart4j.framework.util.ReflectionUtil;
import org.smart4j.framework.util.StreamUtil;

@WebServlet(urlPatterns = "/*", loadOnStartup = 0)
public class DispatcherServlet extends HttpServlet {
  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    HelperLoader.init();

    ServletContext servletContext = servletConfig.getServletContext();

    ServletRegistration jspServlet = servletContext.getServletRegistration("jsp");
    jspServlet.addMapping(ConfigHelper.getAppJspPath() + "*");

    ServletRegistration defaultServlet = servletContext.getServletRegistration("default");
    defaultServlet.addMapping(ConfigHelper.getAppAssetPath() + "*");
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestMethod = req.getMethod().toLowerCase();
    String requestPath = req.getPathInfo();
    Handler handler = ControllerHelper.getHandler(requestMethod, requestPath);

    if (Objects.nonNull(handler)) {

      Map<String, Object> paramMap = new HashMap<>();
      Enumeration<String> paramNames = req.getParameterNames();
      while (paramNames.hasMoreElements()) {
        String paramName = paramNames.nextElement();
        String paramValue = req.getParameter(paramName);
        paramMap.put(paramName, paramValue);
      }

      InputStream is = req.getInputStream();
      String body = CodecUtil.decodeURL(StreamUtil.getString(is));
      if (StringUtils.isNotBlank(body)) {
        String[] params = StringUtils.split(body, "&");
        if (ArrayUtils.isNotEmpty(params)) {
          for (String param : params) {
            String[] array = StringUtils.split(param, "=");
            String paramName = array[0];
            String paramValue = array[1];

            paramMap.put(paramName, paramValue);
          }
        }
      }

      Param param = new Param(paramMap);

      Object controllerBean = BeanHelper.getBean(handler.getControllerClass());

      Object result = ReflectionUtil.invokeMethod(controllerBean, handler.getActionMethod(), param);

      if (result instanceof View) {
        View view = (View)result;
        String path = view.getPath();
        if (StringUtils.isBlank(path)) {
          return;
        }

        if (StringUtils.startsWith(path, "/")) {
          resp.sendRedirect(req.getContextPath() + path);
        } else {
          Map<String, Object> model = view.getModel();
          model.forEach(req::setAttribute);
          req.getRequestDispatcher(ConfigHelper.getAppJspPath() + path).forward(req, resp);
        }

      } else if (result instanceof Data) {
        Data data = (Data)result;
        Object model = data.getModel();

        if (Objects.nonNull(model)) {
          resp.setContentType("application/json");
          resp.setCharacterEncoding("UTF-8");
          String json = JsonUtil.toJson(model);
          try (PrintWriter writer = resp.getWriter()) {
            writer.write(json);
            writer.flush();
          }

        }
      }
    }
  }
}
