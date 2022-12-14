package com.fitwise.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Images;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.Workouts;
import com.fitwise.entity.thumbnail.BulkUpload;
import com.fitwise.entity.thumbnail.BulkUploadFailure;
import com.fitwise.entity.thumbnail.BulkUploadFailureCsv;
import com.fitwise.entity.thumbnail.ThumbnailCount;
import com.fitwise.entity.thumbnail.ThumbnailImages;
import com.fitwise.entity.thumbnail.ThumbnailMainTags;
import com.fitwise.entity.thumbnail.ThumbnailSubTags;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.thumbnail.SystemThumbnailUploadModel;
import com.fitwise.model.thumbnail.ThumbnailMainTagsModel;
import com.fitwise.model.thumbnail.ThumbnailSubTagsModel;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.repository.ImageRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.WorkoutRepository;
import com.fitwise.repository.thumbnail.BulkUploadFailureCsvRepository;
import com.fitwise.repository.thumbnail.BulkUploadFailureRepository;
import com.fitwise.repository.thumbnail.BulkUploadRepository;
import com.fitwise.repository.thumbnail.ThumbnailCountRepository;
import com.fitwise.repository.thumbnail.ThumbnailMainTagsRepository;
import com.fitwise.repository.thumbnail.ThumbnailRepository;
import com.fitwise.repository.thumbnail.ThumbnailSubTagsRepository;
import com.fitwise.specifications.ThumbnailSpecifications;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.ThumbnailFilterView;
import com.fitwise.view.ThumbnailListResponseView;
import com.fitwise.view.ThumbnailResponseView;
import com.fitwise.view.ThumbnailSubTagResponseView;
import com.fitwise.view.ThumbnailTagResponseView;
import com.fitwise.view.ThumbnailTagView;
import com.fitwise.view.ThumbnailUploadResponseView;
import com.fitwise.view.thumbnail.BulkUploadFailureView;
import com.fitwise.view.thumbnail.BulkUploadView;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ThumbnailService {

    public static final List<String> CSV_HEADERS = Arrays.asList(new String[]{"File Name", "Gender", "Location", "Fitness activity", "No of People", "Equipment", "Exercise/Movement", "Muscle groups"});

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private UploadImageService uploadImageService;

    @Autowired
    private ThumbnailRepository thumbnailRepository;

    @Autowired
    private ThumbnailSubTagsRepository thumbnailSubTagsRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private FitwiseUtils fitwiseUtils;

    @Autowired
    private ThumbnailMainTagsRepository thumbnailMainTagsRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private ThumbnailCountRepository thumbnailCountRepository;
    @Autowired
    BulkUploadRepository bulkUploadRepository;
    @Autowired
    BulkUploadFailureRepository bulkUploadFailureRepository;
    @Autowired
    BulkUploadFailureCsvRepository bulkUploadFailureCsvRepository;
    @Autowired
    private GeneralProperties generalProperties;

    @Autowired
    BulkUploadService bulkUploadService;

    /**
     * Upload thumbnail by instructor
     * @param multipartFile
     * @return
     */
    public ResponseModel uploadCustomThumbnail(MultipartFile multipartFile) {
        log.info("uploadCustomThumbnail starts.");
        long apiStartTimeMillis = new Date().getTime();

        long profilingStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        List<ThumbnailImages> thumbnailImagesList = thumbnailRepository.findByUserAndImagesFileName(user,  multipartFile.getOriginalFilename());
        if(!thumbnailImagesList.isEmpty()){
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_ERR_THUMBNAIL_FILENAME_EXISTS, null);
        }
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Validation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        Images image = uploadImageService.uploadImage(multipartFile, KeyConstants.KEY_THUMBNAIL);

        profilingStartTimeMillis = new Date().getTime();
        ThumbnailImages thumbnailImages = new ThumbnailImages();
        thumbnailImages.setImages(image);
        thumbnailImages.setUser(user);
        thumbnailImages.setType(KeyConstants.KEY_CUSTOM);
        thumbnailRepository.save(thumbnailImages);
        ThumbnailResponseView thumbnailResponseView = new ThumbnailResponseView();
        thumbnailResponseView.setThumbnailId(thumbnailImages.getImages().getImageId());
        thumbnailResponseView.setImageUrl(thumbnailImages.getImages().getImagePath());
        thumbnailResponseView.setFileName(thumbnailImages.getImages().getFileName());
        thumbnailResponseView.setType(thumbnailImages.getType());
        profilingEndTimeMillis = new Date().getTime();
        log.info("DB new row : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("uploadCustomThumbnail ends.");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_THUMBNAIL_ADDED, thumbnailResponseView);
    }


    /**
     * Upload system thumbnail- by admin
     * @param systemThumbnailUploadModel
     * @return
     */
    public ResponseModel uploadSystemThumbnail(User user, SystemThumbnailUploadModel systemThumbnailUploadModel) {
        log.info("Upload system thumbnail starts.");
        long apiStartTimeMillis = new Date().getTime();

        boolean isAdmin = fitwiseUtils.isAdmin(user);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        log.info("Verify the user is admin or not : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();


            List<ThumbnailMainTags> thumbnailMainTagsList = new ArrayList<>();
            List<ThumbnailSubTags> thumbnailSubTags = new ArrayList<>();

            for (ThumbnailMainTagsModel thumbnailMainTagModel : systemThumbnailUploadModel.getThumbnailTags()) {
                ThumbnailMainTags thumbnailMainTag = thumbnailMainTagsRepository.findByThumbnailMainTagId(thumbnailMainTagModel.getThumbnailMainTagId());
                if (thumbnailMainTag == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_THUMBNAIL_MAIN_TAG_ID_INCORRECT, MessageConstants.ERROR);

                }

                if (!thumbnailMainTag.isMultipleTagsAllowed()) {
                    if (thumbnailMainTagModel.getThumbnailSubTags().size() > 1) {
                        throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_MULTIPLE_SUB_TAGS_NOT_ALLOWED, MessageConstants.ERROR);

                    }
                }
                thumbnailMainTagsList.add(thumbnailMainTag);


                ThumbnailSubTags thumbnailTag = new ThumbnailSubTags();
                for (ThumbnailSubTagsModel thumbnailSubTagsRequestView : thumbnailMainTagModel.getThumbnailSubTags()) {
                    thumbnailTag = thumbnailSubTagsRepository.findByThumbnailSubTagId(thumbnailSubTagsRequestView.getThumbnailSubTagId());
                    thumbnailSubTags.add(thumbnailTag);
                }
            }
            log.info("Collect thumbnail main and sub tags : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            Images image = imageRepository.findByImageId(systemThumbnailUploadModel.getImageId());
            log.info("Query to get image : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            if(image == null){
                throw  new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_IMAGE_NOT_FOUND, MessageConstants.ERROR);
            }
            ThumbnailImages thumbnailImages = new ThumbnailImages();
            thumbnailImages.setImages(image);
            thumbnailImages.setUser(user);
            thumbnailImages.setType(KeyConstants.KEY_SYSTEM);
            thumbnailImages.setThumbnailMainTags(thumbnailMainTagsList);
            thumbnailImages.setThumbnailSubTags(thumbnailSubTags);
            thumbnailRepository.save(thumbnailImages);
            log.info("Query to save thumbnail : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

        for (ThumbnailMainTagsModel thumbnailMainTagModel : systemThumbnailUploadModel.getThumbnailTags()) {
            ThumbnailMainTags thumbnailMainTag = thumbnailMainTagsRepository.findByThumbnailMainTagId(thumbnailMainTagModel.getThumbnailMainTagId());
            ThumbnailCount thumbnailCount = thumbnailCountRepository.findByThumbnailTagIdAndIsMainTag(thumbnailMainTag.getThumbnailMainTagId(),true);
            int thumbnailCountOnMainTag = thumbnailRepository.countByThumbnailMainTagsThumbnailMainTagIdAndType(thumbnailMainTag.getThumbnailMainTagId(),KeyConstants.KEY_SYSTEM);
            thumbnailCount.setImagesCount(thumbnailCountOnMainTag);
            thumbnailCountRepository.save(thumbnailCount);

            for (ThumbnailSubTagsModel thumbnailSubTagsRequestView : thumbnailMainTagModel.getThumbnailSubTags()) {
                ThumbnailSubTags thumbnailTag = thumbnailSubTagsRepository.findByThumbnailSubTagId(thumbnailSubTagsRequestView.getThumbnailSubTagId());
                ThumbnailCount thumbnailCounts = thumbnailCountRepository.findByThumbnailTagIdAndIsMainTag(thumbnailTag.getThumbnailSubTagId(),false);
                int thumbnailCountOnSubTag = thumbnailRepository.countByThumbnailSubTagsThumbnailSubTagIdAndType(thumbnailTag.getThumbnailSubTagId(),KeyConstants.KEY_SYSTEM);
                thumbnailCounts.setImagesCount(thumbnailCountOnSubTag);
                thumbnailCountRepository.save(thumbnailCounts);
            }
        }
        log.info("Save thumbnail count : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();


        ThumbnailResponseView thumbnailResponseView = new ThumbnailResponseView();
        thumbnailResponseView.setThumbnailId(thumbnailImages.getImages().getImageId());
        thumbnailResponseView.setImageUrl(thumbnailImages.getImages().getImagePath());
        thumbnailResponseView.setFileName(thumbnailImages.getImages().getFileName());
        thumbnailResponseView.setType(thumbnailImages.getType());
        log.info("Response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Upload system thumbnail ends.");



        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_THUMBNAIL_ADDED, thumbnailResponseView);
    }


    /**
     * get all thumbnails uploaded by instructor
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    public ResponseModel getCustomThumbnails(int pageNo, int pageSize, Optional<String> searchName) {
        long startTime = new Date().getTime();
        log.info("Get custom thumbnail started");
        long temp = new Date().getTime();
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        log.info("Pagination validation " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        User user = userComponents.getUser();
        log.info("Get user " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        List<ThumbnailResponseView> thumbnailResponseViews = new ArrayList<>();
        ThumbnailListResponseView thumbnailListResponseView = new ThumbnailListResponseView();
        Page<ThumbnailImages> thumbnailImages;
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        if (searchName.isPresent() && !searchName.get().isEmpty()) {
            thumbnailImages  =  thumbnailRepository.findByUserUserIdAndTypeAndImagesFileNameIgnoreCaseContaining(user.getUserId(),KeyConstants.KEY_CUSTOM, searchName, pageRequest);
        }else{
            thumbnailImages = thumbnailRepository.findByUserUserIdAndType(user.getUserId(), KeyConstants.KEY_CUSTOM, pageRequest);
        }
        log.info("Get all thumbnails " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        for (ThumbnailImages thumbnailImage : thumbnailImages) {
            ThumbnailResponseView thumbnailResponseView = new ThumbnailResponseView();
            thumbnailResponseView.setThumbnailId(thumbnailImage.getImages().getImageId());
            thumbnailResponseView.setImageUrl(thumbnailImage.getImages().getImagePath());
            thumbnailResponseView.setFileName(thumbnailImage.getImages().getFileName());
            thumbnailResponseView.setType(thumbnailImage.getType());
            thumbnailResponseViews.add(thumbnailResponseView);
        }
        log.info("Response construction " + (new Date().getTime() - temp));
        thumbnailListResponseView.setTotalCount(thumbnailImages.getTotalElements());
        thumbnailListResponseView.setThumbnailsList(thumbnailResponseViews);
        log.info("Get custom thumbnail completed " + (new Date().getTime() - startTime));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_THUMBNAIL_LIST_RETRIEVED, thumbnailListResponseView);
    }

    public List<ThumbnailImages> getThumbnailResponsePagination(List<ThumbnailImages> thumbnailImages, int pageNo, int pageSize) throws ApplicationException {

        int fromIndex = (pageNo - 1) * pageSize;
        if (thumbnailImages == null || thumbnailImages.size() < fromIndex) {
            return Collections.emptyList();
        }

        return thumbnailImages.subList(fromIndex, Math.min(fromIndex + pageSize, thumbnailImages.size()));
    }

    /**
     * Get all system thumbnails
     * @param pageNo
     * @param pageSize
     * @param thumbnailMainTagId
     * @param thumbnailSubTagId
     * @param searchName
     * @return
     */
    public ResponseModel getSystemThumbnails(int pageNo, int pageSize, long thumbnailMainTagId, long thumbnailSubTagId, Optional<String> searchName, List<ThumbnailFilterView> thumbnailFilters) {
        log.info("Get System Thumbnails starts");
        long start = new Date().getTime();
        long profilingStart;
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        User user = userComponents.getUser();
        List<ThumbnailImages> thumbnailImages = new ArrayList<>();
        List<ThumbnailResponseView> thumbnailResponseViews = new ArrayList<>();
        ThumbnailListResponseView thumbnailListResponseView = new ThumbnailListResponseView();
        Set<ThumbnailImages> thumbnailImagesSet = new HashSet<>();

        PageRequest pageRequest = PageRequest.of(pageNo-1,pageSize);
        long totalCount;
        Page<ThumbnailImages> thumbnailImagesPage = null;
        profilingStart = new Date().getTime();
        Specification<ThumbnailImages> finalSpec = ThumbnailSpecifications.getThumbnailImagesByType(KeyConstants.KEY_SYSTEM);

        if(thumbnailFilters.size() > 0){
            List<Long> mainTagIds = thumbnailFilters.stream().filter(thumbnailFilterView -> thumbnailFilterView.isMainTag()).map(thumbnailFilterView -> thumbnailFilterView.getThumbnailTagId()).collect(Collectors.toList());
            List<Long> subTagIds = thumbnailFilters.stream().filter(thumbnailFilterView -> !thumbnailFilterView.isMainTag()).map(thumbnailFilterView -> thumbnailFilterView.getThumbnailTagId()).collect(Collectors.toList());
            finalSpec = ThumbnailSpecifications.getThumbnailImagesByTagIdsAndCriteria(mainTagIds,subTagIds);

        }else if (searchName.isPresent() && !searchName.get().isEmpty()) {

            Specification<ThumbnailImages> thumbnailMainTagSpec = ThumbnailSpecifications.getThumbnailImagesByMainTagSearch(searchName.get());
            Specification<ThumbnailImages> thumbnailSubTagSpec = ThumbnailSpecifications.getThumbnailImagesBySubTagSearch(searchName.get());
            Specification<ThumbnailImages> searchSpec = thumbnailMainTagSpec.or(thumbnailSubTagSpec);
            finalSpec = finalSpec.and(searchSpec);

        }else{
            if (thumbnailSubTagId != 0) {
                Specification<ThumbnailImages> thumbnailImagesSpecification = ThumbnailSpecifications.getThumbnailImagesBySubTag(thumbnailSubTagId);
                finalSpec = finalSpec.and(thumbnailImagesSpecification);
            } else if (thumbnailMainTagId != 0) {
                Specification<ThumbnailImages> thumbnailImagesSpecification = ThumbnailSpecifications.getThumbnailImagesByMainTag(thumbnailMainTagId);
                finalSpec = finalSpec.and(thumbnailImagesSpecification);
            }
        }

        thumbnailImagesPage = thumbnailRepository.findAll(finalSpec,pageRequest);
        log.info("Query : Time taken in millis : "+(new Date().getTime()-profilingStart));
        totalCount = thumbnailImagesPage.getTotalElements();

        profilingStart = new Date().getTime();
        for (ThumbnailImages thumbnailImage : thumbnailImagesPage.getContent()) {
            ThumbnailResponseView thumbnailResponseView = new ThumbnailResponseView();
            thumbnailResponseView.setThumbnailId(thumbnailImage.getImages().getImageId());
            thumbnailResponseView.setImageUrl(thumbnailImage.getImages().getImagePath());
            thumbnailResponseView.setFileName(thumbnailImage.getImages().getFileName());
            thumbnailResponseView.setType(thumbnailImage.getType());

            thumbnailResponseViews.add(thumbnailResponseView);
        }
        log.info("Response construction : Time taken in millis : "+(new Date().getTime()-profilingStart));
        thumbnailListResponseView.setTotalCount(totalCount);
        thumbnailListResponseView.setThumbnailsList(thumbnailResponseViews);
        log.info("Get All System thumbnails : Total Time taken in millis : "+(new Date().getTime()-start));
        log.info("Get All System Thumbnails ends");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_THUMBNAIL_LIST_RETRIEVED, thumbnailListResponseView);
    }

    /**
     * Get all system thumbnail tags
     * @return
     */
    public ResponseModel getSystemTags() {
        long start = new Date().getTime();
        log.info("Get System Thumbnail tags starts");
        long temp = new Date().getTime();
        long profilingEnd;
        ThumbnailTagView thumbnailTagView = new ThumbnailTagView();
        List<ThumbnailMainTags> thumbnailMainTags = thumbnailMainTagsRepository.findAll();
        if (thumbnailMainTags == null) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        log.info("Get All Main Tags " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        thumbnailTagView.setTotalStockImageCount(thumbnailRepository.countByType(KeyConstants.KEY_SYSTEM));
        log.info("Get Stock image count " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        List<ThumbnailSubTags> thumbnailSubTags = thumbnailSubTagsRepository.findAll();
        log.info("Get All sub tags " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        List<ThumbnailTagResponseView> thumbnailTagResponseViews = new ArrayList<>();
        thumbnailTagView.setThumbnailMainTagCount(thumbnailMainTags.size());
        List<ThumbnailCount> mainTagThumbnailCounts = thumbnailCountRepository.findByIsMainTag(true);
        log.info("Get All Main Tags Counts " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        Map<Long, List<ThumbnailSubTags>> subTagMap = new HashMap<>();
        for(ThumbnailSubTags subTag : thumbnailSubTags){
            if(subTagMap.get(subTag.getThumbnailMainTags().getThumbnailMainTagId()) == null){
                subTagMap.put(subTag.getThumbnailMainTags().getThumbnailMainTagId(), new ArrayList<>());
            }
            subTagMap.get(subTag.getThumbnailMainTags().getThumbnailMainTagId()).add(subTag);
        }
        log.info("Construct sub tag map " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        for(ThumbnailCount mainThumbnailCount : mainTagThumbnailCounts){
            ThumbnailMainTags mainTag = null;
            for(ThumbnailMainTags thumbnailMainTag : thumbnailMainTags){
                if(thumbnailMainTag.getThumbnailMainTagId() == mainThumbnailCount.getThumbnailTagId()){
                    mainTag = thumbnailMainTag;
                    break;
                }
            }
            if(mainTag != null){
                ThumbnailTagResponseView thumbnailTagResponseView = new ThumbnailTagResponseView();
                thumbnailTagResponseView.setThumbnailMainTagId(mainTag.getThumbnailMainTagId());
                thumbnailTagResponseView.setThumbnailMainTag(mainTag.getThumbnailMainTag());
                thumbnailTagResponseView.setMultipleSubTagsAllowed(mainTag.isMultipleTagsAllowed());
                thumbnailTagResponseView.setStockImageCountOnMainTag(mainThumbnailCount.getImagesCount());
                List<ThumbnailSubTags> thumbnailSubTagsList = subTagMap.get(mainTag.getThumbnailMainTagId());
                List<Long> subTagIds = thumbnailSubTagsList.stream().map(subTag -> subTag.getThumbnailSubTagId()).collect(Collectors.toList());
                List<ThumbnailCount> subTagThumbnailCounts = thumbnailCountRepository.findByIsMainTagAndThumbnailTagIdIn(false, subTagIds);
                List<ThumbnailSubTagResponseView> thumbnailSubTagResponseViews = new ArrayList<>();
                for(ThumbnailCount subTagCount : subTagThumbnailCounts){
                    ThumbnailSubTags subTag = null;
                    for(ThumbnailSubTags thumbnailSubTag : thumbnailSubTagsList){
                        if(thumbnailSubTag.getThumbnailSubTagId() == subTagCount.getThumbnailTagId()){
                            subTag = thumbnailSubTag;
                            break;
                        }
                    }
                    if(subTag != null){
                        ThumbnailSubTagResponseView thumbnailSubTagResponseView = new ThumbnailSubTagResponseView();
                        thumbnailSubTagResponseView.setThumbnailSubTagId(subTag.getThumbnailSubTagId());
                        thumbnailSubTagResponseView.setThumbnailSubTag(subTag.getThumbnailSubTag());
                        thumbnailSubTagResponseView.setStockImageCountOnSubTag(subTagCount.getImagesCount());
                        thumbnailSubTagResponseViews.add(thumbnailSubTagResponseView);
                    }
                }
                thumbnailTagResponseView.setThumbnailSubTagsCount(thumbnailSubTagResponseViews.size());
                thumbnailTagResponseView.setThumbnailSubTags(thumbnailSubTagResponseViews);
                thumbnailTagResponseViews.add(thumbnailTagResponseView);
            }
        }
        log.info("Construct response " + (new Date().getTime() - temp));
        thumbnailTagView.setThumbnailTagsList(thumbnailTagResponseViews);
        log.info("Get System thumbnail tags ends " + (new Date().getTime() - start));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, thumbnailTagView);
    }

    public ResponseModel getThumbnail(long thumbnailId){

        Images images = imageRepository.findByImageId(thumbnailId);
        if(images == null){
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INCORRECT_THUMBNAIL_ID, MessageConstants.ERROR);

        }

        ThumbnailImages thumbnailImages =thumbnailRepository.findByImagesImageId(thumbnailId);


        ThumbnailUploadResponseView thumbnailUploadResponseView = new ThumbnailUploadResponseView();
        thumbnailUploadResponseView.setThumbnailId(thumbnailImages.getThumbnailId());
        thumbnailUploadResponseView.setImagePath(thumbnailImages.getImages().getImagePath());
        thumbnailUploadResponseView.setFileName(thumbnailImages.getImages().getFileName());
        thumbnailUploadResponseView.setType(thumbnailImages.getType());
        List<ThumbnailTagResponseView>  thumbnailTagResponseViews = new ArrayList<>();


        if(thumbnailImages.getType().equals(KeyConstants.KEY_SYSTEM)){
            List<ThumbnailMainTags> thumbnailMainTags  = thumbnailImages.getThumbnailMainTags();
            List<ThumbnailSubTags> thumbnailSubTags = thumbnailImages.getThumbnailSubTags();
            for(ThumbnailMainTags thumbnailMainTag : thumbnailMainTags){
                ThumbnailTagResponseView thumbnailTagResponseView = new ThumbnailTagResponseView();
                thumbnailTagResponseView.setThumbnailMainTagId(thumbnailMainTag.getThumbnailMainTagId());
                thumbnailTagResponseView.setThumbnailMainTag(thumbnailMainTag.getThumbnailMainTag());
                thumbnailTagResponseView.setMultipleSubTagsAllowed(thumbnailMainTag.isMultipleTagsAllowed());
                List<ThumbnailSubTagResponseView> thumbnailSubTagResponseViews =  new ArrayList<>();


                for(ThumbnailSubTags thumbnailSubTag : thumbnailSubTags){
                    if(thumbnailSubTag.getThumbnailMainTags().getThumbnailMainTagId() == thumbnailMainTag.getThumbnailMainTagId()){
                        ThumbnailSubTagResponseView thumbnailSubTagResponseView = new ThumbnailSubTagResponseView();
                        thumbnailSubTagResponseView.setThumbnailSubTagId(thumbnailSubTag.getThumbnailSubTagId());
                        thumbnailSubTagResponseView.setThumbnailSubTag(thumbnailSubTag.getThumbnailSubTag());
                        thumbnailSubTagResponseViews.add(thumbnailSubTagResponseView);
                    }

                }
                thumbnailTagResponseView.setThumbnailSubTags(thumbnailSubTagResponseViews);
                thumbnailTagResponseViews.add(thumbnailTagResponseView);

            }
        }

        thumbnailUploadResponseView.setThumbnailTags(thumbnailTagResponseViews);

        return  new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, thumbnailUploadResponseView);

    }

    /**
     * Delete thumbnail from library
     * @param thumbnailId
     * @return
     */
    public ResponseModel deleteThumbnailFromLibrary(long thumbnailId) {
        User user = userComponents.getUser();
        return deleteThumbnailFromLibrary(thumbnailId, user);
    }

    public ResponseModel deleteThumbnailFromLibrary(long thumbnailId, User user){

        Images images  = imageRepository.findByImageId(thumbnailId);
        if(images == null){
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INCORRECT_THUMBNAIL_ID, MessageConstants.ERROR);

        }

        ThumbnailImages thumbnailImages =thumbnailRepository.findByImagesImageIdAndTypeAndUserUserId(thumbnailId, KeyConstants.KEY_CUSTOM,user.getUserId());
        if(thumbnailImages == null){
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_THUMBNAIL_NOT_BELONG_TO_USER, MessageConstants.ERROR);
        }
        List<Programs> programs = programRepository.findByImageImageId(thumbnailId);
        List<Workouts> workouts = workoutRepository.findByImageImageId(thumbnailId);
        if(programs.size()  == 0 && workouts.size() == 0){
            thumbnailRepository.delete(thumbnailImages);
            imageRepository.delete(images);
        }else{
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_THUMBNAIL_CANT_DELETED, MessageConstants.ERROR);

        }

        return  new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_THUMBNAIL_DELETED, null);

    }

    /**
     * To get filtered image list
     * @param thumbnailImagesList
     * @param filterSize
     * @return
     */
    public List<ThumbnailImages> getFilteredImages(List<ThumbnailImages> thumbnailImagesList, int filterSize){
        List<ThumbnailImages> thumbnailImages = new ArrayList<>();

            Map<Long, Integer> thumbnailMap = new HashMap<>();
            for(ThumbnailImages thumbnailImage : thumbnailImagesList){
                if(thumbnailMap.containsKey(thumbnailImage.getThumbnailId())){
                    thumbnailMap.put(thumbnailImage.getThumbnailId(),thumbnailMap.get(thumbnailImage.getThumbnailId())+1);
                }else{
                    thumbnailMap.put(thumbnailImage.getThumbnailId(), 1);
                }
            }

            for (Map.Entry<Long,Integer> entry : thumbnailMap.entrySet()){
                if(entry.getValue() == filterSize){
                    thumbnailImages.add(thumbnailRepository.findByThumbnailId(entry.getKey()));
                }
            }

        return  thumbnailImages;
    }

    /**
     * bulk upload images from csv and drive link
     * @param csvMultipartFile
     * @param driveLink
     */
    public void bulkUpload(MultipartFile csvMultipartFile, String driveLink) {
        log.info("Bulk upload starts.");
        long apiStartTimeMillis = new Date().getTime();

        if (csvMultipartFile == null || csvMultipartFile.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BULK_UPLOAD_CSV_NULL, MessageConstants.ERROR);
        }

        User user = userComponents.getUser();
        log.info("Get user and validate multipart file : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        BulkUpload bulkUpload = new BulkUpload();
        bulkUpload.setFileName(csvMultipartFile.getOriginalFilename());
        bulkUpload.setUploadStatus(KeyConstants.KEY_PROCESSING);
        bulkUpload = bulkUploadRepository.save(bulkUpload);
        log.info("Query to save bulk upload : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        InputStream inputStream = null;
        try {
            inputStream = csvMultipartFile.getInputStream();
        } catch (Exception e) {
            log.error("Exception occurred in processCSVFile() : " + e.getMessage());
            e.printStackTrace();
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BULK_UPLOAD_CSV_INVALID, MessageConstants.ERROR);
        }
        log.info("get input stream from multipart file : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        bulkUploadService.initiateBulkUpload(user, bulkUpload, inputStream, driveLink);
        log.info("Bulk upload initiated : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Bulk upload ends.");

    }

    /**
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String, Object> bulkUploadList(int pageNo, int pageSize, String sortOrder, String sortBy) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
        if (!(sortBy.equalsIgnoreCase(SearchConstants.FILE_NAME) || sortBy.equalsIgnoreCase(SearchConstants.UPLOADED_TIME) ||
                sortBy.equalsIgnoreCase(SearchConstants.TOTAL_IMAGES) || sortBy.equalsIgnoreCase(SearchConstants.TOTAL_SUCCESS) || sortBy.equalsIgnoreCase(SearchConstants.TOTAL_FAILURE) || sortBy.equalsIgnoreCase(SearchConstants.TOTAL_UPLOAD_STATUS))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }

        Sort sort = getBulkUploadListSortCriteria(sortBy);
        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            sort = sort.descending();
        }

        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<BulkUpload> bulkUploadPage = bulkUploadRepository.findAll(pageRequest);
        if (bulkUploadPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        List<BulkUploadView> bulkUploadList = new ArrayList<>();
        for (BulkUpload bulkUpload : bulkUploadPage) {
            BulkUploadView bulkUploadView = new BulkUploadView();
            bulkUploadView.setBulkUploadId(bulkUpload.getBulkUploadId());
            bulkUploadView.setFileName(bulkUpload.getFileName());
            bulkUploadView.setUploadedDate(bulkUpload.getUploadedTime());
            bulkUploadView.setUploadedDateFormatted(fitwiseUtils.formatDateWithTime(bulkUpload.getUploadedTime()));
            bulkUploadView.setTotalImages(bulkUpload.getTotalImages());
            bulkUploadView.setSuccess(bulkUpload.getSuccess());
            bulkUploadView.setFailure(bulkUpload.getFailure());
            bulkUploadView.setUploadStatus(bulkUpload.getUploadStatus());

            bulkUploadList.add(bulkUploadView);
        }

        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put(KeyConstants.KEY_BULK_UPLOAD, bulkUploadList);
        responseMap.put(KeyConstants.KEY_TOTAL_COUNT, bulkUploadPage.getTotalElements());

        return responseMap;
    }

    /**
     * Sort criteria
     * @param sortBy
     * @return
     */
    private Sort getBulkUploadListSortCriteria(String sortBy) {
        Sort sort = null;
        if (sortBy.equalsIgnoreCase(SearchConstants.FILE_NAME)) {
            sort = Sort.by("fileName");
        } else if (sortBy.equalsIgnoreCase(SearchConstants.UPLOADED_TIME)) {
            sort = Sort.by("uploadedTime");
        } else if (sortBy.equalsIgnoreCase(SearchConstants.TOTAL_IMAGES)) {
            sort = Sort.by("totalImages");
        } else if (sortBy.equalsIgnoreCase(SearchConstants.TOTAL_SUCCESS)) {
            sort = Sort.by("success");
        } else if (sortBy.equalsIgnoreCase(SearchConstants.TOTAL_FAILURE)) {
            sort = Sort.by("failure");
        } else if (sortBy.equalsIgnoreCase(SearchConstants.TOTAL_UPLOAD_STATUS)) {
            sort = Sort.by("uploadStatus");
        }
        return sort;
    }

    /**
     * @param bulkUploadId
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String, Object> bulkUploadDetails(Long bulkUploadId, int pageNo, int pageSize, String sortOrder, String sortBy) {

        if (bulkUploadId == null || bulkUploadId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BULK_UPLOAD_ID_NULL, MessageConstants.ERROR);
        }

        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
        if (!(sortBy.equalsIgnoreCase(SearchConstants.IMAGE_TITLE) || sortBy.equalsIgnoreCase(SearchConstants.STATUS) ||
                sortBy.equalsIgnoreCase(SearchConstants.MESSAGE))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }

        Optional<BulkUpload> bulkUploadOptional = bulkUploadRepository.findById(bulkUploadId);
        if (!bulkUploadOptional.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BULK_UPLOAD_NOT_FOUND, MessageConstants.ERROR);
        }

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        Sort sort = getBulkUploadDetailsSortCriteria(sortBy);
        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            sort = sort.descending();
        }
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);

        Page<BulkUploadFailure> bulkUploadFailurePage = bulkUploadFailureRepository.findByBulkUpload(bulkUploadOptional.get(), pageRequest);
        if (bulkUploadFailurePage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        List<BulkUploadFailureView> bulkUploadFailureViewList = new ArrayList<>();
        for (BulkUploadFailure bulkUploadFailure : bulkUploadFailurePage) {
            BulkUploadFailureView bulkUploadFailureView = new BulkUploadFailureView();
            bulkUploadFailureView.setId(bulkUploadFailure.getId());
            bulkUploadFailureView.setImageTitle(bulkUploadFailure.getImageTitle());
            bulkUploadFailureView.setStatus(bulkUploadFailure.getStatus());
            bulkUploadFailureView.setMessage(bulkUploadFailure.getMessage());

            bulkUploadFailureViewList.add(bulkUploadFailureView);
        }

        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put(KeyConstants.KEY_BULK_UPLOAD_FAILURE, bulkUploadFailureViewList);
        responseMap.put(KeyConstants.KEY_TOTAL_COUNT, bulkUploadFailurePage.getTotalElements());
        responseMap.put(KeyConstants.KEY_BULK_UPLOAD_FILE_NAME, bulkUploadOptional.get().getFileName());

        return responseMap;
    }

    /**
     * Sort criteria
     * @param sortBy
     * @return
     */
    private Sort getBulkUploadDetailsSortCriteria(String sortBy) {
        Sort sort = null;
        if (sortBy.equalsIgnoreCase(SearchConstants.IMAGE_TITLE)) {
            sort = Sort.by("imageTitle");
        } else if (sortBy.equalsIgnoreCase(SearchConstants.STATUS)) {
            sort = Sort.by("status");
        } else if (sortBy.equalsIgnoreCase(SearchConstants.MESSAGE)) {
            sort = Sort.by("message");
        }
        return sort;
    }

    /**
     * @param bulkUploadId
     * @return
     */
    public ByteArrayInputStream bulkUploadDetailsCsv(Long bulkUploadId) {
        if (bulkUploadId == null || bulkUploadId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BULK_UPLOAD_ID_NULL, MessageConstants.ERROR);
        }

        Optional<BulkUpload> bulkUploadOptional = bulkUploadRepository.findById(bulkUploadId);
        if (!bulkUploadOptional.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BULK_UPLOAD_NOT_FOUND, MessageConstants.ERROR);
        }

        List<BulkUploadFailureCsv> bulkUploadFailureCsvList = bulkUploadFailureCsvRepository.findByBulkUpload(bulkUploadOptional.get());

        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);

        ByteArrayInputStream byteArrayInputStream = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format);) {
            csvPrinter.printRecord(CSV_HEADERS);
            for (BulkUploadFailureCsv bulkUploadFailureCsv : bulkUploadFailureCsvList) {
                List<String> data = Arrays.asList(
                        bulkUploadFailureCsv.getFileName(),
                        bulkUploadFailureCsv.getGender(),
                        bulkUploadFailureCsv.getLocation(),
                        bulkUploadFailureCsv.getFitnessActivity(),
                        bulkUploadFailureCsv.getPeople(),
                        bulkUploadFailureCsv.getEquipment(),
                        bulkUploadFailureCsv.getExerciseMovement(),
                        bulkUploadFailureCsv.getMuscleGroups()
                );

                csvPrinter.printRecord(data);
            }

            csvPrinter.flush();
            byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_BULK_UPLOAD_FAILURE_CSV_GENERATION_FAILED, MessageConstants.ERROR);
        }

        return byteArrayInputStream;
    }

    /**
     * Sample csv download API
     * @return
     */
    public ByteArrayInputStream bulkUploadSampleCsv() {

        String csvUrl = generalProperties.getBulkUploadSampleCsvUrl();

        ByteArrayInputStream byteArrayInputStream = null;
        HttpURLConnection conn = null;
        InputStream stream = null;
        try {
            URL url = new URL(csvUrl);
            conn = (HttpURLConnection) url.openConnection();
            stream = conn.getInputStream();
            final byte[] bytes = IOUtils.toByteArray(stream);
            byteArrayInputStream = new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_BULK_UPLOAD_SAMPLE_CSV_FAILED, MessageConstants.ERROR);
        } finally {
            if (null != conn) {
                conn.disconnect();
            }
            if (null != stream) {
                IOUtils.closeQuietly(stream);
            }
        }
        return byteArrayInputStream;
    }
}
