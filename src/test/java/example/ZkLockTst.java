package example;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @description: 利用 zk 生成分布式锁
 * 		１. 每个客户端往/Locks下创建临时有序节点/Locks/Lock000000001
 * 		２. 客户端取得/Locks下子节点，并进行排序，判断排在最前面的是否为自己，如果自己的锁节点在第一位，代表获取锁成功
 * 		３. 如果自己的锁节点不在第一位，则监听自己前一位的锁节点。例如，自己锁节点Lock 000000001
 * 		４. 当前一位锁节点（Lock000000002）的逻辑
 *      5.监听客户端重新执行第2步逻辑，判断自己是否获得了锁
 * @author: Seldom
 * @time: 2020/5/16 15:02
 */
public class ZkLockTst {

    // zk连接对象
    public ZooKeeper zooKeeper;
    // 计数器对象
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    // zk的连接串
    private final String IP = "192.168.216.149:2181";
    //  锁的节点名称
    public static final String LOCK_ROOT_PATH = "/Locks";
    public static final String LOCK_NODE_NAME = LOCK_ROOT_PATH + "/lock_";
    private String lockPath;

    private final Watcher watcher = new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                synchronized (this) {
                    notifyAll();
                }
            }
        }
    };


    /**
     * 构造方法
     */
    public ZkLockTst() {
        try {
            // 打开zookeeper连接
            zooKeeper = new ZooKeeper(IP, 5000, event -> {
                if (event.getType() == Watcher.Event.EventType.None) {
                    if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        System.out.println("连接成功");
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取锁
     */
    public void acquireLock() {
        //创建锁节点
        createLock();
        //尝试获取锁
        attemptLock();
    }

    /**
     * 创建锁节点
     */
    public void createLock() {
        // 判断/Locks是否存在，不存在则创建
        try {
            Stat exists = zooKeeper.exists(LOCK_ROOT_PATH, false);
            if (exists == null) {
                zooKeeper.create(LOCK_ROOT_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            // 创建临时有序节点
            lockPath = zooKeeper.create(LOCK_NODE_NAME, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("节点创建成功：" + lockPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 尝试获取锁
     */
    public void attemptLock() {
        try {
            // 获取/Locks节点下的所有子节点
            List<String> childes = zooKeeper.getChildren(LOCK_ROOT_PATH, false);
            // 对子节点进行排序
            Collections.sort(childes);
            int index = childes.indexOf(lockPath.substring((LOCK_NODE_NAME.length() - LOCK_ROOT_PATH.length()) + 1));
            System.out.println(index);
            if (index == 0) {
                System.out.println("获取锁成功");
            } else {
                // 上一个节点的路径
                String path = childes.get(index - 1);
                Stat exists = zooKeeper.exists(LOCK_ROOT_PATH + "/" + path, watcher);
                // 可能上一个节点已经释放了
                if (exists != null) {
                    synchronized (watcher) {
                        watcher.wait();
                    }
                }
                attemptLock();
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放锁
     */
    public void releaseLock() {
        try {
            // 删除临时有序节点
            zooKeeper.delete(this.lockPath, -1);
            zooKeeper.close();
            System.out.println("锁已释放：" + lockPath);
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        ZkLockTst myLock = new ZkLockTst();
        // 获取锁
        myLock.acquireLock();
        sell();
        // 释放锁
        myLock.releaseLock();
    }

    public static void sell() {
        System.out.println("售票开始");
        // 线程休眠，模拟现实中耗时的操作
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("售票结束");
    }
}
