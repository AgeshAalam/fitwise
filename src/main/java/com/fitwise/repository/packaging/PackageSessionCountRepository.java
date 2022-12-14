package com.fitwise.repository.packaging;

import com.fitwise.entity.packaging.PackageSessionCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageSessionCountRepository extends JpaRepository<PackageSessionCount, Long> {
}
