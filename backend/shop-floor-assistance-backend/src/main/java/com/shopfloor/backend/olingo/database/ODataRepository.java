package com.shopfloor.backend.olingo.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * A generic repository interface for OData entities.
 * Extends the JpaRepository for basic CRUD operations
 * and the JpaSpecificationExecutor for querying the database with Specifications.
 * @NoRepositoryBean annotation is used to exclude this interface from being picked up by Spring Data JPA.
 *
 * @param <T> the type of the entity
 * @Author David Todorov (https://github.com/david-todorov)
 */
@NoRepositoryBean
public interface ODataRepository<T> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
}
