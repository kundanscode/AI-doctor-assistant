package com.rxanalyzer.repository;

import com.rxanalyzer.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    // Search by composition 1 or composition 2
    @Query("SELECT m FROM Medicine m WHERE " +
            "LOWER(m.shortComposition1) LIKE LOWER(CONCAT('%', :composition, '%')) OR " +
            "LOWER(m.shortComposition2) LIKE LOWER(CONCAT('%', :composition, '%')) " +
            "AND (m.isDiscontinued = FALSE OR m.isDiscontinued IS NULL) " +
            "ORDER BY m.price ASC")
    List<Medicine> findByComposition(@Param("composition") String composition);

    // Search by medicine name
    @Query("SELECT m FROM Medicine m WHERE " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "AND (m.isDiscontinued = FALSE OR m.isDiscontinued IS NULL)")
    List<Medicine> findByName(@Param("name") String name);

    // Count total medicines loaded
    long count();
}