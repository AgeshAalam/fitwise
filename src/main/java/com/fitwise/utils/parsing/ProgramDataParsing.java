package com.fitwise.utils.parsing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fitwise.constants.KeyConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.WorkoutCompletion;
import com.fitwise.entity.product.FreeAccessProgram;
import com.fitwise.exception.ApplicationException;
import com.fitwise.program.model.ProgramTileModel;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.member.WorkoutCompletionRepository;
import com.fitwise.response.packaging.ProgramTileForPackageView;
import com.fitwise.service.freeproduct.FreeAccessService;
import com.fitwise.utils.Convertions;
import com.fitwise.utils.FitwiseUtils;

import lombok.RequiredArgsConstructor;

/**
 * The Class ProgramDataParsing.
 */
@Component
@RequiredArgsConstructor
public class ProgramDataParsing {

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    private WorkoutCompletionRepository workoutCompletionRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private final FreeAccessService freeAccessService;


    public List<ProgramTileModel> constructProgramTileModelWithFreeAccess(List<Programs> programs, Map<Long, Long> offerCountMap) throws ApplicationException {
        List<ProgramTileModel> programTileModels = new ArrayList<>();
        List<FreeAccessProgram> freeAccessPrograms = freeAccessService.getAllUserFreeProgramsList();
        //GetFreeAccess Programs for Specific user
        List<Long> freeProgramIds = freeAccessService.getUserSpecificFreeAccessProgramsIds();
        for(FreeAccessProgram freeProduct : freeAccessPrograms){
            freeProgramIds.add(freeProduct.getProgram().getProgramId());
        }
        for (Programs program : programs) {
            ProgramTileModel programTileModel = constructProgramTileModel(program, offerCountMap);
            if(freeProgramIds.contains(program.getProgramId())){
                programTileModel.setFreeToAccess(true);
            }
            programTileModels.add(programTileModel);
        }
        return programTileModels;
    }

    /**
     * Construct program tile model.
     *
     * @param program the program
     * @return the program response model
     * @throws ApplicationException the application exception
     */
    public ProgramTileModel constructProgramTileModel(Programs program, Map<Long, Long> offerCountMap) throws ApplicationException {
        ProgramTileModel responseModel = new ProgramTileModel();
        responseModel.setProgramId(program.getProgramId());
        responseModel.setProgramTitle(program.getTitle());
        if(program.getDuration() != null && program.getDuration().getDuration() != null)
            responseModel.setProgramDuration(program.getDuration().getDuration());
        if (program.getProgramType() != null)
            responseModel.setProgramType(program.getProgramType().getProgramTypeName());
        if (program.getProgramExpertiseLevel() != null)
            responseModel.setProgramExpertiseLevel(program.getProgramExpertiseLevel().getExpertiseLevel());
        if (program.getImage() != null) {
            responseModel.setThumbnailUrl(program.getImage().getImagePath());
        }
        String instructorName = "";
        if (program.getOwner() != null) {
            UserProfile userProfile = userProfileRepository.findByUser(program.getOwner());
            if (userProfile != null) {
                if (userProfile.getProfileImage() != null && userProfile.getProfileImage().getImagePath() != null) {
                    responseModel.setInstructorProfileUrl(userProfile.getProfileImage().getImagePath());
                }
                if (userProfile.getFirstName() != null && userProfile.getLastName() != null) {
                    instructorName = userProfile.getFirstName() + KeyConstants.KEY_SPACE + userProfile.getLastName();
                } else if (userProfile.getFirstName() != null) {
                    instructorName = userProfile.getFirstName();
                } else if (userProfile.getLastName() != null) {
                    instructorName = userProfile.getLastName();
                }

            }
        }
        responseModel.setInstructorName(instructorName);
        long noOfCurrentAvailableOffers = offerCountMap.get(program.getProgramId());
        responseModel.setNumberOfCurrentAvailableOffers((int) noOfCurrentAvailableOffers);

        responseModel.setCreatedOn(program.getCreatedDate());
        responseModel.setCreatedOnFormatted(fitwiseUtils.formatDate(program.getCreatedDate()));
        return responseModel;
    }

    public ProgramTileForPackageView constructProgramTileModelForPackage(User user, Programs program, Map<Long, Long> offerCountMap) {


        ProgramTileForPackageView responseModel = new ProgramTileForPackageView();
        responseModel.setProgramId(program.getProgramId());
        responseModel.setProgramTitle(program.getTitle());
        if (program.getDuration() != null && program.getDuration().getDuration() != null)
            responseModel.setProgramDuration(program.getDuration().getDuration());
        if (program.getProgramType() != null)
            responseModel.setProgramType(program.getProgramType().getProgramTypeName());
        if (program.getProgramExpertiseLevel() != null)
            responseModel.setProgramExpertiseLevel(program.getProgramExpertiseLevel().getExpertiseLevel());
        if (program.getImage() != null) {
            responseModel.setThumbnailUrl(program.getImage().getImagePath());
        }
        long noOfCurrentAvailableOffers = offerCountMap.get(program.getProgramId());
        responseModel.setNumberOfCurrentAvailableOffers((int) noOfCurrentAvailableOffers);

        responseModel.setCreatedOn(program.getCreatedDate());
        responseModel.setCreatedOnFormatted(fitwiseUtils.formatDate(program.getCreatedDate()));

        int totalDays = program.getDuration().getDuration().intValue();

        int completedWorkouts = 0;
        String progress = null;
        int progressPercent = 0;
        Date completionDate = null;

        if (user != null) {
            List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserIdAndProgramProgramId(user.getUserId(), program.getProgramId());
            completedWorkouts = workoutCompletionList.size();

            if (completedWorkouts == totalDays) {
                progress = KeyConstants.KEY_COMPLETED;
                progressPercent = 100;
                completionDate = workoutCompletionList.get(workoutCompletionList.size() - 1).getCompletedDate();
            } else {
                progress = Convertions.getNumberWithZero(completedWorkouts) + "/" + totalDays;
                progressPercent = (completedWorkouts * 100) / totalDays;
            }
        }
        responseModel.setProgress(progress);
        responseModel.setProgressPercent(progressPercent);
        responseModel.setCompletedDate(completionDate);
        responseModel.setCompletedDateFormatted(fitwiseUtils.formatDate(completionDate));
        return  responseModel;
    }



}
