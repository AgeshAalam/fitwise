package com.fitwise.service.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.ExportConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserActiveInactiveTracker;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.product.FreeAccessPackages;
import com.fitwise.entity.product.FreeAccessProgram;
import com.fitwise.entity.product.FreeProduct;
import com.fitwise.entity.product.FreeProductUserSpecific;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.UserActiveInactiveTrackerRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.product.FreeAccessPackageReposity;
import com.fitwise.repository.product.FreeAccessProgramReposity;
import com.fitwise.repository.product.FreeProductRepository;
import com.fitwise.repository.product.FreeProductUserSpecificRepository;
import com.fitwise.request.FreeAccessAddRequest;
import com.fitwise.response.AdminListResponse;
import com.fitwise.response.freeaccess.AddFreeAccess;
import com.fitwise.response.freeaccess.AddFreeAccess.FreeAccessResponse;
import com.fitwise.response.freeaccess.FreeAccessPackageDetails;
import com.fitwise.response.freeaccess.FreeAccessProgramDetails;
import com.fitwise.specifications.freeproduct.FreeProductUserSpecificSpecification;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ResponseModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class AdminFreeAccessService.
 */
@Service

/**
 * Instantiates a new admin free access service.
 *
 * @param programRepository     the program repository
 * @param freeProductRepository the free product repository
 */
@RequiredArgsConstructor
@Slf4j
public class AdminFreeAccessService {

	/** The program repository. */
	private final ProgramRepository programRepository;

	/** The free product repository. */
	private final FreeProductRepository freeProductRepository;
	
    /** The user profile repository. */
    private final UserProfileRepository userProfileRepository;

    /** The subscription package repository. */
    private final SubscriptionPackageRepository subscriptionPackageRepository;
    
    private final UserRepository userRepository;
    private final FreeProductUserSpecificRepository freeProductUserSpecificRepository;
	private final FreeAccessProgramReposity freeAccessProgramReposity;
	private final FreeAccessPackageReposity freeAccessPackageReposity;
    private final FitwiseUtils fitwiseUtils;
    private final UserActiveInactiveTrackerRepository userActiveInactiveTrackerRepository;
    private final UserRoleRepository userRoleRepository;

