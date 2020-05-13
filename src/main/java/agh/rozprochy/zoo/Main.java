package agh.rozprochy.zoo;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String... args) {
//        BasicConfigurator.configure();
        String hostPort = "127.0.0.1:2181";
        String zNode = "/z";
        Collection<String> exec = Stream.of("ls").collect(Collectors.toList());
        try {
            new Executor(hostPort, zNode, exec).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
