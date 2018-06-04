# GTG
## 1. DB 구축하기
DB를 구축하기 위해서는 먼저 `setConn` 함수에 MySQL 서버의 주소와 MySQL 유저명, 그리고 비밀번호를 파라미터로 제공하여야 한다.
이 때 해당 유저는 DB를 구축하기 위한 적절한 권한을 가지고 있어야 한다.
`setConn` 함수 사용 후 `createDatabase`, `createTables`, `insertCodes` 함수들을 사용하여 학과 및 교양 코드를 DB에 채워 넣는다.
크롤링으로 바로 가져온 학과 코드들을 사용되지 않는 코드들이 포함되어 있으므로 학과 코드 테이블의 내용을 비운다음 첨부된 csv 파일을 통해 DB에 import 시킨다.
```
DELETE FROM mj;
```

Intellij에서 csv 파일을 통해 DB에 Import하는 방법은 다음의 링크를 참조한다.
[Import/Export options](https://www.jetbrains.com/datagrip/features/importexport.html)

이후 `insertCourses` 함수를 이용하여 과목들을 DB에 채워넣는다.

## 2. DB Schema

교양영역 코드 테이블(gn)

이름 | 설명
----|----
cor_cd(PK) | 교양영역 코드
name | 과목명

    
학과 코드 테이블(mj)

이름 | 설명
----|----
univ_cd | 대학 코드
maj_cd(PK) | 학과 코드
name | 학과 명

    
과목 정보 테이블(course)

이름 | 설명
----|----
year(PK) | 년도
semester(PK) | 학기
code(PK) | 학수번호
title | 과목명
classification | 이수구분
credit | 학점
quota | 정원
time | 강의시간
instructor | 교수 
room | 강의실
grade | 비고
syllabus | 강의계획서
maj_cd(FK) | 학과 코드 (전공과목인 경우)
cor_cd(FK) | 교양영역 코드 (교양과목인 경우)
