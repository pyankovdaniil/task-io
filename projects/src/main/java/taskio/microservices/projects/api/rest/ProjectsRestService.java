package taskio.microservices.projects.api.rest;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taskio.common.dto.projects.create.CreateRequest;
import taskio.common.exceptions.user.UserAlreadyCreatedProjectException;
import taskio.common.exceptions.user.UserNotFoundException;
import taskio.common.model.authentication.User;
import taskio.common.model.projects.Project;
import taskio.common.model.projects.ProjectMember;
import taskio.microservices.projects.clients.AuthenticationClient;
import taskio.microservices.projects.project.ProjectMemberRepository;
import taskio.microservices.projects.project.ProjectRepository;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectsRestService {
    private final AuthenticationClient authenticationClient;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

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
            return authenticationClient.getUserData(bearerToken);
        } catch (FeignException exception) {
            throw new UserNotFoundException("Invalid or expired access token");
        }
    }
}
