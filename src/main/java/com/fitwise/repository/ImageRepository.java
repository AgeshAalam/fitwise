package com.fitwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.Images;

@Repository
public interface ImageRepository extends JpaRepository<Images, Long>{

    Images findByImageId(final Long imageId);

}
