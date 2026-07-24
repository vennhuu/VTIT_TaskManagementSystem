package com.vennhuu.TaskManagementSystem.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.vennhuu.TaskManagementSystem.Entity.ProjectMember;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long>, JpaSpecificationExecutor<ProjectMember> {
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    ProjectMember findByProjectIdAndUserId(Long projectId, Long userId);

    List<ProjectMember> findByProjectId(Long projectId);

    Page<ProjectMember> findAllByProjectId(Long projectId, Specification<ProjectMember> spec, Pageable page) ;
    
}
