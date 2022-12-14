package com.fitwise.service.freeproduct;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.ExportConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.product.FreeAccessNotifyToUsersAudit;
import com.fitwise.entity.product.FreeAccessPackages;
import com.fitwise.entity.product.FreeAccessProgram;
import com.fitwise.entity.product.FreeProduct;
import com.fitwise.entity.product.FreeProductUserSpecific;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.repository.product.FreeAccessNotifyToUsersAuditRepository;
import com.fitwise.repository.product.FreeAccessProgramReposity;
import com.fitwise.repository.product.FreeProductUserSpecificRepository;
import com.fitwise.response.freeaccess.FreeAccessLaunchMessage;
import com.fitwise.specifications.freeproduct.FreeProductUserSpecificSpecification;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.ResponseModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class FreeAccessService {

	private final UserComponents userComponents;
	private final FreeProductUserSpecificRepository freeProductUserSpecificRepository;
	private final PackageProgramMappingRepository packageProgramMappingRepository;
	private final FreeAccessProgramReposity freeAccessProgramReposity;
	private final FitwiseUtils fitwiseUtils;
	private final FreeAccessNotifyToUsersAuditRepository freeAccessNotifyToUsersAuditRepository;

	/**
	 * Gets the user specific free access programs.
	 *
	 * @return the user specific free access programs
	 */
	public List<FreeAccessProgram> getUserSpecificFreeAccessPrograms() {
		List<FreeAccessProgram> programsList = new ArrayList<>();
		User user = userComponents.getAndValidateUser();
		if (user != null && !fitwiseUtils.isUserBlocked(user)) {
			String now = fitwiseUtils.formatDate(new Date());
			Date currentDate = fitwiseUtils.constructDateWithoutTimeZone(now);
			Specification<FreeProductUserSpecific> userSpecificAccessProgram = FreeProductUserSpecificSpecification
					.getUserSpecificFreeAccessPrograms(DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL, user,
							currentDate, null);
			List<FreeProductUserSpecific> list = freeProductUserSpecificRepository.findAll(userSpecificAccessProgram);
			programsList = list.stream().filter(freeAccess -> freeAccess.getFreeAccessProgram() != null)
					.map(freeAccess -> freeAccess.getFreeAccessProgram()).collect(Collectors.toList());
		}
		return programsList;
	}

	/**
	 * Gets the user specific free access programs ids.
	 *
	 * @return the user specific free access programs ids
	 */
	public List<Long> getUserSpecificFreeAccessProgramsIds() {
		List<FreeAccessProgram> freeAccessprogramIds = getUserSpecificFreeAccessPrograms();
		return freeAccessprogramIds.stream()
				.filter(freeAccessProgram -> freeAccessProgram.getProgram() != null)
				.map(freeAccessProgram -> freeAccessProgram.getProgram().getProgramId())
				.collect(Collectors.toList());
	}

	/**
	 * Gets the user specific free access programs by program.
	 *
	 * @param program the program
	 * @return the user specific free access programs by program
	 */
	public List<FreeAccessProgram> getUserSpecificFreeAccessProgramsByProgram(final Programs program) {
		List<FreeAccessProgram> programsList = new ArrayList<>();
		User user = userComponents.getAndValidateUser();
		if (user != null && !fitwiseUtils.isUserBlocked(user)) {
			String now = fitwiseUtils.formatDate(new Date());
			Date currentDate = fitwiseUtils.constructDateWithoutTimeZone(now);
			Specification<FreeProductUserSpecific> userSpecificAccessProgram = FreeProductUserSpecificSpecification
					.getUserSpecificFreeAccessPrograms(DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL, user,
							currentDate, program);
			List<FreeProductUserSpecific> list = freeProductUserSpecificRepository.findAll(userSpecificAccessProgram);
			programsList = list.stream()
					.filter(freeAccess -> freeAccess.getFreeAccessProgram() != null)
					.map(freeAccess -> freeAccess.getFreeAccessProgram())
					.collect(Collectors.toList());
		}
		return programsList;
	}

	/**
	 * Checks if is exits free access program for specific user.
	 *
	 * @param program the program
	 * @return true, if is exits free access program for specific user
	 */
	public boolean isExitsFreeAccessProgramForSpecificUser(final Programs program) {
		boolean isExits = false;
		List<FreeAccessProgram> list = getUserSpecificFreeAccessProgramsByProgram(program);
		for (FreeAccessProgram freeProgram : list) {
			if (freeProgram.getProgram() != null
					&& (program.getProgramId() == freeProgram.getProgram().getProgramId())) {
				isExits = true;
				break;
			}
		}
		return isExits;
	}
	
	/**
	 * Gets the user specific free access package.
	 *
	 * @param subscriptionPackahe the subscription packahe
	 * @return the user specific free access package
	 */
	public List<FreeAccessPackages> getUserSpecificFreeAccessPackage(SubscriptionPackage subscriptionPackahe) {
		List<FreeAccessPackages> packagesList = new ArrayList<>();
		User user = userComponents.getAndValidateUser();
		if (user != null && !fitwiseUtils.isUserBlocked(user)) {
			String now = fitwiseUtils.formatDate(new Date());
			log.info("Formatted Date : {}", now);
			Date currentDate = fitwiseUtils.constructDateWithoutTimeZone(now);
			log.info("System Date : {}, User Date : {}, TimeZone {}", new Date(), currentDate, fitwiseUtils.getUserTimeZone());
			Specification<FreeProductUserSpecific> userSpecificAccessPackageList = FreeProductUserSpecificSpecification
					.getUserSpecificFreeAccessPackages(DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL, user,
							currentDate, subscriptionPackahe);
			List<FreeProductUserSpecific> list = freeProductUserSpecificRepository
					.findAll(userSpecificAccessPackageList);
			packagesList = list.stream()
					.filter(freeAccessPackage -> freeAccessPackage.getFreeAccessSubscriptionPackages() != null)
					.map(freeAccessPackage -> freeAccessPackage.getFreeAccessSubscriptionPackages())
					.collect(Collectors.toList());
		}
		return packagesList;
	}
	
	/**
	 * Gets the user specific free access package programs ids.
	 *
	 * @return the user specific free access package programs ids
	 */
	public Set<Long> getUserSpecificFreeAccessPackageProgramsIds() {
		Set<Long> subscriptionPackageprogramsIds = new HashSet<>();
		User user = userComponents.getAndValidateUser();
		if (user != null && !fitwiseUtils.isUserBlocked(user)) {
			String now = fitwiseUtils.formatDate(new Date());
			Date currentDate = fitwiseUtils.constructDateWithoutTimeZone(now);
			Specification<FreeProductUserSpecific> userSpecificAccessProgram = FreeProductUserSpecificSpecification
					.getUserSpecificFreeAccessPackages(DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL, user, currentDate, null);
			List<FreeProductUserSpecific> list = freeProductUserSpecificRepository.findAll(userSpecificAccessProgram);

			for (FreeProductUserSpecific userSpecific : list) {
				if (userSpecific.getFreeProduct() != null && userSpecific.getFreeAccessSubscriptionPackages() != null) {
					List<PackageProgramMapping> programPackageMapList = packageProgramMappingRepository
							.findBySubscriptionPackage(
									userSpecific.getFreeAccessSubscriptionPackages().getSubscriptionPackage());
					for (PackageProgramMapping packageMap : programPackageMapList) {
						subscriptionPackageprogramsIds.add(packageMap.getProgram().getProgramId());
					}
				}
			}
		}
		return subscriptionPackageprogramsIds;
	}
	
	/**
	 * Gets the user specific free access subscription package ids.
	 *
	 * @return the user specific free access subscription package ids
	 */
	public List<Long> getUserSpecificFreeAccessSubscriptionPackageIds() {
		List<Long> subscriptionIds = new ArrayList<>();
		User user = userComponents.getAndValidateUser();
		if (user != null && !fitwiseUtils.isUserBlocked(user)) {
			String now = fitwiseUtils.formatDate(new Date());
			Date currentDate = fitwiseUtils.constructDateWithoutTimeZone(now);
			Specification<FreeProductUserSpecific> userSpecificAccessProgram = FreeProductUserSpecificSpecification
					.getUserSpecificFreeAccessPackages(DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL, user, currentDate, null);
			List<FreeProductUserSpecific> list = freeProductUserSpecificRepository.findAll(userSpecificAccessProgram);
			for (FreeProductUserSpecific userSpecific : list) {
				if (userSpecific.getFreeProduct() != null && userSpecific.getFreeAccessSubscriptionPackages() != null) {
					subscriptionIds.add(userSpecific.getFreeAccessSubscriptionPackages().getSubscriptionPackage()
							.getSubscriptionPackageId());
				}
			}
		}
		return subscriptionIds;
	}
	
	/**
	 * Gets the free product programs ids.
	 *
	 * @return the free product programs ids
	 */
	public Set<Long> getFreeProductProgramsIds() {
		Set<Long> userSpecificPackageProgramIds = new HashSet<>();
		// Get All default access free programs
		List<FreeAccessProgram> freeProducts = getAllUserFreeProgramsList();
		for (FreeAccessProgram freeAccess : freeProducts) {
			userSpecificPackageProgramIds.add(freeAccess.getProgram().getProgramId());
		}
				// Get member specific access free programs
		List<Long> userSpecificProgramIds = getUserSpecificFreeAccessProgramsIds();
		for (Long id : userSpecificProgramIds) {
			userSpecificPackageProgramIds.add(id);
		}
		// Get member specific free access package programs ids
		Set<Long> packageProgramIds = getUserSpecificFreeAccessPackageProgramsIds();
		if (!packageProgramIds.isEmpty())
			userSpecificPackageProgramIds.addAll(packageProgramIds);
		return userSpecificPackageProgramIds;
	}
	
	/**
	 * Gets the all user free programs list.
	 *
	 * @return the all user free programs list
	 */
	public List<FreeAccessProgram> getAllUserFreeProgramsList() {
		List<FreeAccessProgram> freeProducts = new ArrayList<>();
		User user = userComponents.getAndValidateUser();
		if (user != null && !fitwiseUtils.isUserBlocked(user)) {
			freeProducts = freeAccessProgramReposity.findByFreeProductTypeAndIsActive(DBConstants.FREE_ACCESS_TYPE_ALL,
					true);
		}
		return freeProducts;
	}
	
	/**
	 * Gets the all users free programs by program.
	 *
	 * @param programs the programs
	 * @return the all users free programs by program
	 */
	public FreeAccessProgram getAllUsersFreeProgramsByProgram(Programs programs) {
		FreeAccessProgram freeProgramTypeAll = null;
		User user = userComponents.getAndValidateUser();
		if (user != null && !fitwiseUtils.isUserBlocked(user)) {
			freeProgramTypeAll = freeAccessProgramReposity.findByIsActiveAndProgramAndFreeProductType(true, programs,
					DBConstants.FREE_ACCESS_TYPE_ALL);
		}
		return freeProgramTypeAll;
	}
	
	/**
	 * Checks if is exits free access package for specific user.
	 *
	 * @param subscriptionPackahe the subscription packahe
	 * @return true, if is exits free access package for specific user
	 */
	public boolean isExitsFreeAccessPackageForSpecificUser(final SubscriptionPackage subscriptionPackahe) {
		boolean isExits = false;
		if (subscriptionPackahe != null) {
			List<FreeAccessPackages> list = getUserSpecificFreeAccessPackage(subscriptionPackahe);
			List<Long> packageIdsList = list.stream()
					.filter(freePackages -> freePackages.getSubscriptionPackage() != null)
					.map(freePackages -> freePackages.getSubscriptionPackage().getSubscriptionPackageId())
					.collect(Collectors.toList());
			if (packageIdsList.contains(subscriptionPackahe.getSubscriptionPackageId())) {
				isExits = true;
			}
		}
		return isExits;
	}
	
	/**
	 * Gets the free user spec access by user and package program id.
	 *
	 * @param user the user
	 * @param subscriptionPackageProgramId the subscription package program id
	 * @return the free user spec access by user and package program id
	 */
	public FreeProduct getFreeUserSpecAccessByUserAndPackageProgramId(User user,
			Long subscriptionPackageProgramId) {
		FreeProduct freeProduct = null;
		if (user != null && !fitwiseUtils.isUserBlocked(user)) {
			List<PackageProgramMapping> programPackageMapList = packageProgramMappingRepository
					.findByProgramProgramId(subscriptionPackageProgramId);
			List<Long> subscriptionPackageList = programPackageMapList.stream()
					.map(subscriptionPackage -> subscriptionPackage.getSubscriptionPackage().getSubscriptionPackageId())
					.collect(Collectors.toList());
			String now = fitwiseUtils.formatDate(new Date());
			Date currentDate = fitwiseUtils.constructDateWithoutTimeZone(now);
			for (Long packageId : subscriptionPackageList) {
				Specification<FreeProductUserSpecific> specification = FreeProductUserSpecificSpecification
						.getUserSpecificFreeAccessByUserAndPackageId(user, packageId, false);
				Specification<FreeProductUserSpecific> dateSpecification = FreeProductUserSpecificSpecification
						.getActiveFreeProgarmsAndPackagesBasesdOnDates(currentDate);
				Specification<FreeProductUserSpecific> finalSpec = specification.and(dateSpecification);
				List<FreeProductUserSpecific> list = freeProductUserSpecificRepository.findAll(finalSpec);
				for (FreeProductUserSpecific userSpecific : list) {
					if (userSpecific.getFreeProduct() != null
							&& userSpecific.getFreeAccessSubscriptionPackages() != null) {
						freeProduct = userSpecific.getFreeProduct();
						break;
					}
				}
			}
		}
		return freeProduct;
	}
	
	/**
	 * Gets the free user spec access by user and program id.
	 *
	 * @param user the user
	 * @param programId the program id
	 * @return the free user spec access by user and program id
	 */
	public FreeProduct getFreeUserSpecAccessByUserAndProgramId(User user,
			Long programId) {
		FreeProduct freeProduct = null;
		if (user != null && !fitwiseUtils.isUserBlocked(user)) {
			String now = fitwiseUtils.formatDate(new Date());
			Date currentDate = fitwiseUtils.constructDateWithoutTimeZone(now);
			Specification<FreeProductUserSpecific> specification = FreeProductUserSpecificSpecification
					.getUserSpecificFreeAccessByUserAndPackageId(user, programId, true);
			Specification<FreeProductUserSpecific> dateSpecification = FreeProductUserSpecificSpecification
					.getActiveFreeProgarmsAndPackagesBasesdOnDates(currentDate);
			Specification<FreeProductUserSpecific> finalSpec = specification.and(dateSpecification);
			List<FreeProductUserSpecific> list = freeProductUserSpecificRepository.findAll(finalSpec);
			for (FreeProductUserSpecific userSpecific : list) {
				if (userSpecific.getFreeProduct() != null && userSpecific.getFreeAccessProgram() != null) {
					freeProduct = userSpecific.getFreeProduct();
					break;
				}
			}
		}
		return freeProduct;
	}
	
	/**
	 * Gets the user free access program and packages counts.
	 *
	 * @return the user free access program and packages counts
	 */
	public ResponseModel getUserFreeAccessProgramAndPackagesCounts() {
		ResponseModel response = new ResponseModel();
		response.setStatus(Constants.SUCCESS_STATUS);
		response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
		FreeAccessLaunchMessage freeAccessLaunchMessage = new FreeAccessLaunchMessage();
		User user = userComponents.getAndValidateUser();
		if (user != null && !fitwiseUtils.isUserBlocked(user)) {
			String now = fitwiseUtils.formatDate(new Date());
			Date currentDate = fitwiseUtils.constructDateWithoutTimeZone(now);
			int userSpecificFreeProgramCount = getUserSpecificFreeProgramsCount(user, currentDate);
			userSpecificFreeProgramCount += getFreeProgramCountForAllUsers(user);
			int userSpecificFreePackageCount = getUserSpecificFreePackagesCount(user, currentDate);
			boolean userHavingFreeAccess = (userSpecificFreeProgramCount > 0) || (userSpecificFreePackageCount > 0);
			String programCountAndText = userSpecificFreeProgramCount + " "
					+ ExportConstants.KEY_NAME_FREE_ACCESS_PROGRAM + (userSpecificFreeProgramCount > 1 ? "s" : "");
			String packageCountAndText = userSpecificFreePackageCount + " "
					+ ExportConstants.KEY_NAME_FREE_ACCESS_PACKAGE + (userSpecificFreePackageCount > 1 ? "s" : "");
			freeAccessLaunchMessage.setUserHaveFreeAccess(userHavingFreeAccess);
			freeAccessLaunchMessage.setFreeProgramsCount(userSpecificFreeProgramCount);
			freeAccessLaunchMessage.setFreePackagesCount(userSpecificFreePackageCount);
			String message = "You have free access to " + programCountAndText
					+ ", Navigate to the Programs tab to view them";
			if (userSpecificFreeProgramCount > 0 && userSpecificFreePackageCount > 0) {
				message = "You have free access to " + programCountAndText + ", and " + packageCountAndText
						+ ". Navigate to the Programs / Packages tab to view them";
			} else if (userSpecificFreeProgramCount == 0 && userSpecificFreePackageCount > 0) {
				message = "You have free access to " + packageCountAndText
						+ ". Navigate to the Packages tab to view them";
			}
			freeAccessLaunchMessage.setMessage(userHavingFreeAccess ? message : "");
		}
		response.setPayload(freeAccessLaunchMessage);
		return response;
	}
	
	/**
	 * Gets the free program count for all users.
	 *
	 * @param user the user
	 * @return the free program count for all users
	 */
	public int getFreeProgramCountForAllUsers(User user) {
		int userSpecificFreeProgramCount = 0;
		List<FreeAccessNotifyToUsersAudit> auditList = new ArrayList<>();
		// Get existing free product ids in audit table for the user and type program
		// all users
		List<FreeAccessNotifyToUsersAudit> existingProgramAuditList = freeAccessNotifyToUsersAuditRepository
				.findByUserAndType(user, DBConstants.FREE_ACCESS_TYPE_ALL);
		List<Long> programsIdsList = existingProgramAuditList.stream()
				.filter(freeProduct -> freeProduct.getFreeProduct() != null)
				.map(freePackages -> freePackages.getFreeProduct().getFreeProductId()).collect(Collectors.toList());
		List<FreeAccessProgram> freeProgramList = new ArrayList<>();
		if (!programsIdsList.isEmpty()) {
			freeProgramList = freeAccessProgramReposity.findByFreeProductTypeAndIsActiveAndFreeProductFreeProductIdNotIn(
					DBConstants.FREE_ACCESS_TYPE_ALL, true, programsIdsList);
		} else {
			freeProgramList = freeAccessProgramReposity
					.findByFreeProductTypeAndIsActive(DBConstants.FREE_ACCESS_TYPE_ALL, true);
		}
		for (FreeAccessProgram program : freeProgramList) {
			if (program.getFreeProduct() != null) {
				FreeAccessNotifyToUsersAudit programAudit = new FreeAccessNotifyToUsersAudit();
				programAudit.setFreeProduct(program.getFreeProduct());
				programAudit.setUser(user);
				programAudit.setType(DBConstants.FREE_ACCESS_TYPE_ALL);
				auditList.add(programAudit);
				userSpecificFreeProgramCount++;
			}
		}
		freeAccessNotifyToUsersAuditRepository.saveAll(auditList);
		return userSpecificFreeProgramCount;
	}
	
	/**
	 * Gets the user specific free programs count.
	 *
	 * @param user the user
	 * @param currentDate the current date
	 * @return the user specific free programs count
	 */
	public int getUserSpecificFreeProgramsCount(User user, Date currentDate) {
		int userSpecificFreeProgramCount = 0;
		List<FreeAccessNotifyToUsersAudit> auditList = new ArrayList<>();
		// For User Specific programs
		// Get existing free product ids in audit table for the user and type program
		List<FreeAccessNotifyToUsersAudit> existingProgramAuditList = freeAccessNotifyToUsersAuditRepository
				.findByUserAndType(user, DiscountsConstants.PROGRAM_LEVEL);
		List<Long> programsIdsList = existingProgramAuditList.stream()
				.filter(freeProduct -> freeProduct.getFreeProduct() != null)
				.map(freePackages -> freePackages.getFreeProduct().getFreeProductId()).collect(Collectors.toList());

		Specification<FreeProductUserSpecific> userSpecificAccessProgramList = FreeProductUserSpecificSpecification
				.getUserSpecificFreeAccessPrograms(DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL, user, currentDate, null);
		if (!programsIdsList.isEmpty()) {
			Specification<FreeProductUserSpecific> freeProductNotInQuery = FreeProductUserSpecificSpecification
					.getFreeProductNotInSpecification(programsIdsList);
			userSpecificAccessProgramList = userSpecificAccessProgramList.and(freeProductNotInQuery);
		}
		List<FreeProductUserSpecific> userFreeProgramsList = freeProductUserSpecificRepository
				.findAll(userSpecificAccessProgramList);
		for (FreeProductUserSpecific userFreeAccess : userFreeProgramsList) {
			if (userFreeAccess.getFreeProduct() != null && userFreeAccess.getFreeAccessProgram() != null) {
				FreeAccessNotifyToUsersAudit programAudit = new FreeAccessNotifyToUsersAudit();
				programAudit.setFreeProduct(userFreeAccess.getFreeProduct());
				programAudit.setUser(user);
				programAudit.setType(DiscountsConstants.PROGRAM_LEVEL);
				auditList.add(programAudit);
				userSpecificFreeProgramCount++;
			}
		}
		freeAccessNotifyToUsersAuditRepository.saveAll(auditList);
		return userSpecificFreeProgramCount;
	}
	
	/**
	 * Gets the user specific free packages count.
	 *
	 * @param user the user
	 * @param currentDate the current date
	 * @return the user specific free packages count
	 */
	public int getUserSpecificFreePackagesCount(User user, Date currentDate) {
		int userSpecificFreePackageCount = 0;
		List<FreeAccessNotifyToUsersAudit> auditList = new ArrayList<>();
		// For User Specific packages
		// Get existing free product ids in audit table for the user and type packages
		List<FreeAccessNotifyToUsersAudit> existingPackagesAuditList = freeAccessNotifyToUsersAuditRepository
				.findByUserAndType(user, DBConstants.FREE_ACCESS_TYPE_PACKAGE);
		List<Long> packagesIdsList = existingPackagesAuditList.stream()
				.filter(freeProduct -> freeProduct.getFreeProduct() != null)
				.map(freeProduct -> freeProduct.getFreeProduct().getFreeProductId()).collect(Collectors.toList());

		Specification<FreeProductUserSpecific> userSpecificAccessPackagesList = FreeProductUserSpecificSpecification
				.getUserSpecificFreeAccessPackages(DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL, user, currentDate, null);
		if (!packagesIdsList.isEmpty()) {
			Specification<FreeProductUserSpecific> freeProductNotInQuery = FreeProductUserSpecificSpecification
					.getFreeProductNotInSpecification(packagesIdsList);
			userSpecificAccessPackagesList = userSpecificAccessPackagesList.and(freeProductNotInQuery);
		}
		List<FreeProductUserSpecific> userFreePackagesList = freeProductUserSpecificRepository
				.findAll(userSpecificAccessPackagesList);
		for (FreeProductUserSpecific userFreeAccess : userFreePackagesList) {
			if (userFreeAccess.getFreeProduct() != null && userFreeAccess.getFreeAccessSubscriptionPackages() != null) {
				FreeAccessNotifyToUsersAudit programAudit = new FreeAccessNotifyToUsersAudit();
				programAudit.setFreeProduct(userFreeAccess.getFreeProduct());
				programAudit.setUser(user);
				programAudit.setType(DBConstants.FREE_ACCESS_TYPE_PACKAGE);
				auditList.add(programAudit);
				userSpecificFreePackageCount++;
			}
		}
		freeAccessNotifyToUsersAuditRepository.saveAll(auditList);
		return userSpecificFreePackageCount;
	}

}
