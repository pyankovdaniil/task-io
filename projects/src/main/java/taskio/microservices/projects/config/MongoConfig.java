package taskio.microservices.projects.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig {
    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private Integer port;

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    @Value("${spring.data.mongodb.authentication-database}")
    private String authenticationDatabase;

    @Value("${spring.data.mongodb.database}")
    private String targetDatabase;

    @Bean
    public MongoClientFactoryBean mongo() {
        MongoClientFactoryBean mongo = new MongoClientFactoryBean();
        mongo.setHost(host);
        mongo.setPort(port);

        mongo.setCredential(new MongoCredential[] {
                MongoCredential.createCredential(username, authenticationDatabase, password.toCharArray())
        });

        return mongo;
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(
                        builder -> builder.hosts(Collections.singletonList(new ServerAddress(host, port))))
                .credential(MongoCredential.createCredential(username, authenticationDatabase,
                        password.toCharArray()))
                .build();

        return new MongoTemplate(MongoClients.create(settings), targetDatabase);
    }
}
