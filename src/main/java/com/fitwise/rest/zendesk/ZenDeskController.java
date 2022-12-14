package com.fitwise.rest.zendesk;

import com.fitwise.request.zenDesk.ZenDeskClientView;
import com.fitwise.request.zenDesk.ZenDeskMailModel;
import com.fitwise.service.zenDesk.ZenDeskService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.ZenDeskWebHookResponseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/zenDesk")
public class ZenDeskController {

    @Autowired
    ZenDeskService zenDeskService;

    /**
     * Whenever a ticket is created in ZenDesk, a trigger comes back from ZenDesk to the below end point as a web-hook!
     * The status of the ticket along with the ZenDesk ticket id is saved into our Back end system!
     *
     * @param zenDeskResponse
     * @return
     */
    @PostMapping("/zenDeskTicketWebHook")
    public ResponseModel zenDeskTicketWebHook(@RequestBody ZenDeskWebHookResponseView zenDeskResponse) {
        return zenDeskService.zenDeskTicketWebHook(zenDeskResponse);
    }

    /**
     * Method used to create a ticket in ZenDesk using API
     *
     * @return
     */
    @PostMapping("/createZenDeskTicket")
    public ResponseModel createZenDeskTicket(@RequestBody ZenDeskClientView zenDeskClientView){
      return zenDeskService.createZenDeskTicket(zenDeskClientView);
    }

    /**
     * Creating a zen desk ticket through support link on email
     * @param zenDeskMailModel
     * @return
     */
    @PostMapping("/createZenDeskTicketFromEmail")
    public ResponseModel createZenDeskTicketFromEmail(@RequestBody ZenDeskMailModel zenDeskMailModel) {
        return zenDeskService.createZenDeskTicketFromEmail(zenDeskMailModel);
    }


}
