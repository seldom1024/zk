package com.zk;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import sun.plugin2.gluegen.runtime.CPU;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @description:
 * @author: Seldom
 * @time: 2020/5/14 17:54
 */
public class ZkMain {

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        ZooKeeper zooKeeper = new ZooKeeper("192.168.216.149:2181", 5000, new Watcher() {
            public void process(WatchedEvent event) {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("连接成功");
                    countDownLatch.countDown();
                }
            }
        });
        // 查询
        List<ACL> hadoop = zooKeeper.getACL("/hadoop", new Stat());
        for (ACL acl : hadoop) {
            System.out.println(acl);
        }

        countDownLatch.await();
        System.out.println(zooKeeper.getSessionId());
        zooKeeper.close();
    }
}
