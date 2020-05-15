package agh.rozprochy.zoo.boundary;

import agh.rozprochy.zoo.control.ZNodeThread;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;

public class ZNodeWatcher extends ZNodeThread implements Watcher, StatCallback {

    final private String origin;
    final private String zNode;
    final private ZooKeeper zk;

    private List<String> kids = new ArrayList<>();

    ZNodeWatcher(String origin, String zNode, ZooKeeper zk) {
        this.origin = origin;
        this.zk = zk;
        this.zNode = zNode;
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getPath().equals(zNode)) {
            zk.exists(zNode, true, this, null);
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (Code.get(rc) == Code.OK) {
            try {
                kids = updateKids(zk, origin, zNode, kids, this);
            } catch (KeeperException | InterruptedException ignored) {
            }
        } else {
            zk.exists(zNode, true, this, null);
        }
    }

    private List<String> updateKids(ZooKeeper zk, String origin, String zNode, List<String> kids, Watcher watcher) throws KeeperException, InterruptedException {
        List<String> newKids = zk.getChildren(zNode, watcher);
        newKids.removeAll(kids);
        if (newKids.size() == 1) {
            for (String kid : newKids) {
                final String newKid = zNode + "/" + kid;
                zk.exists(newKid, true, new ZNodeWatcher(origin, newKid, zk), null);
                System.out.println(String.format("Current kids: %d", listAllNodes(zk, "", origin.substring(1)).size() - 1));
            }
        }
        return newKids;
    }
}
