package io.moquette.Interceptor;

import java.lang.reflect.Method;

public interface Process {

    //真实对象前调用返回true，反射真实对象的方法；返回false时则调用around方法
     boolean before(Object proxy, Object target, Method method, Object[] args);

     void around(Object proxy, Object target, Method method, Object[] args);

     void after(Object proxy, Object target, Method method, Object[] args);
}
