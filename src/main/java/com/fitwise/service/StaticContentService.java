package com.fitwise.service;

import com.fitwise.entity.packaging.PackageSessionCount;
import com.fitwise.entity.social.SocialMedia;
import com.fitwise.repository.packaging.PackageSessionCountRepository;
import com.fitwise.repository.social.SocialMediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaticContentService {

    private final PackageSessionCountRepository packageSessionCountRepository;
    private final SocialMediaRepository socialMediaRepository;

    /**
     * Get all the package session counts
     * @return Package session count list
     */
    public List<PackageSessionCount> getPackageSessionCounts(){
        return  packageSessionCountRepository.findAll();
    }

    /**
     * Get all social media supported in the system
     * @return social media list
     */
    public List<SocialMedia> getSocialMedia(){
        return socialMediaRepository.findAll();
    }

}
