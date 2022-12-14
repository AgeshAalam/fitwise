package com.fitwise.repository.payments.authnet;

import com.fitwise.entity.payments.authNet.Countries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountriesRepository extends JpaRepository<Countries, Long> {
    Countries findByCountryCode(String countryCode);
}
