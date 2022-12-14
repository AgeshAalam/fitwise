package com.fitwise.repository;

import com.fitwise.entity.User;
import com.fitwise.entity.VideoManagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoManagementRepo extends JpaRepository<VideoManagement, Long>{	
	
	VideoManagement findByUrl(String url);

    List<VideoManagement> findByUploadStatusIsNullOrUploadStatusNotIn(final List<String> statusList, Pageable pageable);

    VideoManagement findByVideoManagementId(Long videoManagementId);
    
    Page<VideoManagement>  findByOwner(User user, Pageable pageable);
    
    Page<VideoManagement>  findByOwnerAndTitle(User user, String title, Pageable pageable);

}
