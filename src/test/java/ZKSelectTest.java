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
 * @time: 2020/5/14 20:48
 */
public class ZKSelectTest {

    private final String IP = "192.168.216.149:2181";

    ZooKeeper zooKeeper = null;

    /**
     * 获取连接
     */
    @Before
    public void before() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
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
     * 同步查询节点
     */
    @Test
    public void get1() throws KeeperException, InterruptedException {
        /**
         * 第一个参数：节点的路径
         * 第二个参数：后面讲解 这里为false即可
         * 第三个参数：读取节点属性的对象
         */
        byte[] data = zooKeeper.getData("/delete", false, new Stat());
        System.out.println(new String(data));
    }

    /**
     * 异步查询节点
     */
    @Test
    public void get2() throws KeeperException, InterruptedException {
        /**
         * 第一个参数：节点的路径
         * 第二个参数：后面讲解 这里为false即可
         * 第三个参数：异步回调接口
         * 第四个参数：上下文参数
         */
        zooKeeper.getData("/delete", false, (rc, path, ctx, data, stat) -> {
            // 0代表读取成功
            System.out.println(rc);
            // 节点的路径
            System.out.println(path);
            // 上下文参数
            System.out.println(ctx);
            // 节点的详细信息
            System.out.println(stat);
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
