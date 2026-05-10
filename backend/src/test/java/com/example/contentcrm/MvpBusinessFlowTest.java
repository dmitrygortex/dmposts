package com.example.contentcrm;

import com.example.contentcrm.business.model.enums.ContentType;
import com.example.contentcrm.business.model.enums.ContentUnitStatus;
import com.example.contentcrm.business.model.enums.Platform;
import com.example.contentcrm.business.model.enums.PlatformMode;
import com.example.contentcrm.business.model.enums.PublicationVariantStatus;
import com.example.contentcrm.business.model.enums.Role;
import com.example.contentcrm.business.model.enums.TaskPriority;
import com.example.contentcrm.business.model.enums.TaskStatus;
import com.example.contentcrm.business.model.enums.TaskType;
import com.example.contentcrm.business.service.ApprovalService;
import com.example.contentcrm.business.service.AuthService;
import com.example.contentcrm.business.service.ContentUnitService;
import com.example.contentcrm.business.service.PlatformSettingService;
import com.example.contentcrm.business.service.PublicationService;
import com.example.contentcrm.business.service.TaskService;
import com.example.contentcrm.business.service.UserService;
import com.example.contentcrm.dataaccess.integration.PlatformPublisher;
import com.example.contentcrm.dataaccess.integration.PublishRequest;
import com.example.contentcrm.dataaccess.integration.PublishResult;
import com.example.contentcrm.dataaccess.entity.PublicationVariantEntity;
import com.example.contentcrm.dataaccess.repository.PublicationVariantRepository;
import com.example.contentcrm.dataaccess.repository.UserRepository;
import com.example.contentcrm.security.SecurityUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.contentcrm.presentation.dto.approval.ApprovalDecisionRequest;
import com.example.contentcrm.presentation.dto.approval.ApprovalResponse;
import com.example.contentcrm.presentation.dto.approval.ApprovalSubmitRequest;
import com.example.contentcrm.presentation.dto.auth.AuthResponse;
import com.example.contentcrm.presentation.dto.auth.AuthUserResponse;
import com.example.contentcrm.presentation.dto.auth.RegisterRequest;
import com.example.contentcrm.presentation.dto.content.ContentUnitRequest;
import com.example.contentcrm.presentation.dto.content.ContentUnitResponse;
import com.example.contentcrm.presentation.dto.platform.PlatformSettingResponse;
import com.example.contentcrm.presentation.dto.platform.PlatformSettingUpdateRequest;
import com.example.contentcrm.presentation.dto.publication.ManualCompleteRequest;
import com.example.contentcrm.presentation.dto.publication.PublicationVariantRequest;
import com.example.contentcrm.presentation.dto.publication.PublicationVariantResponse;
import com.example.contentcrm.presentation.dto.publication.PublicationVariantUpdateRequest;
import com.example.contentcrm.presentation.dto.publication.SchedulePublicationRequest;
import com.example.contentcrm.presentation.dto.task.TaskRequest;
import com.example.contentcrm.presentation.dto.task.TaskResponse;
import com.example.contentcrm.presentation.dto.task.TaskStatusRequest;
import com.example.contentcrm.presentation.dto.user.CreateUserRequest;
import com.example.contentcrm.presentation.dto.user.UserResponse;
import com.example.contentcrm.business.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEventsTestExecutionListener;
import org.springframework.test.context.event.EventPublishingTestExecutionListener;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:contentcrm;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.files.upload-dir=${java.io.tmpdir}/content-crm-test-uploads",
        "app.time-zone=Europe/Moscow"
})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(MvpBusinessFlowTest.MastodonPublisherTestConfig.class)
@AutoConfigureMockMvc
@TestExecutionListeners(
        listeners = {
                ServletTestExecutionListener.class,
                DirtiesContextBeforeModesTestExecutionListener.class,
                ApplicationEventsTestExecutionListener.class,
                DependencyInjectionTestExecutionListener.class,
                DirtiesContextTestExecutionListener.class,
                TransactionalTestExecutionListener.class,
                SqlScriptsTestExecutionListener.class,
                EventPublishingTestExecutionListener.class
        },
        mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS
)
class MvpBusinessFlowTest {

