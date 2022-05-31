package com.example.wantudy.domain.study.service;

import com.example.wantudy.domain.study.domain.Study;
import com.example.wantudy.domain.study.domain.*;
import com.example.wantudy.domain.study.dto.*;
import com.example.wantudy.domain.study.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor //final에 있는 애로 생성자 만들어줌 (lombok의 기능)
public class StudyService {

    private final StudyRepository studyRepository;
    private final CategoryRepository categoryRepository;
    private final StudyCategoryRepository studyCategoryRepository;

    private final RequiredInfoRepository requiredInfoRepository;
    private final StudyRequiredInfoRepository studyRequiredInfoRepository;

    private final DesiredTimeRepository desiredTimeRepository;
    private final StudyDesiredTimeRepository studyDesiredTimeRepository;

    private final StudyFileRepository studyFileRepository;

    private final CommentRepository commentRepository;


    public Study findByStudyId(long studyId) {
        Optional<Study> study = studyRepository.findById(studyId);
        return study.orElse(null);
    }


    public Page<StudyAllResponseDto> getSearch(String studyName, String location, StudyStatus status, Pageable pageable) {
        Page<Study> studies = studyRepository.findBySearchOption(pageable, studyName, location, status);
        Page<StudyAllResponseDto> pageDto = studies.map(StudyAllResponseDto::from);

        //카테고리 리스트 채워주기
        for (int i = 0; i < studies.getContent().size(); i++) {
            pageDto.getContent().get(i).setCategories(this.getCategory(studies.getContent().get(i)));
        }

        return pageDto;
    }

    public Page<StudyAllResponseDto> getStudies(String studyName, String location, StudyStatus status, String category, Pageable pageable) {

        Page<Study> studies = studyRepository.findBySearchOption(pageable, studyName, location, status);

        //카테고리 파라미터 없으면 찾은거 그대로 보내줌
        if(category == null){

            Page<StudyAllResponseDto> pageDto = studies.map(StudyAllResponseDto::from);

            //카테고리 리스트 채워주기
            for (int i = 0; i < studies.getContent().size(); i++) {
                pageDto.getContent().get(i).setCategories(this.getCategory(studies.getContent().get(i)));
            }
            return pageDto;
        }

        //찾은거에서 카테고리로 한번 더 필터링
        else{

            List<String> categories = Arrays.asList(category.split(","));

            //순회하면서 찾을 전체 스터디
            List<Study> search = studies.getContent();

            List<Study> result = new ArrayList<>();

            for(int i=0; i<search.size(); i++) {

                Optional<Study> study = studyRepository.findById(search.get(i).getStudyId());
                List<String> studyCategories = this.getCategory(study.get());

                List<String> containCategory = studyCategories.stream()
                        .filter(element -> listContains(categories, element))
                        .collect(Collectors.toList());

                //필터로 들어온 카테고리를 다 포함해야 함
                if(!containCategory.isEmpty() && containCategory.size() == categories.size()){
                    result.add(search.get(i));
                }
            }

            final int start = (int)pageable.getOffset();
            final int end = Math.min((start + pageable.getPageSize()), result.size());

            final Page<Study> page = new PageImpl<>(result.subList(start, end), pageable, result.size());
            Page<StudyAllResponseDto> pageDto = page.map(StudyAllResponseDto::from);

            //카테고리 리스트 채워주기
            for (int i = 0; i < pageDto.getContent().size(); i++) {
                pageDto.getContent().get(i).setCategories(this.getCategory(page.getContent().get(i)));
            }
            return pageDto;
        }
    }

    public static <T> boolean listContains(List<T> array, T element) {
        return array.stream()
                .anyMatch(e -> e.equals(element));
    }

    public StudyDetailResponseDto getOneStudy(Study study) {
        StudyDetailResponseDto studyDetailResponseDto = StudyDetailResponseDto.from(study);

        //카테고리, 필수정보, 희망시간, 파일 리스트 매칭
        studyDetailResponseDto.setCategories(this.getCategory(study));
        studyDetailResponseDto.setDesiredTime(this.getDesiredTime(study));
        studyDetailResponseDto.setRequiredInfo(this.getRequiredInfo(study));
        studyDetailResponseDto.setStudyFiles(this.getStudyFiles(study));
        studyDetailResponseDto.setComments(this.getComments(study));

        return studyDetailResponseDto;
    }

    private List<CommentResponseDto> getComments(Study studyComment) {
        Optional<Study> study = studyRepository.findById(studyComment.getStudyId());

        List<CommentResponseDto> comments = commentRepository.findCommentByStudyId(studyComment.getStudyId());
        return comments;
    }


