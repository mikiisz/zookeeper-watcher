package agh.rozprochy.zoo.boundary;

import agh.rozprochy.zoo.control.ZNodeThread;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;

import static org.apache.zookeeper.KeeperException.Code;

public class Executor extends ZNodeThread implements Watcher, StatCallback {

    private final Collection<String> exec;
    private final String zNode;
    private final ZooKeeper zk;
    private final Scanner scanner = new Scanner(System.in);
    private Process child;

    public Executor(String hostPort, String zNode, Collection<String> exec) throws IOException {
        this.exec = exec;
        this.zNode = zNode;
        this.zk = new ZooKeeper(hostPort, 3000, this);
        zk.exists(zNode, true, this, null);
        zk.exists(zNode, true, new ZNodeWatcher(zNode, zNode, zk), null);

        executor.submit(() -> {
            while (scanner.hasNextLine()) {
                if (scanner.nextLine().equals("ls")) {
                    listAllNodes(zk, "", "z").forEach(System.out::println);
                }
            }
        });
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getPath().equals(zNode)) {
            zk.exists(zNode, true, this, null);
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        switch (Code.get(rc)) {
            case OK:
                if (child == null) {
                    System.out.println("Starting app");
                    try {
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case NONODE:
                if (child.isAlive()) {
                    System.out.println("Stopping app");
                    child.destroy();
                    try {
                        child.waitFor();
                    } catch (InterruptedException ignored) {
                    }
                    child = null;
                }
                break;
            case SESSIONEXPIRED:
            case NOAUTH:
                break;
            default:
                zk.exists(zNode, true, this, null);
        }
    }
}