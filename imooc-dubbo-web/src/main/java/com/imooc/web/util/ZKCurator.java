package com.imooc.web.util;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ZKCurator
{
//    @Resource(name = "client")
//    private CuratorFramework client;
//
//    private static final Logger logger = LoggerFactory.getLogger(ZKCurator.class);
//
////    public ZKCurator(CuratorFramework client)
////    {
////        this.client = client;
////    }
//
////    public void init()
////    {
////        client = client.usingNamespace("zk-Curator-connector");
////    }
//
//    public boolean isZKAlive()
//    {
//        return  client.isStarted();
//    }
}
