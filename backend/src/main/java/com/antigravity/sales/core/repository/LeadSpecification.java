package com.antigravity.sales.core.repository;

import com.antigravity.sales.core.model.Lead;
import com.antigravity.sales.core.model.Company;
import com.antigravity.sales.core.model.LeadProfessionalInfo;
import com.antigravity.sales.core.model.JobFunction;
import com.antigravity.sales.core.model.CompanySize;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LeadSpecification {

    public static Specification<Lead> filterBy(String q, String companyName, JobFunction jobFunction, CompanySize size, java.math.BigDecimal minMrr) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Avoid duplicate rows when joining collections
            query.distinct(true);

            if (StringUtils.hasText(q)) {
                String term = "%" + q.toLowerCase() + "%";
                Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), term);
                Predicate emailMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), term);
                predicates.add(criteriaBuilder.or(nameMatch, emailMatch));
            }

            if (StringUtils.hasText(companyName) || size != null || minMrr != null) {
                Join<Lead, Company> companyJoin = root.join("company", JoinType.LEFT);
                if (StringUtils.hasText(companyName)) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(companyJoin.get("companyName")), "%" + companyName.toLowerCase() + "%"));
                }
                if (size != null) {
                    predicates.add(criteriaBuilder.equal(companyJoin.get("companySize"), size));
                }
                if (minMrr != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(companyJoin.get("mrr"), minMrr));
                }
            }

            if (jobFunction != null) {
                Join<Lead, LeadProfessionalInfo> proJoin = root.join("professionalInfo", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(proJoin.get("jobFunction"), jobFunction));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
