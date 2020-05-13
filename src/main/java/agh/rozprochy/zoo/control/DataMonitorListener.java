package agh.rozprochy.zoo.control;

import org.apache.zookeeper.KeeperException.Code;

import java.io.IOException;

public interface DataMonitorListener {
    void exists(byte[] data) throws IOException;

    void closing(Code code);
}