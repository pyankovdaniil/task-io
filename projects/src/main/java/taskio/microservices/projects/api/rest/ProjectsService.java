package taskio.microservices.projects.api.rest;

import org.springframework.stereotype.Service;
import taskio.common.dto.projects.confirmdelete.ConfirmDeleteRequest;
import taskio.common.dto.projects.confirminvite.ConfirmInviteRequest;
import taskio.common.dto.projects.create.CreateRequest;
import taskio.common.dto.projects.delete.DeleteProjectRequest;
import taskio.common.dto.projects.id.ChangeProjectIdentifierRequest;
import taskio.common.dto.projects.invite.InviteRequest;
import taskio.common.dto.projects.leave.LeaveProjectRequest;
import taskio.common.dto.projects.list.ProjectsListResponse;
import taskio.common.dto.projects.makeadmin.MakeAdminRequest;

@Service
public interface ProjectsService {
    void create(CreateRequest request, String bearerToken);
    void changeIdentifier(ChangeProjectIdentifierRequest request, String bearerToken);
    void delete(DeleteProjectRequest request, String bearerToken);
    void confirmDelete(ConfirmDeleteRequest request);
    void invite(InviteRequest request, String bearerToken);
    void confirmInvite(ConfirmInviteRequest request);
    void makeAdmin(MakeAdminRequest request, String bearerToken);
    void leave(LeaveProjectRequest request, String bearerToken);
    ProjectsListResponse list(String bearerToken);
}
