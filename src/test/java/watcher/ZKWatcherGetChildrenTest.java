package watcher;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Seldom
 * @time: 2020/5/14 21:55
 */
public class ZKWatcherGetChildrenTest {

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
            System.out.println("path：" + event.getPath());
            System.out.println("evenType：" + event.getType());
            System.out.println("evenState：" + event.getState());
        });
        // 主线程阻塞等待连接对象的创建成功
        countDownLatch.await();
    }

    /**
     * 使用查询子节点信息的方法为节点注册watcher，使用连接对象的watcher
     */
    @Test
    public void watcherGetChildren1() throws KeeperException, InterruptedException {
        // 参数1：节点的路径
        // 参数2：使用连接对象的watcher
        zooKeeper.getChildren("/watcher3", true, null);
        TimeUnit.SECONDS.sleep(10);
    }

    /**
     * 使用查询子节点信息的方法为节点注册watcher，使用自定义的watcher
     */
    @Test
    public void watcherGetChildren2() throws KeeperException, InterruptedException {
        // 参数1：节点的路径
        // 参数2：使用自定义的watcher
        zooKeeper.getChildren("/watcher3", event -> {
            System.out.println("自定义Watcher");
            System.out.println("path：" + event.getPath());
            System.out.println("evenType：" + event.getType());
            System.out.println("evenState：" + event.getState());
        }, null);
        TimeUnit.SECONDS.sleep(10);
    }

    /**
     * 使用查询子节点信息的方法为节点注册watcher，并验证watcher是一次性的
     */
    @Test
    public void watcherGetChildren3() throws KeeperException, InterruptedException {
        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("自定义Watcher");
                System.out.println("path：" + event.getPath());
                System.out.println("evenType：" + event.getType());
                System.out.println("evenState：" + event.getState());
                if (event.getType() == Event.EventType.NodeChildrenChanged) {
                    try {
                        zooKeeper.getChildren("/watcher3", this, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        zooKeeper.getChildren("/watcher3", watcher, null);
        TimeUnit.SECONDS.sleep(100);
    }

    /**
     * 使用查询子节点信息的方法为节点注册多个watcher
     * 略
     */

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
