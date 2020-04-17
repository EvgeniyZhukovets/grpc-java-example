package ru.itis.grpc.server;

import ru.itis.proto.*;
import io.grpc.stub.StreamObserver;

public class MessageServiceImpl extends MessageServiceGrpc.MessageServiceImplBase {

    @Override
    public void messageManyTimes(final MessageManyTimesRequest request, final StreamObserver<MessageManyTimesResponse> responseObserver) {
        final String name = request.getMessage().getText();

        try {
            for (int i = 0; i < name.length(); i++) {
                Thread.sleep(1000L);
                final MessageManyTimesResponse response = MessageManyTimesResponse.newBuilder()
                        .setMessage(String.valueOf(name.charAt(i)))
                        .build();
                responseObserver.onNext(response);
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<LongMessageRequest> longMessage(final StreamObserver<LongMessageResponse> responseObserver) {
        return new StreamObserver<LongMessageRequest>() {
            final StringBuilder response = new StringBuilder();

            @Override
            public void onNext(final LongMessageRequest value) {
                response.append("Hello ").append(value.getMessage().getText());
            }

            @Override
            public void onError(final Throwable t) {
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(
                        LongMessageResponse.newBuilder()
                                .setMessage(response.toString())
                                .build()
                );
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<MessageEveryOneRequest> messageEveryOne(final StreamObserver<MessageEveryOneResponse> responseObserver) {
        return new StreamObserver<MessageEveryOneRequest>() {
            @Override
            public void onNext(final MessageEveryOneRequest value) {
                final String result = "Hello " + value.getMessage().getText();
                final MessageEveryOneResponse greetEveryOneResponse = MessageEveryOneResponse.newBuilder()
                        .setMessage(result).build();
                responseObserver.onNext(greetEveryOneResponse);
            }

            @Override
            public void onError(final Throwable t) {
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
