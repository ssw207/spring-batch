# MySql docker-compose로 실행
docker-compse 설치 확인
```
docker-compose -v
```
실행
- -f : 파일명지정
- -d : 백그라운드 실행
- 현재 init.db 파일이 최초 실행되지 않는 오류가 있으므로 수동으로 DDL 실행필요
```
cd docker
docker-compose -f docker-compose-mysql.yml up -d
```
종료
```
docker-compose -f docker-compose-mysql.yml down
```
# Spring Batch 메타데이터 테이블 수동생성시
- IDE파일검색 > "schema-" 검색후 DDL 수동 실행필요

# Spring Batch 주요 테이블정보
```sql
-- 스프링 배치는 job실행이력을 메타데이터 테이블에 남긴다
-- BATCH_JOB_INSTANCE 테이블은 job_name + parameter로 고유한 job정보를 저장한다
-- BATCH_JOB_EXECUTION 테이블은 실행이력을 저장한다. job_instance_id 기준으로 성공이력이 없으면 재실행가능하다
-- BATCH_JOB_EXECUTION_PARAMS 테이블은 job 실행시 어떤 parameter로 실행했는지 저장하는 테이블이다.

-- job 실행이력 확인
SELECT *
FROM BATCH_JOB_INSTANCE;
DESC BATCH_JOB_INSTANCE;

-- BATCH_JOB_INSTANCE별 실행횟수 확인
-- JOB_INSTANCE_ID별 성공,실패이력을 확인할수 있다.
-- JOB_INSTANCE_ID별로 성공값은 1개만 가능함
SELECT *
FROM BATCH_JOB_EXECUTION;
DESC BATCH_JOB_INSTANCE;

-- JOB 실행시 어떤파라미터로 실행했는지 확인하는 테이블
-- BATCH_JOB_EXECUTION 테이블과 1:1이다
-- 동일 파라미터로 이미성공한 JOB_INSTANCE를 2회 실행시킬수 없다(에러가 발생함)
SELECT *
FROM BATCH_JOB_EXECUTION_PARAMS;
DESC BATCH_JOB_INSTANCE;

```