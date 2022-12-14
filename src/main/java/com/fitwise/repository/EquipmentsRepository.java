package com.fitwise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.Equipments;

/**
 * The Interface EquipmentsRepository.
 */
public interface EquipmentsRepository extends JpaRepository<Equipments, Long>{
	
	/**
	 * Find by equipment.
	 *
	 * @param equipmentName the equipment name
	 * @return the equipments
	 */
	Equipments findByEquipmentName(String equipmentName);
	
	/**
	 * Find by equipment ignore case containing.
	 *
	 * @param equipment the equipment
	 * @return the list
	 */
	List<Equipments> findByEquipmentNameIgnoreCaseContaining(final String equipment);

	List<Equipments> findByEquipmentIdIn(final List<Long> equipmentIds);

	Equipments findByEquipmentId(final long equipmentId);

	Equipments findByEquipmentIdNotAndEquipmentName(Long id, String name);


}
