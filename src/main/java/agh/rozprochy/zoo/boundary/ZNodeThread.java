package agh.rozprochy.zoo.boundary;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ZNodeThread {

    final static ExecutorService executor = Executors.newCachedThreadPool();
}
