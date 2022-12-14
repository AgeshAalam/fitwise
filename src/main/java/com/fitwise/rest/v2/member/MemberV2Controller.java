package com.fitwise.rest.v2.member;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.program.model.ProgramTypeWithProgramTileModel;
import com.fitwise.service.member.MemberInstructorService;
import com.fitwise.service.member.MemberService;
import com.fitwise.service.v2.member.MemberProgramV2Service;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

/**
 * The Class MemberController.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/fw/v2/member")
public class MemberV2Controller {

    private final MemberService memberService;
    private final MemberProgramV2Service memberProgramV2Service;
    private final MemberInstructorService memberInstructorService;
    /**
     * Instructor Profiles for member app
     *
     * @param pageNo
     * @param pageSize
     * @param isFeatured
     * @param programTypeIds
     * @return Instructor Profile
     * @throws ApplicationException the application exception
     */
    @GetMapping(value = "/instructors")
    public ResponseModel getInstructors(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam(required = false) List<Long> programTypeIds, @RequestParam boolean isFeatured, @RequestParam Optional<String> search) {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, memberService.getInstructors(pageNo, pageSize, programTypeIds, isFeatured, search));
    }

    /**
     * Get program details
     * @param programId
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws ParseException
     */
    @GetMapping(value = "/program")
    public ResponseModel getProgramDetails(@RequestParam Long programId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, memberProgramV2Service.getProgramDetails(programId));
    }

    /**
     * Gets the trending programs.
     *
     * @param pageNo
     * @param pageSize
     * @param durationIdList
     * @param isFeatured
     * @param search
     * @return the trending programs
     * @throws ApplicationException the application exception
     */
    @GetMapping(value = "/programs/trending")
    public ResponseModel getTrendingPrograms(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam boolean isFeatured, @RequestParam(required = false) List<Long> durationIdList, @RequestParam Optional<String> search) {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(memberProgramV2Service.getTrendingPrograms(pageNo, pageSize, isFeatured, durationIdList, search));
        return responseModel;
    }

    /**
     * Get instructor details
     * @param userId Param to get specific instructor
     * @return Instructor details
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    @GetMapping("/instructor")
    public ResponseModel getInstructor(@RequestParam Long userId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return memberInstructorService.getInstructor(userId);
    }

    /**
     * Get all instructor programs by pagination
     * @param userId User Id
     * @param pageNo Page number
     * @param pageSize Page size
     * @return List of programs in the response
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    @GetMapping("/instructor/program/all")
    public ResponseModel getInstructorPrograms(@RequestParam Long userId, @RequestParam int pageNo, @RequestParam int pageSize, Optional<String> search, Optional<Long> expertiseLevelId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return memberInstructorService.getPrograms(userId, pageNo, pageSize, search, expertiseLevelId);
    }
    /**
     * Gett featured program type and corresponding programs
     * @return
     */
    @GetMapping(value = "/program/featured/all")
    public ResponseModel getFeaturedPrograms(){
        List<ProgramTypeWithProgramTileModel> programTypeWithProgramTileModels = memberProgramV2Service.getFeaturedPrograms();
        if(programTypeWithProgramTileModels.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, null, null);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, programTypeWithProgramTileModels);
    }

}
