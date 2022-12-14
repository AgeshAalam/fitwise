package com.fitwise.repository.calendar;

import com.fitwise.entity.User;
import com.fitwise.entity.calendar.UserKloudlessMeeting;
import com.fitwise.entity.calendar.UserKloudlessSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserKloudlessScheduleRepository extends JpaRepository<UserKloudlessSchedule, Long> {

    List<UserKloudlessSchedule> findByUser(final User user);

    UserKloudlessSchedule findByUserAndUserKloudlessScheduleId(final User user, final Long fitwiseKlooudlessScheduleId);

    List<UserKloudlessSchedule> findByUserAndSubscriptionPackageSubscriptionPackageId(final User user, final long subscriptionPackageId);

    int countByUserAndPackageKloudlessMappingSessionMappingId(final User user, final long packageKloudlessMappingId);

    List<UserKloudlessSchedule> findByUserAndPackageKloudlessMappingSessionMappingIdAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(final User user, final long packageKloudlessMappingId, Date startDate, Date endDate);

    List<UserKloudlessSchedule> findByUserKloudlessMeeting(UserKloudlessMeeting userKloudlessMeeting);

    List<UserKloudlessSchedule> findBySubscriptionPackageOwnerUserIdAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(Long userId, Date startDate, Date enddate);

    List<UserKloudlessSchedule> findByUserAndBookingDateGreaterThanEqualAndBookingDateLessThanEqualAndMeetingTypeIdIsNull(final User user, Date startDate, Date enddate);
    
    UserKloudlessSchedule findByUserKloudlessScheduleId(Long userkloudlessScheduleId);

    List<UserKloudlessSchedule> findByUserAndPackageKloudlessMappingSessionMappingId(final User user, final long packageKloudlessMappingId);

    List<UserKloudlessSchedule> findBySubscriptionPackageOwnerUserId(Long userId);

    Optional<UserKloudlessSchedule> findByScheduleId(String scheduleId);
    
    Optional<UserKloudlessSchedule>findByUserKloudlessMeetingAndUserKloudlessScheduleId(UserKloudlessMeeting userKloudlessMeeting,Long userkloudlessScheduleId);
    
    List<UserKloudlessSchedule> findByUserAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(final User user, Date startDate, Date enddate);
    
    List<UserKloudlessSchedule> findByUserAndBookingDateAndSubscriptionPackageSubscriptionPackageId(final User user, Date bookingDate, final long subscriptionPackageId );

    int countBySubscriptionPackageSubscriptionPackageId(final long subscriptionPackageId);
    int countBySubscriptionPackageSubscriptionPackageIdAndUserKloudlessMeetingUserKloudlessMeetingIdAndPackageKloudlessMappingSessionMappingId(final long subscriptionPackageId,final long userKloudlessMeetingId,final long packageKloudlessMappingId);
}
