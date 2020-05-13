package agh.rozprochy.zoo;

public interface DataMonitorListener {
    void exists(byte[] data);

    void closing(int rc);
}