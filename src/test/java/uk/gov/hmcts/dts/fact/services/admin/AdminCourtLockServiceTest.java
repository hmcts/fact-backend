package uk.gov.hmcts.dts.fact.services.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dts.fact.entity.CourtLock;
import uk.gov.hmcts.dts.fact.exception.NotFoundException;
import uk.gov.hmcts.dts.fact.repositories.CourtLockRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = AdminCourtLockService.class)
public class AdminCourtLockServiceTest {

    @MockBean
    private CourtLockRepository courtLockRepository;

    @MockBean
    private AdminAuditService adminAuditService;

    @Autowired
    private AdminCourtLockService adminCourtLockService;

    @Captor
    private ArgumentCaptor<CourtLock> captor;

    private static final String TEST_SLUG_1 = "mosh-slug";
    private static final String TEST_SLUG_2 = "kupo-slug";
    private static final String TEST_USER_2 = "kupo@cat.com";
    private static final LocalDateTime TEST_LOCK_ACQUIRED_2 =
        LocalDateTime.of(2001, 8, 28, 20, 20);
    private static final CourtLock ENTITY_COURT_LOCK_1 = new CourtLock(
        1,
        LocalDateTime.of(2000, 8, 28, 20, 20),
        "mosh@cat.com",
        TEST_SLUG_1
    );
    private static final CourtLock ENTITY_COURT_LOCK_2 = new CourtLock(
        2,
        TEST_LOCK_ACQUIRED_2,
        TEST_USER_2,
        TEST_SLUG_2
    );
    private static final CourtLock ENTITY_COURT_LOCK_3 = new CourtLock(
        3,
        LocalDateTime.of(2002, 8, 28, 20, 20),
        "dil@cat.com",
        "dil-slug"
    );

    private static final List<CourtLock> EXPECTED_COURT_LOCK_LIST = Arrays.asList(
        ENTITY_COURT_LOCK_1, ENTITY_COURT_LOCK_2, ENTITY_COURT_LOCK_3
    );

    @Test
    void shouldGetCourtLocks() {
        when(courtLockRepository.findCourtLockByCourtSlug(TEST_SLUG_1)).thenReturn(Collections.singletonList(
            EXPECTED_COURT_LOCK_LIST.get(0)));
        assertThat(adminCourtLockService.getCourtLocks(TEST_SLUG_1)).containsExactlyElementsOf(
            Collections.singletonList(new uk.gov.hmcts.dts.fact.model.admin.CourtLock(
                ENTITY_COURT_LOCK_1))
        );
        verify(courtLockRepository, times(1)).findCourtLockByCourtSlug(TEST_SLUG_1);
    }

    //    @Test
    //    void shouldThrowLockInUseExceptionWhenAddingLock() {
    //        when(courtLockRepository.findCourtLockByCourtSlug(TEST_SLUG_2)).thenReturn(Collections.singletonList(
    //            EXPECTED_COURT_LOCK_LIST.get(0)));
    //        assertThatThrownBy(() -> adminCourtLockService.addNewCourtLock(new uk.gov.hmcts.dts.fact.model.admin.CourtLock(
    //            ENTITY_COURT_LOCK_2)))
    //            .isInstanceOf(LockExistsException.class)
    //            .hasMessage("Lock for court 'kupo-slug' is currently held by user 'mosh@cat.com'");
    //        verify(courtLockRepository, times(1)).findCourtLockByCourtSlug(TEST_SLUG_2);
    //    }

    //    @Test
    //    void shouldUpdateLockWhenAddingIfUserIsTheSame() {
    //        when(courtLockRepository.findCourtLockByCourtSlug(TEST_SLUG_2)).thenReturn(Collections.singletonList(
    //            EXPECTED_COURT_LOCK_LIST.get(1)));
    //        when(courtLockRepository.findCourtLockByCourtSlugAndUserEmail(TEST_SLUG_2, TEST_USER_2)).thenReturn(
    //            Collections.singletonList(
    //                EXPECTED_COURT_LOCK_LIST.get(1)));
    //        when(courtLockRepository.save(any())).thenReturn(EXPECTED_COURT_LOCK_LIST.get(1));
    //
    //        assertThat(adminCourtLockService.addNewCourtLock(new uk.gov.hmcts.dts.fact.model.admin.CourtLock(
    //            ENTITY_COURT_LOCK_2))).isEqualTo(new uk.gov.hmcts.dts.fact.model.admin.CourtLock(ENTITY_COURT_LOCK_2));
    //
    //        verify(courtLockRepository, times(1)).findCourtLockByCourtSlug(TEST_SLUG_2);
    //        verify(courtLockRepository, times(1)).findCourtLockByCourtSlugAndUserEmail(TEST_SLUG_2, TEST_USER_2);
    //        verify(courtLockRepository, times(1)).save(captor.capture());
    //        verify(adminAuditService, times(1)).saveAudit(anyString(), anyString(), anyString(), anyString());
    //
    //        // Check that the edited court has a date larger than the original
    //        List<CourtLock> courtLock = captor.getAllValues();
    //        assertEquals(1, courtLock.size());
    //        assertThat(courtLock.get(0).getLockAcquired()).isAfter(TEST_LOCK_ACQUIRED_2);
    //    }

