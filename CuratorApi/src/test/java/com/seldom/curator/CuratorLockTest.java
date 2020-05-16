package com.seldom.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Seldom
 * @time: 2020/5/16 18:50
 */
public class CuratorLockTest {

    CuratorFramework client;

    /**
     * 获取连接
     */
    @Before
    public void before() {
        // 创建连接对象
        client = CuratorFrameworkFactory
                .builder()
                // IP地址端口号
                .connectString("192.168.216.149:2181,192.168.216.150:2181,192.168.216.151:2181")
                // 会话超时时间
                .sessionTimeoutMs(5000)
                // 重连机制
                // new RetryOneTime(3000)：每三秒重连一次，只重连一次
                // new RetryNTimes(3, 3000)：每每三秒重连一次，共重连3次
                // new RetryUntilElapsed(10000, 3000)：每三秒重连一次，10秒后停止重连
                // new ExponentialBackoffRetry(1000, 3)：重连3次，每次重连的间隔会越来越长
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                // 构建连接对象
                .build();
        // 打开连接
        client.start();
    }

    /**
     * 排他锁
     */
    @Test
    public void lock1() throws Exception {
        // 参数1：连接对象
        // 参数2：节点路径
        InterProcessLock interProcessLock = new InterProcessMutex(client, "/lock1");
        System.out.println("等待获取锁对象");
        // 获取锁
        interProcessLock.acquire();
        for (int i = 0; i < 5; i++) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println(i);
        }
        System.out.println("等待释放锁");
        // 释放锁
        interProcessLock.release();
    }

    /**
     * 读写锁 - 读锁
     */
    @Test
    public void lock2() throws Exception {
        // 读写锁
        InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(client, "/lock1");
        // 获取读锁对象
        InterProcessMutex interProcessMutex = readWriteLock.readLock();
        System.out.println("等待获取锁对象");
        // 获取锁
        interProcessMutex.acquire();
        for (int i = 0; i < 10; i++) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println(i);
        }
        System.out.println("等待释放锁");
        // 释放锁
        interProcessMutex.release();
    }

    /**
     * 读写锁 - 写锁
     */
    @Test
    public void lock3() throws Exception {
        // 读写锁
        InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(client, "/lock1");
        // 获取读锁对象
        InterProcessMutex interProcessMutex = readWriteLock.writeLock();
        System.out.println("等待获取锁对象");
        // 获取锁
        interProcessMutex.acquire();
        for (int i = 0; i < 10; i++) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println(i);
        }
        System.out.println("等待释放锁");
        // 释放锁
        interProcessMutex.release();
    }

    /**
     * 关闭连接
     */
    @After
    public void after() {
        if (client != null) {
            client.close();
        }
    }
}
