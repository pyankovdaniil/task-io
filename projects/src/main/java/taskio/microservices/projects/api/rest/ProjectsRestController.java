package taskio.microservices.projects.api.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import taskio.common.dto.authentication.message.ResponseMessage;
import taskio.common.dto.projects.confirminvite.ConfirmInviteRequest;
import taskio.common.dto.projects.create.CreateRequest;
import taskio.common.dto.projects.id.ChangeProjectIdentifierRequest;
import taskio.common.dto.projects.invite.InviteRequest;
import taskio.common.dto.projects.leave.LeaveProjectRequest;
import taskio.common.dto.projects.list.ProjectsListResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rest/v1/projects")
public class ProjectsRestController {
    private final ProjectsService projectsService;

    @PostMapping("/create")
    public ResponseMessage create(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
                                  @Valid @RequestBody CreateRequest request) {
        projectsService.create(request, bearerToken);
        return ResponseMessage.withMessage("Successfully created project");
    }

    @PostMapping("/change-identifier")
    public ResponseMessage changeIdentifier(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
                                            @Valid @RequestBody ChangeProjectIdentifierRequest request) {
        projectsService.changeProjectIdentifier(request, bearerToken);
        return ResponseMessage.withMessage("Successfully changed project indentifier to " +
                request.getNewProjectIdentifier());
    }

    @PostMapping("/invite")
    public ResponseMessage invite(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
                                  @Valid @RequestBody InviteRequest request) {
        projectsService.invite(request, bearerToken);
        return ResponseMessage.withMessage("Successfully send confirm invite to " + request.getInvitedPersonEmail() +
                "! Now he can confirm this invitation from his email!");
    }

    @PostMapping("/confirm-invite")
    public ResponseMessage confirmInvite(@Valid @RequestBody ConfirmInviteRequest request) {
        projectsService.confirmInvite(request);
        return ResponseMessage.withMessage("You were successfully invited to a project!");
    }

    @PostMapping("/leave")
    public ResponseMessage leave(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
                                 @Valid @RequestBody LeaveProjectRequest request) {
        projectsService.leaveProject(request, bearerToken);
        return ResponseMessage.withMessage("You have successfully left the project!");
    }

    @PostMapping("/list")
    public ProjectsListResponse getAllProjects(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        return projectsService.getAllProjects(bearerToken);
    }
}
