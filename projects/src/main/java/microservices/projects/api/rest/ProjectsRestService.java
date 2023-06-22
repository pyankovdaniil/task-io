package microservices.projects.api.rest;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import microservices.projects.clients.AuthenticationClient;
import microservices.projects.project.Project;
import microservices.projects.project.ProjectRepository;
import taskio.common.dto.authentication.userdata.UserDataRequest;
import taskio.common.dto.authentication.userdata.UserDataResponse;
import taskio.common.dto.projects.CreateRequest;
import taskio.common.mapping.ObjectMapperWrapper;

@Service
@RequiredArgsConstructor
public class ProjectsRestService {
    private final Logger logger = LoggerFactory.getLogger(ProjectsRestController.class);
    private final AuthenticationClient authenticationClient;
    private final ProjectRepository projectRepository;
    private final ObjectMapperWrapper objectMapper;

    @Value("${request.auth-header-prefix}")
    private String authHeaderPrefix;

    public boolean create(CreateRequest request, String bearerToken) {
        try {
            UserDataRequest userDataRequest = UserDataRequest.builder()
                    .accessToken(bearerToken.substring(authHeaderPrefix.length() + 1)).build();

            ResponseEntity<?> userDataResponse = authenticationClient.getUserData(userDataRequest);
            if (userDataResponse.getStatusCode().equals(HttpStatusCode.valueOf(HttpStatus.OK.value()))) {
                if (userDataResponse.getBody() instanceof UserDataResponse userData) {
                    logger.info("Successfully extracted user data:\n{}",
                            objectMapper.toPrettyJson(userData));

                    Project newProject = Project.builder()
                            .name(request.getProjectName())
                            .creatorEmail(userData.getEmail())
                            .creationDate(new Date(System.currentTimeMillis()))
                            .build();

                    logger.info("Now this project will be saved to db:\n{}",
                            objectMapper.toPrettyJson(newProject));

                    projectRepository.save(newProject);
                    logger.info("Successfully saved project:\n{}",
                            objectMapper.toPrettyJson(newProject));

                    return true;
                }
            }

            return false;
        } catch (Exception exception) {
            logger.error("Caught {} with message: {}", exception.getClass().getSimpleName(),
                    exception.getMessage());
            return false;
        }
    }
}
