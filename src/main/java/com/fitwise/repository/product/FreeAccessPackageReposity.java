package com.fitwise.repository.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.product.FreeAccessPackages;
import com.fitwise.entity.product.FreeProduct;

@Repository
public interface FreeAccessPackageReposity extends JpaRepository< FreeAccessPackages, Long> {
	
	/**
	 * Find by is active and free product free product id in.
	 *
	 * @param active the active
	 * @param freeAccessIdList the free access id list
	 * @return the list
	 */
	List<FreeAccessPackages> findByIsActiveAndFreeProductFreeProductIdIn(final boolean active, final List<Long> freeAccessIdList);
	
	/**
	 * Find by free product and subscription package.
	 *
	 * @param freeProduct the free product
	 * @param sunscriptionPackage the sunscription package
	 * @return the free access packages
	 */
	FreeAccessPackages findByFreeProductAndSubscriptionPackage(final FreeProduct freeProduct, final SubscriptionPackage sunscriptionPackage);


}
