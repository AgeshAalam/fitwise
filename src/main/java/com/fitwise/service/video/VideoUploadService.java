package com.fitwise.service.video;

import com.fitwise.constants.VideoUploadStatus;
import com.fitwise.entity.VideoManagement;
import com.fitwise.repository.VideoManagementRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Video upload handling
 */
@Service
public class VideoUploadService {

    @Autowired
    private VideoManagementRepo videoManagementRepo;

    /**
     * Updating the upload status as in progress
     *
     * @param videoManagement
     */
    public void videoUploadUpdate(final VideoManagement videoManagement) {

        if (videoManagement.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.UPLOAD) || (videoManagement).getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_PROCESSING_FAILED)) {
            videoManagement.setUploadStatus(VideoUploadStatus.INPROGRESS);
        } else if (videoManagement.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.REUPLOAD) || (videoManagement).getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED)) {
            videoManagement.setUploadStatus(VideoUploadStatus.REUPLOAD_INPROGRESS);
        }

        videoManagementRepo.save(videoManagement);
    }
}