    public List<StudyFileDto> getStudyFiles(Study fileStudy){
        Optional<Study> study = studyRepository.findById(fileStudy.getStudyId());

        List<StudyFileDto> files = new ArrayList<>();
        List<StudyFile> studyFiles = studyFileRepository.findByStudy(study.get());

        for(int i=0;i<studyFiles.size(); i++) {
            StudyFileDto fileDto = new StudyFileDto();
            fileDto.setStudyFileId(studyFiles.get(i).getStudyFileId());
            fileDto.setFileName(studyFiles.get(i).getFileName());
            fileDto.setFilePath(studyFiles.get(i).getFilePath());
            files.add(fileDto);
        }
        return files;
    }

    public List<String> getCategory(Study categoryStudy){
        Optional<Study> study = studyRepository.findById(categoryStudy.getStudyId());
        List<String> categories = new ArrayList<>();
        List<StudyCategory> studyCategories = studyCategoryRepository.findByStudy(study.get());

        for(int i=0;i<studyCategories .size();i++)
            categories.add(studyCategories.get(i).getCategory().getCategoryName());

        return categories;
    }

    public List<String> getRequiredInfo(Study requiredInfoStudy){
        Optional<Study> study = studyRepository.findById(requiredInfoStudy.getStudyId());

        List<String> requiredInfoList = new ArrayList<>();
        List<StudyRequiredInfo> studyRequiredInfoList = studyRequiredInfoRepository.findByStudy(study.get());
        for(int i=0;i<studyRequiredInfoList .size();i++)
            requiredInfoList.add(studyRequiredInfoList.get(i).getRequiredInfo().getRequiredInfoName());
        return requiredInfoList;
    }

    public List<String> getDesiredTime(Study desiredTimeStudy){
        Optional<Study> study = studyRepository.findById(desiredTimeStudy.getStudyId());

        List<String> desiredTimeList = new ArrayList<>();
        List<StudyDesiredTime> studyDesiredTimeList  = studyDesiredTimeRepository.findByStudy(study.get());
        for(int i=0;i<studyDesiredTimeList.size();i++)
            desiredTimeList.add(studyDesiredTimeList.get(i).getDesiredTime().getDesiredTime());
        return desiredTimeList;
    }

    public long saveStudy(Study study){
        Study createStudy = studyRepository.save(study);
        study.setRemainNum(study.getPeopleNum().intValue() - study.getCurrentNum().intValue());
        return createStudy.getStudyId();
    }

    public void deleteCategories(long studyId) {
        Optional<Study> study = studyRepository.findById(studyId);
        List<StudyCategory> studyCategory = studyCategoryRepository.findByStudy(study.get());
        if(studyCategory != null) {
            for(int i =0; i< studyCategory.size(); i++){
                studyCategoryRepository.delete(studyCategory.get(i));
            }
        }
    }

    public void deleteRequiredInfo(long studyId){
        Optional<Study> study = studyRepository.findById(studyId);

        List<StudyRequiredInfo> studyRequiredInfo = studyRequiredInfoRepository.findByStudy(study.get());
        if(studyRequiredInfo != null){
            for(int i =0; i< studyRequiredInfo.size(); i++){
                studyRequiredInfoRepository.delete(studyRequiredInfo.get(i));
            }
        }
    }

    public void deleteDesiredTime(long studyId){
        Optional<Study> study = studyRepository.findById(studyId);

        List<StudyDesiredTime> studyDesiredTime = studyDesiredTimeRepository.findByStudy(study.get());
        if(studyDesiredTime != null){
            for(int i =0; i< studyDesiredTime .size(); i++){
                studyDesiredTimeRepository.delete(studyDesiredTime.get(i));
            }
        }

    }

    public void saveRequiredInfo(List<String> requiredInfoList, Study study) {
        for (int i = 0; i < requiredInfoList.size(); i++){
            Optional<RequiredInfo> existedRequiredInfo= Optional.ofNullable(requiredInfoRepository.findByRequiredInfoName(requiredInfoList.get(i)));
            StudyRequiredInfo studyRequiredInfo = new StudyRequiredInfo();

            if(existedRequiredInfo.isPresent()) {
                studyRequiredInfo.setRequiredInfo(existedRequiredInfo.get());
            }
            else {
                RequiredInfo requiredInfo = new RequiredInfo(requiredInfoList.get(i));
                requiredInfoRepository.save(requiredInfo);
                studyRequiredInfo.setRequiredInfo(requiredInfo);
            }
            studyRequiredInfo.setStudy(study);
            studyRequiredInfoRepository.save(studyRequiredInfo);
        }
    }

