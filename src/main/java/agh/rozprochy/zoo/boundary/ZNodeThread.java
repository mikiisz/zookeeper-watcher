package agh.rozprochy.zoo.boundary;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ZNodeThread {

    final static ExecutorService executor = Executors.newCachedThreadPool();

    protected Thread printAllNodes(ZooKeeper zk, Watcher watcher) {
        return new Thread(() -> {
            final Scanner input = new Scanner(System.in);
            while (input.hasNextLine()) {
                if (input.nextLine().equals("ls")) {
                    List<String> kids = null;
                    try {
                        kids = zk.getChildren("/z", watcher);
                        listAllNodes(zk, watcher, kids, "/z");
                        System.out.println("/z");
                    } catch (KeeperException | InterruptedException e) {
                        System.out.println("Node does not exists");
                    }
                }
            }
        });

    }

    private void listAllNodes(ZooKeeper zk, Watcher watcher, List<String> kids, String parent) throws KeeperException, InterruptedException {
        if (!kids.isEmpty()) {
            for (String node : kids) {
                List<String> newKids = zk.getChildren(parent + "/" + node, watcher);
                listAllNodes(zk, watcher, newKids, parent + "/" + node);
                System.out.println(parent + "/" + node);
            }
        }
    }
}
