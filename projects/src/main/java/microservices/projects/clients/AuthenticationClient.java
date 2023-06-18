package microservices.projects.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import taskio.common.dto.authentication.extractemail.ExtractEmailRequest;

@FeignClient(name = "authentication", url = "${clients.authentication.url}")
public interface AuthenticationClient {
    @PostMapping("/extract-email")
    ResponseEntity<String> extractEmail(@RequestBody ExtractEmailRequest request);
}
