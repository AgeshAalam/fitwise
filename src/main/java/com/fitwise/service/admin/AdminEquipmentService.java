package com.fitwise.service.admin;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Equipments;
import com.fitwise.entity.Exercises;
import com.fitwise.entity.User;
import com.fitwise.entity.view.ViewEquipment;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.EquipmentModel;
import com.fitwise.repository.EquipmentsRepository;
import com.fitwise.repository.ExerciseRepository;
import com.fitwise.repository.view.ViewEquipmentRepository;
import com.fitwise.specifications.view.ViewEquipmentSpecification;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.EquipmentView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.admin.EquipmentListResponseView;
import com.fitwise.view.admin.EquipmentResponseView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminEquipmentService {

    private final EquipmentsRepository equipmentsRepository;
    private final UserComponents userComponents;
    private final FitwiseUtils fitwiseUtils;
    private final ViewEquipmentRepository viewEquipmentRepository;
    private final ExerciseRepository exerciseRepository;

    /**
     * Add or update equipment
     *
     * @param equipmentModel
     * @return
     */
    public ResponseModel addOrUpdateEquipment(EquipmentModel equipmentModel) {
        log.info("Add or update equipment starts.");
        long apiStartTimeMillis = new Date().getTime();
        if (equipmentModel.getEquipment() == null || equipmentModel.getEquipment().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EQUIPMENT_NULL, null);
        }
        Equipments equipment;
        boolean isNew = false;
        if (equipmentModel.getEquipmentId() == 0) {
            equipment = new Equipments();
            Equipments duplicateEquipment = equipmentsRepository.findByEquipmentName(equipmentModel.getEquipment());
            if (duplicateEquipment != null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EQUIPMENT_PRESENT, null);
            }
            isNew = true;
        } else {
            equipment = equipmentsRepository.findByEquipmentId(equipmentModel.getEquipmentId());
            if (equipment == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EQUIPMENT_NOT_FOUND, null);
            }
            Equipments duplicateEquipment = equipmentsRepository.findByEquipmentIdNotAndEquipmentName(equipmentModel.getEquipmentId(), equipmentModel.getEquipment());
            if (duplicateEquipment != null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EQUIPMENT_PRESENT, null);
            }
        }
        log.info("Query to get equipment : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        equipment.setEquipmentName(equipmentModel.getEquipment());
        equipment.setPrimary(false);
        equipmentsRepository.save(equipment);
        EquipmentView equipmentView = new EquipmentView();
        equipmentView.setEquipmentId(equipment.getEquipmentId());
        equipmentView.setEquipment(equipment.getEquipmentName());
        log.info("Query to save equipment and response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        if(isNew){
            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
            log.info("Add or update equipment ends.");
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_EQUIPMENT_ADDED, equipmentView);
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Add or update equipment ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_EQUIPMENT_UPDATED, equipmentView);
    }

    /**
     * Delete equipment
     *
     * @param equipmentId
     * @return
     */
    public ResponseModel deleteEquipment(Long equipmentId) {
        log.info("Delete equipment starts.");
        long apiStartTimeMillis = new Date().getTime();
        Equipments equipments = equipmentsRepository.findByEquipmentId(equipmentId);
        log.info("Query: get equipment from DB : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        if (equipmentId == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EQUIPMENT_NOT_FOUND, null);
        }
        if(equipments.isPrimary()){
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_EQUIPMENT_CANNOT_DELETED_ADMIN, null);
        }
        log.info("Basic validation : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<Exercises> exercises = exerciseRepository.findByEquipmentsEquipmentId(equipmentId);
        log.info("Query: get exercises : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        System.out.println(exercises.size());
        List<Exercises> modified = new ArrayList<>();
        for(Exercises exercise : exercises){
            List<Equipments> equipmentsList = exercise.getEquipments();
            List<Equipments> newEquipmentList = equipmentsList.stream()
                    .filter(equipments1 -> equipments1.getEquipmentId() != equipments.getEquipmentId())
                    .collect(Collectors.toList());
            exercise.setEquipments(newEquipmentList);
            modified.add(exercise);
        }
        log.info("Construct equipment list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        exerciseRepository.saveAll(modified);
        log.info("Query: save exercise : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        equipmentsRepository.delete(equipments);
        log.info("Query: delete equipment : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Delete equipment ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_EQUIPMENT_DELETED, null);
    }

    /**
     * Get all equipment
     *
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @param sortOrder
     * @return
     */
    public ResponseModel getALlEquipments(int pageNo, int pageSize, String sortBy, String sortOrder, Optional<String> search) {
        log.info("Get all equipments starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        List<String> allowedSortByList = Arrays.asList(new String[]{SearchConstants.EQUIPMENT_NAME, SearchConstants.PROGRAM_COUNT, SearchConstants.EXERCISE_COUNT, SearchConstants.CREATED_DATE, SearchConstants.USAGE});
        boolean isSortByAllowed = allowedSortByList.stream().anyMatch(sortBy::equalsIgnoreCase);
        if (!isSortByAllowed) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_SORT_BY, null);
        }
        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
        if (!fitwiseUtils.isAdmin(user)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        log.info("Basic validations : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        Specification<ViewEquipment> sortSpecification = ViewEquipmentSpecification.getInstructorSortSpecification(sortBy, sortOrder);
        if(search.isPresent() && !search.get().isEmpty()){
            sortSpecification = sortSpecification.and(ViewEquipmentSpecification.getSearchByEquipmentNameSpecification(search.get()));
        }
        Page<ViewEquipment> viewEquipments = viewEquipmentRepository.findAll(sortSpecification, pageRequest);
        log.info("Query to get equipments : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (viewEquipments.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        List<EquipmentResponseView> equipmentsResponseViews = new ArrayList<>();
        for (ViewEquipment viewEquipment : viewEquipments) {
            EquipmentResponseView equipmentResponseView = new EquipmentResponseView();
            equipmentResponseView.setEquipmentId(viewEquipment.getEquipmentId());
            equipmentResponseView.setEquipmentName(viewEquipment.getEquipmentName());
            equipmentResponseView.setExerciseCount(viewEquipment.getExerciseCount());
            equipmentResponseView.setPrimary(viewEquipment.isPrimary());
            equipmentResponseView.setProgramCount(viewEquipment.getProgramCount());
            equipmentResponseView.setCreatedDate(viewEquipment.getCreatedDate());
            equipmentResponseView.setCreatedDateFormatted(fitwiseUtils.formatDateWithTime(viewEquipment.getCreatedDate()));
            equipmentResponseView.setUsedInExercises(viewEquipment.isUsed());
            equipmentsResponseViews.add(equipmentResponseView);
        }
        EquipmentListResponseView equipmentListResponseView = new EquipmentListResponseView();
        equipmentListResponseView.setEquipmentList(equipmentsResponseViews);
        equipmentListResponseView.setTotalCount(viewEquipments.getTotalElements());
        log.info("Construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get all equipments ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, equipmentListResponseView);
    }

}