package com.fitwise.repository.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.Programs;
import com.fitwise.entity.product.FreeAccessProgram;
import com.fitwise.entity.product.FreeProduct;

@Repository
public interface FreeAccessProgramReposity extends JpaRepository<FreeAccessProgram, Long> {
	
	/**
	 * Find by is active and free product free product id in.
	 *
	 * @param active the active
	 * @param freeAccessIdList the free access id list
	 * @return the list
	 */
	List<FreeAccessProgram> findByIsActiveAndFreeProductFreeProductIdIn(final boolean active, final List<Long> freeAccessIdList);

	/**
	 * Find by is active and program and free product type.
	 *
	 * @param active the active
	 * @param program the program
	 * @param freeAceesType the free acees type
	 * @return the free access program
	 */
	FreeAccessProgram findByIsActiveAndProgramAndFreeProductType(final boolean active, final Programs program,
			final String freeAceesType);
	
	/**
	 * Find by program and free product type.
	 *
	 * @param program the program
	 * @param freeAceesType the free acees type
	 * @return the free access program
	 */
	FreeAccessProgram findByProgramAndFreeProductType(final Programs program, final String freeAceesType);
	
    /**
     * Exists by program and free product type and is active.
     *
     * @param program the program
     * @param type the type
     * @param aciveState the acive state
     * @return true, if successful
     */
    boolean existsByProgramAndFreeProductTypeAndIsActive(final Programs program, final String type, final boolean aciveState);
    
    /**
     * Find by free product type and is active.
     *
     * @param freeAccessTypeAll the free access type all
     * @param aciveState the acive state
     * @return the list
     */
    List<FreeAccessProgram> findByFreeProductTypeAndIsActive(final String freeAccessTypeAll, final boolean aciveState);
    
    /**
     * Find by free product and program.
     *
     * @param freeProduct the free product
     * @param program the program
     * @return the free access program
     */
    FreeAccessProgram findByFreeProductAndProgram(final FreeProduct freeProduct, final Programs program);
    
	/**
	 * Find by free product type and is active free product free product id.
	 *
	 * @param freeAccessTypeAll the free access type all
	 * @param aciveState the acive state
	 * @param idList the id list
	 * @return the list
	 */
	List<FreeAccessProgram> findByFreeProductTypeAndIsActiveAndFreeProductFreeProductIdNotIn(final String freeAccessTypeAll,
			final boolean aciveState, final List<Long> idList);




}
