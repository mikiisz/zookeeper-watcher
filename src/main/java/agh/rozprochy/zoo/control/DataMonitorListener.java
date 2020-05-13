package agh.rozprochy.zoo;

import org.apache.zookeeper.KeeperException.Code;

public interface DataMonitorListener {
    void exists(byte[] data);

    void closing(Code code);
}