    //    @Test
    //    void shouldThrowLockInUseExceptionWhenMoreThanOneUserForCourt() {
    //        when(courtLockRepository.findCourtLockByCourtSlug(TEST_SLUG_2)).thenReturn(Arrays.asList(
    //            EXPECTED_COURT_LOCK_LIST.get(0), EXPECTED_COURT_LOCK_LIST.get(1)));
    //        assertThatThrownBy(() -> adminCourtLockService.addNewCourtLock(new uk.gov.hmcts.dts.fact.model.admin.CourtLock(
    //            ENTITY_COURT_LOCK_2)))
    //            .isInstanceOf(LockExistsException.class)
    //            .hasMessage(String.format("More than one lock exists for kupo-slug: %s", Arrays.asList(
    //                EXPECTED_COURT_LOCK_LIST.get(0), EXPECTED_COURT_LOCK_LIST.get(1))));
    //        verify(courtLockRepository, times(1)).findCourtLockByCourtSlug(TEST_SLUG_2);
    //    }

    @Test
    void shouldAddLockSuccess() {
        when(courtLockRepository.findCourtLockByCourtSlug(TEST_SLUG_2)).thenReturn(Collections.emptyList());
        when(courtLockRepository.save(any())).thenReturn(EXPECTED_COURT_LOCK_LIST.get(1));

        assertThat(adminCourtLockService.addNewCourtLock(new uk.gov.hmcts.dts.fact.model.admin.CourtLock(
            ENTITY_COURT_LOCK_2))).isEqualTo(new uk.gov.hmcts.dts.fact.model.admin.CourtLock(ENTITY_COURT_LOCK_2));

        verify(courtLockRepository, times(1)).findCourtLockByCourtSlug(TEST_SLUG_2);
        verify(courtLockRepository, times(1)).save(captor.capture());
        verify(adminAuditService, times(1))
            .saveAudit(
                "Create court lock",
                Collections.emptyList(),
                new uk.gov.hmcts.dts.fact.model.admin.CourtLock(ENTITY_COURT_LOCK_2),
                TEST_SLUG_2
            );

        List<CourtLock> courtLock = captor.getAllValues();
        assertEquals(1, courtLock.size());
        assertThat(courtLock.get(0).getLockAcquired()).isAfter(TEST_LOCK_ACQUIRED_2);
    }

    //    @Test
    //    void shouldThrowLockInUseExceptionWhenMoreThanOneUserForCourtWhenUpdating() {
    //        when(courtLockRepository.findCourtLockByCourtSlugAndUserEmail(TEST_SLUG_2, TEST_USER_2)).thenReturn(
    //            Arrays.asList(EXPECTED_COURT_LOCK_LIST.get(0), EXPECTED_COURT_LOCK_LIST.get(1)));
    //        when(courtLockRepository.save(any())).thenReturn(EXPECTED_COURT_LOCK_LIST.get(1));
    //
    //        assertThatThrownBy(() -> adminCourtLockService.updateCourtLock(TEST_SLUG_2, TEST_USER_2))
    //            .isInstanceOf(LockExistsException.class)
    //            .hasMessage(String.format("More than one lock exists for kupo-slug: %s", Arrays.asList(
    //                EXPECTED_COURT_LOCK_LIST.get(0), EXPECTED_COURT_LOCK_LIST.get(1))));
    //
    //        verify(courtLockRepository, times(1))
    //            .findCourtLockByCourtSlugAndUserEmail(TEST_SLUG_2, TEST_USER_2);
    //        verify(courtLockRepository, never()).save(any());
    //    }

    @Test
    void shouldAddLockWhenUpdatingIfUserNotExistsAndSlugHasNoLocks() {
        when(courtLockRepository.findCourtLockByCourtSlug(TEST_SLUG_2)).thenReturn(Collections.emptyList());
        when(courtLockRepository.findCourtLockByCourtSlugAndUserEmail(TEST_SLUG_2, TEST_USER_2)).thenReturn(
            Collections.emptyList());
        when(courtLockRepository.save(any())).thenReturn(EXPECTED_COURT_LOCK_LIST.get(1));

        assertThat(adminCourtLockService.updateCourtLock(TEST_SLUG_2, TEST_USER_2))
            .isEqualTo(new uk.gov.hmcts.dts.fact.model.admin.CourtLock(ENTITY_COURT_LOCK_2));

        verify(courtLockRepository, times(1)).findCourtLockByCourtSlug(TEST_SLUG_2);
        verify(courtLockRepository, times(1)).findCourtLockByCourtSlugAndUserEmail(TEST_SLUG_2, TEST_USER_2);
        verify(courtLockRepository, times(1)).save(captor.capture());
        verify(adminAuditService, times(1))
            .saveAudit(
                "Create court lock",
                Collections.emptyList(),
                new uk.gov.hmcts.dts.fact.model.admin.CourtLock(EXPECTED_COURT_LOCK_LIST.get(1)),
                TEST_SLUG_2
            );
    }

