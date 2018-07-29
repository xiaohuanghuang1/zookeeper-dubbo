package com.imooc.web.service.impl;

import com.imooc.web.config.ZKConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;


@Component
public class DistributedLock
{
    private static final Logger logger = LoggerFactory.getLogger(DistributedLock.class);

//    @Resource(name = "client")
//    private CuratorFramework client;

    //用于挂起当前线程，等待上一个锁释放
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    private static final String ZK_LOCK_PROJECT = "ZK_LOCK";

    private static final String DISTRIBUTED_LOCK = "distributed_lock";

    private static final CuratorFramework client = ZKConfig.getZKClient();

    public DistributedLock()
    {
        System.out.println("=================================");
        init();
    }

    public void init()
    {
        try
        {
            if (client.checkExists().forPath("/"+ZK_LOCK_PROJECT) == null)
            {

                    client.create().creatingParentsIfNeeded()
                            .withMode(CreateMode.PERSISTENT)
                            .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                            .forPath("/"+ZK_LOCK_PROJECT);
            }
            //给总结点添加事件
            addWatcherToLock("/"+ZK_LOCK_PROJECT);
        } catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    public void getLock()
    {
        while (true)
        {
            try
            {
                client.create().creatingParentContainersIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath("/"+ZK_LOCK_PROJECT+"/"+DISTRIBUTED_LOCK);

                logger.info("获取分布式锁成功....");
                return;
            } catch (Exception e)
            {
                logger.info("获取分布式锁失败....");
                if (countDownLatch.getCount() <= 0)
                {
                    countDownLatch = new CountDownLatch(1);
                }
                try
                {
                    countDownLatch.await();
                } catch (InterruptedException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void addWatcherToLock(String path) throws Exception
    {
        final PathChildrenCache pathChildrenCache = new PathChildrenCache(client,path,true);
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener()
        {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception
            {
                if (pathChildrenCacheEvent.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED))
                {
                    String path1 = pathChildrenCacheEvent.getData().getPath();
                    logger.info("上一个{} 锁已经释放",path1);

                    //判断是哪一把锁被删除
                    if (path1.contains(DISTRIBUTED_LOCK))
                    {
                        countDownLatch.countDown();
                        logger.info("正在获取锁............");
                    }
                }
            }
        });
    }

    public boolean releaseLock()
    {
        try
        {
            if (client.checkExists().forPath("/"+ZK_LOCK_PROJECT+"/"+DISTRIBUTED_LOCK) != null)
            {
                client.delete().forPath("/"+ZK_LOCK_PROJECT+"/"+DISTRIBUTED_LOCK);
                logger.info("分布式锁释放完毕.....");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
