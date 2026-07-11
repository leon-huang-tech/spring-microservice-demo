package com.demo;

public class LogUtils {

  public static String getLogPrefix(Class<?> clazz) {
    String methodName = StackWalker.getInstance().walk(frames -> frames.map(StackWalker.StackFrame::getMethodName)
        .filter(name -> !name.equals("getLogPrefix") && !name.equals("getMethodName")).findFirst().orElse("unknown"));
    return "---------------- " + clazz.getName() + " - " + methodName + "---------------- ";
  }

  public static String getLogPrefix2(Class<?> clazz) {
    String methodName = new Throwable().getStackTrace()[1].getMethodName();
    return "---------------- " + clazz.getName() + " - " + methodName + "---------------- ";
  }
}