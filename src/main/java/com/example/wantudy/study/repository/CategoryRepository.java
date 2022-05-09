package com.example.wantudy.study.repository;

import com.example.wantudy.study.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>  {
    Category findByCategoryName(String categoryName);
}