package agh.rozprochy.zoo;

import agh.rozprochy.zoo.boundary.DataMonitor;
import agh.rozprochy.zoo.control.DataMonitorListener;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Collection;

public class Executor implements Watcher, Runnable, DataMonitorListener {

    private final DataMonitor dm;
    private final Collection<String> exec;

    private Process child;

    public Executor(String hostPort, String znode, Collection<String> exec) throws IOException {
        this.exec = exec;
        final ZooKeeper zooKeeper = new ZooKeeper(hostPort, 3000, this);
        dm = new DataMonitor(zooKeeper, znode, null, this);
    }

    public void process(WatchedEvent event) {
        dm.process(event);
    }

    public void run() {
        try {
            synchronized (this) {
                while (!dm.isDead()) {
                    wait();
                }
            }
        } catch (InterruptedException ignored) {
        }
    }

    public void closing(KeeperException.Code rc) {
        synchronized (this) {
            notifyAll();
        }
    }


    public void exists(byte[] data) {
        if (data == null) {
            if (child != null) {
                System.out.println("Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException ignored) {
                }
            }
            child = null;
        } else {
            if (child != null) {
                System.out.println("Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(exec.toArray(new String[0]));
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}