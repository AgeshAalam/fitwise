package com.fitwise.repository.thumbnail;

import com.fitwise.entity.thumbnail.BulkUpload;
import com.fitwise.entity.thumbnail.BulkUploadFailureCsv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
 * Created by Vignesh G on 11/08/20
 */
public interface BulkUploadFailureCsvRepository extends JpaRepository<BulkUploadFailureCsv, Long> {

    List<BulkUploadFailureCsv> findByBulkUpload(BulkUpload bulkUpload);

}
