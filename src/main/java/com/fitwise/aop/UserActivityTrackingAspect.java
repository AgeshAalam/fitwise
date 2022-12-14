package com.fitwise.aop;

/*
 * Created by Vignesh G on 16/03/20
 */

import com.fitwise.components.UserComponents;
import com.fitwise.entity.User;
import com.fitwise.entity.UserActiveInactiveTracker;
import com.fitwise.entity.UserActivityAudit;
import com.fitwise.entity.UserRole;
import com.fitwise.repository.UserActiveInactiveTrackerRepository;
import com.fitwise.repository.UserActivityAuditRepository;
import com.fitwise.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Aspect
@Component
@Slf4j
public class UserActivityTrackingAspect {

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private UserActivityAuditRepository userActivityAuditRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    UserActiveInactiveTrackerRepository userActiveInactiveTrackerRepository;

    @Pointcut("within(com.fitwise.rest..*) && @within(org.springframework.web.bind.annotation.RestController)")
    public void restControllers() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PathVariable)" +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)"
    )
    public void mappingAnnotations() {
    }

    @Before("restControllers() && mappingAnnotations()")
    public void auditUserActivity(JoinPoint joinPoint) {

        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String requestUri = request.getRequestURI();
            boolean doNothing = false;
            if (requestUri.contains("/v1/user/login")) {
                doNothing = true;
            }

            if (!doNothing) {
                User user = userComponents.getAndValidateUser();
                if (user != null) {
                    UserActivityAudit userActivityAudit = new UserActivityAudit();
                    userActivityAudit.setUser(user);

                    String role = userComponents.getRole();
                    UserRole userRole = userRoleRepository.findByName(role);
                    userActivityAudit.setUserRole(userRole);

                    boolean isEntryAdded = isEntryAddedtoday(user, userRole);
                    if (!isEntryAdded) {
                        userActivityAuditRepository.save(userActivityAudit);
                    }
                    updateUserActiveInactiveStatus(user, userRole);
                }
            }
        } catch (Exception e) {
            log.debug("Issue while auditing user activity : " + e.getMessage());
        }
    }

    private boolean isEntryAddedtoday(User user, UserRole userRole) {
        boolean isEntryAdded = false;

        LocalDate today = LocalDate.now();
        LocalDateTime localDateTime = today.atStartOfDay();
        Date startofToday = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        Date now = new Date();

        List<UserActivityAudit> auditEntries = userActivityAuditRepository.findByUserAndUserRoleAndLastActiveTimeBetween(user, userRole, startofToday, now);
        if (auditEntries.size() > 0) {
            isEntryAdded = true;
        }
        return isEntryAdded;

    }

    /**
     * Updating the user active status
     * last active time updated only once in 6 hours
     * @param user
     * @param userRole
     */
    public void updateUserActiveInactiveStatus(User user, UserRole userRole) {
        List<UserActiveInactiveTracker> activeInactiveTrackerList = userActiveInactiveTrackerRepository.findByUserUserIdAndUserRoleRoleIdOrderByIdDesc(user.getUserId(), userRole.getRoleId());
        boolean updatedInLast6Hours = false;
        UserActiveInactiveTracker activeInactiveTracker = null;
        if (activeInactiveTrackerList.isEmpty()) {
            activeInactiveTracker = new UserActiveInactiveTracker();
            activeInactiveTracker.setUser(user);
            activeInactiveTracker.setUserRole(userRole);
            updatedInLast6Hours = false;
        }else {
            activeInactiveTracker = activeInactiveTrackerList.get(0);

            /*
             * Checking if updated in last 6 hours
             * */
            LocalDateTime localDateTime = LocalDateTime.now();
            localDateTime = localDateTime.minusHours(6);
            Date date6HoursAgo = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            if(activeInactiveTracker.getModifiedDate().after(date6HoursAgo)) {
                updatedInLast6Hours = true;
            }
        }
        if (!updatedInLast6Hours) {
            activeInactiveTracker.setActive(true);
            activeInactiveTracker.setModifiedDate(new Date());
            userActiveInactiveTrackerRepository.save(activeInactiveTracker);
        }
    }

}
