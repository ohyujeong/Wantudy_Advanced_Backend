package com.example.wantudy.study.domain;

import com.example.wantudy.users.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@JsonIgnoreProperties(value={"categories", "desiredTime","requiredInfo"})
public class Study {

    @Id
    @Column(name = "study_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studyId;

    //연관관계 주인 ( 항상 N 이 연관관계 주인 )
    //이 값을 변경하면 user_id foreign key값이 변경됨
    //study입장에서 User랑 N:1 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") //매핑 키 설정
    private User user;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    @Column(name = "categories")
    private List<StudyCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    @Column(name = "desired_time")
    private List<StudyDesiredTime> desiredTime = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    @Column(name = "required_info")
    private List<StudyRequiredInfo> requiredInfo = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(name = "study_file")
    private List<StudyFile> studyFiles = new ArrayList<>();

    @Column(name = "fixed_study_schedule")
    private String fixedStudySchedule;

    @Column(name = "study_name", length = 50)
    private String studyName;

    @Column(name = "description")
    private String description;

    @Column(name = "level")
    private String level;

    @Column(name = "format")
    private String format;

    @Column(name = "location")
    private String location;

    @Column(name = "people_num")
    private Number peopleNum;

    @Column(name = "current_num")
    private Number currentNum;

    @Column(name = "deadline")
    private Date deadline;

    @Column(name = "period")
    private String period;

    @Enumerated(EnumType.STRING)
    @Column(length = 7, columnDefinition = "varchar(7) default 'UNKNOWN'")
    private StudyStatus studyStatus = StudyStatus.RECRUIT;

    @Column(name="like_num")
    private Number likeNum;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @PrePersist
    public void prePersist() {
        this.fixedStudySchedule = this.format == null ? null : this.fixedStudySchedule;
        this.likeNum = this.likeNum == null ? 0 : this.likeNum;
        this.currentNum= this.currentNum == null ? 1 : this.currentNum;
        this.createAt = LocalDateTime.now();
    }

    @Builder
    public Study(String studyName, String description, String level, String format, String location, String period, Number peopleNum,Date deadline) {
        this.studyName = studyName;
        this.description = description;
        this.level = level;
        this.format = format;
        this.location = location;
        this.period = period;
        this.peopleNum = peopleNum;
        this.deadline = deadline;
    }


    // -- 파일 연관 관계 메서드 -- //
    public void addStudyFile(StudyFile studyFile) {
        this.studyFiles.add(studyFile);
    }

}