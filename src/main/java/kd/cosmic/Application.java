package kd.cosmic;

import kd.cosmic.server.Launcher;

/**
 * 启动本地应用程序(微服务节点)
 */
public class Application {

    public static void main(String[] args) {
        Launcher cosmic = new Launcher();

        cosmic.setClusterNumber("cosmic");
        cosmic.setTenantNumber("ierp");
        cosmic.setServerIP("127.0.0.1");

        cosmic.setAppName("cosmic-xjkvbnwe-eJjK7cuL");
        cosmic.setWebPath("D:/小工具文件夹/IDEA 工作空间/KingdeeProject-server/webapp/static-file-service");


        cosmic.setStartWithQing(false);

        cosmic.set("lightweightdeploy", "true");  //标注环境为轻量级
        cosmic.set("redismodelcache.enablelua", "false");  //轻量级不能用lua，设为false
        cosmic.set("lightweightdeploy.services" , "");  //设置服务为空。服务由MC启动，这里就不用启动了
        cosmic.setConfigUrl("127.0.0.1:2181","zookeeper","123003yan");

        cosmic.start();
    }
}