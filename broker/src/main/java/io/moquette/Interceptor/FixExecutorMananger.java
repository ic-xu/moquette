package io.moquette.Interceptor;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.*;

public class FixExecutorMananger {

    private static volatile FixExecutorMananger fixExecutorMananger;
    private ExecutorService executorService;

    private FixExecutorMananger() {
        executorService = new ThreadPoolExecutor(2, 10,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            new DefaultThreadFactory("process Thread"),
            (r, executor) -> r.run());
    }

    public static FixExecutorMananger getInstance() {
        if (null == fixExecutorMananger) {
            synchronized (FixExecutorMananger.class) {
                if (null == fixExecutorMananger) {
                    fixExecutorMananger = new FixExecutorMananger();
                }
            }
        }
        return fixExecutorMananger;
    }

    public void addTask(Runnable task) {
        executorService.execute(task);
    }

}
