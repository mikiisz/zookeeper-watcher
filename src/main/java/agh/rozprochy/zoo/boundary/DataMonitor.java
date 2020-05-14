package agh.rozprochy.zoo.boundary;

import agh.rozprochy.zoo.control.DataMonitorListener;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;

public class DataMonitor extends ZNodeThread implements Watcher, StatCallback {

    final private ZooKeeper zk;
    final private String zNode;
    final private Watcher chainedWatcher;
    final private DataMonitorListener listener;

    private boolean dead;

    public DataMonitor(ZooKeeper zk, String zNode, Watcher chainedWatcher, DataMonitorListener listener) {
        this.zk = zk;
        this.zNode = zNode;
        this.chainedWatcher = chainedWatcher;
        this.listener = listener;

        zk.exists(zNode, true, this, null);
        executor.submit(printAllNodes(zk, this));
    }

    public boolean isDead() {
        return dead;
    }

    @Override
    public void process(WatchedEvent event) {
        final String path = event.getPath();

        if (event.getType() == Event.EventType.None) {
            switch (event.getState()) {
                case SyncConnected:
                    break;
                case Expired:
                    dead = true;
                    listener.closing(Code.SESSIONEXPIRED);
                    break;
            }
        } else {
            if (path != null && path.equals(zNode)) {
                zk.exists(zNode, true, this, null);
            }
        }
        if (chainedWatcher != null) {
            chainedWatcher.process(event);
        }
    }

    @Override
    public void processResult(int code, String path, Object ctx, Stat stat) {
        boolean exists;
        switch (Code.get(code)) {
            case OK:
                exists = true;
                break;
            case NONODE:
                exists = false;
                break;
            case SESSIONEXPIRED:
            case NOAUTH:
                dead = true;
                listener.closing(Code.get(code));
                return;
            default:
                zk.exists(zNode, true, this, null);
                return;
        }

        byte[] b = null;
        if (exists) {
            try {
                b = zk.getData(zNode, false, null);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        }
        try {
            listener.exists(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
