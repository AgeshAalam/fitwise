package com.fitwise.specifications.jpa;

import com.fitwise.entity.Exercises;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Component
@RequiredArgsConstructor
public class EquipmentJpa {

    private final EntityManager entityManager;

    public Long exerciseCountForEquipment(Long equipmentId){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

        Root<Exercises> exercisesRoot = criteriaQuery.from(Exercises.class);

        Predicate eqPred = criteriaBuilder.equal(exercisesRoot.join("equipments").get("equipmentId"),equipmentId);
        criteriaQuery.where(eqPred);
        criteriaQuery.select(criteriaBuilder.count(exercisesRoot));
        return  entityManager.createQuery(criteriaQuery).getSingleResult();

    }
}
