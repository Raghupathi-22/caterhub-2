package com.daily.cetaring.features.booking.repository;

import com.daily.cetaring.features.booking.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    Page<MenuItem> findByMenu_IdAndIsActiveTrue(Long menuId, Pageable pageable);

    List<MenuItem> findByMenu_IdAndIsActiveTrueOrderByRatingDesc(Long menuId);

    Optional<MenuItem> findByIdAndIsActiveTrue(Long id);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND " +
           "mi.isActive = true AND mi.isVegetarian = true " +
           "ORDER BY mi.rating DESC")
    List<MenuItem> findVegetarianItems(@Param("menuId") Long menuId);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND " +
           "mi.isActive = true AND mi.isVegan = true " +
           "ORDER BY mi.rating DESC")
    List<MenuItem> findVeganItems(@Param("menuId") Long menuId);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND " +
           "mi.isActive = true AND (" +
           "mi.name LIKE %:search% OR mi.description LIKE %:search%) " +
           "ORDER BY mi.rating DESC")
    Page<MenuItem> searchItems(@Param("menuId") Long menuId,
                               @Param("search") String search,
                               Pageable pageable);

    @Query("SELECT COUNT(mi) FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.isActive = true")
    Long countActiveItemsByMenuId(@Param("menuId") Long menuId);

    boolean existsByIdAndMenu_Id(Long itemId, Long menuId);
}
