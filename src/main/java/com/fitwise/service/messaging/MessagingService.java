package com.fitwise.service.messaging;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.AppConfigConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.PushNotificationConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.AppConfigKeyValue;
import com.fitwise.entity.ChatBlockStatus;
import com.fitwise.entity.ChatConversation;
import com.fitwise.entity.ChatMessage;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.UserRoleMapping;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.BlockedUserRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleMappingRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.messaging.ChatBlockStatusRepository;
import com.fitwise.repository.messaging.ChatConversationRepository;
import com.fitwise.repository.messaging.ChatMessageRepository;
import com.fitwise.response.messaging.ChatConversationView;
import com.fitwise.response.messaging.ChatMessageView;
import com.fitwise.response.messaging.InboxConversationView;
import com.fitwise.response.messaging.NotificationView;
import com.fitwise.service.fcm.PushNotificationAPIService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.ChatConversationSpecifications;
import com.fitwise.specifications.jpa.ChatConversationJPA;
import com.fitwise.specifications.jpa.dao.ChatMessageDAO;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.fcm.NotificationContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

/*
 * Created by Vignesh G on 03/04/20
 */

@Service
@Slf4j
public class MessagingService {

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private ValidationService validationService;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    private ChatConversationRepository chatConversationRepository;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    ChatBlockStatusRepository chatBlockStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    private UserRoleMappingRepository userRoleMappingRepository;

    @Autowired
    private BlockedUserRepository blockedUserRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private PushNotificationAPIService pushNotificationAPIService;

    @Autowired
    private AppConfigKeyValueRepository appConfigKeyValueRepository;

    @Autowired
    private ChatConversationJPA chatConversationJPA;

    public Map<String, Object> getInboxConversations(int pageNo, int pageSize, Optional<String> searchString) {
        log.info("InboxConversations starts.");
        long apiStartTimeMillis = new Date().getTime();

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        DateFormat dateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
        String userTimeZone = fitwiseUtils.getUserTimeZone();
        if (userTimeZone != null) {
            TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);
            timeFormat.setTimeZone(timeZone);
            dateFormat.setTimeZone(timeZone);
        }

        User user = userComponents.getUser();
        boolean isInstructor = false;

        UserRole userRole = validationService.validateUserRole(userComponents.getRole());

        if (userRole.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
            isInstructor = true;
        }

        long profilingStartTimeMillis = new Date().getTime();

        //Retrieve conversation list by recipient name
        Specification<ChatConversation> finalSpec;

        if (searchString.isPresent() && !searchString.get().isEmpty()) {
            String search = searchString.get();
            if (isInstructor) {
                Specification<ChatConversation> idSpec = ChatConversationSpecifications.getConversationByPrimaryUserId(user.getUserId());
                Specification<ChatConversation> nameSpec = ChatConversationSpecifications.getConversationBySecondaryUserName(search);
                finalSpec = nameSpec.and(idSpec);
            } else {
                Specification<ChatConversation> idSpec = ChatConversationSpecifications.getConversationBySecondaryUserId(user.getUserId());
                Specification<ChatConversation> nameSpec = ChatConversationSpecifications.getConversationByPrimaryUserName(search);
                finalSpec = nameSpec.and(idSpec);
            }
        } else {
            if (isInstructor) {
                Specification<ChatConversation> idSpec = ChatConversationSpecifications.getConversationByPrimaryUserId(user.getUserId());
                finalSpec = idSpec;
            } else {
                Specification<ChatConversation> idSpec = ChatConversationSpecifications.getConversationBySecondaryUserId(user.getUserId());
                finalSpec = idSpec;
            }
        }

        Specification<ChatConversation> lastMsgSpec = ChatConversationSpecifications.getConversationsInnerJoinChatMsgs();
        Specification<ChatConversation> finalLastMsgSpec = finalSpec.and(lastMsgSpec);

        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        Page<ChatConversation> conversationPage = chatConversationRepository.findAll(finalLastMsgSpec, pageRequest);
        //Query to take total count
        long totalCount = chatConversationRepository.count(finalSpec);

