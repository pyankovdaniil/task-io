package microservices.projects.api.rest;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import microservices.projects.clients.AuthenticationClient;
import microservices.projects.project.ProjectMemberRepository;
import microservices.projects.project.ProjectRepository;
import taskio.common.dto.projects.create.CreateRequest;
import taskio.common.exceptions.user.UserAlreadyCreatedProjectException;
import taskio.common.exceptions.user.UserNotFoundException;
import taskio.common.model.authentication.User;
import taskio.common.model.projects.Project;
import taskio.common.model.projects.ProjectMember;

@Service
@RequiredArgsConstructor
public class ProjectsRestService {
    private final AuthenticationClient authenticationClient;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Value("${request.auth-header-prefix}")
    private String authHeaderPrefix;

    public void create(CreateRequest request, String bearerToken) {
        User user = getUserData(bearerToken);

        ProjectMember creator = ProjectMember.builder()
                .user(user)
                .role("Creator")
                .build();

        Project newProject = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .creationDate(new Date(System.currentTimeMillis()))
                .members(List.of(creator))
                .build();

        List<ProjectMember> userMemberships = projectMemberRepository.findAllByUser(user);
        if (!userMemberships.isEmpty()) {
            for (ProjectMember membership : userMemberships) {
                if (membership.getRole().equals("Creator")
                        && membership.getProject().getName().equals(request.getName())) {
                    throw new UserAlreadyCreatedProjectException("You already have created project with this name");
                }
            }
        }

        projectMemberRepository.save(creator);
        projectRepository.save(newProject);

        creator.setProject(newProject);
        projectMemberRepository.save(creator);
    }

    private User getUserData(String bearerToken) {
        try {
            User user = authenticationClient.getUserData(bearerToken);
            return user;
        } catch (FeignException exception) {
            throw new UserNotFoundException("Invalid or expired access token");
        }
    }
}
