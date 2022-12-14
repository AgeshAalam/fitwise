package com.fitwise.repository;

import com.fitwise.entity.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenderRepository extends JpaRepository<Gender,Long> {

    Gender findByGenderId(final long genderId);

    Gender findByGenderType(final String gender);

}
