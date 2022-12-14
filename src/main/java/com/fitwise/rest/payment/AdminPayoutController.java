package com.fitwise.rest.payment;

import com.fitwise.constants.Constants;
import com.fitwise.constants.ExportConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.payment.stripe.AdminPayoutService;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;

/*
 * Created by Vignesh.G on 29/06/21
 */
@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/admin/payout")
@RequiredArgsConstructor
public class AdminPayoutController {

    private final AdminPayoutService adminPayoutService;
    private final FitwiseUtils fitwiseUtils;

    /**
     * Get All payout details for the instructor
     * @param pageNo
     * @param pageSize
     * @param filterType
     * @param sortBy
     * @param sortOrder
     * @param platform
     * @param search
     * @return
     */
    @GetMapping("/all")
    public ResponseModel getPayouts(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String filterType, @RequestParam String sortBy, @RequestParam String sortOrder, @RequestParam String platform, Optional<String> search) {
        Map<String, Object> payoutsMap = adminPayoutService.getPayouts(pageNo, pageSize, filterType, sortBy, sortOrder, platform, search);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PAYOUTS_LIST_FETCHED, payoutsMap);
    }

    /**
     * Export all payout details
     * @param filterType
     * @param sortBy
     * @param sortOrder
     * @param platform
     * @param search
     * @return
     * @throws ApplicationException
     */
    @GetMapping(value = "/csv")
    public ResponseEntity<InputStreamResource> getPayoutsCsv(@RequestParam String filterType, @RequestParam String sortBy, @RequestParam String sortOrder, @RequestParam String platform, Optional<String> search) throws ApplicationException {
        ByteArrayInputStream byteArrayInputStream = adminPayoutService.getPayoutsCsv(filterType, sortBy, sortOrder, platform, search);
        InputStreamResource file = new InputStreamResource(byteArrayInputStream);
        HttpHeaders httpHeaders = fitwiseUtils.getHttpDownloadHeaders(ExportConstants.FILE_NAME_PAYOUT);
        return new ResponseEntity<>(file, httpHeaders, OK);
    }

}
