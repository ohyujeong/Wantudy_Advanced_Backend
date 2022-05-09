package com.example.wantudy.study.repository;

import com.example.wantudy.study.domain.Category;
import com.example.wantudy.study.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long> {
    Study findByStudyName(String studyName);
}