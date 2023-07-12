package taskio.microservices.projects.api.rest;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import taskio.common.dto.authentication.userdata.UserDataFromEmailRequest;
import taskio.common.dto.errors.logic.ErrorCode;
import taskio.common.dto.notification.NotificationRequest;
import taskio.common.dto.projects.confirminvite.ConfirmInviteRequest;
import taskio.common.dto.projects.create.CreateRequest;
import taskio.common.dto.projects.id.ChangeProjectIdentifierRequest;
import taskio.common.dto.projects.invite.InviteRequest;
import taskio.common.dto.projects.leave.LeaveProjectRequest;
import taskio.common.dto.projects.list.ProjectsListResponse;
import taskio.common.dto.projects.list.SimpleProjectMembership;
import taskio.common.exceptions.projects.ProjectIdentifierIsTakenException;
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

import java.time.Instant;
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
        User creator = getUserData(bearerToken);

        ProjectMember creatorMember = ProjectMember.builder()
                .id(uuidGenerator.generateCustomUuid(ProjectMember.class.getSimpleName()))
                .user(creator)
                .role("Creator")
                .build();

        String newProjectId = uuidGenerator.generateCustomUuid(Project.class.getSimpleName());
        Project newProject = Project.builder()
                .id(newProjectId)
                .projectIdentifier(newProjectId)
                .name(request.getProjectName())
                .description(request.getProjectDescription())
                .creatorEmail(creator.getEmail())
                .creationDate(Date.from(Instant.now()))
                .members(List.of(creatorMember))
                .build();

        List<ProjectMember> creatorMemberships = projectMemberRepository.findAllByUser(creator);
        for (ProjectMember membership : creatorMemberships) {
            if (membership.getRole().equals("Creator")
                    && membership.getProject().getName().equals(request.getProjectName())) {
                throw UserAlreadyCreatedProjectException.builder()
                        .errorDate(Date.from(Instant.now()))
                        .errorMessage("You have already created project with this name")
                        .errorCode(ErrorCode.USER_ALREADY_CREATED_PROJECT_WITH_THIS_NAME)
                        .dataCausedError(request)
                        .build();
            }
        }

        projectMemberRepository.save(creatorMember);
        projectRepository.save(newProject);

        creatorMember.setProject(newProject);
        projectMemberRepository.save(creatorMember);
    }

    @Override
    public void changeProjectIdentifier(ChangeProjectIdentifierRequest request, String bearerToken) {
        User changer = getUserData(bearerToken);
        Optional<Project> projectToChangeIdentifier = Optional.empty();

        List<ProjectMember> changerMemberships = projectMemberRepository.findAllByUser(changer);
        for (ProjectMember membership : changerMemberships) {
            if (membership.getProject().getProjectIdentifier().equals(request.getProjectIdentifier())) {
                projectToChangeIdentifier = Optional.of(membership.getProject());
            }
        }

        if (projectToChangeIdentifier.isEmpty()) {
            throw UserIsNotInProjectException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You are not a member of this project!")
                    .errorCode(ErrorCode.USER_IS_NOT_IN_PROJECT)
                    .dataCausedError(request)
                    .build();
        }

        if (!projectToChangeIdentifier.get().getCreatorEmail().equals(changer.getEmail())) {
            throw UserIsNotCreatorException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You are not a creator of that project, so" +
                            " you can't change its identifier!")
                    .errorCode(ErrorCode.USER_IS_NOT_CREATOR)
                    .dataCausedError(request)
                    .build();
        }

        Optional<Project> checkProjectIdentifier = projectRepository
                .findProjectByProjectIdentifier(request.getNewProjectIdentifier());

        if (checkProjectIdentifier.isPresent()) {
            throw ProjectIdentifierIsTakenException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("Sorry, this project identifier is already taken," +
                            " try another one!")
                    .errorCode(ErrorCode.PROJECT_IDENTIFIER_IS_TAKEN)
                    .dataCausedError(request)
                    .build();
        }

        projectToChangeIdentifier.get().setProjectIdentifier(request.getNewProjectIdentifier());
        projectRepository.save(projectToChangeIdentifier.get());
    }

    @Override
    public void invite(InviteRequest request, String bearerToken) {
        User inviter = getUserData(bearerToken);
        List<ProjectMember> inviterMemberships = projectMemberRepository.findAllByUser(inviter);

        Optional<Project> projectToInvite = Optional.empty();
        boolean isInviterMember = false;

        for (ProjectMember inviterMembership : inviterMemberships) {
            if (inviterMembership.getProject().getProjectIdentifier().equals(request.getProjectIdentifier())) {
                projectToInvite = Optional.of(inviterMembership.getProject());
                isInviterMember = true;
                break;
            }
        }

        if (!isInviterMember) {
            throw InviterIsNotMemberException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You are not a member of that project," +
                            " so you can not invite people here!")
                    .errorCode(ErrorCode.INVITER_IS_NOT_MEMBER)
                    .dataCausedError(request)
                    .build();
        }

        User userToInvite = authenticationClient.getUserData(UserDataFromEmailRequest.builder()
                .email(request.getInvitedPersonEmail())
                .build());

        List<ProjectMember> userToInviteMemberships = projectMemberRepository.findAllByUser(userToInvite);
        for (ProjectMember userToInviteMembership : userToInviteMemberships) {
            if (userToInviteMembership.getProject().getProjectIdentifier().equals(request.getProjectIdentifier())) {
                throw UserAlreadyInProjectException.builder()
                        .errorDate(Date.from(Instant.now()))
                        .errorMessage("You have already in project with this name")
                        .errorCode(ErrorCode.USER_ALREADY_IN_PROJECT)
                        .dataCausedError(request)
                        .build();
            }
        }

        ProjectMemberNotVerified newProjectMemberNotVerified = ProjectMemberNotVerified.builder()
                .id(uuidGenerator.generateCustomUuid(ProjectMemberNotVerified.class.getSimpleName()))
                .user(userToInvite)
                .role("Member")
                .project(projectToInvite.get())
                .createdAt(Date.from(Instant.now()))
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
            if (userInvitation.getProject().getProjectIdentifier().equals(request.getProjectIdentifier())) {
                projectToInvite = Optional.of(userInvitation.getProject());
                projectMemberNotVerified = Optional.of(userInvitation);
                wasUserInvited = true;
                break;
            }
        }

        if (!wasUserInvited) {
            throw UserWasNotInvitedToProjectException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You were not invited to a project that you try to confirm!")
                    .errorCode(ErrorCode.USER_WAS_NOT_INVITED_TO_PROJECT)
                    .dataCausedError(request)
                    .build();
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
    public void leaveProject(LeaveProjectRequest request, String bearerToken) {
        User user = getUserData(bearerToken);
        List<ProjectMember> memberships = projectMemberRepository.findAllByUser(user);

        Optional<Project> projectToLeave = Optional.empty();
        Optional<ProjectMember> projectMemberToDelete = Optional.empty();
        boolean isUserInProject = false;

        for (ProjectMember membership : memberships) {
            if (membership.getProject().getProjectIdentifier().equals(request.getProjectIdentifier())) {
                projectToLeave = Optional.of(membership.getProject());
                projectMemberToDelete = Optional.of(membership);
                isUserInProject = true;
                break;
            }
        }

        if (!isUserInProject) {
            throw UserIsNotInProjectException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You are not a member of a project you try to leave!")
                    .errorCode(ErrorCode.USER_IS_NOT_IN_PROJECT)
                    .dataCausedError(request)
                    .build();
        }

        List<ProjectMember> remainingMembers = projectToLeave.get().getMembers();
        remainingMembers.remove(projectMemberToDelete.get());

        if (remainingMembers.isEmpty()) {
            projectRepository.delete(projectToLeave.get());
        } else {
            projectRepository.save(projectToLeave.get());
        }

        projectMemberRepository.delete(projectMemberToDelete.get());
    }

    @Override
    public ProjectsListResponse getAllProjects(String bearerToken) {
        User user = getUserData(bearerToken);
        List<ProjectMember> memberships = projectMemberRepository.findAllByUser(user);

        List<SimpleProjectMembership> simpleProjectMemberships = new ArrayList<>();
        for (ProjectMember membership : memberships) {
            simpleProjectMemberships.add(SimpleProjectMembership.builder()
                    .projectIdentifier(membership.getProject().getProjectIdentifier())
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
            throw UserNotFoundException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("User with this email is not in database")
                    .errorCode(ErrorCode.USER_NOT_FOUND_BY_EMAIL_IN_DATABASE)
                    .dataCausedError(bearerToken)
                    .build();
        }
    }
}