	/**
	 * Adds the free programs for all users.
	 *
	 * @param programIds the program ids
	 */
	@Transactional
	public ResponseModel addFreeProgramsForAllUsers(final List<Long> programIds) {
		ResponseModel responseModel = new ResponseModel();
		if (programIds.isEmpty()) {
			throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, null);
		}
		for (Long programId : programIds) {
			String specifyIdInMessage = " for programId " + programId;
			Programs program = programRepository.findByProgramId(programId);
			if (program == null) {
				throw new ApplicationException(Constants.BAD_REQUEST,
						ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND + specifyIdInMessage, null);
			}
			if (!program.getStatus().equalsIgnoreCase(InstructorConstant.PUBLISH)) {
				throw new ApplicationException(Constants.FORBIDDEN,
						ValidationMessageConstants.MSG_ERR_PGM_NOT_ACCESSIBLE + program.getTitle(), null);
			}

			FreeAccessProgram freeAccessProgram = freeAccessProgramReposity.findByProgramAndFreeProductType(program,
					DBConstants.FREE_ACCESS_TYPE_ALL);
			if (freeAccessProgram != null) {
				if (freeAccessProgram.isActive()) {
					throw new ApplicationException(Constants.BAD_REQUEST,
							ValidationMessageConstants.MSG_FREE_ACCESS_PROGRAM_IS_EXITS + specifyIdInMessage, null);
				} else {
					freeAccessProgram.setActive(true);
				}
			} else {
				FreeProduct freeAccess = new FreeProduct();
				freeAccess.setType(DBConstants.FREE_ACCESS_TYPE_ALL);
				freeAccess = freeProductRepository.save(freeAccess);
				freeAccessProgram = new FreeAccessProgram();
				freeAccessProgram.setActive(true);
				freeAccessProgram.setFreeProduct(freeAccess);
				freeAccessProgram.setProgram(program);

			}
			freeAccessProgramReposity.save(freeAccessProgram);
		}
		responseModel.setStatus(Constants.SUCCESS_STATUS);
		responseModel.setMessage(MessageConstants.MSG_FREE_ACCESS_PROGRAM_ADD_ALL_USERS);
		return responseModel;
	}

	/**
	 * Removes the free programs for all users.
	 *
	 * @param programIds the program ids
	 */
	@Transactional
	public ResponseModel removeFreeProgramsForAllUsers(final List<Long> programIds) throws ApplicationException {
		ResponseModel responseModel = new ResponseModel();
		if (programIds.isEmpty()) {
			throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, null);
		}
		for (Long programId : programIds) {
			String specifyIdInMessage = " for programId " + programId;
			Programs program = programRepository.findByProgramId(programId);
			if (program == null) {
				throw new ApplicationException(Constants.BAD_REQUEST,
						ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND + specifyIdInMessage, null);
			}
			FreeAccessProgram freeAccessProgram = freeAccessProgramReposity.findByProgramAndFreeProductType(program,
					DBConstants.FREE_ACCESS_TYPE_ALL);
			if (freeAccessProgram != null) {
				if (!freeAccessProgram.isActive()) {
					throw new ApplicationException(Constants.BAD_REQUEST,
							ValidationMessageConstants.MSG_FREE_ACCESS_PROGRAM_IS_REMOVED + specifyIdInMessage, null);
				}
			} else {
				throw new ApplicationException(Constants.BAD_REQUEST,
						ValidationMessageConstants.MSG_FREE_ACCESS_PROGRAM_NOT_FOUND + specifyIdInMessage, null);
			}
			freeAccessProgram.setActive(false);
			freeAccessProgramReposity.save(freeAccessProgram);
		}
		responseModel.setStatus(Constants.SUCCESS_STATUS);
		responseModel.setMessage(MessageConstants.MSG_FREE_ACCESS_PROGRAM_REMOVE_ALL_USERS);
		return responseModel;
	}
	
	/**
	 * Adds the program and packages for specific members.
	 *
	 * @param freeAccessAddRequest the free access add request
	 * @return the response model
	 * @throws ApplicationException the application exception
	 * @throws ParseException 
	 */
	public ResponseModel addProgramAndPackagesForSpecificMembers(final FreeAccessAddRequest freeAccessAddRequest)
			throws ApplicationException, ParseException {
		ResponseModel response = new ResponseModel();
		AddFreeAccess freeAccessProgramPackageResponse = new AddFreeAccess();
		validateFreeAccessAddRequest(freeAccessAddRequest);
		Date freeAccessStartDate = fitwiseUtils.convertDateStringToDate(freeAccessAddRequest.getFreeAccessStartDate());
		Date freeAccessEndDate = fitwiseUtils.convertDateStringToDate(freeAccessAddRequest.getFreeAccessEndDate());
		// Create new free product for individual 
		FreeProduct freeProduct = new FreeProduct();
		freeProduct.setType(DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL);
		freeProduct.setFreeAccessStartDate(freeAccessStartDate);
		if (freeAccessAddRequest.getFreeAccessEndDate() != null) {
			freeProduct.setFreeAccessEndDate(freeAccessEndDate);
		}
		freeProduct = freeProductRepository.save(freeProduct);
		Map<Long, User> userIdMapping = new HashedMap<>();
		// Validate member Ids
		for (Long memberId : freeAccessAddRequest.getMemberIdList()) {
			User member = userRepository.findByUserId(memberId);
			if (member != null) {
				userIdMapping.put(memberId, member);
			}
		}
		List<FreeAccessResponse> programResponseList = addFreeAccessProgramsAndValidate(freeAccessAddRequest,
				freeProduct, userIdMapping);
		List<FreeAccessResponse> packageResponseList = addFreeAccessPackagesAndValidate(freeAccessAddRequest,
				freeProduct, userIdMapping);
		freeAccessProgramPackageResponse.setFreePrograms(programResponseList);
		freeAccessProgramPackageResponse.setFreePackages(packageResponseList);
		response.setStatus(Constants.SUCCESS_STATUS);
		response.setMessage(MessageConstants.MSG_FREE_ACCESS_ADDED_INDIVIDUAL_USERS);
		response.setPayload(freeAccessProgramPackageResponse);
		return response;
	}
	
	/**
	 * Revoke free access for program or package.
	 *
	 * @param freeAccessSpecificId the free access specific id
	 * @return the response model
	 */
	@Transactional
	public ResponseModel revokeFreeAccessForProgramOrPackage(final long freeAccessSpecificId) {
		ResponseModel response = new ResponseModel();
		FreeProductUserSpecific freeAccess = freeProductUserSpecificRepository
				.findByFreeProductUserSpecificId(freeAccessSpecificId);
		if (freeAccess == null) {
			throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FREE_ACCESS_INVALID_FREE_PRODUCT_USER_SPECIFIC_ID, null);
		}
		freeProductUserSpecificRepository.delete(freeAccess);
		response.setStatus(Constants.SUCCESS_STATUS);
		response.setMessage(MessageConstants.MSG_FREE_ACCESS_REMOVE_INDIVIDUAL_USERS);
		return response;
	}
	
	/**
	 * Validate free access add request.
	 *
	 * @param freeAccessAddRequest the free access add request
	 * @throws ParseException 
	 */
	private void validateFreeAccessAddRequest(final FreeAccessAddRequest freeAccessAddRequest) throws ParseException {
		Date freeAccessStartDate = fitwiseUtils.convertDateStringToDate(freeAccessAddRequest.getFreeAccessStartDate());
		Date freeAccessEndDate = fitwiseUtils.convertDateStringToDate(freeAccessAddRequest.getFreeAccessEndDate());

		if (freeAccessAddRequest.getMemberIdList().isEmpty()) {
			throw new ApplicationException(Constants.BAD_REQUEST,
					ValidationMessageConstants.MSG_FREE_ACCESS_MEMBER_ID_LIST_NOT_EMPTY, null);
		} else if (freeAccessAddRequest.getProgramIdList().isEmpty()
				&& freeAccessAddRequest.getPackageIdList().isEmpty()) {
			throw new ApplicationException(Constants.BAD_REQUEST,
					ValidationMessageConstants.MSG_FREE_ACCESS_PROGRAM_PACKAGE_ID_LIST_NOT_EMPTY, null);
		} else if (freeAccessAddRequest.getFreeAccessStartDate() == null || freeAccessAddRequest.getFreeAccessStartDate().isEmpty()) {
			throw new ApplicationException(Constants.BAD_REQUEST,
					ValidationMessageConstants.MSG_FREE_ACCESS_START_DATE_NOT_NULL_EMPTY, null);
		}
		if (!freeAccessAddRequest.isInfiniteTimeDuration()) {
			if (freeAccessAddRequest.getFreeAccessEndDate() == null || freeAccessAddRequest.getFreeAccessEndDate().isEmpty()) {
				throw new ApplicationException(Constants.BAD_REQUEST,
						ValidationMessageConstants.MSG_FREE_ACCESS_END_DATE_NOT_NULL_EMPTY, null);
			} else if (!freeAccessEndDate.after(freeAccessStartDate)) {
				throw new ApplicationException(Constants.BAD_REQUEST,
						ValidationMessageConstants.MSG_FREE_ACCESS_END_DATE_AFTER_START_DATE, null);
			}

		}
	}
	
	/**
	 * Gets the free access programs list.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @return the free access programs list
	 */
	public ResponseModel getFreeAccessProgramsList(final int pageNo, final int pageSize, final String search,
			final String sortOrder, final String sortBy) {
		ResponseModel response = new ResponseModel();
		RequestParamValidator.pageSetup(pageNo, pageSize);
		response.setStatus(Constants.SUCCESS_STATUS);
		response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
		AdminListResponse<FreeAccessProgramDetails> res = new AdminListResponse<>();
		try {
			List<Long> freeProductIndividualTypeIdList = getFreeProductIdListForType(
					DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL);
			if (!freeProductIndividualTypeIdList.isEmpty()) {
				List<Long> freeAccessProgramIds = getFreeAccessProgramIdsByInTypeId(freeProductIndividualTypeIdList);
				if (!freeAccessProgramIds.isEmpty()) {
					PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
					Page<FreeProductUserSpecific> freeAccessProgramPageRequest = freeProductUserSpecificRecordsByInProgramOrPackageTypeIds(
							freeAccessProgramIds, pageRequest, true, search, sortOrder, sortBy);
					List<FreeAccessProgramDetails> parseList = parseFreeAccessUserSpecificProductProgram(
							freeAccessProgramPageRequest.getContent());
					res.setTotalSizeOfList(freeAccessProgramPageRequest.getTotalElements());
					res.setPayloadOfAdmin(parseList);
					response.setPayload(res);
				}
			}
			if (response.getPayload() == null) {
				res.setTotalSizeOfList(0);
				res.setPayloadOfAdmin(new ArrayList<FreeAccessProgramDetails>());
				response.setPayload(res);
			}
		} catch (ApplicationException exception) {
			log.error(exception.getMessage());
			response.setStatus(exception.getStatus());
			response.setMessage(exception.getMessage());
		} catch (Exception exception) {
			log.error(exception.getMessage());
			response.setStatus(Constants.ERROR_STATUS);
			response.setMessage(MessageConstants.ERROR_INTERNAL_SERVER_FAILURE);
		}
		return response;
	}
	
	/**
	 * Gets the free access package list.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @return the free access package list
	 */
	public ResponseModel getFreeAccessPackageList(final int pageNo, final int pageSize, final String search,
			final String sortOrder, final String sortBy) {
		ResponseModel response = new ResponseModel();
		RequestParamValidator.pageSetup(pageNo, pageSize);
		response.setStatus(Constants.SUCCESS_STATUS);
		response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
		AdminListResponse<FreeAccessPackageDetails> res = new AdminListResponse<>();
		try {
			List<Long> freeProductIndividualTypeIdList = getFreeProductIdListForType(
					DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL);
			if (!freeProductIndividualTypeIdList.isEmpty()) {
				List<Long> freeAccessPackagesIds = getFreeAccessPackageIdsByInTypeId(freeProductIndividualTypeIdList);
				if (!freeAccessPackagesIds.isEmpty()) {
					PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
					Page<FreeProductUserSpecific> freeAccessPackagePageRequest = freeProductUserSpecificRecordsByInProgramOrPackageTypeIds(
							freeAccessPackagesIds, pageRequest, false, search, sortOrder, sortBy);
					List<FreeAccessPackageDetails> parseList = parseFreeAccessUserSpecificProductPackage(
							freeAccessPackagePageRequest.getContent());
					res.setTotalSizeOfList(freeAccessPackagePageRequest.getTotalElements());
					res.setPayloadOfAdmin(parseList);
					response.setPayload(res);
				}
			}
			if (response.getPayload() == null) {
				res.setTotalSizeOfList(0);
				res.setPayloadOfAdmin(new ArrayList<FreeAccessPackageDetails>());
				response.setPayload(res);
			}
		} catch (ApplicationException exception) {
			log.error(exception.getMessage());
			response.setStatus(exception.getStatus());
			response.setMessage(exception.getMessage());
		} catch (Exception exception) {
			log.error(exception.getMessage());
			response.setStatus(Constants.ERROR_STATUS);
			response.setMessage(MessageConstants.ERROR_INTERNAL_SERVER_FAILURE);
		}
		return response;
	}
	
	/**
	 * Parses the free access user specific product program.
	 *
	 * @param pageRequestList the page request list
	 * @return the list
	 */
	private List<FreeAccessProgramDetails> parseFreeAccessUserSpecificProductProgram(
			List<FreeProductUserSpecific> pageRequestList) {
		UserRole userRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_MEMBER);
		List<FreeAccessProgramDetails> list = new ArrayList<>();
		for (FreeProductUserSpecific userProduct : pageRequestList) {
			FreeAccessProgramDetails details = new FreeAccessProgramDetails();
			details.setFreeAccessUserSpecificId(userProduct.getFreeProductUserSpecificId());
			details.setFreeAccessStartDate(userProduct.getFreeProduct().getFreeAccessStartDate());
			details.setFreeAccessStartDateFormatted(
					fitwiseUtils.formatDate(userProduct.getFreeProduct().getFreeAccessStartDate()));
			if (userProduct.getFreeAccessProgram() != null) {
				details.setProgramName(userProduct.getFreeAccessProgram().getProgram().getTitle());
			}
			if (userProduct.getFreeProduct().getFreeAccessEndDate() != null) {
				details.setFreeAccessEndDate(userProduct.getFreeProduct().getFreeAccessEndDate());
				details.setFreeAccessEndDateFormatted(
						fitwiseUtils.formatDate(userProduct.getFreeProduct().getFreeAccessEndDate()));
			}
			if (userProduct.getUser() != null) {
				UserProfile userProfile = userProfileRepository.findByUser(userProduct.getUser());
				if (userProfile != null) {
					details.setFirstName(userProfile.getFirstName());
					details.setLastName(userProfile.getLastName());
					if (userProfile.getProfileImage() != null)
						details.setImageUrl(userProfile.getProfileImage().getImagePath());
				}
				details.setEmail(userProduct.getUser().getEmail());
				details.setOnboardedOn(userProduct.getUser().getCreatedDate());
				details.setOnboardedDateFormatted(fitwiseUtils.formatDate(userProduct.getUser().getCreatedDate()));
				UserActiveInactiveTracker lastActivity = userActiveInactiveTrackerRepository
						.findTopByUserUserIdAndUserRoleRoleIdOrderByIdDesc(userProduct.getUser().getUserId(),
								userRole.getRoleId());
				if (lastActivity != null) {
					details.setLastAccess(lastActivity.getModifiedDate());
					details.setLastAccessFormatted(fitwiseUtils.formatDate(lastActivity.getModifiedDate()));
				}
			}
			list.add(details);
		}
		return list;
	}
	
	/**
	 * Parses the free access user specific product package.
	 *
	 * @param pageRequestList the page request list
	 * @return the list
	 */
	private List<FreeAccessPackageDetails> parseFreeAccessUserSpecificProductPackage(
			List<FreeProductUserSpecific> pageRequestList) {
		UserRole userRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_MEMBER);
		List<FreeAccessPackageDetails> list = new ArrayList<>();
		for (FreeProductUserSpecific userProduct : pageRequestList) {
			FreeAccessPackageDetails details = new FreeAccessPackageDetails();
			details.setFreeAccessUserSpecificId(userProduct.getFreeProductUserSpecificId());
			details.setFreeAccessStartDate(userProduct.getFreeProduct().getFreeAccessStartDate());
			details.setFreeAccessStartDateFormatted(
					fitwiseUtils.formatDate(userProduct.getFreeProduct().getFreeAccessStartDate()));
			if (userProduct.getFreeAccessSubscriptionPackages() != null) {
				details.setTitle(userProduct.getFreeAccessSubscriptionPackages().getSubscriptionPackage().getTitle());
			}
			if (userProduct.getFreeProduct().getFreeAccessEndDate() != null) {
				details.setFreeAccessEndDate(userProduct.getFreeProduct().getFreeAccessEndDate());
				details.setFreeAccessEndDateFormatted(
						fitwiseUtils.formatDate(userProduct.getFreeProduct().getFreeAccessEndDate()));
			}
			if (userProduct.getUser() != null) {
				UserProfile userProfile = userProfileRepository.findByUser(userProduct.getUser());
				if (userProfile != null) {
					details.setFirstName(userProfile.getFirstName());
					details.setLastName(userProfile.getLastName());
					if (userProfile.getProfileImage() != null)
						details.setImageUrl(userProfile.getProfileImage().getImagePath());
				}
				details.setEmail(userProduct.getUser().getEmail());
				details.setOnboardedOn(userProduct.getUser().getCreatedDate());
				details.setOnboardedDateFormatted(fitwiseUtils.formatDate(userProduct.getUser().getCreatedDate()));
				UserActiveInactiveTracker lastActivity = userActiveInactiveTrackerRepository
						.findTopByUserUserIdAndUserRoleRoleIdOrderByIdDesc(userProduct.getUser().getUserId(),
								userRole.getRoleId());
				if (lastActivity != null) {
					details.setLastAccess(lastActivity.getModifiedDate());
					details.setLastAccessFormatted(fitwiseUtils.formatDate(lastActivity.getModifiedDate()));
				}
			}
			list.add(details);
		}
		return list;
	}
	

	/**
	 * Free product user specific records by in program or package type ids.
	 *
	 * @param freeAccessIds the free access ids
	 * @param pageRequest the page request
	 * @param isProgram the is program
	 * @return the page
	 */
	public Page<FreeProductUserSpecific> freeProductUserSpecificRecordsByInProgramOrPackageTypeIds(List<Long> freeAccessIds,
			PageRequest pageRequest, boolean isProgram, String search, final String sortOrder, final String sortBy) {
		Specification<FreeProductUserSpecific> idSpec = null;
		Specification<FreeProductUserSpecific> searchSpec = null;
		Specification<FreeProductUserSpecific> orderByIdSpec = null;
		// active free access spec
		String now = fitwiseUtils.formatDate(new Date());
		Date currentDate = fitwiseUtils.constructDateWithoutTimeZone(now);
		Specification<FreeProductUserSpecific> activeAndFutureFreeAccessSpec = FreeProductUserSpecificSpecification
				.getActiveAndFutureFreeProgarmsAndPackagesBasesdOnDates(currentDate);
		if (sortOrder != null && sortBy != null) {
			// Sorting based on user selection
			validateFreeAccessSortParameterValues(sortOrder, sortBy);
			orderByIdSpec = FreeProductUserSpecificSpecification.getFreeAccessSortSpecification(sortOrder, sortBy);
		} else {
			// Default sorting based on free access user specific Id
			orderByIdSpec = FreeProductUserSpecificSpecification
					.orderByFreeProductUserSpecificId(SearchConstants.ORDER_DSC);
		}
		if (isProgram) {
			idSpec = FreeProductUserSpecificSpecification.getFreeAccessProgramsInIdList(freeAccessIds);
			if (search != null && !search.isEmpty()) {
				searchSpec = FreeProductUserSpecificSpecification.getFreeAccessProgramsBySearch(search);
			}
		} else {
			idSpec = FreeProductUserSpecificSpecification.getFreeAccessPackageInIdList(freeAccessIds);
			if (search != null && !search.isEmpty()) {
				searchSpec = FreeProductUserSpecificSpecification.getFreeAccessPackageBySearch(search);
			}
		}
		Specification<FreeProductUserSpecific> finalSpec = idSpec.and(activeAndFutureFreeAccessSpec).and(orderByIdSpec);
		if (search != null && !search.isEmpty()) {
			finalSpec = idSpec.and(activeAndFutureFreeAccessSpec).and(searchSpec).and(orderByIdSpec);
		}
		return freeProductUserSpecificRepository.findAll(finalSpec, pageRequest);
	}
	
	/**
	 * Gets the free product id list for type.
	 *
	 * @param type the type
	 * @return the free product id list for type
	 */
	private List<Long> getFreeProductIdListForType(String type) {
		List<FreeProduct> individualTypeFreeProductList = freeProductRepository.findByType(type);
		return individualTypeFreeProductList.stream().map(freeProduct -> freeProduct.getFreeProductId())
				.collect(Collectors.toList());
	}
	
	/**
	 * Gets the free access program ids by in type id.
	 *
	 * @param freeProductIndividualTypeIdList the free product individual type id list
	 * @return the free access program ids by in type id
	 */
	private List<Long> getFreeAccessProgramIdsByInTypeId(List<Long> freeProductIndividualTypeIdList) {
		List<FreeAccessProgram> freeAccessProgramList = freeAccessProgramReposity
				.findByIsActiveAndFreeProductFreeProductIdIn(true, freeProductIndividualTypeIdList);
		return freeAccessProgramList.stream().map(freeAccessProgram -> freeAccessProgram.getFreeAccessProgramId())
				.collect(Collectors.toList());
	}
	
	/**
	 * Gets the free access package ids by in type id.
	 *
	 * @param freeProductIndividualTypeIdList the free product individual type id list
	 * @return the free access package ids by in type id
	 */
	private List<Long> getFreeAccessPackageIdsByInTypeId(List<Long> freeProductIndividualTypeIdList) {
		List<FreeAccessPackages> freeAccessPackageList = freeAccessPackageReposity
				.findByIsActiveAndFreeProductFreeProductIdIn(true, freeProductIndividualTypeIdList);
		return freeAccessPackageList.stream().map(freeAccessPackage -> freeAccessPackage.getFreeAccessPackageId())
				.collect(Collectors.toList());
	}
	
	
	/**
	 * Validate free access sort parameter values.
	 *
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 */
	private void validateFreeAccessSortParameterValues(String sortOrder, String sortBy) {
		List<String> allowedSortByList = Arrays.asList(SearchConstants.MEMBER_NAME, SearchConstants.USER_EMAIL,
				SearchConstants.ONBOARDED_DATE, SearchConstants.PROGRAM_NAME, SearchConstants.PACKAGE_NAME,
				SearchConstants.FREE_ACCESS_START_DATE, SearchConstants.FREE_ACCESS_END_DATE,
				SearchConstants.USER_LAST_ACCESS_DATE);
		RequestParamValidator.sortList(allowedSortByList, sortBy, sortOrder);
	}
	
	/**
	 * Free product user specific records by in program or package type ids for export.
	 *
	 * @param freeAccessIds the free access ids
	 * @param isProgram the is program
	 * @param search the search
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the list
	 */
	public List<FreeProductUserSpecific> freeProductUserSpecificRecordsByInProgramOrPackageTypeIdsForExport(
			final List<Long> freeAccessIds, final boolean isProgram, final String search, final String sortOrder, final String sortBy) {
		Specification<FreeProductUserSpecific> idSpec = null;
		Specification<FreeProductUserSpecific> searchSpec = null;
		Specification<FreeProductUserSpecific> orderByIdSpec = null;
		if (sortOrder != null && sortBy != null) {
			// Sorting based on user selection
			validateFreeAccessSortParameterValues(sortOrder, sortBy);
			orderByIdSpec = FreeProductUserSpecificSpecification.getFreeAccessSortSpecification(sortOrder, sortBy);
		} else {
			// Default sorting based on free access user specific Id
			orderByIdSpec = FreeProductUserSpecificSpecification
					.orderByFreeProductUserSpecificId(SearchConstants.ORDER_DSC);
		}
		if (isProgram) {
			idSpec = FreeProductUserSpecificSpecification.getFreeAccessProgramsInIdList(freeAccessIds);
			if (search != null && !search.isEmpty()) {
				searchSpec = FreeProductUserSpecificSpecification.getFreeAccessProgramsBySearch(search);
			}
		} else {
			idSpec = FreeProductUserSpecificSpecification.getFreeAccessPackageInIdList(freeAccessIds);
			if (search != null && !search.isEmpty()) {
				searchSpec = FreeProductUserSpecificSpecification.getFreeAccessPackageBySearch(search);
			}
		}
		Specification<FreeProductUserSpecific> finalSpec = idSpec.and(orderByIdSpec);
		if (search != null && !search.isEmpty()) {
			finalSpec = idSpec.and(searchSpec).and(orderByIdSpec);
		}
		return freeProductUserSpecificRepository.findAll(finalSpec);
	}
	
	/**
	 * Gets the free access program all data.
	 *
	 * @param search the search
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the free access program all data
	 */
	public List<FreeAccessProgramDetails> getFreeAccessProgramAllData(final String search, final String sortOrder,
			final String sortBy) {
		List<FreeAccessProgramDetails> parseList = new ArrayList<>();
		List<Long> freeProductIndividualTypeIdList = getFreeProductIdListForType(
				DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL);
		if (!freeProductIndividualTypeIdList.isEmpty()) {
			List<Long> freeAccessProgramIds = getFreeAccessProgramIdsByInTypeId(freeProductIndividualTypeIdList);
			if (!freeAccessProgramIds.isEmpty()) {
				List<FreeProductUserSpecific> freeAccessProgramPageRequest = freeProductUserSpecificRecordsByInProgramOrPackageTypeIdsForExport(
						freeAccessProgramIds, true, search, sortOrder, sortBy);
				parseList = parseFreeAccessUserSpecificProductProgram(freeAccessProgramPageRequest);
			}
		}
		return parseList;
	}
	
	/**
	 * Gets the free access package all data.
	 *
	 * @param search the search
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the free access package all data
	 */
	public List<FreeAccessPackageDetails> getFreeAccessPackageAllData(final String search, final String sortOrder,
			final String sortBy) {
		List<FreeAccessPackageDetails> packageList = new ArrayList<>();
		List<Long> freeProductIndividualTypeIdList = getFreeProductIdListForType(
				DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL);
		if (!freeProductIndividualTypeIdList.isEmpty()) {
			List<Long> freeAccessPackagesIds = getFreeAccessPackageIdsByInTypeId(freeProductIndividualTypeIdList);
			if (!freeAccessPackagesIds.isEmpty()) {
				List<FreeProductUserSpecific> freeAccessPackagePageRequest = freeProductUserSpecificRecordsByInProgramOrPackageTypeIdsForExport(
						freeAccessPackagesIds, false, search, sortOrder, sortBy);
				packageList = parseFreeAccessUserSpecificProductPackage(freeAccessPackagePageRequest);
			}
		}
		return packageList;
	}
	
	/**
	 * Export program list.
	 *
	 * @param searchName the search name
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the byte array input stream
	 */
	public ByteArrayInputStream exportProgramList(final String searchName, final String sortOrder, final String sortBy) {
		long startTime = new Date().getTime();
		log.info("Admin free access program export starts.");
		long temp = new Date().getTime();
		List<FreeAccessProgramDetails> list = getFreeAccessProgramAllData(searchName, sortOrder, sortBy);
		log.info("Get data from db " + (new Date().getTime() - temp));
		ValidationUtils.emptyList(list);
		temp = new Date().getTime();
		CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
		ByteArrayInputStream byteArrayInputStream;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format)) {
			csvPrinter.printRecord(ExportConstants.EXPORT_FREE_ACCESS_HEADER_PROGRAM);
			for (FreeAccessProgramDetails viewMember : list) {
				List<Object> rowData = new ArrayList<>();
				// Member Name
				String memberFirstName = viewMember.getFirstName() != null ? viewMember.getFirstName() : "";
				String memberLastName = viewMember.getLastName() != null ? viewMember.getLastName() : "";
				rowData.add(memberFirstName + " " + memberLastName);
				// Email
				rowData.add(viewMember.getEmail() != null ? viewMember.getEmail() : "");
				// Onboarding Date
				rowData.add(viewMember.getOnboardedDateFormatted());
				// Program Name
				rowData.add(viewMember.getProgramName());
				// Free Access Start Date
				rowData.add(viewMember.getFreeAccessStartDateFormatted() != null
						? viewMember.getFreeAccessStartDateFormatted()
						: "");
				// Free Access End Date
				rowData.add(
						viewMember.getFreeAccessEndDateFormatted() != null ? viewMember.getFreeAccessEndDateFormatted()
								: "NA");
				// Last Active Date
				rowData.add(viewMember.getLastAccessFormatted());
				csvPrinter.printRecord(rowData);
			}
			csvPrinter.flush();
			byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());
			log.info("Response construction " + (new Date().getTime() - temp));
		} catch (Exception exception) {
			throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAYOUTS_CSV_GENERATION_FAILED,
					MessageConstants.ERROR);
		}
		log.info("Export free access program completed : " + (new Date().getTime() - startTime));
		return byteArrayInputStream;
	}
	
	/**
	 * Export package list.
	 *
	 * @param searchName the search name
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the byte array input stream
	 */
	public ByteArrayInputStream exportPackageList(final String searchName, final String sortOrder, final String sortBy) {
		long startTime = new Date().getTime();
		log.info("Admin free access package export starts.");
		long temp = new Date().getTime();
		List<FreeAccessPackageDetails> list = getFreeAccessPackageAllData(searchName, sortOrder, sortBy);
		log.info("Get data from db " + (new Date().getTime() - temp));
		ValidationUtils.emptyList(list);
		temp = new Date().getTime();
		CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
		ByteArrayInputStream byteArrayInputStream;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format)) {
			csvPrinter.printRecord(ExportConstants.EXPORT_FREE_ACCESS_HEADER_PACKAGE);
			for (FreeAccessPackageDetails viewMember : list) {
				List<Object> rowData = new ArrayList<>();
				// Member Name
				String memberFirstName = viewMember.getFirstName() != null ? viewMember.getFirstName() : "";
				String memberLastName = viewMember.getLastName() != null ? viewMember.getLastName() : "";
				rowData.add(memberFirstName + " " + memberLastName);
				// Email
				rowData.add(viewMember.getEmail() != null ? viewMember.getEmail() : "");
				// Onboarding Date
				rowData.add(viewMember.getOnboardedDateFormatted());
				// Package Name
				rowData.add(viewMember.getTitle());
				// Free Access Start Date
				rowData.add(viewMember.getFreeAccessStartDateFormatted() != null
						? viewMember.getFreeAccessStartDateFormatted()
						: "");
				// Free Access End Date
				rowData.add(
						viewMember.getFreeAccessEndDateFormatted() != null ? viewMember.getFreeAccessEndDateFormatted()
								: "NA");
				// Last Active Date
				rowData.add(viewMember.getLastAccessFormatted());
				csvPrinter.printRecord(rowData);
			}
			csvPrinter.flush();
			byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());
			log.info("Response construction " + (new Date().getTime() - temp));
		} catch (Exception exception) {
			throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAYOUTS_CSV_GENERATION_FAILED,
					MessageConstants.ERROR);
		}
		log.info("Export free access package completed : " + (new Date().getTime() - startTime));
		return byteArrayInputStream;
	}
	
	/**
	 * Check free access date present in programs.
	 *
	 * @param user the user
	 * @param program the program
	 * @param date the date
	 * @param checkWithEndDateAlso the check with end date also
	 * @return true, if successful
	 */
	public boolean checkFreeAccessDatePresentInPrograms(User user, Programs program, Date date,
			boolean checkWithEndDateAlso) {
		boolean isPresent = false;
		Specification<FreeProductUserSpecific> spec = FreeProductUserSpecificSpecification
				.getUserFreeProgramBetweenDates(user, program, date, checkWithEndDateAlso);
		List<FreeProductUserSpecific> list = freeProductUserSpecificRepository.findAll(spec);
		if (!list.isEmpty()) {
			isPresent = true;
		}
		return isPresent;
	}
	
	/**
	 * Check free access date present in package.
	 *
	 * @param user the user
	 * @param subscriptionPackage the subscription package
	 * @param date the date
	 * @param checkWithEndDateAlso the check with end date also
	 * @return true, if successful
	 */
	public boolean checkFreeAccessDatePresentInPackage(User user, SubscriptionPackage subscriptionPackage, Date date,
			boolean checkWithEndDateAlso) {
		boolean isPresent = false;
		Specification<FreeProductUserSpecific> spec = FreeProductUserSpecificSpecification
				.getUserFreePackagesBetweenDates(user, subscriptionPackage, date, checkWithEndDateAlso);
		List<FreeProductUserSpecific> list = freeProductUserSpecificRepository.findAll(spec);
		if (!list.isEmpty()) {
			isPresent = true;
		}
		return isPresent;
	}
	
	/**
	 * Gets the existing free product by type and dates.
	 *
	 * @param type the type
	 * @param startDate the start date
	 * @param endDate the end date
	 * @return the existing free product by type and dates
	 */
	public FreeProduct getExistingFreeProductByTypeAndDates(final String type, final Date startDate,
			final Date endDate) {
		FreeProduct freeProduct = null;
		List<FreeProduct> list = new ArrayList<>();
		if (endDate != null) {
			list = freeProductRepository.findByTypeAndFreeAccessStartDateAndFreeAccessEndDate(type, startDate, endDate);
		} else {
			list = freeProductRepository.findByTypeAndFreeAccessStartDateAndFreeAccessEndDateIsNull(type, startDate);
		}
		if (!list.isEmpty()) {
			freeProduct = list.get(0);
		}
		return freeProduct;
	}
	
	/**
	 * Adds the free access programs and validate.
	 *
	 * @param freeAccessAddRequest the free access add request
	 * @param freeProduct the free product
	 * @param userIdMapping the user id mapping
	 * @return the list
	 * @throws ParseException 
	 */
	public List<FreeAccessResponse> addFreeAccessProgramsAndValidate(FreeAccessAddRequest freeAccessAddRequest,
			FreeProduct freeProduct, Map<Long, User> userIdMapping) throws ParseException {
		// Program
		List<FreeAccessResponse> programResponseList = new ArrayList<>();
		Date freeAccessStartDate = fitwiseUtils.convertDateStringToDate(freeAccessAddRequest.getFreeAccessStartDate());
		Date freeAccessEndDate = fitwiseUtils.convertDateStringToDate(freeAccessAddRequest.getFreeAccessEndDate());
		log.info("Free access start date : {}, end date : {} ", freeAccessStartDate, freeAccessEndDate);
		for (Long programId : freeAccessAddRequest.getProgramIdList()) {
			Programs program = programRepository.findByProgramId(programId);
			if (program != null) {
				for (Long memberId : freeAccessAddRequest.getMemberIdList()) {
					FreeAccessResponse freeAccessResponse = new FreeAccessResponse();
					User member = userIdMapping.get(memberId);
					// Check existing free access
					boolean isExistingFreeAccessDateConflict = false;
					if (!freeAccessAddRequest.isInfiniteTimeDuration()
							&& freeAccessAddRequest.getFreeAccessEndDate() != null) {
						boolean isStartDateConflict = checkFreeAccessDatePresentInPrograms(member, program,
								freeAccessStartDate, true);
						boolean isEndDateConflict = checkFreeAccessDatePresentInPrograms(member, program,
								freeAccessEndDate, true);
						isExistingFreeAccessDateConflict = isStartDateConflict && isEndDateConflict;
					} else {
						isExistingFreeAccessDateConflict = checkFreeAccessDatePresentInPrograms(member, program,
								freeAccessStartDate, true);
					}
					if (!isExistingFreeAccessDateConflict) {
						FreeAccessProgram freeProgram = new FreeAccessProgram();
						freeProgram.setProgram(program);
						freeProgram.setFreeProduct(freeProduct);
						freeProgram.setActive(true);
						
						FreeProductUserSpecific userSpecific = new FreeProductUserSpecific();
						userSpecific.setUser(member);
						userSpecific.setFreeProduct(freeProduct);
						userSpecific.setFreeAccessProgram(freeProgram);
						freeProductUserSpecificRepository.save(userSpecific);
						freeAccessResponse.setEmail(member.getEmail());
						freeAccessResponse.setStatus(Constants.RESPONSE_SUCCESS);
						freeAccessResponse.setTitle(program.getTitle());
						freeAccessResponse.setMessage(MessageConstants.MSG_FREE_ACCESS_PROGRAM_ADDED_INDIVIDUAL_USERS);
					} else {
						freeAccessResponse.setEmail(member.getEmail());
						freeAccessResponse.setTitle(program.getTitle());
						freeAccessResponse.setStatus(Constants.RESPONSE_ERROR);
						freeAccessResponse.setMessage(
								ValidationMessageConstants.MSG_FREE_ACCESS_PROGRAM_START_OR_END_DATE_IS_CONFLICT);
					}
					programResponseList.add(freeAccessResponse);
				}
			} else {
				// Program not present
				FreeAccessResponse freeAccessResponse = new FreeAccessResponse();
				freeAccessResponse.setEmail("");
				freeAccessResponse.setStatus(Constants.RESPONSE_ERROR);
				freeAccessResponse.setMessage(ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND);
				programResponseList.add(freeAccessResponse);
			}
		}
		return programResponseList;
	}
	
	/**
	 * Adds the free access packages and validate.
	 *
	 * @param freeAccessAddRequest the free access add request
	 * @param freeProduct the free product
	 * @param userIdMapping the user id mapping
	 * @return the list
	 * @throws ParseException 
	 */
	public List<FreeAccessResponse> addFreeAccessPackagesAndValidate(FreeAccessAddRequest freeAccessAddRequest,
			FreeProduct freeProduct, Map<Long, User> userIdMapping) throws ParseException {
		// Packages
		List<FreeAccessResponse> packageResponseList = new ArrayList<>();
		Date freeAccessStartDate = fitwiseUtils.convertDateStringToDate(freeAccessAddRequest.getFreeAccessStartDate());
		Date freeAccessEndDate = fitwiseUtils.convertDateStringToDate(freeAccessAddRequest.getFreeAccessEndDate());
		log.info("Free access start date : {}, end date : {} ", freeAccessStartDate, freeAccessEndDate);
		for (Long packageId : freeAccessAddRequest.getPackageIdList()) {
			SubscriptionPackage subPackage = subscriptionPackageRepository.findBySubscriptionPackageId(packageId);
			if (subPackage != null) {
				for (Long memberId : freeAccessAddRequest.getMemberIdList()) {
					FreeAccessResponse freeAccessResponse = new FreeAccessResponse();
					User member = userIdMapping.get(memberId);
					boolean isExistingFreeAccessDateConflict = false;
					if (!freeAccessAddRequest.isInfiniteTimeDuration()
							&& freeAccessAddRequest.getFreeAccessEndDate() != null) {
						boolean isStartDateConflict = checkFreeAccessDatePresentInPackage(member, subPackage,
								freeAccessStartDate, true);
						boolean isEndDateConflict = checkFreeAccessDatePresentInPackage(member, subPackage,
								freeAccessEndDate, true);
						isExistingFreeAccessDateConflict = isStartDateConflict && isEndDateConflict;
					} else {
						isExistingFreeAccessDateConflict = checkFreeAccessDatePresentInPackage(member, subPackage,
								freeAccessStartDate, true);
					}
					if (!isExistingFreeAccessDateConflict) {
						FreeAccessPackages freeSubPackage = new FreeAccessPackages();
						freeSubPackage.setSubscriptionPackage(subPackage);
						freeSubPackage.setFreeProduct(freeProduct);
						freeSubPackage.setActive(true);
						
						FreeProductUserSpecific userSpecific = new FreeProductUserSpecific();
						userSpecific.setUser(member);
						userSpecific.setFreeProduct(freeProduct);
						userSpecific.setFreeAccessSubscriptionPackages(freeSubPackage);
						freeProductUserSpecificRepository.save(userSpecific);
						freeAccessResponse.setEmail(member.getEmail());
						freeAccessResponse.setStatus(Constants.RESPONSE_SUCCESS);
						freeAccessResponse.setTitle(subPackage.getTitle());
						freeAccessResponse.setMessage(MessageConstants.MSG_FREE_ACCESS_PACKAGE_ADDED_INDIVIDUAL_USERS);
					} else {
						freeAccessResponse.setEmail(member.getEmail());
						freeAccessResponse.setTitle(subPackage.getTitle());
						freeAccessResponse.setStatus(Constants.RESPONSE_ERROR);
						freeAccessResponse.setMessage(
								ValidationMessageConstants.MSG_FREE_ACCESS_PACKAGE_START_OR_END_DATE_IS_CONFLICT);
					}
					packageResponseList.add(freeAccessResponse);
				}
			} else {
				// packages not present
				FreeAccessResponse freeAccessResponse = new FreeAccessResponse();
				freeAccessResponse.setEmail("");
				freeAccessResponse.setStatus(Constants.RESPONSE_ERROR);
				freeAccessResponse.setMessage(ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND);
				packageResponseList.add(freeAccessResponse);
			}
		}
		return packageResponseList;
	}
}
