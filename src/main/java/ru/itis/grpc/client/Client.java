package ru.itis.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import ru.itis.proto.*;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class Client {

    private final Integer[] numbers = {1, 2, 3, 8, 9, 6, 7, 5, 4};
    private final CountDownLatch latch = new CountDownLatch(1);

    public static void main(final String[] args) throws InterruptedException {
        final Client client = new Client();
        client.run();
    }

    private void run() throws InterruptedException {
        final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        doUnaryCall(channel);
        doClientCall(channel);
        doBiDiCall(channel);
        doServerCall(channel);

        channel.shutdown();
    }

    private void doUnaryCall(final ManagedChannel channel) throws InterruptedException {
        System.out.println("Unary call");

        final MessageServiceGrpc.MessageServiceBlockingStub greetClient = MessageServiceGrpc.newBlockingStub(channel);

        final int number = 36;
        System.out.println("Request: " + number);
        final UnaryMessage unaryMessage = UnaryMessage.newBuilder().setNumber(number).build();

        final MessageRequest greetRequest = MessageRequest.newBuilder()
                .setMessage(unaryMessage)
                .build();

        final MessageResponse response = greetClient.unaryMessage(greetRequest);
        System.out.println("Response: " + response.getNumber());

        System.out.println("Unary call finished");
        Thread.sleep(1000L);
    }

    private void doBiDiCall(final ManagedChannel channel) throws InterruptedException {
        System.out.println("BiDi call");
        final MessageServiceGrpc.MessageServiceStub asyncClient = MessageServiceGrpc.newStub(channel);

        final StreamObserver<MessageEveryOneRequest> requestObserver = asyncClient.biDiCallMessage(
                new StreamObserver<MessageEveryOneResponse>() {
                    @Override
                    public void onNext(final MessageEveryOneResponse value) {
                        System.out.println("New maximum: " + value.getMessage());
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

        Arrays.asList(numbers).forEach(
                name -> {
                    System.out.println("Request: " + name);
                    requestObserver.onNext(MessageEveryOneRequest.newBuilder()
                            .setMessage(BiDiCallMessage.newBuilder().setNumber(name).build())
                            .build());
                }
        );

        requestObserver.onCompleted();
        Thread.sleep(1000L);
    }

    private void doClientCall(final ManagedChannel channel) throws InterruptedException {
        System.out.println("Client call");
        final MessageServiceGrpc.MessageServiceStub asyncClient = MessageServiceGrpc.newStub(channel);

        final StreamObserver<LongMessageRequest> requestObserver = asyncClient.clientCallMessage(new StreamObserver<LongMessageResponse>() {
            @Override
            public void onNext(final LongMessageResponse value) {
                System.out.println("Standard deviation: " + value.getMessage());
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

        Arrays.asList(numbers).forEach(
                number -> {
                    System.out.println("Request: " + number);
                    requestObserver.onNext(LongMessageRequest.newBuilder()
                            .setMessage(ClientCallMessage.newBuilder().setNumber(number).build())
                            .build());
                }
        );

        requestObserver.onCompleted();
        Thread.sleep(1000L);
    }

    private void doServerCall(final ManagedChannel channel) throws InterruptedException {
        System.out.println("Server call");
        final MessageServiceGrpc.MessageServiceBlockingStub client = MessageServiceGrpc.newBlockingStub(channel);

        final int number = 36;
        System.out.println("Request: " + number);
        final MessageManyTimesRequest request = MessageManyTimesRequest.newBuilder()
                .setMessage(ServerCallMessage.newBuilder().setNumber(number).build())
                .build();

        client.serverCallMessage(request)
                .forEachRemaining(response -> {
                    System.out.println("Factor: " + response.getMessage());
                });
        Thread.sleep(1000L);
    }
}
