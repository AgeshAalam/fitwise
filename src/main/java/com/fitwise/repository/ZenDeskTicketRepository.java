package com.fitwise.repository;

import com.fitwise.entity.ZenDeskTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZenDeskTicketRepository extends JpaRepository<ZenDeskTicket, Long> {
}