    @Test
    void shouldUpdateLockSuccess() {
        when(courtLockRepository.findCourtLockByCourtSlugAndUserEmail(TEST_SLUG_2, TEST_USER_2)).thenReturn(
            Collections.singletonList(
                EXPECTED_COURT_LOCK_LIST.get(1)));
        when(courtLockRepository.save(any())).thenReturn(EXPECTED_COURT_LOCK_LIST.get(1));

        assertThat(adminCourtLockService.updateCourtLock(TEST_SLUG_2, TEST_USER_2))
            .isEqualTo(new uk.gov.hmcts.dts.fact.model.admin.CourtLock(ENTITY_COURT_LOCK_2));

        verify(courtLockRepository, times(1)).findCourtLockByCourtSlugAndUserEmail(TEST_SLUG_2, TEST_USER_2);
        verify(courtLockRepository, times(1)).save(captor.capture());
        verify(adminAuditService, times(1))
            .saveAudit(
                "Update court lock",
                String.format("User kupo@cat.com has a lock acquired time (before) of: %s", TEST_LOCK_ACQUIRED_2),
                String.format(
                    "User kupo@cat.com has a lock acquired time (after) of: %s",
                    ENTITY_COURT_LOCK_2.getLockAcquired()
                ),
                TEST_SLUG_2
            );

        // Check that the edited court has a date larger than the original
        List<CourtLock> courtLock = captor.getAllValues();
        assertEquals(1, courtLock.size());
        assertThat(courtLock.get(0).getLockAcquired()).isAfter(TEST_LOCK_ACQUIRED_2);
    }

    @Test
    void shouldDeleteCourtLock() {
        when(courtLockRepository.findCourtLockByCourtSlugAndUserEmail(TEST_SLUG_2, TEST_USER_2))
            .thenReturn(Collections.singletonList(
                EXPECTED_COURT_LOCK_LIST.get(0)));
        assertThat(adminCourtLockService.deleteCourtLock(TEST_SLUG_2, TEST_USER_2)).containsExactlyElementsOf(
            Collections.singletonList(new uk.gov.hmcts.dts.fact.model.admin.CourtLock(
                ENTITY_COURT_LOCK_1))
        );
        verify(courtLockRepository, times(1))
            .findCourtLockByCourtSlugAndUserEmail(TEST_SLUG_2, TEST_USER_2);
        verify(courtLockRepository, times(1))
            .delete(EXPECTED_COURT_LOCK_LIST.get(0));
        verify(adminAuditService, times(1))
            .saveAudit(
                "Delete court lock",
                Collections.singletonList(EXPECTED_COURT_LOCK_LIST.get(0)),
                null,
                TEST_SLUG_2
            );
    }

    @Test
    void shouldDeleteCourtLockByEmail() {
        when(courtLockRepository.deleteAllByUserEmail(TEST_USER_2))
            .thenReturn(Collections.singletonList(
                EXPECTED_COURT_LOCK_LIST.get(0)));
        assertThat(adminCourtLockService.deleteCourtLockByEmail(TEST_USER_2)).containsExactlyElementsOf(
            Collections.singletonList(new uk.gov.hmcts.dts.fact.model.admin.CourtLock(
                ENTITY_COURT_LOCK_1))
        );
        verify(courtLockRepository, times(1))
            .deleteAllByUserEmail(TEST_USER_2);
        verify(adminAuditService, times(1))
            .saveAudit(
                "Delete court lock",
                Collections.singletonList(EXPECTED_COURT_LOCK_LIST.get(0)),
                null,
                null
            );
    }

    @Test
    void shouldReturnNotFoundWhenDeletingCourtThatDoesNotExist() {
        when(courtLockRepository.findCourtLockByCourtSlugAndUserEmail(TEST_SLUG_2, TEST_USER_2))
            .thenReturn(Collections.emptyList());
        assertThatThrownBy(() -> adminCourtLockService.deleteCourtLock(TEST_SLUG_2, TEST_USER_2))
            .isInstanceOf(NotFoundException.class)
            .hasMessage(String.format("Not found: No lock found for court: %s and user: %s",
                                      TEST_SLUG_2, TEST_USER_2
            ));

        verify(courtLockRepository, times(1))
            .findCourtLockByCourtSlugAndUserEmail(TEST_SLUG_2, TEST_USER_2);
        verify(courtLockRepository, never()).delete(any());
        verify(adminAuditService, never()).saveAudit(any(), any(), any(), any());
    }
}
