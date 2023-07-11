package taskio.microservices.projects.api.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import taskio.common.dto.projects.confirminvite.ConfirmInviteRequest;
import taskio.common.dto.projects.create.CreateRequest;
import taskio.common.dto.projects.invite.InviteRequest;
import taskio.common.dto.projects.list.ProjectsListResponse;

@Service
public interface ProjectsService {
    void create(CreateRequest request, String bearerToken);
    void invite(InviteRequest request, String bearerToken);
    void confirmInvite(ConfirmInviteRequest request);
    ProjectsListResponse getAllProjects(String bearerToken);
}
