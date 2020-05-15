package agh.rozprochy.zoo;

import agh.rozprochy.zoo.boundary.Executor;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    static {
        // Enable for logging:
//        BasicConfigurator.configure();
    }

    public static void main(String... args) {
        String hostPort = "127.0.0.1:2181";
        String zNode = "/z";
        Collection<String> exec = Stream.of("./dummyProgram.sh").collect(Collectors.toList());
        try {
            new Executor(hostPort, zNode, exec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
