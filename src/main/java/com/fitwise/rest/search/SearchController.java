package com.fitwise.rest.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fitwise.exception.ApplicationException;
import com.fitwise.search.model.SearchModel;
import com.fitwise.search.service.SearchService;
import com.fitwise.view.ResponseModel;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(value="/v1")
public class SearchController {

	@Autowired
	SearchService searchService;
	
	/*
	 * Search Filter Request
	 * */
	@PutMapping("/searchfilter")
	public ResponseModel search(@RequestBody SearchModel model) {
		log.info("Inside user Search filter Controller");
		ResponseModel response = new ResponseModel();
		try {
			response = searchService.searchFilterbyLevel(model);		
		}catch(ApplicationException aex) {
			response= new ResponseModel();
			response.setStatus(aex.getStatus());
			response.setError(aex.getMessage());
		}
		return response;
	}
	
	/*
	 * Search Keyword Request
	 * */
	@PutMapping("/searchkeyword")
	public ResponseModel searchKeyword(@RequestBody SearchModel model) {
		log.info("Inside user Search keyword Controller");
		ResponseModel response = new ResponseModel();
		try {
			response = searchService.searchKeyword(model);		
		}catch(ApplicationException aex) {
			response= new ResponseModel();
			response.setStatus(aex.getStatus());
			response.setError(aex.getMessage());
		}
		return response;
	}

	@GetMapping(value = "/getSearchedPrograms")
	public  ResponseModel getSearchedPrograms(@RequestParam String searchName){
		return searchService.programSearch(searchName);
	}
}
