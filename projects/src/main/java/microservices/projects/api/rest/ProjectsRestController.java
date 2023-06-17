package microservices.projects.api.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import microservices.projects.dto.create.CreateRequest;
import microservices.authentication.mapping.ObjectMapperWrapper;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rest/api/v1/projects")
public class ProjectsRestController {
    private final Logger logger = LoggerFactory.getLogger(ProjectsRestController.class);

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateRequest request) {
        return ResponseEntity.ok("cock");
    }
}
