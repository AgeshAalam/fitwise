package com.fitwise.view.fcm;

import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 22/07/20
 */
@Data
public class PushNotificationApiRequest {

    List<String> registration_ids;

    NotificationContent notification;

    NotificationContent data;

}
