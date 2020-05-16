package example;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @description: 利用 zk 生成分布式唯一id
 *          １. 连接zookeeper服务器
 * 		    ２. 指定路径生成临时有序节点
 *          3.取序列号及为分布式环境下的唯一ID
 * @author: Seldom
 * @time: 2020/5/16 14:51
 */
public class ZkGlobalUUIDTest implements Watcher {

    private ZooKeeper zooKeeper;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private final String IP = "192.168.216.149:2181";

    //  用户生成序号的节点
    String defaultPath = "/uId";

    /**
     * 构造方法
     */
    public ZkGlobalUUIDTest() {
        try {
            // 打开连接对象
            zooKeeper = new ZooKeeper(IP, 5000, this);
            // 阻塞线程
            countDownLatch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            // 时间类型
            if (event.getType() == Watcher.Event.EventType.None) {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("连接创建成功");
                    countDownLatch.countDown();
                } else if (event.getState() == Watcher.Event.KeeperState.Disconnected) {
                    System.out.println("断开连接");
                } else if (event.getState() == Watcher.Event.KeeperState.Expired) {
                    System.out.println("会话超时");
                    zooKeeper = new ZooKeeper(IP, 5000, this); // 重连
                } else if (event.getState() == Watcher.Event.KeeperState.AuthFailed) {
                    System.out.println("验证失败");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成ID的方法
     */
    public String getUniqueId() {
        String path = "";
        try {
            path = zooKeeper.create(defaultPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path.substring(defaultPath.length());
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        ZkGlobalUUIDTest globallyUniqueId = new ZkGlobalUUIDTest();
        for (int i = 0; i < 5; i++) {
            String uniqueId = globallyUniqueId.getUniqueId();
            System.out.println(uniqueId);
        }
    }
}
