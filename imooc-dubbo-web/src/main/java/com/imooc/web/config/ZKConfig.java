package com.imooc.web.config;

import com.imooc.web.util.ZKCurator;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

public class ZKConfig
{
    private static final String serverPath = "192.168.118.130:2181,192.168.118.130:2182,192.168.118.130:2183";

    private  static volatile CuratorFramework client = null;

    private ZKConfig(){}

    public static CuratorFramework getZKClient()
    {
        System.out.println("ZKConfig===========");
        if (client == null)
        {
            synchronized (ZKConfig.class)
            {
                if (client == null)
                {
                    RetryPolicy retryPolicy = new RetryNTimes(5,3000);

                    CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().connectString(serverPath)
                            .sessionTimeoutMs(10000).retryPolicy(retryPolicy)
                            .namespace("zk-Curator-connector").build();
                    curatorFramework.start();
                    client = curatorFramework;
                }
            }
        }
        return client;
    }


//    @Bean("client")
//    public CuratorFramework getClient()
//    {
//        RetryPolicy retryPolicy = new RetryNTimes(5,3000);
//
//        CuratorFramework client = CuratorFrameworkFactory.builder().connectString(serverPath)
//                .sessionTimeoutMs(10000).retryPolicy(retryPolicy)
//                .namespace("zk-Curator-connector").build();
//        client.start();
//        return client;
//    }
}
