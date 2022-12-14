package com.fitwise.rest.mailchimp;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecwid.maleorang.MailchimpException;
import com.fitwise.service.mailchimp.MailchimpService;
import com.fitwise.view.ResponseModel;

@RestController
@RequestMapping(value = "/mailchimp")
@CrossOrigin("*")
public class MailChimpController {

	@Autowired
	MailchimpService mailchimpService;
	
	@PostMapping(value="/createList")
	public ResponseModel createList(@RequestParam String email,@RequestParam String role) throws IOException, MailchimpException {
		return mailchimpService.createEmailList(email,role);
	}
}
