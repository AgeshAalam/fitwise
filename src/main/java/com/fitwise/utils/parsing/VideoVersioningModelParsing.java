package com.fitwise.utils.parsing;

import com.fitwise.constants.VimeoConstants;
import com.fitwise.model.instructor.VimeoVersioningModel;
import com.fitwise.model.instructor.VideoVersioningModel;
import com.fitwise.model.instructor.VimeoVersionUploadModel;

/**
 * The Class VideoVersioningModelParsing for parsing the data
 */
public class VideoVersioningModelParsing {

    /**
     * This method parse the data into the vimeo model
     * @param videoVersioningModel
     * @return
     */
    public static VimeoVersioningModel parseToVimeoModel(VideoVersioningModel videoVersioningModel){
        VimeoVersioningModel vimeoModel = new VimeoVersioningModel();
        vimeoModel.setFile_name(videoVersioningModel.getFileName());
        VimeoVersionUploadModel upload = new VimeoVersionUploadModel();
        upload.setSize(videoVersioningModel.getFileSize());
        upload.setApproach(VimeoConstants.APPROACH);
        vimeoModel.setUpload(upload);
        return vimeoModel;
    }
}
