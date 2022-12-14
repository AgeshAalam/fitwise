package com.fitwise.rest.admin;

import com.fitwise.model.EquipmentModel;
import com.fitwise.service.admin.AdminEquipmentService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value = "/v1/admin")
public class AdminEquipmentController {

    @Autowired
    private AdminEquipmentService adminEquipmentService;

    /**
     * Add or update equipment
     *
     * @param equipmentModel
     * @return
     */
    @PostMapping(value = "/equipment")
    public ResponseModel addOrEditEquipment(@RequestBody EquipmentModel equipmentModel) {
        return adminEquipmentService.addOrUpdateEquipment(equipmentModel);
    }

    /**
     * '
     * Delete equipment
     *
     * @param equipmentId
     * @return
     */
    @DeleteMapping(value = "/equipment")
    public ResponseModel deleteEquipment(@RequestParam Long equipmentId) {
        return adminEquipmentService.deleteEquipment(equipmentId);
    }

    /**
     * Get all equipments
     *
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @param sortOrder
     * @return
     */
    @GetMapping(value = "/equipment/all")
    public ResponseModel getAllEquipments(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String sortBy, @RequestParam String sortOrder, Optional<String> search) {
        return adminEquipmentService.getALlEquipments(pageNo, pageSize, sortBy, sortOrder, search);
    }
}