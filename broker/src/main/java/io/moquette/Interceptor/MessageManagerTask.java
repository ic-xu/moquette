package io.moquette.Interceptor;

import java.util.List;

public class MessageManager implements Runnable {

    List<Interceptor> customInterceptor;

    @Override
    public void run() {
        InterceptorChain interceptorChain = new InterceptorChain();
        interceptorChain.addInterceptor(new ExpresstionInterceptor());
        if (null != customInterceptor)
            interceptorChain.addAllInterceptor(customInterceptor);
        interceptorChain.addInterceptor(new LastMessageInterceotor());
    }
}
