package example;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @description: 从 zk 中获取配置信息
 * @author: Seldom
 * @time: 2020/5/16 14:31
 */
public class ZkConfigCenterTest implements Watcher {

    // zk 对象
    private ZooKeeper zooKeeper;

    // 计数
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    private final String IP = "192.168.216.149:2181";

    private String url;
    private String username;
    private String pwd;

    // 构造方法
    public ZkConfigCenterTest() {
        initValue();
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
            } else if (event.getType() == Event.EventType.NodeDataChanged) { // 当配置信息发生变化，就重新去加载配置信息
                initValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接zookeeper服务器，读取配置信息
     */
    private void initValue() {
        //创建连对象
        try {
            // 创建链接
            if (zooKeeper == null) {
                zooKeeper = new ZooKeeper(IP, 5000, this);
            }
            // 阻塞线程，等待连接成功
            countDownLatch.await();
            // 读取配置信息
            this.url = new String(zooKeeper.getData("/config/url", true, null));
            this.username = new String(zooKeeper.getData("/config/username", true, null));
            this.pwd = new String(zooKeeper.getData("/config/pwd", true, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        try {
            ZkConfigCenterTest myConfigCenter = new ZkConfigCenterTest();
            for (int i = 0; i < 10; i++) {
                TimeUnit.SECONDS.sleep(10);
                System.out.println(myConfigCenter);
                System.out.println("==================================================");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "MyConfigCenter{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public static CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public static void setCountDownLatch(CountDownLatch countDownLatch) {
        ZkConfigCenterTest.countDownLatch = countDownLatch;
    }

    public String getIP() {
        return IP;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
