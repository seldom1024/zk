package com.seldom.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Seldom
 * @time: 2020/5/16 18:46
 */
public class CuratorDeleteTest {

    CuratorFramework client;

    /**
     * @功能: 获取连接
     * @作者: 高志红
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
                // 命名空间
                .namespace("delete")
                // 构建连接对象
                .build();
        // 打开连接
        client.start();
    }

    /**
     * 删除节点
     */
    @Test
    public void delete1() throws Exception {
        client.delete()
                // 第一个参数：节点路径
                .forPath("/node1");
        System.out.println("已删除！");
    }

    /**
     * 除节点，并进行版本检测
     */
    @Test
    public void delete2() throws Exception {
        client.setData()
                // 版本号 -1 标识忽略版本号
                .withVersion(0)
                .forPath("/node1");
        System.out.println("已删除");
    }

    /**
     * 删除包含子节点的节点
     */
    @Test
    public void delete3() throws Exception {
        client.delete()
                // 如有子节点 则一并删除
                .deletingChildrenIfNeeded()
                .withVersion(-1)
                .forPath("/node1");
        System.out.println("已删除");
    }

    /**
     * 异步删除节点
     */
    @Test
    public void delete4() throws Exception {
        client.delete()
                .deletingChildrenIfNeeded()
                .withVersion(0)
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                        System.out.println(curatorEvent.getPath());
                        System.out.println(curatorEvent.getType());
                    }
                })
                .forPath("/node1");
        TimeUnit.SECONDS.sleep(1);
        System.out.println("已删除");
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
