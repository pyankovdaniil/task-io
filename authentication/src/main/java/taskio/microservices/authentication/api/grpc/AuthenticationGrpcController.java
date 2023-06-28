package taskio.microservices.authentication.api.grpc;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;
import microservices.authentication.grpc.AuthenticationGrpc;
import microservices.authentication.grpc.AuthenticationOuterClass;
import taskio.common.mapping.ObjectMapperWrapper;

public class AuthenticationGrpcController extends AuthenticationGrpc.AuthenticationImplBase {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationGrpcController.class);
    private final ObjectMapperWrapper objectMapper = new ObjectMapperWrapper();

    @Override
    public void authenticate(AuthenticationOuterClass.AuthenticationRequest request,
            StreamObserver<AuthenticationOuterClass.AuthenticationResponse> responseObserver) {
        logger.info("Got an GRPC /authenticate request with body:\n{}", objectMapper.toPrettyJson(request.getAllFields()
                .entrySet().stream()
                .map(entry -> new HashMap.SimpleEntry<>(
                        entry.getKey().toString().substring(entry.getKey().toString().lastIndexOf(".") + 1), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));

        AuthenticationOuterClass.AuthenticationResponse response = AuthenticationOuterClass.AuthenticationResponse.newBuilder()
                .setAccessToken("DEFAULT_ACCESS_TOKEN").setRefreshToken("DEFAULT_REFRESH_TOKEN").build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
