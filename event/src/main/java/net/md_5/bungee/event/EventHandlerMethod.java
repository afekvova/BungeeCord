package net.md_5.bungee.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiredArgsConstructor()
public class EventHandlerMethod {

    @Getter
    private final Object listener;
    @Getter
    private final Method method;


    public void invoke(Object event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        method.invoke(listener, event);
    }
}
