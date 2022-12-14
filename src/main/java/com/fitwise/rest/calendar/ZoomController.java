package com.fitwise.rest.calendar;

import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.response.zoom.DeauthorizationEventNotification;
import com.fitwise.service.calendar.ZoomService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.calendar.ZoomCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/cal/instructor/zoom")
public class ZoomController {

    @Autowired
    private ZoomService zoomService;

    @GetMapping(value = "/account")
    public ResponseModel getAccount() {
        return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.CAL_SCS_ZOOM_AUTH, zoomService.getAccount());
    }

    @PostMapping(value = "/account")
    public ResponseModel saveAccount(@RequestBody ZoomCredentials zoomCredentials) {
        return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.CAL_SCS_ZOOM_AUTHORIZED, zoomService.saveAccount(zoomCredentials));
    }

    @PostMapping(value = "/deauth")
    public ResponseModel removeAccount(@RequestHeader("authorization") String verificationToken, @RequestBody DeauthorizationEventNotification notification) {
        zoomService.removeAccount(verificationToken, notification);
        return new ResponseModel(Constants.SUCCESS_STATUS, "", null);
    }

    @GetMapping(value = "/authorizeZoom")
    public ResponseModel authorizeZoom() {
        return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.CAL_SCS_ZOOM_AUTH, zoomService.authorizeZoom());
    }

    @GetMapping(value = "/allAccounts")
    public ResponseModel getMyAllAccount() {
        return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.CAL_SCS_ZOOM_ACCOUNTS, zoomService.getMyAllZoomAccounts());
    }

    @PutMapping(value = "/changeActiveStatus")
    public ResponseModel changeCalendarActiveStatus(final @RequestParam String userZoomAccountId, final @RequestParam boolean setActive){
        zoomService.changeZoomAccountStatus(userZoomAccountId, setActive);
        return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.MSG_CAL_ACTIVE_STATUS_CHANGED, null);
    }

    @DeleteMapping(value = "/account")
    public ResponseModel deleteZoomAccount(final @RequestParam String userZoomAccountId){
        zoomService.deleteZoomAccount(userZoomAccountId);
        return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.MSG_SCS_ZOOM_ACCOUNT_DELETE, null);
    }
}
