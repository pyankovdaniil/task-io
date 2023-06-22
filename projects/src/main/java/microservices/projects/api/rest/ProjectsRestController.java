package microservices.projects.api.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import taskio.common.dto.authentication.message.ResponseMessage;
import taskio.common.dto.projects.CreateRequest;
import taskio.common.mapping.ObjectMapperWrapper;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rest/api/v1/projects")
public class ProjectsRestController {
    private final Logger logger = LoggerFactory.getLogger(ProjectsRestController.class);
    private final ObjectMapperWrapper objectMapper;
    private final ProjectsRestService projectsService;

    @Value("${request.auth-header-name}")
    private String authenticationHeaderName;

    @PostMapping("/create")
    public ResponseEntity<?> create(HttpServletRequest request, @RequestBody CreateRequest createRequest) {
        logger.info("POST /create request received with data:\n{}",
                objectMapper.toPrettyJson(request));

        String bearerTokenHeader = request.getHeader(authenticationHeaderName);
        if (projectsService.create(createRequest, bearerTokenHeader)) {
            return ResponseEntity.ok(ResponseMessage
                .withMessage("Successfully created project!"));
        }

        return ResponseEntity.badRequest().body(ResponseMessage
                .withMessage("Can not create project. Expired or invalid access token"));
    }
}
