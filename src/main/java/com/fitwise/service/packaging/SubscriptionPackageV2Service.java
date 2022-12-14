package com.fitwise.service.packaging;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Promotions;
import com.fitwise.entity.VideoManagement;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.service.VimeoService;
import com.fitwise.repository.PromotionRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionPackageV2Service {

    private final PromotionRepository promotionRepository;
    private final SubscriptionPackageRepository subscriptionPackageRepository;
    private final VimeoService vimeoService;

    /**
     * Delete promotion video
     *
     * @param promotionId
     * @param packageId
     * @return
     * @throws ApplicationException
     */
    public ResponseModel deletePromotion(Long promotionId, Long packageId) {
        ResponseModel responseModel = new ResponseModel();
        if (promotionId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_ID_NULL, null);
        }

        if (packageId == null || packageId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, null);
        }

        Promotions promotions = promotionRepository.findByPromotionId(promotionId);
        if (promotions == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_NOT_FOUND, null);
        }

        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(packageId);
        if (subscriptionPackage == null) {
            throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, null);
        }

        List<SubscriptionPackage> packageList = subscriptionPackageRepository.findByPromotionPromotionId(promotionId);
        if (packageList.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_NOT_FOUND, null);
        }

        if (!packageList.contains(subscriptionPackage)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PACKAGE_ID_INCORRECT, null);
        }

        boolean isRelatedToOnePackage = false;
        if (packageList.size() == 1) {
            isRelatedToOnePackage = true;
        }

        subscriptionPackage.setPromotion(null);
        subscriptionPackageRepository.save(subscriptionPackage);
        if (isRelatedToOnePackage) {
            //deleting promo video on vimeo
            VideoManagement videoManagement = promotions.getVideoManagement();
            try {
                if (videoManagement != null) {
                    vimeoService.deleteVimeoVideo(videoManagement.getUrl());
                }
            } catch (Exception e) {
                log.info(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
            }

            promotionRepository.delete(promotions);

        }
        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_PROMOTION_DELETED);
        return responseModel;
    }

}
