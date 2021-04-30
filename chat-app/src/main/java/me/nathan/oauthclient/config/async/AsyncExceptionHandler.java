package me.nathan.oauthclient.config.async;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        System.out.println("=======Thread Error======");
        System.out.println("Exception Message :: " + ex.getMessage());
        System.out.println("Method Name :: " + method.getName());
        for (Object param : params) {
            System.out.println("Parameter Value :: " + param);
        }


        System.out.println("======Thread Error End========");
    }
}
