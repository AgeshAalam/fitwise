package com.fitwise.service.admin;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.exception.ApplicationException;
import com.fitwise.program.model.ProgramTileModel;
import com.fitwise.program.service.ProgramService;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.service.discountservice.DiscountsService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.ProgramSpecifications;
import com.fitwise.specifications.jpa.UserRoleMappingJPA;
import com.fitwise.specifications.jpa.dao.AdminUserDao;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.admin.AdminUserListResponseView;
import com.fitwise.view.admin.AdminUserTileResponseView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Date;
import java.util.stream.Collectors;

/*
 * Created by Vignesh G on 26/06/20
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    @Autowired
    ProgramService programService;

    @Autowired
    ValidationService validationService;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private FitwiseUtils fitwiseUtils;

    @Autowired
    private GeneralProperties generalProperties;

    private final UserRoleMappingJPA userRoleMappingJPA;
    private final DiscountsService discountsService;

    /**
     * @param instructorId
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String, Object> getProgramsOfInstructor(Long instructorId, int pageNo, int pageSize) {
        log.info("Get program of instructor starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = validationService.validateInstructorId(instructorId);

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        log.info("Basic validation : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        Sort sort = Sort.by("modifiedDate");
        sort = sort.descending();
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);

        Specification<Programs> ownerSpec = ProgramSpecifications.getProgramByOwnerUserId(instructorId);
        Specification<Programs> stripeActiveSpec = ProgramSpecifications.getProgramByStripeMapping();
        Specification<Programs> statusSpec = ProgramSpecifications.getProgramByStatus(InstructorConstant.PUBLISH);

        Specification<Programs> finalSpec = statusSpec.and(ownerSpec).and(stripeActiveSpec);

        Page<Programs> programsPage = programRepository.findAll(finalSpec, pageRequest);
        log.info("Query to get programs : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        if (programsPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        List<Long> programIdList = programsPage.stream().map(programs -> programs.getProgramId()).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForInstructor(programIdList);
        log.info("Get offer count map : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        List<ProgramTileModel> programTileModels = new ArrayList<ProgramTileModel>();
        for (Programs program : programsPage) {
            programTileModels.add(programService.constructProgramTileModel(program, offerCountMap));
        }
        log.info("Construct program tile model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));

        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put(KeyConstants.KEY_PROGRAMS, programTileModels);
        responseMap.put(KeyConstants.KEY_TOTAL_COUNT, programsPage.getTotalElements());
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get program of instructor ends.");

        return responseMap;

    }

    /**
     * Get Admin Users List
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param search
     * @return
     */
    public ResponseModel getAdminUsers(int pageNo, int pageSize, String sortOrder, String sortBy, Optional<String> search){
        log.info("Get admin users starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();


        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        List<String> allowedSortByList = Arrays.asList(new String[]{SearchConstants.USER_EMAIL, SearchConstants.USER_ROLE, SearchConstants.ONBOARDED_DATE, SearchConstants.USER_LAST_ACCESS_DATE});
        boolean isSortByAllowed = allowedSortByList.stream().anyMatch(sortBy::equalsIgnoreCase);

        if (!isSortByAllowed) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }

        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }


        if(!fitwiseUtils.isAdmin(user)){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        log.info("Basic validations : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();


        String superAdminEmailAddresses = generalProperties.getSuperAdminEmailAddresses();
        String[] superAdminEmails = superAdminEmailAddresses.split(",");
        List<String> superAdminEmailAddressList = Arrays.asList(superAdminEmails);

        if(!superAdminEmailAddressList.contains(user.getEmail())){
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_ACCESS_SA_RESTRICTED, null);
        }

        List<AdminUserDao> adminUserDaoList = userRoleMappingJPA.getAdminUsers(superAdminEmailAddressList, pageNo-1, pageSize, sortOrder, sortBy, search);
        log.info("Query: get admin users : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (adminUserDaoList.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        AdminUserListResponseView adminUserListResponseView = new AdminUserListResponseView();
        List<AdminUserTileResponseView> adminUserTileResponseViews = new ArrayList<>();
        for(AdminUserDao adminUserDao : adminUserDaoList){
            AdminUserTileResponseView adminUserTileResponseView = new AdminUserTileResponseView();
            adminUserTileResponseView.setUserId(adminUserDao.getUserId());
            adminUserTileResponseView.setEmail(adminUserDao.getEmail());
            adminUserTileResponseView.setRole(adminUserDao.getUserRole());
            adminUserTileResponseView.setOnboardedDate(adminUserDao.getOnboardedDate());
            adminUserTileResponseView.setOnboardedDateFormatted(fitwiseUtils.formatDate(adminUserDao.getOnboardedDate()));
            adminUserTileResponseView.setLastAccessDate(adminUserDao.getLastAccessDate());
            adminUserTileResponseView.setLastAccessDateFormatted(fitwiseUtils.formatDate(adminUserDao.getLastAccessDate()));
            adminUserTileResponseViews.add(adminUserTileResponseView);
        }
        adminUserListResponseView.setAdminUsers(adminUserTileResponseViews);
        log.info("Construct admin user tile response view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        adminUserListResponseView.setTotalCount(userRoleMappingJPA.getCountOfAdminUsers(search));
        log.info("Query: get admin user counts : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get admin users ends.");
        return  new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, adminUserListResponseView);
    }

}
