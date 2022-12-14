package com.fitwise.specifications.jpa.runnable;

import com.fitwise.entity.view.ViewMember;
import com.fitwise.repository.view.ViewMemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

@Slf4j
public class MemberCountJpaRunnable implements Runnable {

    private ViewMemberRepository viewMemberRepository;
    private Specification<ViewMember> specification;
    private Map<String, Object> response;

    public MemberCountJpaRunnable(ViewMemberRepository viewMemberRepository, Specification<ViewMember> specification, Map<String, Object> response) {
        this.viewMemberRepository = viewMemberRepository;
        this.specification = specification;
        this.response = response;
    }

    @Override
    public void run() {
        long temp = System.currentTimeMillis();
        log.info("Member count Collection started");
        Long count = viewMemberRepository.count(specification);
        response.put("count", count);
        log.info("Member count collection completed : " + (System.currentTimeMillis() - temp));
    }
}