        long profilingEndTimeMillis = new Date().getTime();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        if (conversationPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_INBOX_EMPTY, null);
        }

        profilingStartTimeMillis = new Date().getTime();
        List<Long> chatConIdList = conversationPage.stream().map(ChatConversation::getConversationId).collect(Collectors.toList());
        Map<Long, ChatMessageDAO> chatMessageDAOMap = chatConversationJPA.getLastMessageAndBlockStatusByConversationIdList(chatConIdList);

        List<InboxConversationView> inboxView = new ArrayList<>();

        for (ChatConversation conversation : conversationPage) {
            InboxConversationView chatInboxView = new InboxConversationView();

            chatInboxView.setConversationId(conversation.getConversationId());

            UserProfile recipient = null;
            if (isInstructor) {
                recipient = conversation.getSecondaryUser();
            } else {
                recipient = conversation.getPrimaryUser();
            }

            if (recipient != null) {
                chatInboxView.setRecipientName(recipient.getFirstName() + " " + recipient.getLastName());
                if (recipient.getProfileImage() != null) {
                    chatInboxView.setRecipientProfileImage(recipient.getProfileImage().getImagePath());
                }
            } else if (recipient == null && conversation.isReceiveOnly()) {
                chatInboxView.setRecipientName(KeyConstants.KEY_ADMIN_CHAT_NAME);
                AppConfigKeyValue appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_TRAINNR_PROFILE);
                if (appConfig != null) {
                    chatInboxView.setRecipientProfileImage(appConfig.getValueString());
                }
            } else {
                chatInboxView.setRecipientName(KeyConstants.KEY_ANONYMOUS);
            }

            //Setting Conversation's last msg time.
            ChatMessage lastMessage = chatMessageDAOMap.get(conversation.getConversationId()).getLastChatMessage();
            if (lastMessage != null) {
                String lastMessageTime;
                Date lastMessageDate = lastMessage.getCreatedDate();
                LocalDate lastMessageLocalDate = lastMessageDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate today = LocalDate.now();
                if (lastMessageLocalDate.isEqual(today)) {
                    lastMessageTime = timeFormat.format(lastMessageDate);
                } else {
                    lastMessageTime = dateFormat.format(lastMessageDate);
                }
                chatInboxView.setLastMessageTime(lastMessageTime);
                chatInboxView.setLastMessageDate(lastMessageDate);
                chatInboxView.setLastMessage(lastMessage.getContent());
                chatInboxView.setReceiveOnly(conversation.isReceiveOnly());
            }

            //Set conversation's unread received msgs count.
            if (recipient != null) {
                int unreadChatMessageCount = chatMessageRepository.countByConversationConversationIdAndSenderUserIdAndIsUnread(conversation.getConversationId(), recipient.getUser().getUserId(), true);
                if (unreadChatMessageCount > 0) {
                    chatInboxView.setUnread(true);
                    chatInboxView.setUnreadMessageCount(unreadChatMessageCount);
                }
            } else if (recipient == null && conversation.isReceiveOnly()) {
                int unreadChatMessageCount = chatMessageRepository.countByConversationConversationIdAndIsUnread(conversation.getConversationId(), true);
                if (unreadChatMessageCount > 0) {
                    chatInboxView.setUnread(true);
                    chatInboxView.setUnreadMessageCount(unreadChatMessageCount);
                }
            }

            //Conversation block status set
            Long isConversationBlocked = chatMessageDAOMap.get(conversation.getConversationId()).getBlockStatus();
            if (isConversationBlocked != null && isConversationBlocked > 0) {
                chatInboxView.setBlocked(true);
                ChatBlockStatus chatBlockStatus = chatBlockStatusRepository.findByConversationConversationIdAndBlockedByUserUserId(conversation.getConversationId(), user.getUserId());
                if (chatBlockStatus != null) {
                    chatInboxView.setCanUnblock(true);
                }
            } else {
                chatInboxView.setBlocked(false);
            }
            inboxView.add(chatInboxView);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        Map<String, Object> respMap = new HashMap<>();
        respMap.put(KeyConstants.KEY_CONVERSATIONS, inboxView);
        respMap.put(KeyConstants.KEY_TOTAL_COUNT, totalCount);

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("InboxConversations ends.");

        return respMap;
    }

    public ChatConversationView getConversationMessages(int pageNo, int pageSize, Long conversationId) {
        log.info("ConversationMessages starts.");
        long apiStartTimeMillis = new Date().getTime();

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        long profilingStartTimeMillis = new Date().getTime();

        ChatConversation chatConversation = fitwiseUtils.validateChatConversation(conversationId);

        User currentUser = userComponents.getUser();
        UserRole userRole = validationService.validateUserRole(userComponents.getRole());
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Query and validation: Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        boolean isInstructor = false;

        if (userRole.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
            isInstructor = true;
        }

        DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        DateFormat dateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
        String userTimeZone = fitwiseUtils.getUserTimeZone();
        if (userTimeZone != null) {
            TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);
            timeFormat.setTimeZone(timeZone);
            dateFormat.setTimeZone(timeZone);
        }

        UserProfile recipient = null;
        if (isInstructor) {
            recipient = chatConversation.getSecondaryUser();
        } else {
            recipient = chatConversation.getPrimaryUser();
        }

        ChatConversationView chatConversationView = new ChatConversationView();

        profilingStartTimeMillis = new Date().getTime();

        chatConversationView.setConversationId(conversationId);
        if (recipient != null) {
            chatConversationView.setRecipientName(recipient.getFirstName() + " " + recipient.getLastName());
            if (recipient.getProfileImage() != null) {
                chatConversationView.setRecipientProfileImage(recipient.getProfileImage().getImagePath());
            }
        } else if (recipient == null && chatConversation.isReceiveOnly()) {
            chatConversationView.setRecipientName(KeyConstants.KEY_ADMIN_CHAT_NAME);
            chatConversationView.setReceiveOnly(true);
            AppConfigKeyValue appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_TRAINNR_PROFILE);
            if(appConfig != null){
                chatConversationView.setRecipientProfileImage(appConfig.getValueString());
            }
        } else {
            chatConversationView.setRecipientName(KeyConstants.KEY_ANONYMOUS);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Response construction : basic details : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        Page<ChatMessage> chatMessagePage = chatMessageRepository.findByConversationConversationIdOrderByCreatedDateDesc(conversationId, pageRequest);

        if (chatMessagePage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        List<ChatMessageView> messageList = new ArrayList<>();
        boolean isSent = false;

        for (ChatMessage chatMessage : chatMessagePage) {
            ChatMessageView messageView = new ChatMessageView();

            messageView.setMessageContent(chatMessage.getContent());
            if (!chatConversation.isReceiveOnly()) {
                isSent = currentUser.getUserId().equals(chatMessage.getSender().getUserId());

            }
            messageView.setMessageSent(isSent);

            Date msgDate = chatMessage.getCreatedDate();
            messageView.setMessageTime(timeFormat.format(msgDate));
            messageView.setMessageDay(dateFormat.format(msgDate));
            messageView.setMessageTimeStamp(msgDate);

            //Only received message are set as new, if unread.
            if (!isSent) {
                messageView.setMessageNew(chatMessage.isUnread());
            } else {
                messageView.setMessageNew(false);
            }

            messageList.add(messageView);
        }
        chatConversationView.setMessages(messageList);
        chatConversationView.setTotalMessageCount(Math.toIntExact(chatMessagePage.getTotalElements()));

        profilingEndTimeMillis = new Date().getTime();
        log.info("Response construction : Chat messages : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();

        //Set block status to restrict sending msg. And , set CanUnBlock status - to allow unblock
        boolean isConversationBlocked = chatBlockStatusRepository.existsByConversationConversationId(conversationId);
        if (isConversationBlocked) {
            chatConversationView.setBlocked(true);
            ChatBlockStatus chatBlockStatus = chatBlockStatusRepository.findByConversationConversationIdAndBlockedByUserUserId(conversationId, currentUser.getUserId());
            if (chatBlockStatus != null) {
                chatConversationView.setCanUnblock(true);
            }
        } else {
            chatConversationView.setBlocked(false);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Response construction : Block status : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        //Updating unread status of conversation's received messages to read.
        List<ChatMessage> unreadChatMessages;
        if (recipient != null) {
            unreadChatMessages = chatMessageRepository.findByConversationConversationIdAndSenderUserIdAndIsUnread(conversationId, recipient.getUser().getUserId(), true);
        } else {
            unreadChatMessages = chatMessageRepository.findByConversationConversationIdAndIsUnread(conversationId, true);
        }
        for (ChatMessage eachMessage : unreadChatMessages) {
            eachMessage.setUnread(false);
            eachMessage.setModifiedDate(new Date());
        }
        chatMessageRepository.saveAll(unreadChatMessages);
        profilingEndTimeMillis = new Date().getTime();
        log.info("Updating unread messages : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("ConversationMessages ends.");

        return chatConversationView;

    }

    public String blockConversation(Long conversationId) {
        log.info("Block conversation starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        ChatConversation chatConversation = fitwiseUtils.validateChatConversation(conversationId);
        log.info("Validate chat conversation : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        ChatBlockStatus chatBlockStatus = chatBlockStatusRepository.findByConversationConversationIdAndBlockedByUserUserId(conversationId, user.getUserId());
        String responseMsg = MessageConstants.MSG_CONVERSATION_BLOCKED_ALREADY;

        if (chatBlockStatus == null) {
            chatBlockStatus = new ChatBlockStatus();
            chatBlockStatus.setConversation(chatConversation);
            chatBlockStatus.setBlockedByUser(user);

            chatBlockStatusRepository.save(chatBlockStatus);
            responseMsg = MessageConstants.MSG_CONVERSATION_BLOCKED;
        }
        log.info("Block conversation : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Block conversation ends.");

        return responseMsg;

    }

    /**
     * Unblock chat can be performed only by the user who blocked the conversation
     *
     * @param conversationId
     * @return
     */
    public String unblockConversation(Long conversationId) {

        ChatConversation chatConversation = fitwiseUtils.validateChatConversation(conversationId);
        User user = userComponents.getUser();

        ChatBlockStatus chatBlockStatus = chatBlockStatusRepository.findByConversationConversationIdAndBlockedByUserUserId(conversationId, user.getUserId());
        String responseMsg = MessageConstants.MSG_CONVERSATION_CANT_UNBLOCK;

        if (chatBlockStatus != null) {
            chatBlockStatusRepository.delete(chatBlockStatus);
            responseMsg = MessageConstants.MSG_CONVERSATION_UNBLOCKED;
        }

        return responseMsg;

    }

    public ChatMessage sendMessageByAdmin(Long conversationId, String messageContent, Long recipientUserId, String recipientRole) {

        if (conversationId == null || conversationId.equals(0L)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONVERSATION_ID_NULL, MessageConstants.ERROR);
        }
        ChatConversation conversation;
        if (recipientRole.equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
            conversation = chatConversationRepository.findByConversationIdAndPrimaryUserUserUserId(conversationId, recipientUserId);

        } else {
            conversation = chatConversationRepository.findByConversationIdAndSecondaryUserUserUserId(conversationId, recipientUserId);
        }
        if (conversation == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONVERSATION_NOT_FOUND, MessageConstants.ERROR);
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversation(conversation);
        chatMessage.setContent(messageContent);
        //chatMessage.setSender(user);
        chatMessage.setUnread(true);

        chatMessageRepository.save(chatMessage);

        UserProfile recipient = null;
        boolean isInstructor = false;
        if(recipientRole.equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)){
            recipient = conversation.getPrimaryUser();
            isInstructor = true;
        }else{
            recipient = conversation.getSecondaryUser();
        }

        if (recipient != null) {
            triggerPushNotificationForAdminMessage(isInstructor,recipient, messageContent);
        }
        return chatMessage;
    }

    public ChatMessageView sendMessage(Long conversationId, String messageContent) {

        ChatConversation chatConversation = fitwiseUtils.validateChatConversation(conversationId);
        if(chatConversation.isReceiveOnly()){
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CANT_CHAT_ADMIN, MessageConstants.ERROR);
        }
        boolean isConversationBlocked = chatBlockStatusRepository.existsByConversationConversationId(conversationId);
        if (isConversationBlocked) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONVERSATION_BLOCKED, null);
        }

        User user = userComponents.getUser();

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversation(chatConversation);
        chatMessage.setContent(messageContent);
        chatMessage.setSender(user);
        chatMessage.setUnread(true);

        chatMessageRepository.save(chatMessage);

        DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        DateFormat dateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
        String userTimeZone = fitwiseUtils.getUserTimeZone();
        if (userTimeZone != null) {
            TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);
            timeFormat.setTimeZone(timeZone);
            dateFormat.setTimeZone(timeZone);
        }

        ChatMessageView messageView = new ChatMessageView();

        messageView.setMessageContent(chatMessage.getContent());
        messageView.setMessageSent(true);

        Date msgDate = chatMessage.getCreatedDate();
        messageView.setMessageTime(timeFormat.format(msgDate));
        messageView.setMessageDay(dateFormat.format(msgDate));
        messageView.setMessageTimeStamp(msgDate);
        messageView.setMessageNew(false);

        UserRole userRole = validationService.validateUserRole(userComponents.getRole());
        boolean isInstructor = userRole.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR) ? true : false;

        UserProfile recipient = null;
        if (isInstructor) {
            recipient = chatConversation.getSecondaryUser();
        } else {
            recipient = chatConversation.getPrimaryUser();
        }
        if (recipient != null) {
            triggerPushNotification(isInstructor, chatConversation.getPrimaryUser(), chatConversation.getSecondaryUser(), messageContent);
        }

        return messageView;
    }

    /**
     * @param isInstructor
     * @param primaryUser
     * @param secondaryUser
     * @param messageContent
     */
    private void triggerPushNotification(boolean isInstructor, UserProfile primaryUser, UserProfile secondaryUser, String messageContent) {
        User notifyUser;
        String notifyRole;
        String senderName;
        String notificationMessage;
        if (isInstructor) {
            notifyUser = secondaryUser.getUser();
            senderName = fitwiseUtils.getUserFullName(primaryUser);
            notificationMessage = PushNotificationConstants.NEW_MESSAGE_MEMBER_MESSAGE.replace("#INSTRUCTOR_NAME#", senderName);
            notifyRole = KeyConstants.KEY_MEMBER;
        } else {
            notifyUser = primaryUser.getUser();
            senderName = fitwiseUtils.getUserFullName(secondaryUser);
            notificationMessage = PushNotificationConstants.NEW_MESSAGE_INSTRUCTOR_MESSAGE.replace("#MEMBER_NAME#", senderName);
            notifyRole = KeyConstants.KEY_INSTRUCTOR;
        }

        messageContent = messageContent.substring(0, Math.min(200, messageContent.length())) + "...";
        notificationMessage = notificationMessage.replace("#MESSAGE#", messageContent);

        try {
            NotificationContent notificationContent = new NotificationContent();
            notificationContent.setTitle(PushNotificationConstants.NEW_MESSAGE_TITLE);
            notificationContent.setBody(notificationMessage);

            pushNotificationAPIService.sendOnlyNotification(notificationContent, notifyUser, notifyRole);
        } catch (Exception e) {
            log.error("Exception while sending chat notification : " + e.getMessage());
        }
    }

    private void triggerPushNotificationForAdminMessage(boolean isInstructor, UserProfile recipientUser, String messageContent) {
        User notifyUser;
        String notifyRole;
        String notificationMessage;
        notifyUser = recipientUser.getUser();
        notificationMessage = PushNotificationConstants.NEW_MESSAGE_ADMIN_MESSAGE.replace("#ADMIN_NAME#", KeyConstants.KEY_ADMIN_CHAT_NAME);
        if (isInstructor) {
            notifyRole = KeyConstants.KEY_INSTRUCTOR;
        } else {
            notifyRole = KeyConstants.KEY_MEMBER;
        }

        messageContent = messageContent.substring(0, Math.min(200, messageContent.length())) + "...";
        notificationMessage = notificationMessage.replace("#MESSAGE#", messageContent);

        try {
            NotificationContent notificationContent = new NotificationContent();
            notificationContent.setTitle(PushNotificationConstants.NEW_MESSAGE_TITLE);
            notificationContent.setBody(notificationMessage);

            pushNotificationAPIService.sendOnlyNotification(notificationContent, notifyUser, notifyRole);
        } catch (Exception e) {
            log.error("Exception while sending chat notification : " + e.getMessage());
        }
    }

    public Long startConversation(Long recipientUserId, String messageContent) {

        User user = userComponents.getUser();

        if (recipientUserId == null || recipientUserId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }

        User recipientUser = userRepository.findByUserId(recipientUserId);
        if (recipientUser == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }

        if (user.getUserId().equals(recipientUserId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SELF_RECIPIENT, MessageConstants.ERROR);
        }

        if (messageContent == null || messageContent.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_MESSAGE_EMPTY, MessageConstants.ERROR);

        }

        UserRole userRole = validationService.validateUserRole(userComponents.getRole());
        boolean isInstructor = false;
        if (userRole.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
            isInstructor = true;
        }

        //Can not start conversation with blocked user(s)
        if (isInstructor) {
            if (blockedUserRepository.existsByUserUserIdAndUserRoleName(user.getUserId(), KeyConstants.KEY_INSTRUCTOR)) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CANT_START_CONVERSATION_USER_BLOCKED, MessageConstants.ERROR);
            }
            if (blockedUserRepository.existsByUserUserIdAndUserRoleName(recipientUser.getUserId(), KeyConstants.KEY_MEMBER)) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CANT_START_CONVERSATION_RECIPIENT_BLOCKED, MessageConstants.ERROR);
            }
        } else {
            if (blockedUserRepository.existsByUserUserIdAndUserRoleName(user.getUserId(), KeyConstants.KEY_MEMBER)) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CANT_START_CONVERSATION_USER_BLOCKED, MessageConstants.ERROR);
            }
            if (blockedUserRepository.existsByUserUserIdAndUserRoleName(recipientUser.getUserId(), KeyConstants.KEY_INSTRUCTOR)) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CANT_START_CONVERSATION_RECIPIENT_BLOCKED, MessageConstants.ERROR);
            }
        }


        ChatConversation conversation = null;
        if (isInstructor) {
            conversation = chatConversationRepository.findByPrimaryUserUserUserIdAndSecondaryUserUserUserId(user.getUserId(), recipientUserId);
        } else {
            conversation = chatConversationRepository.findByPrimaryUserUserUserIdAndSecondaryUserUserUserId(recipientUserId, user.getUserId());
        }

        if (conversation == null) {
            UserProfile memberProfile = null;
            UserProfile instructorProfile = null;
            String instructorName = null;
            String memberName = null;
            String requiredRecipientRole;

            UserProfile userProfile = userProfileRepository.findByUser(user);
            UserProfile recipientUserProfile = userProfileRepository.findByUserUserId(recipientUserId);

            if (isInstructor) {
                requiredRecipientRole = KeyConstants.KEY_MEMBER;
                instructorProfile = userProfile;
                memberProfile = recipientUserProfile;
                instructorName = userProfile.getFirstName() + " " + userProfile.getLastName();
                memberName = recipientUserProfile.getFirstName() + " " + recipientUserProfile.getLastName();
            } else {
                requiredRecipientRole = KeyConstants.KEY_INSTRUCTOR;
                instructorProfile = recipientUserProfile;
                memberProfile = userProfile;
                instructorName = recipientUserProfile.getFirstName() + " " + recipientUserProfile.getLastName();
                memberName = userProfile.getFirstName() + " " + userProfile.getLastName();
            }

            List<UserRoleMapping> userRoleMapping = userRoleMappingRepository.findByUserUserIdAndUserRoleName(recipientUserId, requiredRecipientRole);
            if (userRoleMapping.isEmpty()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SAME_ROLE_CHAT_NOT_ALLOWED, MessageConstants.ERROR);
            }

            conversation = new ChatConversation();
            conversation.setPrimaryUser(instructorProfile);
            conversation.setSecondaryUser(memberProfile);
            conversation = chatConversationRepository.save(conversation);
        }

        sendMessage(conversation.getConversationId(), messageContent);

        return conversation.getConversationId();

    }

    /**
     * Start conversation by admin
     * @param recipientUserId
     * @param messageContent
     * @param recipientRole
     * @return
     */
    public Long startConversationByAdmin(Long recipientUserId, String messageContent, String recipientRole) {

        if (recipientUserId == null || recipientUserId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }

        User recipientUser = userRepository.findByUserId(recipientUserId);
        if (recipientUser == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }


        if (messageContent == null || messageContent.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_MESSAGE_EMPTY, MessageConstants.ERROR);

        }
        ChatConversation conversation = null;

        if (conversation == null) {
            UserProfile memberProfile = null;
            UserProfile instructorProfile = null;

            UserProfile recipientUserProfile = userProfileRepository.findByUserUserId(recipientUserId);
            if (recipientRole.equalsIgnoreCase(KeyConstants.KEY_MEMBER)) {
                memberProfile = recipientUserProfile;

            } else {
                instructorProfile = recipientUserProfile;

            }

            conversation = new ChatConversation();
            conversation.setPrimaryUser(instructorProfile);
            conversation.setSecondaryUser(memberProfile);
            conversation.setReceiveOnly(true);
            conversation = chatConversationRepository.save(conversation);
        }

        sendMessageByAdmin(conversation.getConversationId(), messageContent, recipientUserId, recipientRole);


        return conversation.getConversationId();

    }

    public Long getConversationId(Long recipientUserId) {
        Long conversationId = null;

        User user = userComponents.getUser();

        if (recipientUserId == null || recipientUserId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }

        User recipientUser = userRepository.findByUserId(recipientUserId);
        if (recipientUser == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }

        if (user.getUserId().equals(recipientUserId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SELF_RECIPIENT, MessageConstants.ERROR);
        }

        UserRole userRole = validationService.validateUserRole(userComponents.getRole());
        boolean isInstructor = false;
        if (userRole.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
            isInstructor = true;
        }

        ChatConversation conversation = null;
        if (isInstructor) {
            conversation = chatConversationRepository.findByPrimaryUserUserUserIdAndSecondaryUserUserUserId(user.getUserId(), recipientUserId);
        } else {
            conversation = chatConversationRepository.findByPrimaryUserUserUserIdAndSecondaryUserUserUserId(recipientUserId, user.getUserId());
        }

        if (conversation != null) {
            conversationId = conversation.getConversationId();
        }

        return conversationId;

    }

    public String blockConversationByAdmin(Long conversationId) {
        User user = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(user);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }

        Optional<ChatConversation> conversation = chatConversationRepository.findById(conversationId);

        if (!conversation.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONVERSATION_NOT_FOUND, MessageConstants.ERROR);
        }

        ChatConversation chatConversation = conversation.get();

        UserRole userRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_ADMIN);
        ChatBlockStatus chatBlockStatus = chatBlockStatusRepository.findByConversationConversationIdAndBlockedByUserUserRoleMappingsUserRole(conversationId, userRole);
        String responseMsg = MessageConstants.MSG_CONVERSATION_BLOCKED_ALREADY;

        if (chatBlockStatus == null) {
            chatBlockStatus = new ChatBlockStatus();
            chatBlockStatus.setConversation(chatConversation);
            chatBlockStatus.setBlockedByUser(user);

            chatBlockStatusRepository.save(chatBlockStatus);
            responseMsg = MessageConstants.MSG_CONVERSATION_BLOCKED;
        }

        return responseMsg;

    }

    public String unblockConversationByAdmin(Long conversationId) {
        User user = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(user);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }

        Optional<ChatConversation> conversation = chatConversationRepository.findById(conversationId);

        if (!conversation.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONVERSATION_NOT_FOUND, MessageConstants.ERROR);
        }

        UserRole userRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_ADMIN);
        ChatBlockStatus chatBlockStatus = chatBlockStatusRepository.findByConversationConversationIdAndBlockedByUserUserRoleMappingsUserRole(conversationId, userRole);
        String responseMsg = MessageConstants.MSG_CONVERSATION_CANT_UNBLOCK;

        if (chatBlockStatus != null) {
            List<UserRoleMapping> roleMappings = chatBlockStatus.getBlockedByUser().getUserRoleMappings();
            Optional<UserRole> adminRole = roleMappings.stream().map(roleMapping -> roleMapping.getUserRole()).filter(role -> role.getName().equalsIgnoreCase(KeyConstants.KEY_ADMIN)).findAny();

            if (adminRole.isPresent()) {
                chatBlockStatusRepository.delete(chatBlockStatus);
                responseMsg = MessageConstants.MSG_CONVERSATION_UNBLOCKED;
            }
        }

        return responseMsg;

    }

    public List<ChatConversation> getInstructorConversations(Long instructorId) {
        User user = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(user);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }

        if (instructorId == null || instructorId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }

        User instructorUser = userRepository.findByUserId(instructorId);
        if (instructorUser == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }

        List<ChatConversation> conversationList = chatConversationRepository.findByPrimaryUserUserUserId(instructorUser.getUserId());

        return conversationList;

    }

    public List<ChatConversation> getMemberConversations(Long memberId) {
        User user = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(user);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }

        if (memberId == null || memberId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }

        User memberUser = userRepository.findByUserId(memberId);
        if (memberUser == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }

        List<ChatConversation> conversationList = chatConversationRepository.findBySecondaryUserUserUserId(memberUser.getUserId());

        return conversationList;

    }

    /**
     * Method to get Messaging NotificationDetails
     *
     * @return
     */
    public NotificationView getNotificationDetails() {
        log.info("messaging notification starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        long profilingStartTimeMillis = System.currentTimeMillis();
        User user = userComponents.getUser();
        boolean isInstructor = false;

        UserRole userRole = validationService.validateUserRole(userComponents.getRole());

        if (userRole.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
            isInstructor = true;
        }

        List<ChatConversation> conversationList = null;
        if (isInstructor) {
            conversationList = chatConversationRepository.findByPrimaryUserUserUserId(user.getUserId());
        } else {
            conversationList = chatConversationRepository.findBySecondaryUserUserUserId(user.getUserId());
        }
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        int unreadConversationCount = 0;
        for (ChatConversation conversation : conversationList) {
            UserProfile recipient = null;
            if (isInstructor) {
                recipient = conversation.getSecondaryUser();
            } else {
                recipient = conversation.getPrimaryUser();
            }

            if (recipient != null) {
                int unreadChatMessageCount = chatMessageRepository.countByConversationConversationIdAndSenderUserIdAndIsUnread(conversation.getConversationId(), recipient.getUser().getUserId(), true);
                if (unreadChatMessageCount > 0) {
                    unreadConversationCount++;
                }
            } else if(recipient == null && conversation.isReceiveOnly()){
                int unreadMessageCount = chatMessageRepository.countByConversationConversationIdAndIsUnread(conversation.getConversationId(),true);
                if(unreadMessageCount > 0){
                    unreadConversationCount++;
                }
            }
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Unread msg count : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        NotificationView notificationView = new NotificationView();
        if (unreadConversationCount > 0) {
            notificationView.setHasUnreadConversations(true);
            notificationView.setUnreadConversationCount(unreadConversationCount);
        }

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("messaging notification ends.");

        return notificationView;
    }

}
