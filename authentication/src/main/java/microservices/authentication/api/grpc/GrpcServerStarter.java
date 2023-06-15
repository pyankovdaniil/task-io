package microservices.authentication.api.grpc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import io.grpc.Server;
import io.grpc.ServerBuilder;

@Configuration
@PropertySource("classpath:application.properties")
public class GrpcServerStarter implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(GrpcServerStarter.class);

    @Value("${grpc.server.port}")
    private String applicationPort;
    private Server server;

    public void startGrpcServer() {
        try {
            server = ServerBuilder.forPort(Integer.parseInt(applicationPort))
            .addService(new AuthenticationGrpcController())
            .build();
            server.start();
        } catch (IOException e) {
            logger.error("Error while starting an authentication Grpc server, error is {}", e);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Starting authentication Grpc server on port {}", Integer.parseInt(applicationPort));
        startGrpcServer();
        logger.info("Authentication Grpc server started on port {}", Integer.parseInt(applicationPort));
    }
}
