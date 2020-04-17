package ru.itis.grpc.server;

import ru.itis.proto.*;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageServiceImpl extends MessageServiceGrpc.MessageServiceImplBase {

    @Override
    public void unaryMessage(final MessageRequest request, final StreamObserver<MessageResponse> responseObserver) {
        final UnaryMessage message = request.getMessage();
        final int number = message.getNumber();


        final MessageResponse response = MessageResponse.newBuilder()
                .setNumber(Math.sqrt(number))
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }


    @Override
    public void serverCallMessage(final MessageManyTimesRequest request, final StreamObserver<MessageManyTimesResponse> responseObserver) {
        final List<Integer> factors = primeFactors(request.getMessage().getNumber());
        System.out.println(factors);

        try {
            for (Integer factor : factors) {
                Thread.sleep(1000L);
                final MessageManyTimesResponse response = MessageManyTimesResponse.newBuilder()
                        .setMessage(factor)
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
    public StreamObserver<LongMessageRequest> clientCallMessage(final StreamObserver<LongMessageResponse> responseObserver) {
        return new StreamObserver<LongMessageRequest>() {
            final AtomicInteger response = new AtomicInteger();
            final AtomicInteger count = new AtomicInteger();

            @Override
            public void onNext(final LongMessageRequest value) {
                response.getAndAdd(value.getMessage().getNumber());
                count.getAndIncrement();
            }

            @Override
            public void onError(final Throwable t) {
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(
                        LongMessageResponse.newBuilder()
                                .setMessage(response.intValue() / count.intValue())
                                .build()
                );
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<MessageEveryOneRequest> biDiCallMessage(final StreamObserver<MessageEveryOneResponse> responseObserver) {
        return new StreamObserver<MessageEveryOneRequest>() {
            final Vector<Integer> numbers = new Vector<>();

            @Override
            public void onNext(final MessageEveryOneRequest value) {
                numbers.add(value.getMessage().getNumber());
                final MessageEveryOneResponse greetEveryOneResponse = MessageEveryOneResponse.newBuilder()
                        .setMessage(Collections.max(numbers)).build();
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

    public List<Integer> primeFactors(int num) {
        final List<Integer> factors = new ArrayList<>();
        factors.add(1);
        for (int a = 2; num > 1; )
            if (num % a == 0) {
                factors.add(a);
                num /= a;
            } else a++;
        return factors;
    }
}
