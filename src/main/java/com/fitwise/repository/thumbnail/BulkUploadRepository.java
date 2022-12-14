package com.fitwise.repository.thumbnail;

import com.fitwise.entity.thumbnail.BulkUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 07/08/20
 */
@Repository
public interface BulkUploadRepository extends JpaRepository<BulkUpload, Long> {

}
