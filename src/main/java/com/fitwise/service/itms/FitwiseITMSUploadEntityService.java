package com.fitwise.service.itms;

import com.fitwise.constants.DBConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.itms.ItmsPublish;
import com.fitwise.entity.itms.ItmsUnpublish;
import com.fitwise.repository.itms.ITMSPublishRepository;
import com.fitwise.repository.itms.ITMSUnPublishRepository;
import com.fitwise.service.discountservice.DiscountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class FitwiseITMSUploadEntityService {

    @Autowired
    private ITMSPublishRepository itmsPublishRepository;

    @Autowired
    private ITMSUnPublishRepository itmsUnPublishRepository;

    @Autowired
    private DiscountsService discountsService;

    public void publishOrRepublish(final Programs program){
        List<ItmsPublish> itmsPublishes = itmsPublishRepository.findByProgram(program);
        ItmsPublish itmsPublish = new ItmsPublish();
        if(itmsPublishes.isEmpty()){
            itmsPublish.setIsNew(true);
            itmsPublish.setProgram(program);
        }else{
            itmsPublish = itmsPublishes.get(0);
            /*
            * 'is_new' column is updated to false if any of the previous publish is successful.
            * If publish failed previous time, and any older publish was successful, is_new column will already be as false, and updating to false is not necessary.
            * */
            if (!itmsPublish.getNeedUpdate().booleanValue() && DBConstants.ITMS_UPDATED.equals(itmsPublish.getStatus())) {
                itmsPublish.setIsNew(false);
            }
        }
        List<ItmsUnpublish> itmsUnpublishes = itmsUnPublishRepository.findByProgram(program);
        for(ItmsUnpublish itmsUnpublish : itmsUnpublishes){
            itmsUnpublish.setNeedUpdate(false);
            itmsUnPublishRepository.save(itmsUnpublish);
        }
        itmsPublish.setNeedUpdate(true);
        itmsPublishRepository.save(itmsPublish);
    }

    public void unPublish(final Programs program){
        List<ItmsUnpublish> itmsUnpublishes = itmsUnPublishRepository.findByProgram(program);
        ItmsUnpublish itmsUnPublish = new ItmsUnpublish();
        if(itmsUnpublishes.isEmpty()){
            itmsUnPublish.setProgram(program);
        }else{
            itmsUnPublish = itmsUnpublishes.get(0);
        }
        boolean isNewAndNotUploadedProgram = false;
        List<ItmsPublish> itmsPublishes = itmsPublishRepository.findByProgram(program);
        for(ItmsPublish itmsPublish : itmsPublishes){

            /*New program that has not been uploaded on ITMS*/
            if(itmsPublish.getIsNew().booleanValue() && itmsPublish.getNeedUpdate().booleanValue()) {
                isNewAndNotUploadedProgram = true;
            }

            itmsPublish.setNeedUpdate(false);
            itmsPublishRepository.save(itmsPublish);
        }

        /*Upload unpublished programs to ITMS only if the program publish was uploaded already atleast once*/
        if(isNewAndNotUploadedProgram) {
            itmsUnPublish.setNeedUpdate(false);
        } else {
            itmsUnPublish.setNeedUpdate(true);
        }
        itmsUnPublishRepository.save(itmsUnPublish);
        discountsService.unpublishDiscounts(program);
    }
}
