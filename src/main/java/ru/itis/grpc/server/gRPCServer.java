package ru.itis.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class gRPCServer {

    public static void main(final String[] args) throws IOException, InterruptedException {
        final Server server = ServerBuilder.forPort(50051)
                .addService(new MessageServiceImpl()
                ).build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }
}
