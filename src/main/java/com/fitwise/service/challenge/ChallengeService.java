package com.fitwise.service.challenge;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.entity.Challenge;
import com.fitwise.entity.User;
import com.fitwise.entity.UserWorkoutStatus;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserWorkoutStatusRepository;
import com.fitwise.repository.WorkoutMappingRepository;
import com.fitwise.repository.challenge.ChallengeRepository;
import com.fitwise.response.challenge.ChallengeResponse;
import com.fitwise.view.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ChallengeService {

    @Autowired
    ProgramRepository programRepository;

    @Autowired
    WorkoutMappingRepository workoutMappingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChallengeRepository challengeRepository;

    @Autowired
    UserWorkoutStatusRepository userWorkoutStatusRepository;

    @Autowired
    UserComponents userComponents;

    public ResponseModel challengeStatus(String challengeType){
        log.info("Get challenge status starts");
        long start = new Date().getTime();
        long profilingStart;

        if(!(KeyConstants.KEY_WEEKLY.equalsIgnoreCase(challengeType) || KeyConstants.KEY_MONTHLY.equalsIgnoreCase(challengeType))){
            throw new ApplicationException(Constants.ERROR_STATUS , MessageConstants.MSG_WRONG_CHALLENGE_TYPE , null);
        }

        User user = userComponents.getUser();

        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);


        //check challenges are there or not based on user
       if(!challengeRepository.existsByUserUserIdAndChallengeType(user.getUserId() , challengeType)){
           throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS , MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
       }
       log.info("Basic validations and DB check for existence of challenge : Time taken in millis : "+(new Date().getTime()-start));

           //check active challenge is there or not
        profilingStart = new Date().getTime();
        Challenge challenge = challengeRepository.findByUserUserIdAndChallengeTypeAndIsExpired(user.getUserId() , challengeType , false);
        log.info("Query : Time taken in millis : "+(new Date().getTime()-profilingStart));


        if(challenge != null){
            Date today = new Date();
            if(challenge.getChallengeEndDate().getTime() <= today.getTime()){
                challenge.setExpired(true);
            }
            challengeRepository.save(challenge);
            log.info("DB update for expiry status : Time taken in millis : "+(new Date().getTime()-profilingStart));
            profilingStart = new Date().getTime();
            responseModel.setPayload(constructChallengeResponse(challenge));
            log.info("Response construction : Time taken in millis : "+(new Date().getTime()-profilingStart));
            }else {
            profilingStart = new Date().getTime();
            Challenge completedChallenge = challengeRepository.findFirstByUserAndChallengeTypeIgnoreCaseOrderByChallengeEndDateDesc(user, challengeType);
            log.info("Completed challenge query : Time taken in millis : "+(new Date().getTime()-profilingStart));
            responseModel.setPayload(constructChallengeResponse(completedChallenge));
        }
        log.info("Get challenge status : Total Time taken in millis : "+(new Date().getTime()-start));
        log.info("Get challenge status ends");
        return responseModel;
    }


    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ResponseModel createChallenge(String challengeType, int numberOfWorkouts ){
        log.info("Create challenge starts.");
        long apiStartTimeMillis = new Date().getTime();

        if(!(KeyConstants.KEY_WEEKLY.equalsIgnoreCase(challengeType) || KeyConstants.KEY_MONTHLY.equalsIgnoreCase(challengeType))){
            throw new ApplicationException(Constants.ERROR_STATUS , MessageConstants.MSG_WRONG_CHALLENGE_TYPE , null);
        }

        User user = userComponents.getUser();
        if(challengeRepository.existsByUserUserIdAndChallengeTypeAndIsExpired(user.getUserId() , challengeType , false)){
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_ALL_READY_CHALLENGED, null);
        }
        log.info("Basic validation with getting user : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        Challenge newChallenge = new Challenge();
        ResponseModel responseModel = new ResponseModel();


        Date startDate = new Date();
        newChallenge.setChallengeStartedDay(startDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
            /*calendar.set(Calendar.MILLISECOND, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.HOUR, 23);*/

        newChallenge.setChallengeType(challengeType);

        if(challengeType.equalsIgnoreCase(KeyConstants.KEY_WEEKLY)){
            calendar.add(Calendar.DAY_OF_MONTH , 7);
            Date endDate = calendar.getTime();
            newChallenge.setChallengeEndDate(endDate);
            newChallenge.setChallengeDays(7);
            newChallenge.setChallengeWorkouts(numberOfWorkouts);
        }else if(challengeType.equalsIgnoreCase(KeyConstants.KEY_MONTHLY)){

            calendar.add(Calendar.DAY_OF_MONTH , 30);
            Date endDate = calendar.getTime();
            newChallenge.setChallengeEndDate(endDate);
            newChallenge.setChallengeDays(30);
            newChallenge.setChallengeWorkouts(numberOfWorkouts);
        }

        newChallenge.setPercentage(0.0);
        //newChallenge.setStatus(KeyConstants.KEY_ACTIVE);
        newChallenge.setUser(user);
        newChallenge.setRemainingWorkouts(newChallenge.getChallengeWorkouts());
        newChallenge.setCompletedWorkouts(0);
        log.info("Construct new challenge : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        challengeRepository.save(newChallenge);
        log.info("Query: save new challenge : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_CHALLENGE_CREATED);
        responseModel.setPayload(constructChallengeResponse(newChallenge));
        log.info("Construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Create challenge ends.");
        return responseModel;
    }

    private ChallengeResponse constructChallengeResponse(Challenge challenge){

        ChallengeResponse challengeResponse = new ChallengeResponse();

        challengeResponse.setChallengeId(challenge.getChallengeId());
        challengeResponse.setChallengeWorkouts(challenge.getChallengeWorkouts());
        challengeResponse.setPercentage(challenge.getPercentage());
        challengeResponse.setRemainingWorkouts(challenge.getRemainingWorkouts());
        //challengeResponse.setStatus(challenge.getStatus());
        challengeResponse.setExpired(challenge.isExpired());

        Date today = new Date();
        Period period = Period.between(challenge.getChallengeStartedDay().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate() , today.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate());
        challengeResponse.setCompletedDays(period.getDays());

        return challengeResponse;
    }

    public ResponseModel updateChallengeWorkouts(Long challengeId, int numberOfWorkouts) {

        if(!challengeRepository.existsByChallengeId(challengeId)){
            throw new ApplicationException(Constants.ERROR_STATUS , MessageConstants.MSG_CHALLENGE_NOT_EXIST, null);
        }

        User user = userComponents.getUser();
        Challenge challenge = challengeRepository.findByChallengeIdAndUser(challengeId, user);

        if(challenge.isExpired()){
            throw new ApplicationException(Constants.CONFLICT , MessageConstants.MSG_CHALLENGE_UPDATE_ERROR , null);
        }

        challenge.setChallengeWorkouts(numberOfWorkouts);
        int remainingWorkouts =numberOfWorkouts - challenge.getCompletedWorkouts();
        if(numberOfWorkouts < challenge.getCompletedWorkouts()){
            challenge.setRemainingWorkouts(0);
        }else{
            challenge.setRemainingWorkouts(remainingWorkouts);

        }
        double percentage = 0.0;
        if(challenge.getCompletedWorkouts() >= challenge.getChallengeWorkouts()){
            percentage = 100;
        }else{
            percentage = (challenge.getCompletedWorkouts()*100)/numberOfWorkouts;
        }
        challenge.setPercentage(percentage);

        challengeRepository.save(challenge);
        return new ResponseModel(Constants.SUCCESS_STATUS , MessageConstants.MSG_CHALLENGE_UPDATED, constructChallengeResponse(challenge));
    }
}

