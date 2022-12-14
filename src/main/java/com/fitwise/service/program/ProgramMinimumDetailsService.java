package com.fitwise.service.program;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.product.FreeAccessProgram;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.product.FreeAccessProgramReposity;
import com.fitwise.response.AdminListResponse;
import com.fitwise.response.freeaccess.ProgramMinimumDetails;
import com.fitwise.specifications.ProgramSpecifications;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.view.ResponseModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgramMinimumDetailsService {

	private final ProgramRepository programRepository;
	private final UserProfileRepository userProfileRepository;
	private final FreeAccessProgramReposity freeAccessProgramReposity;

	/**
	 * Program minimum details.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @param search the search
	 * @return the response model
	 */
	public ResponseModel programMinimumDetails(final int pageNo, final int pageSize, String search) {
		ResponseModel response = new ResponseModel();
		RequestParamValidator.pageSetup(pageNo, pageSize);
		try {
			List<ProgramMinimumDetails> responseList = new ArrayList<>();
			DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
			PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
			// Get Default free access programs, and exclude from search list
			List<FreeAccessProgram> defaultFreeAccessProgramList = freeAccessProgramReposity
					.findByFreeProductTypeAndIsActive(DBConstants.FREE_ACCESS_TYPE_ALL, true);
			List<Long> freeProgramIdsList = defaultFreeAccessProgramList.stream()
					.map(freeProduct -> freeProduct.getProgram().getProgramId()).collect(Collectors.toList());
			Specification<Programs> programIdExcludeSpec = null;
			if(!freeProgramIdsList.isEmpty()) {
				programIdExcludeSpec = ProgramSpecifications.getProgramsNotInIdList(freeProgramIdsList);
			}
			Specification<Programs> programSpec = ProgramSpecifications.getProgramByTitleContains(search);
			Specification<Programs> programStatusSpec = ProgramSpecifications
					.getProgramByStatus(KeyConstants.KEY_PUBLISH);
			Specification<Programs> finalSpec = programSpec.and(programStatusSpec);
			if(programIdExcludeSpec != null) {
				finalSpec = finalSpec.and(programIdExcludeSpec);
			}
			Page<Programs> programPageRequest = programRepository.findAll(finalSpec, pageRequest);
			for (Programs program : programPageRequest.getContent()) {
				UserProfile userProfile = userProfileRepository.findByUser(program.getOwner());
				ProgramMinimumDetails searchProgramView = new ProgramMinimumDetails();
				searchProgramView.setProgramId(program.getProgramId());
				searchProgramView.setProgramName(program.getTitle());
				searchProgramView.setProgramPrice(String.valueOf(program.getProgramPrices().getPrice()));
				searchProgramView.setFormattedProgramPrice(
						KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(program.getProgramPrice()));
				if (userProfile != null) {
					searchProgramView.setInstructorName(userProfile.getFirstName() + " " + userProfile.getLastName());
				}
				responseList.add(searchProgramView);
			}
			AdminListResponse<ProgramMinimumDetails> res = new AdminListResponse<>();
			res.setTotalSizeOfList(programPageRequest.getTotalElements());
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

}
