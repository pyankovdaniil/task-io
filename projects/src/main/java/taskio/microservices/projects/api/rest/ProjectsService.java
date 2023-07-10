package taskio.microservices.projects.api.rest;

import org.springframework.stereotype.Service;
import taskio.common.dto.projects.create.CreateRequest;
import taskio.common.dto.projects.invite.InviteRequest;

@Service
public interface ProjectsService {
    void create(CreateRequest request, String bearerToken);
    void invite(InviteRequest request, String bearerToken);
}
