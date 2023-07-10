package taskio.microservices.projects.api.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taskio.common.dto.authentication.message.ResponseMessage;
import taskio.common.dto.projects.create.CreateRequest;
import taskio.common.dto.projects.invite.InviteRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rest/v1/projects")
public class ProjectsRestController {
    private final ProjectsService projectsService;

    @Value("${request.auth-header-name}")
    private String authenticationHeaderName;

    @PostMapping("/create")
    public ResponseMessage create(HttpServletRequest servletRequest, @Valid @RequestBody CreateRequest request) {
        String bearerTokenHeader = servletRequest.getHeader(authenticationHeaderName);
        projectsService.create(request, bearerTokenHeader);
        return ResponseMessage.withMessage("Successfully created project");
    }

    @PostMapping("/invite")
    public ResponseMessage invite(HttpServletRequest servletRequest, @Valid @RequestBody InviteRequest request) {
        String bearerTokenHeader = servletRequest.getHeader(authenticationHeaderName);
        projectsService.invite(request, bearerTokenHeader);
        return ResponseMessage.withMessage("Successfully invited " + request.getInvitedPersonEmail() + "!");
    }
}
