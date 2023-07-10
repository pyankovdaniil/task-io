package taskio.microservices.projects.api.rest;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import taskio.common.dto.authentication.userdata.UserDataFromEmailRequest;
import taskio.common.dto.projects.create.CreateRequest;
import taskio.common.dto.projects.invite.InviteRequest;
import taskio.common.exceptions.user.InviterIsNotMemberException;
import taskio.common.exceptions.user.UserAlreadyCreatedProjectException;
import taskio.common.exceptions.user.UserAlreadyInProjectException;
import taskio.common.exceptions.user.UserNotFoundException;
import taskio.common.model.authentication.User;
import taskio.common.model.projects.Project;
import taskio.common.model.projects.ProjectMember;
import taskio.common.uuid.CustomCompositeUuidGenerator;
import taskio.microservices.projects.clients.AuthenticationClient;
import taskio.microservices.projects.project.ProjectMemberRepository;
import taskio.microservices.projects.project.ProjectRepository;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectsRestService implements ProjectsService {
    private final AuthenticationClient authenticationClient;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CustomCompositeUuidGenerator uuidGenerator;

    @Override
    public void create(CreateRequest request, String bearerToken) {
        User user = getUserData(bearerToken);

        ProjectMember creator = ProjectMember.builder()
                .id(uuidGenerator.generateCustomUuid(ProjectMember.class.getSimpleName()))
                .user(user)
                .role("Creator")
                .build();

        Project newProject = Project.builder()
                .id(uuidGenerator.generateCustomUuid(Project.class.getSimpleName()))
                .name(request.getProjectName())
                .description(request.getProjectDescription())
                .creationDate(new Date(System.currentTimeMillis()))
                .members(List.of(creator))
                .build();

        List<ProjectMember> userMemberships = projectMemberRepository.findAllByUser(user);
        for (ProjectMember membership : userMemberships) {
            if (membership.getRole().equals("Creator")
                    && membership.getProject().getName().equals(request.getProjectName())) {
                throw new UserAlreadyCreatedProjectException("You already have created project with this name");
            }
        }

        projectMemberRepository.save(creator);
        projectRepository.save(newProject);

        creator.setProject(newProject);
        projectMemberRepository.save(creator);
    }

    @Override
    public void invite(InviteRequest request, String bearerToken) {
        User inviter = getUserData(bearerToken);
        List<ProjectMember> inviterMemberships = projectMemberRepository.findAllByUser(inviter);

        Project projectToInvite = null;
        boolean isInviterMember = false;

        for (ProjectMember inviterMembership : inviterMemberships) {
            if (inviterMembership.getProject().getName().equals(request.getProjectName())) {
                projectToInvite = inviterMembership.getProject();
                isInviterMember = true;
                break;
            }
        }

        if (!isInviterMember || projectToInvite == null) {
            throw new InviterIsNotMemberException("You are not a member of that project," +
                    " so you can not invite people here!");
        }

        User userToInvite = authenticationClient.getUserData(UserDataFromEmailRequest.builder()
                .email(request.getInvitedPersonEmail())
                .build());

        List<ProjectMember> userToInviteMemberships = projectMemberRepository.findAllByUser(userToInvite);
        for (ProjectMember userToInviteMembership : userToInviteMemberships) {
            if (userToInviteMembership.getProject().getName().equals(request.getProjectName())) {
                throw new UserAlreadyInProjectException("User with this email is already in this project!");
            }
        }

        ProjectMember newProjectMember = ProjectMember.builder()
                .id(uuidGenerator.generateCustomUuid(ProjectMember.class.getSimpleName()))
                .user(userToInvite)
                .role("Member")
                .project(projectToInvite)
                .build();

        projectToInvite.getMembers().add(newProjectMember);
        projectMemberRepository.save(newProjectMember);
        projectRepository.save(projectToInvite);
    }

    private User getUserData(String bearerToken) {
        try {
            return authenticationClient.getUserData(bearerToken);
        } catch (FeignException exception) {
            throw new UserNotFoundException("Invalid or expired access token");
        }
    }
}
