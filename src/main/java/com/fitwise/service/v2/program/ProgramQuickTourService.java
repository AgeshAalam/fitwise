package com.fitwise.service.v2.program;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.QuickTourVideos;
import com.fitwise.entity.VideoManagement;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.QuickTourRepository;
import com.fitwise.repository.VideoManagementRepo;
import com.fitwise.view.QuickTourVideosResponseView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.TourListResponseView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProgramQuickTourService {

    final private QuickTourRepository quickTourRepository;
    final private VideoManagementRepo videoManagementRepo;

    /**
     * Get program quick tour videos
     * @param pageNo
     * @param pageSize
     * @return
     */
    public ResponseModel getQuickTourVideos(int pageNo, int pageSize){
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        TourListResponseView tourListResponseView = new TourListResponseView();
        Page<QuickTourVideos> quickTourPage = quickTourRepository.findAll(PageRequest.of(pageNo - 1, pageSize));
        if(quickTourPage.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, null, null);
        }
        List<QuickTourVideos> quickTourVideosList = quickTourPage.getContent();
        List<QuickTourVideosResponseView> quickTourVideosResponseViews = new ArrayList<>();
        if (quickTourVideosList != null && !quickTourVideosList.isEmpty()) {
            tourListResponseView.setTourVideosCount((int)quickTourPage.getTotalElements());
            for (QuickTourVideos quickTourVideos : quickTourVideosList) {
                QuickTourVideosResponseView quickTourVideosResponseView = new QuickTourVideosResponseView();
                VideoManagement videoManagement = videoManagementRepo.findByVideoManagementId(quickTourVideos.getVideoManagement().getVideoManagementId());
                quickTourVideosResponseView.setQuickTourVideoId(quickTourVideos.getQuickTourVideoId());
                quickTourVideosResponseView.setTitle(videoManagement.getTitle());
                quickTourVideosResponseView.setThumbnailUrl(videoManagement.getThumbnail().getImagePath());
                quickTourVideosResponseView.setVideoUrl(videoManagement.getUrl());
                quickTourVideosResponseView.setDuration(videoManagement.getDuration());
                quickTourVideosResponseViews.add(quickTourVideosResponseView);
            }
        }
        tourListResponseView.setQuickTourVideos(quickTourVideosResponseViews);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_QUICK_TOUR_VIDEOS_FETCHED, tourListResponseView);
    }

}
