package taskio.microservices.projects.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import taskio.common.model.authentication.User;

@FeignClient(name = "authentication", url = "${clients.authentication.url}")
public interface AuthenticationClient {
    @PostMapping("/user-data")
    User getUserData(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
}
