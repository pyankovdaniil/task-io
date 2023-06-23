package microservices.projects.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import taskio.common.dto.authentication.userdata.UserDataRequest;
import taskio.common.model.authentication.User;

@FeignClient(name = "authentication", url = "${clients.authentication.url}")
public interface AuthenticationClient {
    @PostMapping("/user-data")
    ResponseEntity<User> getUserData(@RequestBody UserDataRequest request);
}
