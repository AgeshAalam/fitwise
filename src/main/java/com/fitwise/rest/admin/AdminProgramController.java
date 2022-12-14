package com.fitwise.rest.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fitwise.service.program.ProgramMinimumDetailsService;
import com.fitwise.view.ResponseModel;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/v1/admin/program")
@RequiredArgsConstructor
public class AdminProgramController {
	
    private final ProgramMinimumDetailsService programMinimumDetailsService;
    
    /**
     * Gets the all programs min details.
     *
     * @param pageNo the page no
     * @param pageSize the page size
     * @param search the search
     * @return the all programs min details
     */
    @GetMapping("/all/mindetails")
	public ResponseModel getAllProgramsMinDetails(@RequestParam final int pageNo, @RequestParam final int pageSize,
			@RequestParam String search) {
		return programMinimumDetailsService.programMinimumDetails(pageNo, pageSize, search);
	}
    
    

}
