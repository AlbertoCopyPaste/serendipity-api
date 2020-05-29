package org.serendipity.restapi.repository;

import org.serendipity.restapi.entity.Individual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface IndividualRepository extends PagingAndSortingRepository<Individual, Long> {

  Page<Individual> findAll(Pageable pageable);

  Page<Individual> findByNameFamilyNameStartsWith(String name, Pageable pageable);

}

// https://docs.spring.io/spring-data/rest/docs/current/reference/html/#paging-and-sorting
