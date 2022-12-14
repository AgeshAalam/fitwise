package com.fitwise.rest.packaging;

import com.fitwise.constants.Constants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.program.model.PromoUploadModel;
import com.fitwise.program.service.ProgramService;
import com.fitwise.service.instructor.InstructorProgramService;
import com.fitwise.service.packaging.SubscriptionPackageV2Service;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/fw/v2/subscriptionPackage")
public class SubscriptionPackageV2Controller {

    private final ProgramService programService;
    private final InstructorProgramService instructorProgramService;
    private final SubscriptionPackageV2Service subscriptionPackageV2Service;

    /**
     * Upload promo video for package
     * @param promoUploadModel
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/promo")
    public ResponseModel getPrograms(@RequestBody final PromoUploadModel promoUploadModel) throws IOException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, programService.uploadPromotion(promoUploadModel, KeyConstants.KEY_SUBSCRIPTION_PACKAGE));
    }

    /**
     * Update the upload completed status for promo
     *
     * @param promotionId
     * @return
     * @throws IOException
     */
    @PutMapping(value = "/promo/uploadcompleted")
    public ResponseModel updatePromoVideoUploaded(@RequestParam Long promotionId) {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SCS_VIDEO_UPLOAD, instructorProgramService.updateVideoUploaded(promotionId, InstructorConstant.VIDEO_TYPE_PROMO_VIDEO));
    }

    /**
     * Delete promotion
     *
     * @param promotionId
     * @return
     */
    @DeleteMapping(value = "/promo")
    public ResponseModel deletePromotion(@RequestParam Long promotionId, @RequestParam Long packageId) {
        return subscriptionPackageV2Service.deletePromotion(promotionId, packageId);
    }


}
