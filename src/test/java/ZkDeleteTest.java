import org.apache.zookeeper.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Seldom
 * @time: 2020/5/14 20:43
 */
public class ZkDeleteTest {

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
     * 同步更新数据
     */
    @Test
    public void delete1() throws KeeperException, InterruptedException {
        /**
         * 第一个参数：删除节点的路径
         * 第二个参数：数据版本信息，-1代表不考律版本信息
         */
        zooKeeper.delete("/delete/node1", -1);
    }

    /**
     * 异步更新数据
     */
    @Test
    public void delete2() throws InterruptedException {
        /**
         * 第一个参数：删除节点的路径
         * 第二个参数：数据版本信息，-1代表不考律版本信息
         * 第三个参数：异步回调接口
         * 第四个参数：上下文参数
         */
        zooKeeper.delete("/delete/node2", -1, (rc, path, ctx) -> {
            // 0代表删除成功
            System.out.println(rc);
            // 删除节点的路径
            System.out.println(path);
            // 上下文参数
            System.out.println(ctx);
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
