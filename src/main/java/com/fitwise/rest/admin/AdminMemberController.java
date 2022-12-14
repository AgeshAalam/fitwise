package com.fitwise.rest.admin;

import com.fitwise.admin.service.AdminService;
import com.fitwise.constants.Constants;
import com.fitwise.constants.ExportConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.request.InviteMember;
import com.fitwise.service.admin.AdminMemberService;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value = "/v1/admin/member")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;
    private final FitwiseUtils fitwiseUtils;
    private final AdminService adminService;

    /**
     * Get Member Details
     * @param memberId
     * @return
     */
    @GetMapping
    public ResponseModel getMemberDetails(@RequestParam Long memberId) {
        return adminService.getMemberDetails(memberId);
    }

    /**
     * Get All Members and details for admin user
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param search
     * @param blockStatus
     * @return
     */
    @GetMapping(value = "all")
    public ResponseModel getAllMembers(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> search, @RequestParam String blockStatus){
        Map<String, Object> response = new HashMap<>();
        response.put(KeyConstants.KEY_MEMBER, adminMemberService.getMembers(pageNo, pageSize, sortOrder, sortBy, search, blockStatus));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }

    /**
     * Export member details for Admin
     * @param sortOrder
     * @param sortBy
     * @param search
     * @param blockStatus
     * @return
     */
    @GetMapping(value = "all/export")
    public ResponseEntity<InputStreamResource> export(@RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> search, @RequestParam String blockStatus){
        ByteArrayInputStream byteArrayInputStream = adminMemberService.export(sortOrder, sortBy, search, blockStatus);
        InputStreamResource file = new InputStreamResource(byteArrayInputStream);
        HttpHeaders httpHeaders = fitwiseUtils.getHttpDownloadHeaders(ExportConstants.FILE_NAME_MEMBER);
        return new ResponseEntity<>(file, httpHeaders, OK);
    }
    

	/**
	 * Gets the all member minimum details.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @param search the search
	 * @return the all member minimum details
	 */
	@GetMapping("/all/mindetails")
	public ResponseModel getAllMemberMinimumDetails(@RequestParam final int pageNo, @RequestParam final int pageSize,
			@RequestParam String search) {
		return adminMemberService.memberMinimumDetails(pageNo, pageSize, search);
	}
	
	/**
	 * Invite member.
	 *
	 * @param inviteMember the invite member
	 * @return the response model
	 */
	@PostMapping("/invite")
	public ResponseModel inviteMember(@RequestBody InviteMember inviteMember) {
		return adminMemberService.inviteMember(inviteMember);
	}

	@PostMapping("/bulkInvite")
	public ResponseModel bulkInviteMember(@RequestParam("file") MultipartFile file) throws IOException {
		return adminMemberService.checkCsvFile(file);
	}

	@GetMapping(value = "/bulkInvite/csv/sample")
	public ResponseEntity<InputStreamResource> bulkinviteSampleCsv() {
		ByteArrayInputStream byteArrayInputStream = adminMemberService.bulkInviteSampleCsv();
		String filename = "member_bulk_invite_sample.csv";
		InputStreamResource file = new InputStreamResource(byteArrayInputStream);
		HttpHeaders httpHeaders = getHttpHeaders(filename);
		return new ResponseEntity<>(file, httpHeaders, OK);
	}

	private HttpHeaders getHttpHeaders(String filename) {
		HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentType(MediaType.parseMediaType("application/csv"));
		respHeaders.setContentDispositionFormData("attachment", filename);
		return respHeaders;
	}

	/**
	 * Invite members list.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @param search the search
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the response model
	 */
	@GetMapping("/invite/list")
	public ResponseModel inviteMembersList(@RequestParam final int pageNo, @RequestParam final int pageSize,
			@RequestParam(required = false) String search, @RequestParam(required = false) String sortOrder,
			@RequestParam(required = false) String sortBy) {
		return adminMemberService.getInviteMembersList(pageNo, pageSize, search, sortOrder, sortBy);
	}
	
	/**
	 * Invite members export.
	 *
	 * @param search the search
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the response entity
	 */
	@GetMapping("/invite/export")
	public ResponseEntity<InputStreamResource> inviteMembersExport(@RequestParam(required = false) String search, @RequestParam(required = false) String sortOrder,
			@RequestParam(required = false) String sortBy) {
        ByteArrayInputStream byteArrayInputStream = adminMemberService.getExportInviteMembersList(search, sortOrder, sortBy);
        InputStreamResource file = new InputStreamResource(byteArrayInputStream);
        HttpHeaders httpHeaders = fitwiseUtils.getHttpDownloadHeaders(ExportConstants.FILE_NAME_INVITE_MEMBERS);
        return new ResponseEntity<>(file, httpHeaders, OK);
	}
}