    @Autowired
    AuthService authService;

    @Autowired
    UserService userService;

    @Autowired
    ContentUnitService contentUnitService;

    @Autowired
    TaskService taskService;

    @Autowired
    ApprovalService approvalService;

    @Autowired
    PublicationService publicationService;

    @Autowired
    PlatformSettingService platformSettingService;

    @Autowired
    PublicationVariantRepository publicationVariantRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    FakeMastodonPublisher mastodonPublisher;

    @Test
    void firstRegistrationCreatesOwnerAndClosesPublicRegistration() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));

        assertThat(owner.user().role()).isEqualTo(Role.OWNER);
        assertThat(authService.setupStatus().hasUsers()).isTrue();
        assertThat(authService.setupStatus().registrationAvailable()).isFalse();
        assertThatThrownBy(() -> authService.register(new RegisterRequest("second@example.com", "password123", "Второй")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Registration is closed");
    }

    @Test
    void lastOwnerCannotBeDeactivated() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));

        assertThatThrownBy(() -> userService.deactivate(owner.user().id()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("last active OWNER");
    }

    @Test
    void executorCannotAccessOtherExecutorsTask() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        UserResponse executorOne = createUser("executor1@example.com", "Исполнитель 1", Role.EXECUTOR);
        UserResponse executorTwo = createUser("executor2@example.com", "Исполнитель 2", Role.EXECUTOR);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());

        TaskResponse task = taskService.create(new TaskRequest(
                content.id(),
                "Подготовить баннер",
                "Сделать изображение",
                TaskType.DESIGN,
                TaskPriority.HIGH,
                executorOne.id(),
                LocalDateTime.now().plusDays(1)
        ));

        assertThatThrownBy(() -> taskService.getForCurrentUser(task.id(), executorTwo.id()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("own tasks");
    }

    @Test
    void executorCanOpenContentUnitForAssignedTask() throws Exception {
        authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        UserResponse executor = createUser("executor@example.com", "Исполнитель", Role.EXECUTOR);
        ContentUnitResponse content = createDraftContent(manager.id());
        taskService.create(new TaskRequest(
                content.id(),
                "Написать текст",
                "Подготовить пост",
                TaskType.COPYWRITING,
                TaskPriority.MEDIUM,
                executor.id(),
                LocalDateTime.now().plusDays(1)
        ));

        mockMvc.perform(get("/api/content-units/{id}", content.id()).with(user(securityUser(executor))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(content.id()))
                .andExpect(jsonPath("$.baseText").value("Базовый текст"));
    }

    @Test
    void executorCannotOpenForeignContentUnit() throws Exception {
        authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        UserResponse executor = createUser("executor@example.com", "Исполнитель", Role.EXECUTOR);
        ContentUnitResponse content = createDraftContent(manager.id());

        mockMvc.perform(get("/api/content-units/{id}", content.id()).with(user(securityUser(executor))))
                .andExpect(status().isForbidden());
    }

    @Test
    void executorWithCopywritingTaskCanPatchOnlyBaseText() throws Exception {
        authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        UserResponse executor = createUser("executor@example.com", "Исполнитель", Role.EXECUTOR);
        ContentUnitResponse content = createDraftContent(manager.id());
        TaskResponse task = taskService.create(new TaskRequest(
                content.id(),
                "Написать текст",
                "Подготовить пост",
                TaskType.COPYWRITING,
                TaskPriority.MEDIUM,
                executor.id(),
                LocalDateTime.now().plusDays(1)
        ));
        taskService.changeStatus(task.id(), new TaskStatusRequest(TaskStatus.IN_PROGRESS, "В работе"), executor.id());

        mockMvc.perform(patch("/api/content-units/{id}/base-text", content.id())
                        .with(user(securityUser(executor)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BaseTextPayload("Обновленный текст", "Нельзя менять title"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Пост про акцию"))
                .andExpect(jsonPath("$.baseText").value("Обновленный текст"));
    }

    @Test
    void executorWithoutCopywritingTaskCannotPatchBaseText() throws Exception {
        authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        UserResponse executor = createUser("executor@example.com", "Исполнитель", Role.EXECUTOR);
        ContentUnitResponse content = createDraftContent(manager.id());
        TaskResponse task = taskService.create(new TaskRequest(
                content.id(),
                "Сделать дизайн",
                "Подготовить изображение",
                TaskType.DESIGN,
                TaskPriority.MEDIUM,
                executor.id(),
                LocalDateTime.now().plusDays(1)
        ));
        taskService.changeStatus(task.id(), new TaskStatusRequest(TaskStatus.IN_PROGRESS, "В работе"), executor.id());

        mockMvc.perform(patch("/api/content-units/{id}/base-text", content.id())
                        .with(user(securityUser(executor)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BaseTextPayload("Обновленный текст", null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanPatchBaseText() throws Exception {
        authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createDraftContent(manager.id());

        mockMvc.perform(patch("/api/content-units/{id}/base-text", content.id())
                        .with(user(securityUser(manager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BaseTextPayload("Текст менеджера", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseText").value("Текст менеджера"));
    }

    @Test
    void ownerCanPatchBaseText() throws Exception {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createDraftContent(manager.id());

        mockMvc.perform(patch("/api/content-units/{id}/base-text", content.id())
                        .with(user(securityUser(owner.user())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BaseTextPayload("Текст владельца", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseText").value("Текст владельца"));
    }

    @Test
    void approvalApproveChangesContentStatus() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createDraftContent(manager.id());

        ApprovalResponse approval = approvalService.submit(new ApprovalSubmitRequest(
                content.id(),
                owner.user().id(),
                "Материал готов"
        ));
        assertThat(contentUnitService.get(content.id()).status()).isEqualTo(ContentUnitStatus.ON_REVIEW);

        approvalService.approve(approval.id(), new ApprovalDecisionRequest("Согласовано"));

        assertThat(contentUnitService.get(content.id()).status()).isEqualTo(ContentUnitStatus.APPROVED);
    }

    @Test
    void publicationSuccessCreatesSuccessAttempt() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());

        PublicationVariantResponse variant = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.TELEGRAM,
                "Telegram text",
                LocalDateTime.now().plusMinutes(5)
        ));

        PublicationVariantResponse published = publicationService.publishNow(variant.id());

        assertThat(published.status()).isEqualTo(PublicationVariantStatus.PUBLISHED);
        assertThat(publicationService.getAttemptsByVariant(variant.id())).hasSize(1);
        assertThat(publicationService.getAttemptsByVariant(variant.id()).get(0).status().name()).isEqualTo("SUCCESS");
    }

    @Test
    void publicationErrorCreatesFailedAttemptAndManualRequired() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());

        PublicationVariantResponse variant = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.VK,
                "VK text with FAIL",
                LocalDateTime.now().plusMinutes(5)
        ));

        PublicationVariantResponse failed = publicationService.publishNow(variant.id());

        assertThat(failed.status()).isEqualTo(PublicationVariantStatus.MANUAL_REQUIRED);
        assertThat(failed.errorMessage()).contains("VK token and community id are required");
        assertThat(publicationService.getAttemptsByVariant(variant.id())).hasSize(1);
        assertThat(publicationService.getAttemptsByVariant(variant.id()).get(0).status().name()).isEqualTo("FAILED");
    }

    @Test
    void mastodonTextOnlyPublishSuccessCreatesPublishedVariantAndSuccessAttempt() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());
        platformSettingService.update(
                Platform.MASTODON,
                new PlatformSettingUpdateRequest(true, PlatformMode.AUTO_WITH_MANUAL_FALLBACK, "mastodon-token", null, "5.199", null, "https://mastodon.example")
        );
        mastodonPublisher.setNextResult(PublishResult.success("109", "https://mastodon.example/@owner/109", "{\"id\":\"109\"}"));
        PublicationVariantResponse variant = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.MASTODON,
                "Mastodon text",
                LocalDateTime.now().plusMinutes(5)
        ));

        PublicationVariantResponse published = publicationService.publishNow(variant.id());

        assertThat(published.status()).isEqualTo(PublicationVariantStatus.PUBLISHED);
        assertThat(published.externalPostId()).isEqualTo("109");
        assertThat(published.externalPostUrl()).isEqualTo("https://mastodon.example/@owner/109");
        assertThat(publicationService.getAttemptsByVariant(variant.id())).hasSize(1);
        assertThat(publicationService.getAttemptsByVariant(variant.id()).get(0).status().name()).isEqualTo("SUCCESS");
    }

    @Test
    void mastodonMissingTokenOrInstanceFallsBackToManualRequired() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());
        PublicationVariantResponse variant = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.MASTODON,
                "Mastodon text",
                LocalDateTime.now().plusMinutes(5)
        ));

        PublicationVariantResponse manual = publicationService.publishNow(variant.id());

        assertThat(manual.status()).isEqualTo(PublicationVariantStatus.MANUAL_REQUIRED);
        assertThat(manual.errorMessage()).contains("Mastodon instance URL and access token are required");
        assertThat(manual.manualInstruction()).contains("Mastodon");
        assertThat(publicationService.getAttemptsByVariant(variant.id())).hasSize(1);
        assertThat(publicationService.getAttemptsByVariant(variant.id()).get(0).status().name()).isEqualTo("FAILED");
    }

    @Test
    void mastodonApiErrorFallsBackAndDoesNotBreakOtherVariants() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());
        platformSettingService.update(
                Platform.MASTODON,
                new PlatformSettingUpdateRequest(true, PlatformMode.AUTO_WITH_MANUAL_FALLBACK, "bad-token", null, "5.199", null, "https://mastodon.example/")
        );
        mastodonPublisher.setNextResult(PublishResult.failure("Mastodon API error: invalid token", "{\"error\":\"invalid token\"}"));
        PublicationVariantResponse mastodon = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.MASTODON,
                "Mastodon text",
                LocalDateTime.now().plusMinutes(5)
        ));
        PublicationVariantResponse telegram = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.TELEGRAM,
                "Telegram text",
                LocalDateTime.now().plusMinutes(5)
        ));

        PublicationVariantResponse manual = publicationService.publishNow(mastodon.id());
        PublicationVariantResponse publishedTelegram = publicationService.publishNow(telegram.id());

        assertThat(manual.status()).isEqualTo(PublicationVariantStatus.MANUAL_REQUIRED);
        assertThat(manual.errorMessage()).contains("Mastodon API error");
        assertThat(publicationService.getAttemptsByVariant(mastodon.id()).get(0).status().name()).isEqualTo("FAILED");
        assertThat(publishedTelegram.status()).isEqualTo(PublicationVariantStatus.PUBLISHED);
        assertThat(publishedTelegram.externalPostUrl()).isEqualTo("https://t.me/mock_channel/" + telegram.id());
    }

    @Test
    void blankPlatformTokenUpdateKeepsExistingToken() {
        platformSettingService.update(
                Platform.VK,
                new PlatformSettingUpdateRequest(true, PlatformMode.AUTO_WITH_MANUAL_FALLBACK, "vk-token", "238241783", "5.199")
        );

        PlatformSettingResponse updated = platformSettingService.update(
                Platform.VK,
                new PlatformSettingUpdateRequest(true, PlatformMode.AUTO_WITH_MANUAL_FALLBACK, "", "https://vk.com/club238241783", "5.199")
        );

        assertThat(updated.tokenConfigured()).isTrue();
        assertThat(updated.communityId()).isEqualTo("https://vk.com/club238241783");
    }

    @Test
    void vkManualDetailsOpenPlatformUsesConfiguredCommunityUrl() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());
        platformSettingService.update(
                Platform.VK,
                new PlatformSettingUpdateRequest(true, PlatformMode.AUTO_WITH_MANUAL_FALLBACK, "vk-token", "https://vk.com/club238241783", "5.199")
        );
        PublicationVariantResponse variant = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.VK,
                "VK text",
                LocalDateTime.now().plusMinutes(5)
        ));

        publicationService.switchToManual(variant.id(), null);

        assertThat(publicationService.manualDetails(variant.id()).platformUrl())
                .isEqualTo("https://vk.com/club238241783");
    }

    @Test
    void tenchatPublishNowRequiresManualPublicationAndUsesEditorUrl() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());
        PublicationVariantResponse variant = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.TENCHAT,
                "TenChat text",
                LocalDateTime.now().plusMinutes(5)
        ));

        PublicationVariantResponse manual = publicationService.publishNow(variant.id());

        assertThat(manual.status()).isEqualTo(PublicationVariantStatus.MANUAL_REQUIRED);
        assertThat(manual.errorMessage()).contains("manual");
        assertThat(manual.manualInstruction()).contains("скопируйте текст")
                .contains("скачайте медиа")
                .contains("откройте платформу")
                .contains("вставьте ссылку");
        assertThat(publicationService.manualDetails(variant.id()).platformUrl())
                .isEqualTo("https://tenchat.ru/editor");
    }

    @Test
    void setkaPublishNowRequiresManualPublicationAndUsesEditorUrl() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());
        PublicationVariantResponse variant = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.SETKA,
                "Setka text",
                LocalDateTime.now().plusMinutes(5)
        ));

        PublicationVariantResponse manual = publicationService.publishNow(variant.id());

        assertThat(manual.status()).isEqualTo(PublicationVariantStatus.MANUAL_REQUIRED);
        assertThat(manual.errorMessage()).contains("manual");
        assertThat(manual.manualInstruction()).contains("скопируйте текст")
                .contains("скачайте медиа")
                .contains("откройте платформу")
                .contains("вставьте ссылку");
        assertThat(publicationService.manualDetails(variant.id()).platformUrl())
                .isEqualTo("https://setka.ru/posts/regular/new");
    }

    @Test
    void maxPublishNowRequiresManualPublication() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());
        PublicationVariantResponse variant = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.MAX,
                "MAX text",
                LocalDateTime.now().plusMinutes(5)
        ));

        PublicationVariantResponse manual = publicationService.publishNow(variant.id());

        assertThat(manual.status()).isEqualTo(PublicationVariantStatus.MANUAL_REQUIRED);
        assertThat(manual.errorMessage()).contains("manual");
    }

    @Test
    void maxManualDetailsUsesConfiguredChannelUrl() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());
        platformSettingService.update(
                Platform.MAX,
                new PlatformSettingUpdateRequest(true, PlatformMode.MANUAL, null, null, "5.199", "https://web.max.ru/-74213461897922")
        );
        PublicationVariantResponse variant = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.MAX,
                "MAX text",
                LocalDateTime.now().plusMinutes(5)
        ));
        publicationService.publishNow(variant.id());

        assertThat(publicationService.manualDetails(variant.id()).platformUrl())
                .isEqualTo("https://web.max.ru/-74213461897922");
    }

    @Test
    void maxManualDetailsUsesSafeFallbackWhenChannelUrlIsMissing() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());
        PublicationVariantResponse variant = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.MAX,
                "MAX text",
                LocalDateTime.now().plusMinutes(5)
        ));
        publicationService.publishNow(variant.id());

        assertThat(publicationService.manualDetails(variant.id()).platformUrl())
                .isEqualTo("https://web.max.ru/");
    }

    @Test
    void scheduledManualPlatformsBecomeManualRequiredWhenDue() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());
        List<PublicationVariantResponse> variants = List.of(
                publicationService.create(new PublicationVariantRequest(content.id(), Platform.TENCHAT, "TenChat scheduled text", LocalDateTime.now().plusMinutes(5))),
                publicationService.create(new PublicationVariantRequest(content.id(), Platform.SETKA, "Setka scheduled text", LocalDateTime.now().plusMinutes(5))),
                publicationService.create(new PublicationVariantRequest(content.id(), Platform.MAX, "MAX scheduled text", LocalDateTime.now().plusMinutes(5)))
        );
        variants.forEach(variant -> {
            PublicationVariantEntity entity = publicationVariantRepository.findById(variant.id()).orElseThrow();
            entity.setStatus(PublicationVariantStatus.SCHEDULED);
            entity.setScheduledAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")).minusMinutes(1));
            publicationVariantRepository.saveAndFlush(entity);
        });

        publicationService.publishScheduledDuePublications();

        variants.forEach(variant -> assertThat(publicationService.get(variant.id()).status())
                .isEqualTo(PublicationVariantStatus.MANUAL_REQUIRED));
    }

    @Test
    void manualCompleteChangesStatusToManualCompleted() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());
        PublicationVariantResponse variant = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.TENCHAT,
                "TenChat text",
                LocalDateTime.now().plusMinutes(5)
        ));
        publicationService.publishNow(variant.id());

        PublicationVariantResponse completed = publicationService.manualComplete(
                variant.id(),
                new ManualCompleteRequest("https://tenchat.ru/media/demo"),
                manager.id()
        );

        assertThat(completed.status()).isEqualTo(PublicationVariantStatus.MANUAL_COMPLETED);
        assertThat(completed.externalPostUrl()).isEqualTo("https://tenchat.ru/media/demo");
    }

    @Test
    void finalPublicationVariantsAllowExternalPostUrlUpdateOnly() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());

        PublicationVariantResponse telegram = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.TELEGRAM,
                "Telegram text",
                LocalDateTime.now().plusMinutes(5)
        ));
        PublicationVariantResponse tenchat = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.TENCHAT,
                "TenChat text",
                LocalDateTime.now().plusMinutes(5)
        ));
        PublicationVariantResponse published = publicationService.publishNow(telegram.id());

        PublicationVariantResponse publishedWithUrl = publicationService.update(
                published.id(),
                new PublicationVariantUpdateRequest(null, null, "https://t.me/demo/101")
        );

        assertThat(publishedWithUrl.status()).isEqualTo(PublicationVariantStatus.PUBLISHED);
        assertThat(publishedWithUrl.externalPostUrl()).isEqualTo("https://t.me/demo/101");
        assertThatThrownBy(() -> publicationService.update(
                published.id(),
                new PublicationVariantUpdateRequest("Changed text", null, null)
        ))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Final publication variant cannot be edited");

        publicationService.publishNow(tenchat.id());
        PublicationVariantResponse manualCompleted = publicationService.manualComplete(
                tenchat.id(),
                new ManualCompleteRequest("https://tenchat.ru/media/old"),
                manager.id()
        );

        PublicationVariantResponse manualCompletedWithUrl = publicationService.update(
                manualCompleted.id(),
                new PublicationVariantUpdateRequest(null, null, "https://tenchat.ru/media/new")
        );

        assertThat(manualCompletedWithUrl.status()).isEqualTo(PublicationVariantStatus.MANUAL_COMPLETED);
        assertThat(manualCompletedWithUrl.externalPostUrl()).isEqualTo("https://tenchat.ru/media/new");
    }

    @Test
    void contentStatusRecalculatesFromPublicationVariants() {
        AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
        UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
        ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());

        PublicationVariantResponse telegram = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.TELEGRAM,
                "Telegram text",
                LocalDateTime.now().plusMinutes(5)
        ));
        PublicationVariantResponse tenchat = publicationService.create(new PublicationVariantRequest(
                content.id(),
                Platform.TENCHAT,
                "TenChat text",
                LocalDateTime.now().plusMinutes(5)
        ));
        publicationService.schedule(telegram.id(), new SchedulePublicationRequest(LocalDateTime.now().plusMinutes(5)));
        publicationService.schedule(tenchat.id(), new SchedulePublicationRequest(LocalDateTime.now().plusMinutes(5)));

        assertThat(contentUnitService.get(content.id()).status()).isEqualTo(ContentUnitStatus.SCHEDULED);

        publicationService.publishNow(telegram.id());
        publicationService.publishNow(tenchat.id());

        assertThat(contentUnitService.get(content.id()).status()).isEqualTo(ContentUnitStatus.PARTIALLY_PUBLISHED);

        publicationService.manualComplete(tenchat.id(), new ManualCompleteRequest("https://tenchat.ru/media/demo"), manager.id());

        assertThat(contentUnitService.get(content.id()).status()).isEqualTo(ContentUnitStatus.PUBLISHED);
    }

    @Test
    void scheduledPublisherUsesApplicationTimeZoneForDueCheck() {
        TimeZone previousTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            AuthResponse owner = authService.register(new RegisterRequest("owner@example.com", "password123", "Иван Владелец"));
            UserResponse manager = createUser("manager@example.com", "Контент Менеджер", Role.CONTENT_MANAGER);
            ContentUnitResponse content = createApprovedReadyContent(manager.id(), owner.user().id());
            PublicationVariantResponse variant = publicationService.create(new PublicationVariantRequest(
                    content.id(),
                    Platform.TELEGRAM,
                    "Telegram scheduled text",
                    LocalDateTime.now(ZoneId.of("Europe/Moscow")).plusMinutes(5)
            ));
            PublicationVariantEntity entity = publicationVariantRepository.findById(variant.id()).orElseThrow();
            entity.setStatus(PublicationVariantStatus.SCHEDULED);
            entity.setScheduledAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")).minusMinutes(1));
            publicationVariantRepository.saveAndFlush(entity);

            publicationService.publishScheduledDuePublications();

            PublicationVariantResponse published = publicationService.get(variant.id());
            assertThat(published.status()).isEqualTo(PublicationVariantStatus.PUBLISHED);
            assertThat(published.externalPostUrl()).isEqualTo("https://t.me/mock_channel/" + variant.id());
        } finally {
            TimeZone.setDefault(previousTimeZone);
        }
    }

    private UserResponse createUser(String email, String name, Role role) {
        return userService.create(new CreateUserRequest(email, "password123", name, role));
    }

    private SecurityUser securityUser(UserResponse user) {
        return securityUser(user.id());
    }

    private SecurityUser securityUser(AuthUserResponse user) {
        return securityUser(user.id());
    }

    private SecurityUser securityUser(Long userId) {
        return userRepository.findById(userId)
                .map(SecurityUser::new)
                .orElseThrow();
    }

    private ContentUnitResponse createDraftContent(Long managerId) {
        return contentUnitService.create(new ContentUnitRequest(
                "Пост про акцию",
                "Описание",
                "Базовый текст",
                ContentType.POST,
                managerId,
                LocalDateTime.now().plusDays(1)
        ));
    }

    private ContentUnitResponse createApprovedReadyContent(Long managerId, Long ownerId) {
        ContentUnitResponse content = createDraftContent(managerId);
        TaskResponse task = taskService.create(new TaskRequest(
                content.id(),
                "Подготовить текст",
                "Сделать короткий текст",
                TaskType.COPYWRITING,
                TaskPriority.MEDIUM,
                managerId,
                LocalDateTime.now().plusDays(1)
        ));
        taskService.changeStatus(task.id(), new TaskStatusRequest(TaskStatus.IN_PROGRESS, "Взял в работу"), managerId);
        taskService.changeStatus(task.id(), new TaskStatusRequest(TaskStatus.ON_REVIEW, "Готово"), managerId);
        taskService.changeStatus(task.id(), new TaskStatusRequest(TaskStatus.DONE, "Принято"), managerId);
        ApprovalResponse approval = approvalService.submit(new ApprovalSubmitRequest(content.id(), ownerId, "На согласование"));
        approvalService.approve(approval.id(), new ApprovalDecisionRequest("Ок"));
        return contentUnitService.get(content.id());
    }

    @TestConfiguration
    static class MastodonPublisherTestConfig {
        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        FakeMastodonPublisher fakeMastodonPublisher() {
            return new FakeMastodonPublisher();
        }
    }

    static class FakeMastodonPublisher implements PlatformPublisher {
        private PublishResult nextResult;

        @Override
        public Platform platform() {
            return Platform.MASTODON;
        }

        @Override
        public PublishResult publish(PublishRequest request) {
            if (nextResult != null) {
                PublishResult result = nextResult;
                nextResult = null;
                return result;
            }
            if (request.instanceUrl() == null || request.instanceUrl().isBlank()
                    || request.accessToken() == null || request.accessToken().isBlank()) {
                return PublishResult.failure("Mastodon instance URL and access token are required", "{\"error\":\"mastodon-settings-missing\"}");
            }
            return PublishResult.success("mastodon-" + request.variantId(), request.instanceUrl() + "/@mock/" + request.variantId(), "{\"mock\":\"mastodon-success\"}");
        }

        void setNextResult(PublishResult nextResult) {
            this.nextResult = nextResult;
        }
    }

    record BaseTextPayload(String baseText, String title) {
    }
}
