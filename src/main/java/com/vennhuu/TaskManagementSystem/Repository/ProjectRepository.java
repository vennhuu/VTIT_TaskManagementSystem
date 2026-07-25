package com.vennhuu.TaskManagementSystem.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vennhuu.TaskManagementSystem.Entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findDistinctByMembersUserId(Long userId);

    Page<Project> findAll(Specification<Project>spec, Pageable pageable) ;
}
