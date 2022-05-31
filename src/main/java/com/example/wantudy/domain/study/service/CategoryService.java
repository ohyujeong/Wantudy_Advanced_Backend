package com.example.wantudy.domain.study.service;

import com.example.wantudy.domain.study.domain.Study;
import com.example.wantudy.domain.study.domain.Category;
import com.example.wantudy.domain.study.domain.StudyCategory;
import com.example.wantudy.domain.study.dto.CategoryDto;
import com.example.wantudy.domain.study.repository.CategoryRepository;
import com.example.wantudy.domain.study.repository.StudyCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final StudyCategoryRepository studyCategoryRepository;

    public List<CategoryDto> getV1() {
        final List<Category> all = categoryRepository.findAllByParentIsNull();
        return all.stream().map(CategoryDto::new).collect(Collectors.toList());
    }

    public Category findCategory(String categoryName){
        Category category = categoryRepository.findByCategoryName(categoryName);
        return category;
    }

    public Category findParent(String parentCategory) {
        Category parent = categoryRepository.findByCategoryName(parentCategory);
        return parent;
    }

    public void saveParentCategory(String parentCategory){
        Category category= new Category(parentCategory);
        categoryRepository.save(category);
    }

    public void saveCategory(List<String> categories, Study study, String parentCategory){

        Category parent = categoryRepository.findByCategoryName(parentCategory);
        StudyCategory existedParent = studyCategoryRepository.findByCategoryAndStudy(parent,study);

        //부모카테고리는 단일 선택이니까 한 번만 studycategory에 저장
        if(existedParent == null){
            StudyCategory studyCategoryParent = new StudyCategory();
            studyCategoryParent.setCategory(parent);
            studyCategoryParent.setStudy(study);
            studyCategoryRepository.save(studyCategoryParent);
        }

        for (String s : categories) {
            Optional<Category> existedCategory = Optional.ofNullable(categoryRepository.findByCategoryName(s));
            StudyCategory studyCategory = new StudyCategory();
            if (existedCategory.isPresent()) {
                studyCategory.setCategory(existedCategory.get());
            } else {
                Category category = new Category(s);
                categoryRepository.save(category);

                category.setParent(parent);
                parent.addChildCategory(category);
                studyCategory.setCategory(category);
            }
            studyCategory.setStudy(study);
            studyCategoryRepository.save(studyCategory);
        }
    }
}
