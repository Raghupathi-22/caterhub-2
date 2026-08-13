package com.daily.cetaring.features.booking.repository;

import com.daily.cetaring.features.booking.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    Page<Menu> findByBusinessIdAndIsActiveTrue(Long businessId, Pageable pageable);

    Page<Menu> findByIsActiveTrueAndCuisineTypeContaining(String cuisineType, Pageable pageable);

    List<Menu> findByBusinessIdAndIsActiveTrueOrderByRatingDesc(Long businessId);

    @Query("SELECT m FROM Menu m WHERE m.isActive = true AND " +
           "(m.name LIKE %:search% OR m.cuisineType LIKE %:search%) " +
           "ORDER BY m.rating DESC")
    Page<Menu> searchMenus(@Param("search") String search, Pageable pageable);

    @Query("SELECT m FROM Menu m WHERE m.isActive = true AND " +
           "m.isVegetarian = true ORDER BY m.rating DESC")
    Page<Menu> findVegetarianMenus(Pageable pageable);

    @Query("SELECT m FROM Menu m WHERE m.isActive = true AND " +
           "m.isVegan = true ORDER BY m.rating DESC")
    Page<Menu> findVeganMenus(Pageable pageable);

    Optional<Menu> findByIdAndIsActiveTrue(Long id);

    boolean existsByIdAndBusinessId(Long menuId, Long businessId);
}

