package com.fitwise.service.v2.member;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.product.FreeAccessPackages;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.response.packaging.MemberPackageTileView;
import com.fitwise.service.freeproduct.FreeAccessService;
import com.fitwise.specifications.jpa.SubscriptionPackageJpa;
import com.fitwise.utils.FitwiseUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberPackageV2Service {

	private final FreeAccessService freeAccessService;
	private final PackageProgramMappingRepository packageProgramMappingRepository;
	private final SubscriptionPackageJpa subscriptionPackageJpa;
	private final FitwiseUtils fitwiseUtils;

	/**
	 * Construct free access package list.
	 *
	 * @return the list
	 */
	public List<MemberPackageTileView> constructFreeAccessPackageList() {
		List<MemberPackageTileView> memberPackageTileViewList = new ArrayList<>();
		List<FreeAccessPackages> freeAccessSupscriptionPackageList = freeAccessService
				.getUserSpecificFreeAccessPackage(null);
		Set<Long> packageIdsList = new HashSet<>();
		for (FreeAccessPackages freeSubscriptionPackages : freeAccessSupscriptionPackageList) {
			if(!packageIdsList.contains(freeSubscriptionPackages.getSubscriptionPackage().getSubscriptionPackageId())) {
				packageIdsList.add(freeSubscriptionPackages.getSubscriptionPackage().getSubscriptionPackageId());
				MemberPackageTileView memberPackageTileView = new MemberPackageTileView();
				SubscriptionPackage packageSubscription = freeSubscriptionPackages.getSubscriptionPackage();
				memberPackageTileView.setFreeToAccess(true);
				memberPackageTileView.setSubscriptionPackageId(packageSubscription.getSubscriptionPackageId());
				memberPackageTileView.setTitle(packageSubscription.getTitle());
				memberPackageTileView
						.setDuration(packageSubscription.getPackageDuration().getDuration().intValue() + " days");
				memberPackageTileView.setImageId(packageSubscription.getImage().getImageId());
				memberPackageTileView.setImageUrl(packageSubscription.getImage().getImagePath());
				int programCount = packageProgramMappingRepository.countBySubscriptionPackage(packageSubscription);
				memberPackageTileView.setNoOfPrograms(programCount);
				int sessionCount = subscriptionPackageJpa
						.getBookableSessionCountForPackage(packageSubscription.getSubscriptionPackageId());
				memberPackageTileView.setSessionCount(sessionCount);
				memberPackageTileView.setSubscribedDate(freeSubscriptionPackages.getFreeProduct().getFreeAccessStartDate());
				memberPackageTileView.setSubscribedDateFormatted(
						fitwiseUtils.formatDate(freeSubscriptionPackages.getFreeProduct().getFreeAccessStartDate()));
				memberPackageTileViewList.add(memberPackageTileView);
			}
		}
		return memberPackageTileViewList;

	}

}
