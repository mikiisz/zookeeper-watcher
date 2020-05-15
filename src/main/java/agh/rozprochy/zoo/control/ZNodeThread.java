package agh.rozprochy.zoo.control;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ZNodeThread {

    protected final static ExecutorService executor = Executors.newCachedThreadPool();

    protected List<String> listAllNodes(ZooKeeper zk, String prefix, String zNode) {
        final String newKid = prefix + "/" + zNode;
        final List<String> kids = new ArrayList<>();
        try {
            zk.exists(newKid, false);
            kids.add(newKid);
            List<String> newKids = zk.getChildren(newKid, null);
            for (String kid : newKids) {
                kids.addAll(listAllNodes(zk, newKid, kid));
            }
        } catch (KeeperException | InterruptedException e) {
            return kids;
        }
        return kids;
    }
}
