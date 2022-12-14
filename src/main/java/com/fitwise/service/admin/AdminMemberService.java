package com.fitwise.service.admin;

import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.ExportConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringArrayConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.admin.InviteMemberDetails;
import com.fitwise.entity.view.ViewMember;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.admin.InviteMemberDetailsRepository;
import com.fitwise.repository.view.ViewMemberRepository;
import com.fitwise.request.InviteMember;
import com.fitwise.response.AdminInviteMembersDetails;
import com.fitwise.response.AdminListResponse;
import com.fitwise.response.MemberResponse;
import com.fitwise.response.freeaccess.MemberMinimumDetails;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.InviteMemberSpecification;
import com.fitwise.specifications.UserProfileSpecifications;
import com.fitwise.specifications.jpa.runnable.MemberCountJpaRunnable;
import com.fitwise.specifications.jpa.runnable.MemberDataJpaRunnable;
import com.fitwise.specifications.view.ViewMemberSpecification;
import com.fitwise.utils.AppUtils;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.admin.MultipleInvitePayloadview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminMemberService {

    private final ViewMemberRepository viewMemberRepository;
    private final FitwiseUtils fitwiseUtils;
    private final EntityManager entityManager;
    private final UserProfileRepository userProfileRepository;
    private final UserRoleRepository userRoleRepository;
    private final InviteMemberDetailsRepository inviteMemberDetailsRepository;
    private final GeneralProperties generalProperties;
    private final AsyncMailer asyncMailer;
    private final EmailContentUtil emailContentUtil;
    private final UserRepository userRepository;

    /**
     * Get all members and details
     * @param pageNo Page number
     * @param pageSize Number of records for the page
     * @param sortOrder Order of sorting
     * @param sortBy Column which involved in sort
     * @param searchName Searching string
     * @param blockStatus Current user status
     * @return Return admin response
     */
    public AdminListResponse getMembers(final int pageNo, final int pageSize, final String sortOrder, final String sortBy, final Optional<String> searchName, final String blockStatus) {
        long startTime = new Date().getTime();
        log.info("Admin member L1 starts.");
        long temp = new Date().getTime();
        RequestParamValidator.pageSetup(pageNo, pageSize);
		RequestParamValidator.sortList(StringArrayConstants.SORT_ADMIN_MEMBER, sortBy, sortOrder);
        RequestParamValidator.userBlockStatus(blockStatus);
        Specification<ViewMember> viewMemberSpecification = ViewMemberSpecification.getMemberSortSpecification(sortBy, sortOrder);
        if(KeyConstants.KEY_BLOCKED.equalsIgnoreCase(blockStatus)){
            viewMemberSpecification = viewMemberSpecification.and(ViewMemberSpecification.getBlockStatusSpecification(true));
        }else if(KeyConstants.KEY_OPEN.equalsIgnoreCase(blockStatus)){
            viewMemberSpecification = viewMemberSpecification.and(ViewMemberSpecification.getBlockStatusSpecification(false));
        }
        if(searchName.isPresent() && !searchName.get().isEmpty()){
            viewMemberSpecification = viewMemberSpecification.and(ViewMemberSpecification.getSearchByNameSpecification(searchName.get()));
        }
        log.info("Field validation and spec generation. " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Map<String, Object> response = new HashMap<>();
        Thread dataThread = new Thread(new MemberDataJpaRunnable(entityManager, sortBy, sortOrder, searchName, blockStatus, pageNo - 1, pageSize, response));
        executorService.execute(dataThread);
        Thread countThread = new Thread(new MemberCountJpaRunnable(viewMemberRepository, viewMemberSpecification, response));
        executorService.execute(countThread);
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            log.info("Admin Member Data Collection thread execution");
        }
        log.info("Get data from db " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        AdminListResponse adminListResponse = new AdminListResponse();
        adminListResponse.setTotalSizeOfList((Long)response.get("count"));
        List<MemberResponse> memberResponses = new ArrayList<>();
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        for(ViewMember viewMember : (List<ViewMember>)response.get("data")) {
            MemberResponse memberResponse = new MemberResponse();
            memberResponse.setUserId(viewMember.getUserId());
            memberResponse.setMemberName(viewMember.getName());
            memberResponse.setEmail(viewMember.getEmail());
            if(!StringUtils.isEmpty(viewMember.getImageUrl())){
                memberResponse.setImageUrl(viewMember.getImageUrl());
            }
            memberResponse.setTotalSubscription(viewMember.getActiveProgramSubscriptions());
            memberResponse.setCompletedProgram(viewMember.getCompletedPrograms());
            memberResponse.setAmountSpent(viewMember.getTotalSpent());
            memberResponse.setAmountSpentFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(viewMember.getTotalSpent()));
            if(viewMember.getLastUserAccess() != null){
                memberResponse.setLastAccess(viewMember.getLastUserAccess());
                memberResponse.setLastAccessFormatted(fitwiseUtils.formatDate(viewMember.getLastUserAccess()));
            }
            if(viewMember.getOnboardedOn() != null){
                memberResponse.setOnboardedDate(viewMember.getOnboardedOn());
                memberResponse.setOnboardedDateFormatted(fitwiseUtils.formatDate(viewMember.getOnboardedOn()));
            }
            memberResponse.setBlocked(viewMember.getBlocked());
            memberResponse.setAmountSpentOnProgram(viewMember.getProgramAmtSpent());
            memberResponse.setAmountSpentOnProgramFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(viewMember.getProgramAmtSpent()));
            memberResponse.setAmountSpentOnPackage(viewMember.getPackageAmtSpent());
            memberResponse.setAmountSpentOnPackageFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(viewMember.getPackageAmtSpent()));
            memberResponse.setPackageSubscriptionCount(viewMember.getActivePackageSubscriptions());
            memberResponses.add(memberResponse);
        }
        log.info("Response construction " + (new Date().getTime() - temp));
        adminListResponse.setPayloadOfAdmin(memberResponses);
        log.info("Admin member L1 ends. " + (new Date().getTime() - startTime));
        return adminListResponse;
    }

    /**
     * Export member list for admin
     * @param sortOrder
     * @param sortBy
     * @param searchName
     * @param blockStatus
     * @return
     */
    public ByteArrayInputStream export(String sortOrder, String sortBy, Optional<String> searchName, String blockStatus) {
        long startTime = new Date().getTime();
        log.info("Admin member export starts.");
        long temp = new Date().getTime();
        List<String> allowedSortByList = Arrays.asList(SearchConstants.MEMBER_NAME, SearchConstants.AMOUNT_SPENT, SearchConstants.TOTAL_SUBSCRIPTION, SearchConstants.COMPLETED_PROGRAM, SearchConstants.STATUS, SecurityFilterConstants.ROLE_INSTRUCTOR, SearchConstants.PACKAGE_SUBSCRIPTION_COUNT, SearchConstants.USER_LAST_ACCESS_DATE, SearchConstants.ONBOARDED_DATE, SearchConstants.USER_EMAIL);
        RequestParamValidator.sortList(allowedSortByList, sortBy, sortOrder);
        RequestParamValidator.userBlockStatus(blockStatus);
        Specification<ViewMember> viewMemberSpecification = ViewMemberSpecification.getMemberSortSpecification(sortBy, sortOrder);
        if(KeyConstants.KEY_BLOCKED.equalsIgnoreCase(blockStatus)){
            viewMemberSpecification = viewMemberSpecification.and(ViewMemberSpecification.getBlockStatusSpecification(true));
        }else if(KeyConstants.KEY_OPEN.equalsIgnoreCase(blockStatus)){
            viewMemberSpecification = viewMemberSpecification.and(ViewMemberSpecification.getBlockStatusSpecification(false));
        }
        if(searchName.isPresent() && !searchName.get().isEmpty()){
            viewMemberSpecification = viewMemberSpecification.and(ViewMemberSpecification.getSearchByNameSpecification(searchName.get()));
        }
        log.info("Field validation and spec generation. " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        List<ViewMember> viewMemberList = viewMemberRepository.findAll(viewMemberSpecification);
        log.info("Get data from db " + (new Date().getTime() - temp));
        ValidationUtils.emptyList(viewMemberList);
        temp = new Date().getTime();
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        ByteArrayInputStream byteArrayInputStream;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format)) {
            csvPrinter.printRecord(ExportConstants.EXPORT_HEADER_MEMBER);
            for (ViewMember viewMember : viewMemberList) {
                List<Object> rowData = new ArrayList<>();
                //Member Name
                rowData.add(viewMember.getName() != null ? viewMember.getName() : "");
                //Blocked
                String userBlockStatus = (viewMember.getBlocked()) ? KeyConstants.KEY_BLOCKED : KeyConstants.KEY_UNBLOCKED;
                rowData.add(userBlockStatus);
                //Total Spent
                double totalSpent = viewMember.getTotalSpent();
                rowData.add(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalSpent));
                //Total Spent For Program
                double programAmtSpent = viewMember.getProgramAmtSpent();
                rowData.add(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(programAmtSpent));
                //Total Spent For Package
                double packageAmtSpent = viewMember.getPackageAmtSpent();
                rowData.add(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(packageAmtSpent));
                //Package Subscriptions
                rowData.add(viewMember.getActivePackageSubscriptions());
                //Program Subscriptions
                rowData.add(viewMember.getActiveProgramSubscriptions());
                //Completed Programs
                rowData.add(viewMember.getCompletedPrograms());
                //Last Active
                rowData.add(fitwiseUtils.formatDate(viewMember.getLastUserAccess()));
                rowData.add(fitwiseUtils.formatDate(viewMember.getOnboardedOn()));
                rowData.add(viewMember.getEmail());
                rowData.add(viewMember.getContactNumber());
                csvPrinter.printRecord(rowData);
            }
            csvPrinter.flush();
            byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());
            log.info("Response construction " + (new Date().getTime() - temp));
        } catch (Exception exception) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAYOUTS_CSV_GENERATION_FAILED, MessageConstants.ERROR);
        }
        log.info("Export member completed : " + (new Date().getTime() - startTime));
        return byteArrayInputStream;
    }
    

	/**
	 * Member minimum details.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @param search the search
	 * @return the response model
	 */
	public ResponseModel memberMinimumDetails(final int pageNo, final int pageSize, final String search) {
		ResponseModel response = new ResponseModel();
		RequestParamValidator.pageSetup(pageNo, pageSize);
		try {
			List<MemberMinimumDetails> responseList = new ArrayList<>();
			PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
	        UserRole userRole = userRoleRepository.findByName(KeyConstants.KEY_MEMBER);
			Specification<UserProfile> userSpec = UserProfileSpecifications.getUsersProfileByDetailsSearch(search, userRole.getRoleId());
			Page<UserProfile> userProfilePage = userProfileRepository.findAll(userSpec, pageRequest);
			for (UserProfile user : userProfilePage.getContent()) {
				// To check user is blocked user or not. Because query not exclude the blocked users
				if (user.getUser() != null && !fitwiseUtils.isUserBlocked(user.getUser())) {
					MemberMinimumDetails member = new MemberMinimumDetails();
					member.setFirstName(user.getFirstName());
					member.setLastName(user.getLastName());
					if (user.getUser() != null) {
						member.setUserId(user.getUser().getUserId());
						member.setEmail(user.getUser().getEmail());
						member.setOnboardedOn(user.getUser().getCreatedDate());
						member.setOnboardedDateFormatted(fitwiseUtils.formatDate(user.getUser().getCreatedDate()));
					}
					if (user.getProfileImage() != null)
						member.setImageUrl(user.getProfileImage().getImagePath());
					responseList.add(member);
				}
			}
			AdminListResponse<MemberMinimumDetails> res = new AdminListResponse<>();
			res.setTotalSizeOfList(
					(userProfilePage.getTotalElements() - (userProfilePage.getContent().size() - responseList.size()))); // Exclude blocked users count
			res.setPayloadOfAdmin(responseList);
			response.setStatus(Constants.SUCCESS_STATUS);
			response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
			response.setPayload(res);
		} catch (Exception exception) {
			log.error(exception.getMessage());
			response.setStatus(Constants.ERROR_STATUS);
			response.setMessage(MessageConstants.ERROR_INTERNAL_SERVER_FAILURE);
		}
		return response;
	}
	
	/**
	 * Invite member.
	 *
	 * @param inviteMember the invite member
	 * @return the response model
	 */
	public ResponseModel inviteMember(InviteMember inviteMember) {
		ResponseModel response = new ResponseModel();
		validateInviteMemberDetails(inviteMember);
		InviteMemberDetails inviteMemberDetails = getExistingInviteMemberRecord(inviteMember.getEmail());
		inviteMemberDetails.setEmail(inviteMember.getEmail());
		inviteMemberDetails.setFirstName(inviteMember.getFirstName());
		inviteMemberDetails.setLastName(inviteMember.getLastName());
		inviteMemberDetailsRepository.save(inviteMemberDetails);
		// Sent Invite
		String subject = EmailConstants.MEMBER_INVITE_BY_ADMIN_SUBJECT;
		String trainnrSite = EmailConstants.TRAINNR_SITE_LINK;
		String mailBody = EmailConstants.MEMBER_INVITE_BY_ADMIN_CONTENT;
		mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON
				.replace(EmailConstants.EMAIL_GREETINGS,
						"Hi " + inviteMember.getFirstName() + " " + inviteMember.getLastName() + ",")
				.replace(EmailConstants.EMAIL_BODY, mailBody).replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrSite);
		mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
		mailBody = mailBody.replace(EmailConstants.LITERAL_APP_URL,
				generalProperties.getMemberBaseUrl() + "/member/email?email=" + inviteMember.getEmail());
		asyncMailer.sendHtmlMail(inviteMember.getEmail(), subject, mailBody);
		response.setStatus(Constants.SUCCESS_STATUS);
		response.setMessage(MessageConstants.MSG_ADMIN_INVITE_MEMBERS);
		return response;
	}

	/**
	 * checkCsvFile.
	 * check the file format is csv or not
	 * param CSV file
	 * @return the response model
	 */
	public ResponseModel checkCsvFile(MultipartFile file) throws IOException{
		ResponseModel response = new ResponseModel();
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		if(extension.equalsIgnoreCase("csv")){
			return bulkInviteMember(file);
		}
		else {
			response.setStatus(Constants.CONTENT_NEEDS_TO_BE_VALIDATE);
			response.setMessage(MessageConstants.MSG_ERR_ADMIN_UPLOAD_CSVFILE);
			return response;
		}

	}

	public static boolean checkEquality(String[] s1, String[] s2)
	{
		if (s1 == s2) {
			return true;
		}
		if (s1 == null || s2 == null) {
			return false;
		}
		int n = s1.length;
		if (n != s2.length) {
			return false;
		}
		for (int i = 0; i < n; i++)
		{
			if (!s1[i].equals(s2[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Bulk Invite member.
	 *
	 * param CSV file
	 * @return the response model
	 */

	public ResponseModel bulkInviteMember(MultipartFile file) throws IOException {
		String[] headers = {StringConstants.EMAIL, "First Name","Last Name"};
		ResponseModel response = new ResponseModel();
		List<MultipleInvitePayloadview> payload = new ArrayList<>();
		InputStream inputStream = file.getInputStream();
		InputStreamReader reader = new InputStreamReader(inputStream);
		BufferedReader br = new BufferedReader( reader);
		String line = br.readLine(); //skipping header
		String[] fields1 = line.split(",");
		if(checkEquality(headers,fields1)){
			while ((line = br.readLine()) != null && !line.isEmpty()) {
				String[] fields = line.split(",");
				InviteMember iMember = new InviteMember();
				iMember.setEmail(fields[0]);
				iMember.setFirstName(fields[1]);
				iMember.setLastName(fields[2]);
				try {
					ResponseModel responseModel = inviteMember(iMember);
					MultipleInvitePayloadview multipleInvitePayloadview = new MultipleInvitePayloadview();
					multipleInvitePayloadview.setStatus(responseModel.getStatus());
					multipleInvitePayloadview.setMessage(responseModel.getMessage());
					multipleInvitePayloadview.setInviteMember(iMember);
					payload.add(multipleInvitePayloadview);

				}
				catch (ApplicationException e){
					MultipleInvitePayloadview multipleInvitePayloadview = new MultipleInvitePayloadview();
					multipleInvitePayloadview.setStatus(e.getStatus());
					multipleInvitePayloadview.setMessage(e.getMessage());
					multipleInvitePayloadview.setInviteMember(iMember);
					payload.add(multipleInvitePayloadview);
				}
			}
			br.close();
		}
		else {
			response.setStatus(Constants.CONTENT_NEEDS_TO_BE_VALIDATE);
			response.setMessage(MessageConstants.MSG_ERR_ADMIN_FILE_FORMAT);
			return response;
		}
		response.setStatus(Constants.SUCCESS_STATUS);
		response.setMessage(MessageConstants.MSG_ADMIN_INVITE_BULK_MEMBERS);
		response.setPayload(payload);
		return response;
	}

	
	/**
	 * Validate invite member details.
	 *
	 * @param inviteMember the invite member
	 */
	private void validateInviteMemberDetails(InviteMember inviteMember) {
		RequestParamValidator.allowOnlyAlphabets(inviteMember.getFirstName(),
				ValidationMessageConstants.MSG_FIRST_NAME_ERROR);
		RequestParamValidator.stringLengthValidation(inviteMember.getFirstName(), null, 50L,
				ValidationMessageConstants.MSG_FIRST_NAME_ERROR);
		RequestParamValidator.allowOnlyAlphabets(inviteMember.getLastName(),
				ValidationMessageConstants.MSG_LAST_NAME_ERROR);
		RequestParamValidator.stringLengthValidation(inviteMember.getLastName(), null, 50L,
				ValidationMessageConstants.MSG_LAST_NAME_ERROR);
		RequestParamValidator.emptyString(inviteMember.getEmail(), ValidationMessageConstants.MSG_EMAIL_EMPTY);
		RequestParamValidator.stringLengthValidation(inviteMember.getEmail(), null, 100L,
				ValidationMessageConstants.MSG_ERR_EMAIL_MAX_LENGTH);
		ValidationUtils.validateEmail(inviteMember.getEmail()); // Validate Email format using regex
		if (!ValidationService.isValidEmailAddress(inviteMember.getEmail())) {
			throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EMAIL_ERROR,
					Constants.RESPONSE_INVALID_DATA);
		}
		// To check user is already present or Not
		User user = userRepository.findByEmail(inviteMember.getEmail());
		if (user != null) {
			for (UserRole userRole : AppUtils.getUserRoles(user)) {
				if (userRole.getName().equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER)) {
					throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_USER_ALREADY_EXIST_ONLY,
							null);
				}
			}
		}
	}
	
	/**
	 * Gets the invite members list.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @param search the search
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the invite members list
	 */
	public ResponseModel getInviteMembersList(final int pageNo, final int pageSize, final String search,
			final String sortOrder, final String sortBy) {
		ResponseModel response = new ResponseModel();
		RequestParamValidator.pageSetup(pageNo, pageSize);
		AdminListResponse<AdminInviteMembersDetails> res = new AdminListResponse<>();
		response.setStatus(Constants.SUCCESS_STATUS);
		response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
		updateUserRegisterOrNotStatus();
		try {
			List<AdminInviteMembersDetails> responseList = new ArrayList<>();
			PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
			Specification<InviteMemberDetails> inviteSortSpec = InviteMemberSpecification
					.getInviteMemberSortingSpecification(sortOrder, sortBy);
			Specification<InviteMemberDetails> finalSpec = inviteSortSpec;
			if (search != null) {
				Specification<InviteMemberDetails> inviteSearchSpec = InviteMemberSpecification
						.getInviteMemberSearchSpecification(search);
				finalSpec = finalSpec.and(inviteSearchSpec);
			}
			Page<InviteMemberDetails> inviteMemberPage = inviteMemberDetailsRepository.findAll(finalSpec, pageRequest);

			for (InviteMemberDetails inviteMember : inviteMemberPage.getContent()) {
				AdminInviteMembersDetails details = new AdminInviteMembersDetails();
				details.setStatus(DBConstants.INVITE_MEMBER_STATUS_PENDING);
				details.setEmail(inviteMember.getEmail());
				details.setFirstName(inviteMember.getFirstName());
				details.setLastName(inviteMember.getLastName());
				details.setInviteSentOn(inviteMember.getCreatedDate());
				details.setInviteSentOnFormatted(fitwiseUtils.formatDate(inviteMember.getCreatedDate()));
				if(inviteMember.isUserRegistered()) {
					details.setStatus(DBConstants.INVITE_MEMBER_STATUS_REGISTERED);
				}
				responseList.add(details);
			}
			res.setTotalSizeOfList(inviteMemberPage.getTotalElements());
			res.setPayloadOfAdmin(responseList);
			response.setPayload(res);
		} catch (Exception exception) {
			log.error(exception.getMessage());
			response.setStatus(Constants.ERROR_STATUS);
			response.setMessage(MessageConstants.ERROR_INTERNAL_SERVER_FAILURE);
		}
		return response;
	}
	
	/**
	 * Update user register or not status.
	 */
	private void updateUserRegisterOrNotStatus() {
		List<InviteMemberDetails> list = inviteMemberDetailsRepository.findByUserRegistered(false);
		for (InviteMemberDetails inviteObj : list) {
			User user = userRepository.findByEmail(inviteObj.getEmail());
			if (user != null) {
				for (UserRole userRole : AppUtils.getUserRoles(user)) {
					if (userRole.getName().equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER)) {
						inviteObj.setUserRegistered(true);
						inviteMemberDetailsRepository.save(inviteObj);
					}
				}
			}
		}
	}
	
	/**
	 * Gets the export invite members list.
	 *
	 * @param searchName the search name
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the export invite members list
	 */
	public ByteArrayInputStream getExportInviteMembersList(final String searchName, final String sortOrder,
			final String sortBy) {
		long startTime = new Date().getTime();
		log.info("Admin invite members export starts.");
		long temp = new Date().getTime();
		List<InviteMemberDetails> list = getAllInviteMembersDetails(searchName, sortOrder, sortBy);
		log.info("Get data from db " + (new Date().getTime() - temp));
		ValidationUtils.emptyList(list);
		temp = new Date().getTime();
		CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
		ByteArrayInputStream byteArrayInputStream;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format)) {
			csvPrinter.printRecord(ExportConstants.EXPORT_INVITE_MEMBERS);
			for (InviteMemberDetails inviteMember : list) {
				List<Object> rowData = new ArrayList<>();
				// Email
				rowData.add(inviteMember.getEmail() != null ? inviteMember.getEmail() : "");
				// First Name
				String memberFirstName = inviteMember.getFirstName() != null ? inviteMember.getFirstName() : "";
				rowData.add(memberFirstName);
				// Last Name
				String memberLastName = inviteMember.getLastName() != null ? inviteMember.getLastName() : "";
				rowData.add(memberLastName);
				// Status
				String status = inviteMember.isUserRegistered() ? DBConstants.INVITE_MEMBER_STATUS_REGISTERED
						: DBConstants.INVITE_MEMBER_STATUS_PENDING;
				rowData.add(status);
				// Invite Date
				rowData.add(fitwiseUtils.formatDate(inviteMember.getCreatedDate()));
				csvPrinter.printRecord(rowData);
			}
			csvPrinter.flush();
			byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());
			log.info("Response construction " + (new Date().getTime() - temp));
		} catch (Exception exception) {
			throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAYOUTS_CSV_GENERATION_FAILED,
					MessageConstants.ERROR);
		}
		log.info("Export invite members completed : " + (new Date().getTime() - startTime));
		return byteArrayInputStream;
	}
	
	/**
	 * Gets the all invite members details.
	 *
	 * @param search the search
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the all invite members details
	 */
	public List<InviteMemberDetails> getAllInviteMembersDetails(final String search, final String sortOrder,
			final String sortBy) {
		Specification<InviteMemberDetails> inviteSortSpec = InviteMemberSpecification
				.getInviteMemberSortingSpecification(sortOrder, sortBy);
		Specification<InviteMemberDetails> finalSpec = inviteSortSpec;
		if (search != null) {
			Specification<InviteMemberDetails> inviteSearchSpec = InviteMemberSpecification
					.getInviteMemberSearchSpecification(search);
			finalSpec = finalSpec.and(inviteSearchSpec);
		}
		return inviteMemberDetailsRepository.findAll(finalSpec);
	}
	
	/**
	 * Gets the existing invite member record.
	 *
	 * @param email the email
	 * @return the existing invite member record
	 */
	private InviteMemberDetails getExistingInviteMemberRecord(String email) {
		InviteMemberDetails inviteMember = new InviteMemberDetails();
		List<InviteMemberDetails> inviteMemberList = inviteMemberDetailsRepository.findByEmail(email);
		if (!inviteMemberList.isEmpty()) {
			inviteMember = inviteMemberList.iterator().next();
		}
		return inviteMember;
	}

	public ByteArrayInputStream bulkInviteSampleCsv() {

		String csvUrl = generalProperties.getBulkInviteSampleCsvUrl();

		ByteArrayInputStream byteArrayInputStream;
		HttpURLConnection conn = null;
		InputStream stream = null;
		try {
			URL url = new URL(csvUrl);
			conn = (HttpURLConnection) url.openConnection();
			stream = conn.getInputStream();
			final byte[] bytes = IOUtils.toByteArray(stream);
			byteArrayInputStream = new ByteArrayInputStream(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_BULK_UPLOAD_SAMPLE_CSV_FAILED, MessageConstants.ERROR);
		} finally {
			if (null != conn) {
				conn.disconnect();
			}
			if (null != stream) {
				IOUtils.closeQuietly(stream);
			}
		}
		return byteArrayInputStream;
	}
}