package ru.itis.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import ru.itis.proto.*;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class Client {

    private final String[] names = {"Evgeniy", "Bulat", "Roman"};
    private final CountDownLatch latch = new CountDownLatch(1);

    public static void main(final String[] args) throws InterruptedException {
        final Client client = new Client();
        client.run();
    }

    private void run() throws InterruptedException {
        final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        doBiDiCall(channel);
        doClientCall(channel);
        doServerCall(channel);

        channel.shutdown();
    }

    private void doBiDiCall(final ManagedChannel channel) throws InterruptedException {
        System.out.println("BiDi call");
        final MessageServiceGrpc.MessageServiceStub asyncClient = MessageServiceGrpc.newStub(channel);

        final StreamObserver<MessageEveryOneRequest> requestObserver = asyncClient.messageEveryOne(
                new StreamObserver<MessageEveryOneResponse>() {
                    @Override
                    public void onNext(final MessageEveryOneResponse value) {
                        System.out.println("Response: " + value.getMessage());
                    }

                    @Override
                    public void onError(final Throwable t) {
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("BiDi call finished");
                        latch.countDown();
                    }
                }
        );

        Arrays.asList(names).forEach(
                name -> {
                    System.out.println("Request: " + name);
                    requestObserver.onNext(MessageEveryOneRequest.newBuilder()
                            .setMessage(Message.newBuilder().setText(name).build())
                            .build());
                }
        );

        requestObserver.onCompleted();
        Thread.sleep(1000L);
    }

    private void doClientCall(final ManagedChannel channel) throws InterruptedException {
        System.out.println("Client call");
        final MessageServiceGrpc.MessageServiceStub asyncClient = MessageServiceGrpc.newStub(channel);

        final StreamObserver<LongMessageRequest> requestObserver = asyncClient.longMessage(new StreamObserver<LongMessageResponse>() {
            @Override
            public void onNext(final LongMessageResponse value) {
                System.out.println("Response: " + value.getMessage());
            }

            @Override
            public void onError(final Throwable t) {
            }

            @Override
            public void onCompleted() {
                System.out.println("Client call finished");
                latch.countDown();
            }
        });

        Arrays.asList(names).forEach(
                name -> {
                    System.out.println("Request: " + name);
                    requestObserver.onNext(LongMessageRequest.newBuilder()
                            .setMessage(Message.newBuilder().setText(name).build())
                            .build());
                }
        );

        requestObserver.onCompleted();
        Thread.sleep(1000L);
    }

    private void doServerCall(final ManagedChannel channel) throws InterruptedException {
        System.out.println("Server call");
        final MessageServiceGrpc.MessageServiceBlockingStub client = MessageServiceGrpc.newBlockingStub(channel);


        final MessageManyTimesRequest request = MessageManyTimesRequest.newBuilder()
                .setMessage(Message.newBuilder().setText(names[0]))
                .build();

        client.messageManyTimes(request)
                .forEachRemaining(response -> {
                    System.out.println(response.getMessage());
                });
        Thread.sleep(1000L);
    }
}
