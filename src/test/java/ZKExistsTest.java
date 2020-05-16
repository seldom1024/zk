import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Seldom
 * @time: 2020/5/14 20:45
 */
public class ZKExistsTest {

    private final String IP = "192.168.216.149:2181";

    ZooKeeper zooKeeper = null;

    /**
     * 获取连接
     */
    @Before
    public void before() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(IP, 5000, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                System.out.println("连接创建成功");
                countDownLatch.countDown();
            }
        });
        // 主线程阻塞等待连接对象的创建成功
        countDownLatch.await();
    }

    /**
     * 同步判断节点是否存在
     */
    @Test
    public void exists1() throws KeeperException, InterruptedException {
        /**
         * 第一个参数：节点的路径
         * 第二个参数：是否启用监控 这里为false即可
         */
        Stat stat = zooKeeper.exists("/hello", false);
        System.out.println(stat);
    }

    /**
     * 异步判断节点是否存在
     */
    @Test
    public void exists2() throws InterruptedException {
        /**
         * 第一个参数：节点的路径
         * 第二个参数：是否启用监控 这里为false即可
         * 第三个参数：异步回调接口
         * 第四个参数：上下文参数
         */
        zooKeeper.exists("/hello", false, new AsyncCallback.StatCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, Stat stat) {
                // 0代表读取成功
                System.out.println(rc);
                // 节点的路径
                System.out.println(path);
                // 上下文参数
                System.out.println(ctx);
                // 节点的详细信息，如果为null则说明该节点不存在
                System.out.println(stat);
            }
        }, "我是上下文参数");
        TimeUnit.SECONDS.sleep(1);
    }


    /**
     * 关闭连接
     */
    @After
    public void after() throws InterruptedException {
        if (zooKeeper != null) {
            System.out.println("关闭成功");
            zooKeeper.close();
        }
    }
}
