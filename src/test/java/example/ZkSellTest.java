package example;

/**
 * @description: 测试
 * @author: Seldom
 * @time: 2020/5/16 15:26
 */
public class ZkSellTest {

    private void sell() {
        System.out.println("开始");
        int sleep = 5000;

        try {
            // 模拟业务
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("售票结束");
    }

    public void sellWithLock(){
        ZkLockTst zkLockTst = new ZkLockTst();
        zkLockTst.acquireLock();
        sell();
        zkLockTst.releaseLock();
    }

    public static void main(String[] args) {
        ZkSellTest zkSellTest = new ZkSellTest();
        for (int i = 0; i < 100; i++) {
            new Thread(zkSellTest::sellWithLock).start();
        }
    }
}
