package taskio.microservices.projects.api.rest;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import taskio.common.dto.authentication.userdata.UserDataFromEmailRequest;
import taskio.common.dto.notification.NotificationRequest;
import taskio.common.dto.projects.confirminvite.ConfirmInviteRequest;
import taskio.common.dto.projects.create.CreateRequest;
import taskio.common.dto.projects.invite.InviteRequest;
import taskio.common.dto.projects.list.ProjectsListResponse;
import taskio.common.dto.projects.list.SimpleProjectMembership;
import taskio.common.exceptions.user.*;
import taskio.common.model.authentication.User;
import taskio.common.model.projects.Project;
import taskio.common.model.projects.ProjectMember;
import taskio.common.model.projects.ProjectMemberNotVerified;
import taskio.common.uuid.CustomCompositeUuidGenerator;
import taskio.common.verification.VerificationCodeGenerator;
import taskio.configs.amqp.RabbitMQMessageProducer;
import taskio.microservices.projects.clients.AuthenticationClient;
import taskio.microservices.projects.project.ProjectMemberNotVerifiedRepository;
import taskio.microservices.projects.project.ProjectMemberRepository;
import taskio.microservices.projects.project.ProjectRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectsRestService implements ProjectsService {
    @Value("${projects.invite.verification-code-length}")
    private int confirmInviteVerificationCodeLength;

    @Value("${rabbitmq.exchanges.taskio-internal}")
    private String rabbitExchange;

    @Value("${rabbitmq.routing-keys.internal-notification}")
    private String rabbitRoutingKey;

    private final AuthenticationClient authenticationClient;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CustomCompositeUuidGenerator uuidGenerator;
    private final ProjectMemberNotVerifiedRepository projectMemberNotVerifiedRepository;
    private final VerificationCodeGenerator verificationCodeGenerator;
    private final RabbitMQMessageProducer messageProducer;

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

        Optional<Project> projectToInvite = Optional.empty();
        boolean isInviterMember = false;

        for (ProjectMember inviterMembership : inviterMemberships) {
            if (inviterMembership.getProject().getName().equals(request.getProjectName())) {
                projectToInvite = Optional.of(inviterMembership.getProject());
                isInviterMember = true;
                break;
            }
        }

        if (!isInviterMember) {
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

        ProjectMemberNotVerified newProjectMemberNotVerified = ProjectMemberNotVerified.builder()
                .id(uuidGenerator.generateCustomUuid(ProjectMemberNotVerified.class.getSimpleName()))
                .user(userToInvite)
                .role("Member")
                .project(projectToInvite.get())
                .createdAt(new Date(System.currentTimeMillis()))
                .build();

        String confirmInviteVerificationCode = verificationCodeGenerator
                .generateVerificationCode(confirmInviteVerificationCodeLength).toUpperCase();

        log.info("Created invite confirmation code: {}", confirmInviteVerificationCode);

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .toEmail(newProjectMemberNotVerified.getUser().getEmail())
                .subject("Your task.io email verification code!")
                .text("Dear " + newProjectMemberNotVerified.getUser().getFullName() +
                        "!\n" + inviter.getFullName() + "<" + inviter.getEmail() + "> was invited you to a project: " +
                        projectToInvite.get().getName() + ".\nIf you want to join this project, here" +
                        " is you confirmation code: " + confirmInviteVerificationCode)
                .build();

        messageProducer.publish(notificationRequest, rabbitExchange, rabbitRoutingKey);
        projectMemberNotVerifiedRepository.save(newProjectMemberNotVerified);
    }

    @Override
    public void confirmInvite(ConfirmInviteRequest request) {
        User notVerifiedInvitedUser = authenticationClient.getUserData(UserDataFromEmailRequest.builder()
                .email(request.getEmail())
                .build());

        List<ProjectMemberNotVerified> userInvitations = projectMemberNotVerifiedRepository
                .findAllByUser(notVerifiedInvitedUser);

        Optional<Project> projectToInvite = Optional.empty();
        Optional<ProjectMemberNotVerified> projectMemberNotVerified = Optional.empty();
        boolean wasUserInvited = false;

        for (ProjectMemberNotVerified userInvitation : userInvitations) {
            if (userInvitation.getProject().getName().equals(request.getProjectName())) {
                projectToInvite = Optional.of(userInvitation.getProject());
                projectMemberNotVerified = Optional.of(userInvitation);
                wasUserInvited = true;
                break;
            }
        }

        if (!wasUserInvited) {
            throw new UserWasNotInvitedToProjectException("You were not invited to a project that you try to confirm!");
        }

        ProjectMember newProjectMember = ProjectMember.builder()
                .id(uuidGenerator.generateCustomUuid(ProjectMember.class.getSimpleName()))
                .user(notVerifiedInvitedUser)
                .role("Member")
                .project(projectToInvite.get())
                .build();

        projectMemberNotVerifiedRepository.delete(projectMemberNotVerified.get());
        projectMemberRepository.save(newProjectMember);

        projectToInvite.get().getMembers().add(newProjectMember);
        projectRepository.save(projectToInvite.get());
    }

    @Override
    public ProjectsListResponse getAllProjects(String bearerToken) {
        User user = getUserData(bearerToken);
        List<ProjectMember> memberships = projectMemberRepository.findAllByUser(user);

        List<SimpleProjectMembership> simpleProjectMemberships = new ArrayList<>();
        for (ProjectMember membership : memberships) {
            simpleProjectMemberships.add(SimpleProjectMembership.builder()
                    .projectName(membership.getProject().getName())
                    .roleInProject(membership.getRole())
                    .build());
        }

        return ProjectsListResponse.builder()
                .numberOfProjects(simpleProjectMemberships.size())
                .memberships(simpleProjectMemberships)
                .build();
    }

    private User getUserData(String bearerToken) {
        try {
            return authenticationClient.getUserData(bearerToken);
        } catch (FeignException exception) {
            throw new UserNotFoundException("Invalid or expired access token");
        }
    }
}
