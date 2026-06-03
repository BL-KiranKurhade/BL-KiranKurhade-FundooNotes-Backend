package com.bridgelabz.fundoonotes.repository;

import com.bridgelabz.fundoonotes.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    @Query("SELECT DISTINCT n FROM Note n LEFT JOIN n.collaborators c WHERE (n.user.id = :userId OR c.id = :userId) AND n.isTrashed = false AND n.isArchived = false")
    List<Note> findByUserIdAndIsTrashedFalseAndIsArchivedFalse(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT n FROM Note n LEFT JOIN n.collaborators c WHERE (n.user.id = :userId OR c.id = :userId) AND n.isArchived = true AND n.isTrashed = false")
    List<Note> findByUserIdAndIsArchivedTrueAndIsTrashedFalse(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT n FROM Note n LEFT JOIN n.collaborators c WHERE (n.user.id = :userId OR c.id = :userId) AND n.isTrashed = true")
    List<Note> findByUserIdAndIsTrashedTrue(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Note n WHERE n.user.id = :userId AND n.isTrashed = false AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Note> searchNotesByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
}
