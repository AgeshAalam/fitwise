package com.fitwise.repository.view;

import com.fitwise.entity.view.ViewEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ViewEquipmentRepository extends JpaRepository<ViewEquipment, Long>, JpaSpecificationExecutor<ViewEquipment> {
}
