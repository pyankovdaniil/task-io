package taskio.microservices.projects.api.rest;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
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
import taskio.common.dto.projects.makeadmin.MakeAdminRequest;
import taskio.common.exceptions.projects.*;
import taskio.common.exceptions.user.UserNotFoundException;
import taskio.common.model.authentication.User;
import taskio.common.model.projects.Project;
import taskio.common.model.projects.ProjectMember;
import taskio.common.model.projects.ProjectMemberNotVerified;
import taskio.common.model.projects.ProjectMemberRole;
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
import java.util.concurrent.TimeUnit;

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

    @Value("${not-verified-project-member-expire-time-seconds}")
    private int notVerifiedProjectMemberExpireTimeSeconds;

    private final AuthenticationClient authenticationClient;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CustomCompositeUuidGenerator uuidGenerator;
    private final ProjectMemberNotVerifiedRepository projectMemberNotVerifiedRepository;
    private final VerificationCodeGenerator verificationCodeGenerator;
    private final RabbitMQMessageProducer messageProducer;
    private final MongoOperations mongoOps;

    @Override
    public void create(CreateRequest request, String bearerToken) {
        User creator = getUserData(bearerToken);

        ProjectMember creatorMember = ProjectMember.builder()
                .id(uuidGenerator.generateCustomUuid(ProjectMember.class.getSimpleName()))
                .user(creator)
                .role(ProjectMemberRole.CREATOR)
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

        projectMemberRepository.save(creatorMember);
        projectRepository.save(newProject);

        creatorMember.setProject(newProject);
        projectMemberRepository.save(creatorMember);
    }

    @Override
    public void changeProjectIdentifier(ChangeProjectIdentifierRequest request, String bearerToken) {
        User changer = getUserData(bearerToken);
        Optional<ProjectMember> changerMembership = checkUserInProjectAndGetMembership(changer, request.getProjectIdentifier());

        if (changerMembership.isEmpty()) {
            throw UserIsNotInProjectException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You are not a member of this project!")
                    .errorCode(ErrorCode.USER_IS_NOT_IN_PROJECT)
                    .dataCausedError(request)
                    .build();
        }

        if (!changerMembership.get().getProject().getCreatorEmail().equals(changer.getEmail())) {
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

        changerMembership.get().getProject().setProjectIdentifier(request.getNewProjectIdentifier());
        projectRepository.save(changerMembership.get().getProject());
    }

    @Override
    public void invite(InviteRequest request, String bearerToken) {
        User inviter = getUserData(bearerToken);
        Optional<ProjectMember> inviterMembership = checkUserInProjectAndGetMembership(inviter, request.getProjectIdentifier());

        if (inviterMembership.isEmpty()) {
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

        Optional<ProjectMember> userToInviteMembership = checkUserInProjectAndGetMembership(userToInvite,
                request.getProjectIdentifier());

        if (userToInviteMembership.isPresent()) {
            throw UserAlreadyInProjectException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You have already in project with this name")
                    .errorCode(ErrorCode.USER_ALREADY_IN_PROJECT)
                    .dataCausedError(request)
                    .build();
        }

        String confirmInviteVerificationCode = verificationCodeGenerator
                .generateVerificationCode(confirmInviteVerificationCodeLength).toUpperCase();

        ProjectMemberNotVerified newProjectMemberNotVerified = ProjectMemberNotVerified.builder()
                .id(uuidGenerator.generateCustomUuid(ProjectMemberNotVerified.class.getSimpleName()))
                .user(userToInvite)
                .role(ProjectMemberRole.MEMBER)
                .verificationCode(confirmInviteVerificationCode)
                .project(inviterMembership.get().getProject())
                .createdAt(Date.from(Instant.now()))
                .build();

        log.info("Created invite confirmation code: {}", confirmInviteVerificationCode);

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .toEmail(newProjectMemberNotVerified.getUser().getEmail())
                .subject("Your task.io email verification code!")
                .text("Dear " + newProjectMemberNotVerified.getUser().getFullName() +
                        "!\n" + inviter.getFullName() + "<" + inviter.getEmail() + "> was invited you to a project: " +
                        inviterMembership.get().getProject().getName() + ".\nIf you want to join this project, here" +
                        " is you confirmation code: " + confirmInviteVerificationCode)
                .build();

        messageProducer.publish(notificationRequest, rabbitExchange, rabbitRoutingKey);

        mongoOps.indexOps(ProjectMemberNotVerified.class).ensureIndex(new Index()
                .named("project_member_not_verified_expire_time_seconds_index")
                .on("createdAt", Sort.Direction.ASC)
                .expire(notVerifiedProjectMemberExpireTimeSeconds, TimeUnit.SECONDS));

        projectMemberNotVerifiedRepository.save(newProjectMemberNotVerified);
    }

    @Override
    public void confirmInvite(ConfirmInviteRequest request) {
        User notVerifiedInvitedUser = authenticationClient.getUserData(UserDataFromEmailRequest.builder()
                .email(request.getEmail())
                .build());

        Optional<ProjectMemberNotVerified> userInvitation =
                checkUserInProjectAndGetNotVerifiedMembership(notVerifiedInvitedUser, request.getProjectIdentifier());

        if (userInvitation.isEmpty()) {
            throw UserWasNotInvitedToProjectException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You were not invited to a project that you try to confirm!")
                    .errorCode(ErrorCode.USER_WAS_NOT_INVITED_TO_PROJECT)
                    .dataCausedError(request)
                    .build();
        }

        if (!userInvitation.get().getVerificationCode().equals(request.getInviteConfirmationCode())) {
            throw InvalidInviteConfirmationCodeException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You were not invited to a project that you try to confirm!")
                    .errorCode(ErrorCode.INVALID_INVITE_CONFIRMATION_CODE)
                    .dataCausedError(request)
                    .build();
        }

        ProjectMember newProjectMember = ProjectMember.builder()
                .id(uuidGenerator.generateCustomUuid(ProjectMember.class.getSimpleName()))
                .user(notVerifiedInvitedUser)
                .role(ProjectMemberRole.MEMBER)
                .project(userInvitation.get().getProject())
                .build();

        projectMemberNotVerifiedRepository.delete(userInvitation.get());
        projectMemberRepository.save(newProjectMember);

        userInvitation.get().getProject().getMembers().add(newProjectMember);
        projectRepository.save(userInvitation.get().getProject());
    }

    @Override
    public void makeAdmin(MakeAdminRequest request, String bearerToken) {
        User adminSetter = getUserData(bearerToken);
        Optional<ProjectMember> adminSetterMembership =
                checkUserInProjectAndGetMembership(adminSetter, request.getProjectIdentifier());

        if (adminSetterMembership.isEmpty()) {
            throw UserIsNotInProjectException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You are not a member of a project you try make admin in!")
                    .errorCode(ErrorCode.USER_IS_NOT_IN_PROJECT)
                    .dataCausedError(request)
                    .build();
        }

        if (!(adminSetterMembership.get().getRole().equals(ProjectMemberRole.CREATOR) ||
                adminSetterMembership.get().getRole().equals(ProjectMemberRole.ADMIN))) {
            throw UserIsNotAllowedToSetAdminException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You are not creator or an admin, so you can not set admins in this project!")
                    .errorCode(ErrorCode.USER_IS_NOT_ALLOWED_TO_SET_ADMIN)
                    .dataCausedError(request)
                    .build();
        }

        User newAdmin = authenticationClient.getUserData(UserDataFromEmailRequest.builder()
                .email(request.getNewAdminEmail())
                .build());

        Optional<ProjectMember> newAdminMembership =
                checkUserInProjectAndGetMembership(newAdmin, request.getProjectIdentifier());

        if (newAdminMembership.isEmpty()) {
            throw UserIsNotInProjectException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("The use you try to set admin is not a member of this project!")
                    .errorCode(ErrorCode.USER_IS_NOT_IN_PROJECT)
                    .dataCausedError(request)
                    .build();
        }

        if (newAdminMembership.get().getRole().equals(ProjectMemberRole.ADMIN)) {
            throw UserIsAlreadyAdminException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage(request.getNewAdminEmail() + " is already admin in this project!")
                    .errorCode(ErrorCode.USER_IS_ALREADY_ADMIN)
                    .dataCausedError(request)
                    .build();
        }

        newAdminMembership.get().setRole(ProjectMemberRole.ADMIN);
        projectMemberRepository.save(newAdminMembership.get());
    }

    @Override
    public void leaveProject(LeaveProjectRequest request, String bearerToken) {
        User leavingUser = getUserData(bearerToken);
        Optional<ProjectMember> leavingUserMembership = checkUserInProjectAndGetMembership(leavingUser, request.getProjectIdentifier());

        if (leavingUserMembership.isEmpty()) {
            throw UserIsNotInProjectException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You are not a member of a project you try to leave!")
                    .errorCode(ErrorCode.USER_IS_NOT_IN_PROJECT)
                    .dataCausedError(request)
                    .build();
        }

        List<ProjectMember> remainingMembers = leavingUserMembership.get().getProject().getMembers();
        long adminsCount = remainingMembers.stream().filter(member -> member.getRole().equals(ProjectMemberRole.ADMIN)).count();
        boolean isCreatorInProject = remainingMembers.stream().anyMatch(member -> member.getRole().equals(ProjectMemberRole.CREATOR));

        if ((leavingUserMembership.get().getRole().equals(ProjectMemberRole.CREATOR) && adminsCount < 1) ||
                (leavingUserMembership.get().getRole().equals(ProjectMemberRole.ADMIN) && (adminsCount < 1 && !isCreatorInProject))) {
            throw UserCanNotLeaveException.builder()
                    .errorDate(Date.from(Instant.now()))
                    .errorMessage("You can not leave, because in project will be no admin!")
                    .errorCode(ErrorCode.USER_CAN_NOT_LEAVE)
                    .dataCausedError(request)
                    .build();
        }

        remainingMembers.remove(leavingUserMembership.get());

        if (remainingMembers.isEmpty()) {
            projectRepository.delete(leavingUserMembership.get().getProject());
        } else {
            projectRepository.save(leavingUserMembership.get().getProject());
        }

        projectMemberRepository.delete(leavingUserMembership.get());
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

    private Optional<ProjectMember> checkUserInProjectAndGetMembership(User user, String projectIdentifier) {
        for (ProjectMember membership : projectMemberRepository.findAllByUser(user)) {
            if (membership.getProject().getProjectIdentifier().equals(projectIdentifier)) {
                return Optional.of(membership);
            }
        }

        return Optional.empty();
    }

    private Optional<ProjectMemberNotVerified> checkUserInProjectAndGetNotVerifiedMembership(User user, String projectIdentifier) {
        for (ProjectMemberNotVerified membership : projectMemberNotVerifiedRepository.findAllByUser(user)) {
            if (membership.getProject().getProjectIdentifier().equals(projectIdentifier)) {
                return Optional.of(membership);
            }
        }

        return Optional.empty();
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
