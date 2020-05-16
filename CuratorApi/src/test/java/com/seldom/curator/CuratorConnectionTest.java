package com.seldom.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;

/**
 * @description:
 * @author: Seldom
 * @time: 2020/5/16 18:22
 */
public class CuratorConnectionTest {

    public static void main(String[] args) {
        CuratorFramework cline = CuratorFrameworkFactory.builder()
                .connectString("192.168.216.149:2181,192.168.216.150:2181,192.168.216.151:2181")
                // 超时时间
                .sessionTimeoutMs(5000)
                // 重连机制
                // new RetryOneTime(3000)：每三秒重连一次，只重连一次
                // new RetryNTimes(3, 3000)：每每三秒重连一次，共重连3次
                // new RetryUntilElapsed(10000, 3000)：每三秒重连一次，10秒后停止重连
                // new ExponentialBackoffRetry(1000, 3)：重连3次，每次重连的间隔会越来越长
                // 会话超时后3s会重连1次
                .retryPolicy(new RetryOneTime(3000))
                // 命名空间（本次对象的根节点）
                .namespace("create")
                .build();

        // 打开连接
        cline.start();
        System.out.println(cline.getState());

        // 关闭
        cline.close();
    }
}
