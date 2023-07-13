package taskio.microservices.projects.api.rest;

import org.springframework.stereotype.Service;
import taskio.common.dto.projects.confirminvite.ConfirmInviteRequest;
import taskio.common.dto.projects.create.CreateRequest;
import taskio.common.dto.projects.id.ChangeProjectIdentifierRequest;
import taskio.common.dto.projects.invite.InviteRequest;
import taskio.common.dto.projects.leave.LeaveProjectRequest;
import taskio.common.dto.projects.list.ProjectsListResponse;
import taskio.common.dto.projects.makeadmin.MakeAdminRequest;

@Service
public interface ProjectsService {
    void create(CreateRequest request, String bearerToken);
    void changeProjectIdentifier(ChangeProjectIdentifierRequest request, String bearerToken);
    void invite(InviteRequest request, String bearerToken);
    void confirmInvite(ConfirmInviteRequest request);
    void makeAdmin(MakeAdminRequest request, String bearerToken);
    void leaveProject(LeaveProjectRequest request, String bearerToken);
    ProjectsListResponse getAllProjects(String bearerToken);
}
