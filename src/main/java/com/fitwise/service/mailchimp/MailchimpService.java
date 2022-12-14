package com.fitwise.service.mailchimp;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecwid.maleorang.MailchimpClient;
import com.ecwid.maleorang.MailchimpException;
import com.ecwid.maleorang.MailchimpObject;

import com.ecwid.maleorang.method.v3_0.lists.members.EditMemberMethod;
import com.ecwid.maleorang.method.v3_0.lists.members.MemberInfo;
import com.fitwise.constants.Constants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.properties.MailchimpProperties;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.view.ResponseModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailchimpService {

	@Autowired
	UserProfileRepository userProfileRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	MailchimpProperties mailchimpProperties;
	
	public ResponseModel createEmailList(String email, String role) throws IOException, MailchimpException {
		log.info(" Entering CreateEmailList method : ");
		log.info(" EMail id {} and Role is {} ", email,role);
		String apiKey=mailchimpProperties.getAccessKey();
		String listId="";
		if(role.equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)) {
			listId=mailchimpProperties.getInstructorListId();
		}
		else if(role.equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER)){
			listId=mailchimpProperties.getMemberListId();
		}
		try {
			MailchimpClient client = new MailchimpClient(apiKey);
			EditMemberMethod method = new EditMemberMethod.CreateOrUpdate(listId, email);

			method.status = "subscribed";
			method.merge_fields = new MailchimpObject();
			MemberInfo member = client.execute(method);
			log.info("unique_email_id :{}",member.unique_email_id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseModel(Constants.SUCCESS_STATUS, "Member has been added succesfully", null);
	}
}
