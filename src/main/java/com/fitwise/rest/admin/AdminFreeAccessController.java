package com.fitwise.rest.admin;

import static org.springframework.http.HttpStatus.OK;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fitwise.constants.ExportConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.request.FreeAccessAddRequest;
import com.fitwise.service.admin.AdminFreeAccessService;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.ResponseModel;

import lombok.RequiredArgsConstructor;

/**
 * The Class AdminFreeAccessController.
 */
@RestController
@RequestMapping(value = "/v1/admin/freeaccess")

/**
 * Instantiates a new admin free access controller.
 *
 * @param adminFreeAccessService the admin free access service
 */
@RequiredArgsConstructor
public class AdminFreeAccessController {

	/** The admin free access service. */
	private final AdminFreeAccessService adminFreeAccessService;
	private final FitwiseUtils fitwiseUtils;

	/**
	 * Adds the free programs for all users.
	 *
	 * @param programIds the program ids
	 * @return the response model
	 */
	@PostMapping("/allusers")
	public ResponseModel addFreeProgramsForAllUsers(@RequestBody final List<Long> programIds) {
		return 	adminFreeAccessService.addFreeProgramsForAllUsers(programIds);
	}

	/**
	 * Removes the free programs for all users.
	 *
	 * @param programIds the program ids
	 * @return the response model
	 */
	@DeleteMapping("/allusers")
	public ResponseModel removeFreeProgramsForAllUsers(@RequestParam final List<Long> programIds) {
		return adminFreeAccessService.removeFreeProgramsForAllUsers(programIds);
	}
	
	/**
	 * Adds the program and packages for specific members.
	 *
	 * @param freeAccessAddRequest the free access add request
	 * @return the response model
	 * @throws ParseException 
	 * @throws ApplicationException 
	 */
	@PostMapping("/specificmember/add")
	public ResponseModel addProgramAndPackagesForSpecificMembers(
			@RequestBody final FreeAccessAddRequest freeAccessAddRequest) throws ApplicationException, ParseException {
		return adminFreeAccessService.addProgramAndPackagesForSpecificMembers(freeAccessAddRequest);
	}

	/**
	 * Revoke free access.
	 *
	 * @param freeAccessSpecificId the free access specific id
	 * @return the response model
	 */
	@DeleteMapping("/specificmember/revoke")
	public ResponseModel revokeFreeAccess(@RequestParam final long freeAccessSpecificId) {
		return adminFreeAccessService.revokeFreeAccessForProgramOrPackage(freeAccessSpecificId);
	}

	/**
	 * Gets the free access programs list.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @return the free access programs list
	 */
	@GetMapping("/programlist")
	public ResponseModel getFreeAccessProgramsList(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam(required = false) final String search,
			@RequestParam(required = false) String sortOrder, @RequestParam(required = false) String sortBy) {
		return adminFreeAccessService.getFreeAccessProgramsList(pageNo, pageSize, search, sortOrder, sortBy);
	}

	/**
	 * Gets the free access package list.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @return the free access package list
	 */
	@GetMapping("/packagelist")
	public ResponseModel getFreeAccessPackageList(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam(required = false) final String search, 
			@RequestParam(required = false) String sortOrder, @RequestParam(required = false) String sortBy) {
		return adminFreeAccessService.getFreeAccessPackageList(pageNo, pageSize, search, sortOrder, sortBy);
	}
	
	/**
	 * Export free access program.
	 *
	 * @param search the search
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the response entity
	 */
	@GetMapping(value = "/programlist/export")
	public ResponseEntity<InputStreamResource> exportFreeAccessProgram(
			@RequestParam(required = false) final String search, @RequestParam(required = false) String sortOrder,
			@RequestParam(required = false) String sortBy) {
		ByteArrayInputStream byteArrayInputStream = adminFreeAccessService.exportProgramList(search, sortOrder, sortBy);
		InputStreamResource file = new InputStreamResource(byteArrayInputStream);
		HttpHeaders httpHeaders = fitwiseUtils.getHttpDownloadHeaders(ExportConstants.FILE_NAME_FREE_ACCESS_PROGRAM);
		return new ResponseEntity<>(file, httpHeaders, OK);
	}
	
	/**
	 * Export free access package.
	 *
	 * @param search the search
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the response entity
	 */
	@GetMapping(value = "/packagelist/export")
	public ResponseEntity<InputStreamResource> exportFreeAccessPackage(
			@RequestParam(required = false) final String search, @RequestParam(required = false) String sortOrder,
			@RequestParam(required = false) String sortBy) {
		ByteArrayInputStream byteArrayInputStream = adminFreeAccessService.exportPackageList(search, sortOrder, sortBy);
		InputStreamResource file = new InputStreamResource(byteArrayInputStream);
		HttpHeaders httpHeaders = fitwiseUtils.getHttpDownloadHeaders(ExportConstants.FILE_NAME_FREE_ACCESS_PACKAGE);
		return new ResponseEntity<>(file, httpHeaders, OK);
	}
}
