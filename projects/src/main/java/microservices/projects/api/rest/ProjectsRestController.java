package microservices.projects.api.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import taskio.common.mapping.ObjectMapperWrapper;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rest/api/v1/projects")
public class ProjectsRestController {
    private final Logger logger = LoggerFactory.getLogger(ProjectsRestController.class);
    private final ObjectMapperWrapper objectMapper;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateRequest request) {
        logger.info("POST /create request received with data:\n{}",
                objectMapper.toPrettyJson(request));
        
        return ResponseEntity.ok("cock");
    }
}
