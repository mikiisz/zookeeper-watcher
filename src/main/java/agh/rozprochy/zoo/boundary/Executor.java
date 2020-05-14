package agh.rozprochy.zoo.boundary;

import agh.rozprochy.zoo.control.DataMonitorListener;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;

public class Executor extends ZNodeThread implements Watcher, Runnable, DataMonitorListener {

    private final DataMonitor dm;
    private final Collection<String> exec;

    private Process child;

    public Executor(String hostPort, String zNode, Collection<String> exec) throws IOException {
        this.exec = exec;
        final ZooKeeper zk = new ZooKeeper(hostPort, 3000, this);
        dm = new DataMonitor(zk, zNode, null, this);
    }

    @Override
    public void process(WatchedEvent event) {
        dm.process(event);
    }

    @Override
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

    @Override
    public void closing(KeeperException.Code rc) {
        synchronized (this) {
            notifyAll();
        }
    }

    @Override
    public void exists(byte[] data) throws IOException {
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
            System.out.println("Starting child");
            child = Runtime.getRuntime().exec(exec.toArray(new String[0]));

            executor.submit(() -> {
                final Scanner input = new Scanner(child.getInputStream());
                while (input.hasNextLine()) {
                    System.out.println(input.nextLine());
                }
            });

            executor.submit(() -> {
                final Scanner input = new Scanner(child.getErrorStream());
                while (input.hasNextLine()) {
                    System.out.println(input.nextLine());
                }
            });
        }
    }
}