package uk.gov.hmcts.dts.fact.services.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dts.fact.exception.LockExistsException;
import uk.gov.hmcts.dts.fact.exception.NotFoundException;
import uk.gov.hmcts.dts.fact.model.admin.CourtLock;
import uk.gov.hmcts.dts.fact.repositories.CourtLockRepository;
import uk.gov.hmcts.dts.fact.util.AuditType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class AdminCourtLockService {
    private final CourtLockRepository courtLockRepository;
    private final AdminAuditService adminAuditService;
    private static final int LOCK_AMOUNT_PER_COURT = 100;

    @Autowired
    public AdminCourtLockService(final CourtLockRepository courtLockRepository,
                                 final AdminAuditService adminAuditService) {
        this.courtLockRepository = courtLockRepository;
        this.adminAuditService = adminAuditService;
    }

    /**
     *
     * <p>Get all court locks from the database that match a provided court slug.</p>
     *
     * <p> Important to note is at the moment one lock is used per court, but this is
     * being returned as a list to accommodate for a future change where more than one
     * lock may be required instead. </p>
     *
     * @param courtSlug the court slug.
     * @return a list of court locks.
     */
    public List<CourtLock> getCourtLocks(String courtSlug) {
        return courtLockRepository
            .findCourtLockByCourtSlug(courtSlug)
            .stream()
            .map(CourtLock::new)
            .collect(Collectors.toList());
    }

    /**
     * <p>Add the new court lock to the database.</p>
     *
     * <p>Exceptions will occur if a lock already exists for a court and a different user is
     * attempting to retrieve that lock (by calling this method).</p>
     *
     * <p>If the same user is hitting the logic that calls this method, then we simply update their
     * timestamp on the database by calling updateCourtLock.</p>
     *
     * <p>Otherwise we add them to the database</p>
     *
     * @param courtLock the new CourtLock object to add to the database.
     * @return The new CourtLock object that has been added to the database.
     */
    public CourtLock addNewCourtLock(CourtLock courtLock) {
        String courtSlug = courtLock.getCourtSlug();
        String courtUserEmail = courtLock.getUserEmail();
        List<uk.gov.hmcts.dts.fact.entity.CourtLock> courtLockEntityList =
            courtLockRepository.findCourtLockByCourtSlug(courtSlug);
        int courtListSize = courtLockEntityList.size();

        if (courtListSize >= LOCK_AMOUNT_PER_COURT && !courtLockEntityList.get(0).getUserEmail().equals(courtUserEmail)) {
            // If the name doesn't match, but we have a lock for the court, return with an error code
            throw new LockExistsException(String.format("Lock for court '%s' is currently held by user '%s'",
                                                        courtSlug, courtLockEntityList.get(0).getUserEmail()
            ));
        } else if (courtListSize == LOCK_AMOUNT_PER_COURT && courtLockEntityList.get(0).getUserEmail().equals(courtUserEmail)) {
            // If we have one row, and the name is the same as the incoming name, update it
            log.debug("Updating court lock for slug {} and user {}", courtSlug, courtUserEmail);
            return updateCourtLock(courtSlug, courtUserEmail);
        } else if (courtListSize > LOCK_AMOUNT_PER_COURT) {
            throw new LockExistsException(String.format(
                "More than one lock exists for %s: %s",
                courtSlug,
                Arrays.toString(courtLockEntityList.toArray())
            ));
        } else {
            // If it exists, delete and update the resource
            log.debug("Creating court lock for slug {} and user {}", courtSlug, courtUserEmail);
            courtLock.setLockAcquired(LocalDateTime.now(ZoneOffset.UTC));
            CourtLock savedCourtLock = new CourtLock(courtLockRepository.save(new uk.gov.hmcts.dts.fact.entity.CourtLock(
                courtLock)));
            adminAuditService.saveAudit(
                AuditType.findByName("Create court lock"),
                courtLockEntityList,
                savedCourtLock,
                courtLock.getCourtSlug()
            );
            return savedCourtLock;
        }
    }

    /**
     *
     * <p>Remove the court lock provided using both a slug and an email.</p>
     *
     * <p>This is primarily used if two people are interacting with a court.</p>
     *
     * <p>The frontend logic for this is as follows:
     *      Person one comes in, makes a change and leaves. 20 minutes passes.
     *      Person two then comes in, clicks on the court and because the time has passed the
     *      threshold, for that specific court only, person 2 gets the lock, and person 1 loses it.</p>
     *
     * @param courtSlug the court slug.
     * @param userEmail the users email, for example moshcat@justice.cat.meow.
     * @return A list of court locks that have been deleted from the database.
     */
    public List<CourtLock> deleteCourtLock(String courtSlug, String userEmail) {
        List<uk.gov.hmcts.dts.fact.entity.CourtLock> courtLockList =
            courtLockRepository.findCourtLockByCourtSlugAndUserEmail(courtSlug, userEmail);
        if (courtLockList.isEmpty()) {
            throw new NotFoundException(String.format("No lock found for court: %s and user: %s",
                                                      courtSlug, userEmail
            ));
        }
        for (uk.gov.hmcts.dts.fact.entity.CourtLock courtLock : courtLockList) {
            courtLockRepository.delete(courtLock);
        }
        adminAuditService.saveAudit(
            AuditType.findByName("Delete court lock"),
            courtLockList,
            null,
            courtSlug
        );
        return courtLockList.stream().map(CourtLock::new).collect(Collectors.toList());
    }

    /**
     *
     * <p>Delete the court lock by email.</p>
     *
     * <p>The frontend logic for this is when a user clicks the logout button, or when they log in.
     * At that point, we would want no locks to be assigned to them.</p>
     *
     * @param userEmail the users email.
     * @return A list of court locks that have been removed.
     */
    @Transactional
    public List<CourtLock> deleteCourtLockByEmail(String userEmail) {
        List<uk.gov.hmcts.dts.fact.entity.CourtLock> courtLockList =
            courtLockRepository.deleteAllByUserEmail(userEmail);
        adminAuditService.saveAudit(
            AuditType.findByName("Delete court lock"),
            courtLockList,
            null,
            null
        );
        return courtLockList.stream().map(CourtLock::new).collect(Collectors.toList());
    }

    /**
     *
     * <p>Update the court locks timestamp.</p>
     *
     * <p>Used when a user is editing a court and performs actions on selected resources.
     * For example, if they are updating the opening hours of a court.</p>
     *
     * <p>The timeout for a lock is based upon the most recent action time of the user who has it allocated to them.
     * This is to ensure that if a user quits the admin portal by closing their browser, that
     * the lock is not indefinitely attributed to them.</p>
     *
     * @param courtSlug the court slug.
     * @param userEmail the users email, for example kupocat@justice.cat.meow.
     * @return The updated court lock object.
     */
    public CourtLock updateCourtLock(String courtSlug, String userEmail) {
        List<uk.gov.hmcts.dts.fact.entity.CourtLock> courtLockList =
            courtLockRepository.findCourtLockByCourtSlugAndUserEmail(courtSlug, userEmail);

        if (courtLockList.isEmpty()) {
            // If a user attempts to update a lock, and no row exists for the slug at all,
            // apply the lock to them
            log.debug("When updating user: {}, no row was found. Creating lock now for court: {}",
                      userEmail, courtSlug
            );
            return addNewCourtLock(new CourtLock(
                LocalDateTime.now(ZoneOffset.UTC),
                courtSlug,
                userEmail
            ));
        } else if (courtLockList.size() > LOCK_AMOUNT_PER_COURT) {
            throw new LockExistsException(String.format(
                "More than one lock exists for %s: %s",
                courtSlug,
                Arrays.toString(courtLockList.toArray())
            ));
        }
        LocalDateTime newTime = LocalDateTime.now(ZoneOffset.UTC);
        adminAuditService.saveAudit(
            AuditType.findByName("Update court lock"),
            String.format(
                "User %s has a lock acquired time (before) of: %s",
                courtLockList.get(0).getUserEmail(),
                courtLockList.get(0).getLockAcquired()
            ),
            String.format(
                "User %s has a lock acquired time (after) of: %s",
                courtLockList.get(0).getUserEmail(),
                newTime
            ),
            courtSlug
        );
        courtLockList.get(0).setLockAcquired(newTime);
        return new CourtLock(courtLockRepository.save(courtLockList.get(0)));
    }
}