    public void saveDesiredTime(List<String> desiredTimeList, Study study) {
        for (int i = 0; i < desiredTimeList.size(); i++){
            Optional<DesiredTime> existedDesiredTime= Optional.ofNullable(desiredTimeRepository.findByDesiredTime(desiredTimeList.get(i)));
            StudyDesiredTime studyDesiredTime= new StudyDesiredTime();

            if(existedDesiredTime.isPresent()) {
                studyDesiredTime.setDesiredTime(existedDesiredTime.get());
            }
            else {
                DesiredTime desiredTime = new DesiredTime(desiredTimeList.get(i));
                desiredTimeRepository.save(desiredTime);
                studyDesiredTime.setDesiredTime(desiredTime);
            }
            studyDesiredTime.setStudy(study);
            studyDesiredTimeRepository.save(studyDesiredTime);
        }
    }

    // 파일 객체 DB에 저장
    public void saveStudyFiles(List<String> studyFilePath, List<String> studyFileName,  List<String> s3FileName, Study study){

        for (int i = 0; i < studyFilePath.size(); i++){
            StudyFile studyFile = new StudyFile();

            studyFile.setStudy(study);
            studyFile.setFilePath(studyFilePath.get(i));
            studyFile.setFileName(studyFileName.get(i));
            studyFile.setS3FileName(s3FileName.get(i));

            study.addStudyFiles(studyFile);
            studyFileRepository.save(studyFile);

        }
    }

    public void updateStudyFiles(List<String> studyFilePath, List<String> studyFileName,  List<String> s3FileName, long studyId){

        Optional<Study> study = studyRepository.findById(studyId);
        for (int i = 0; i < studyFilePath.size(); i++){
            StudyFile studyFile = new StudyFile();

            studyFile.setStudy(study.get());
            studyFile.setFilePath(studyFilePath.get(i));
            studyFile.setFileName(studyFileName.get(i));
            studyFile.setS3FileName(s3FileName.get(i));

            study.get().addStudyFiles(studyFile);

            studyFileRepository.save(studyFile);
        }
    }

    public String downloadFile(long studyFileId) {
        Optional<StudyFile> studyFile = studyFileRepository.findById(studyFileId);
        return studyFile.get().getFilePath();
    }

    public void deleteStudy(long studyId) {
        Optional<Study> study= studyRepository.findById(studyId);
        studyRepository.delete(study.get());
    }

    public void deleteStudyFile(long studyFileId) {
        Optional<StudyFile> studyFile = studyFileRepository.findById(studyFileId);
        studyFileRepository.delete(studyFile.get());
    }

    public void updateStudy(Long studyId, StudyUpdateDto studyUpdateDto) {

        Optional<Study> study= studyRepository.findById(studyId);

      if(studyUpdateDto.getStudyName() == null){
          studyUpdateDto.setStudyName(study.get().getStudyName());
      }
      if(studyUpdateDto.getDeadline() == null){
          studyUpdateDto.setDeadline(study.get().getDeadline());
      }
      if(studyUpdateDto.getDescription() == null){
          studyUpdateDto.setDescription(study.get().getDescription());
      }
      if(studyUpdateDto.getFormat() == null){
          studyUpdateDto.setFormat(study.get().getFormat());
      }
      if(studyUpdateDto.getLevel() == null){
          studyUpdateDto.setLevel(study.get().getLevel());
      }
      if(studyUpdateDto.getPeopleNum() == null){
          studyUpdateDto.setPeopleNum(study.get().getPeopleNum());
      }
      if(studyUpdateDto.getLocation() == null){
          studyUpdateDto.setLocation(study.get().getLocation());
      }
      if(studyUpdateDto.getPeriod() == null){
          studyUpdateDto.setPeriod(study.get().getPeriod());
      }
      if(studyUpdateDto.getOnOff() == null){
            studyUpdateDto.setOnOff(study.get().getOnOff());
      }
      if(studyUpdateDto.getPeopleNum() != null){
          study.get().setRemainNum(studyUpdateDto.getPeopleNum().intValue() - study.get().getCurrentNum().intValue());
      }
      study.get().updateStudy(studyUpdateDto);
    }

    public void deleteStudyFileForUpdate(long studyId) {
        Optional<Study> study = studyRepository.findById(studyId);
        List<StudyFile> studyFile = studyFileRepository.findByStudy(study.get());

        for(int i=0; i<studyFile.size(); i++){
            study.get().removeStudyFiles(studyFile.get(i));
            studyFileRepository.delete(studyFile.get(i));
        }
    }
}
