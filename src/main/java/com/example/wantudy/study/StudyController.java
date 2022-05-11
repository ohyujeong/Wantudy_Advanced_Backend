package com.example.wantudy.study;

import com.example.wantudy.study.domain.Study;
import com.example.wantudy.study.dto.EntityResponseDto;
import com.example.wantudy.study.dto.StudyDetailResponseDto;
import com.example.wantudy.study.dto.StudyCreateDto;
import com.example.wantudy.study.dto.StudyFileUploadDto;
import com.example.wantudy.study.service.AwsS3Service;
import lombok.RequiredArgsConstructor;;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final AwsS3Service s3Service;

    @GetMapping("/{studyId}")
    public EntityResponseDto getOneStudy(@PathVariable("studyId") long studyId) {

        Study study = studyService.findByStudyId(studyId);
        StudyDetailResponseDto studyDetailResponseDto = studyService.getOneStudy(study);

        return new EntityResponseDto(200, "스터디 상세 페이지 조회", studyDetailResponseDto);

    }

    @PostMapping("")
    public EntityResponseDto createStudy(
            @RequestPart(value = "studyCreateDto") StudyCreateDto studyCreateDto,
            @RequestPart(value = "file", required = false) List<MultipartFile> multipartFile) throws Exception {

        Study study = new Study(studyCreateDto.getStudyName(), studyCreateDto.getDescription(), studyCreateDto.getLevel(),
                studyCreateDto.getFormat(), studyCreateDto.getLocation(), studyCreateDto.getPeriod(), studyCreateDto.getPeopleNum(),
                studyCreateDto.getDeadline()); // DTO에서 리스트 제외한 필드 가져와서 스터디 객체 만듦

        //파일 수 만큼 for문 돌리면서 StudyFile 객체들의 리스트 생성해줌
        for (int i = 0; i < multipartFile.size(); i++) {

            StudyFileUploadDto studyFileUploadDto = s3Service.upload(multipartFile.get(i));
            String fileName = multipartFile.get(i).getOriginalFilename();

            List<String> studyFilePath = List.of(studyFileUploadDto.getFilepath());
            List<String> s3FileName = List.of(studyFileUploadDto.getS3FileName());
            List<String> studyFileName = List.of(fileName);

            studyService.saveStudy(study);

            studyService.saveStudyFiles(studyFilePath, studyFileName, s3FileName, study);
        }

        //DTO에서 리스트 정보 값 가져와서 차례대로 넣어주기
        for (int i = 0; i < studyCreateDto.getCategories().size(); i++) {
            String category = studyCreateDto.getCategories().get(i);
            List<String> categories = List.of(category);
            studyService.saveStudy(study);
            studyService.saveCategory(categories, study);
        }

        for (int i = 0; i < studyCreateDto.getDesiredTime().size(); i++) {
            String desiredTime = studyCreateDto.getDesiredTime().get(i);
            List<String> desiredTimeList = List.of(desiredTime);
            studyService.saveStudy(study);
            studyService.saveDesiredTime(desiredTimeList, study);
        }

        for (int i = 0; i < studyCreateDto.getRequiredInfo().size(); i++) {
            String requiredInfo = studyCreateDto.getRequiredInfo().get(i);
            List<String> requiredInfoList = List.of(requiredInfo);
            studyService.saveStudy(study);
            studyService.saveRequiredInfo(requiredInfoList, study);
        }
        return new EntityResponseDto(201, "스터디 등록", study);
    }

//    @PatchMapping("/{studyId")
//    public EntityResponseDto updateStudy(
//            @RequestParam("studyId") long studyId,
//            @RequestPart(value = "studyCreateDto") StudyCreateDto studyCreateDto,
//            @RequestPart(value = "file", required = false) List<MultipartFile> multipartFile) throws Exception{
//
//        studyService.updateStudy(studyId);
//
//        return new EntityResponseDto(200, "스터디 수정", study);
//    }

    @DeleteMapping("/{studyId}")
    public EntityResponseDto.messageResponse deleteStudy(@PathVariable("studyId") long studyId) {

        s3Service.deleteStudyAndFile(studyId);
        studyService.deleteStudy(studyId);

        return new EntityResponseDto.messageResponse(204, "스터디 삭제 완료");
    }

    @GetMapping("/file/{studyFileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("studyFileId") long studyFileId) throws IOException {

        byte[] bytes = s3Service.getObject(studyFileId).getBytes();
        String fileName = s3Service.getObject(studyFileId).getFileName();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentLength(bytes.length);
        httpHeaders.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }

    @DeleteMapping("/file/{studyFileId}")
    public EntityResponseDto.messageResponse deleteFile(@PathVariable("studyFileId") long studyFileId) {
        s3Service.deleteOnlyFile(studyFileId);
        studyService.deleteStudyFile(studyFileId);
        return new EntityResponseDto.messageResponse(204, "파일 삭제 완료");
    }
}
