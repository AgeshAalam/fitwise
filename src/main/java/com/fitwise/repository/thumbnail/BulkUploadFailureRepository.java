package com.fitwise.repository.thumbnail;

import com.fitwise.entity.thumbnail.BulkUpload;
import com.fitwise.entity.thumbnail.BulkUploadFailure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
 * Created by Vignesh G on 07/08/20
 */
public interface BulkUploadFailureRepository extends JpaRepository<BulkUploadFailure, Long> {

    /**
     * @param bulkUpload
     * @return
     */
    Page<BulkUploadFailure> findByBulkUpload(BulkUpload bulkUpload, Pageable pageable);

    /**
     * @param bulkUpload
     * @return
     */
    List<BulkUploadFailure> findByBulkUpload(BulkUpload bulkUpload);

}
