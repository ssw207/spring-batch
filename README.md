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
# Spring Batch Chunk 단위 처리
## 용어정리
- chunk
  - 스프링 배치에서 사용하는 트렌젝션 처리의 단위
    - ex) chunk 사이즈가10이면 10개의 item을 처리한뒤 커밋한다. 총 처리할데이터가 100개이고 90개가 처리되면 9개의 커밋이 일어나고 98개째에서 에러가 발생하면 8개가 롤백된다.
- item 
  - 데이터 처리의 단위. 하나의 데이터만 처리한다.
  - itemReader
    - 데이터를 읽는다
  - itemProcessor
    - 데이터를 처리한다
  - itemWriter
    - 데이터를 쓴다(DB insert, Api return) 
- pageing
  - 데이터 조회의 범위를 지정. 
  - chunk는 처리의단위 pageing 조회의 단위이므로 둘은다르다
    - ex) chunk 50 pageing 10이면 10 * 5만큼 데이터를 조회하고 1번 커밋한다.

# 문제해결
## 1.문제해결 - 1일 10분 간격 으로 데이터를 조회해 처리하는 배치를 구현하려면?
1. 필요기능
   - 장애, 지연등으로 배치가 하루 일정시간 꺼지더라도 과거 정보들을 처리할수 있어야함
   - ex) 00시 10분, 20분 배치는 성공 00시 21분에 전산장애로 1시가지 배치가 톨지 않은경우 1시 10분 배치에서 00시 20 ~ 1시 10분 사이의 데이터를 처리해야함
2. 배치 처리이력을 이용해 이번 배치의 jobParameter를 동적으로 구할수 있을까?
   - 1의 문제를 해결하려면 과거 배치의 성공이력을 확인하고 현재시점 이전에 구간들을 확인해 배치를 모두 돌려야함
3. 배치 실행시 step을 병렬 실행시킬수 있을까?
   - 2를 구현하려면 순차실행시 만약 한달만에 다시 실행한다고 했을때 성능이슈가 발생함
   - 배치 실행시 
     1. 과거 마지막 배치 성공 시간과 언제까지 처리할지 기준일을 알아야함
     2. 1을 이용해 jobParameter 구간을 동적으로 만들어야함
     3. 2에서 구한 jobParamater 구간으로 Step을 병렬 실행해야함

## 메타데이터 테이블을 이용했을때 예상되는 문제점
1. DB이관시 스프링 배치 메타데이터 테이블을 똑같이 이관해야함
   - 메타데이터의 jobInstnace, parameter정보를 이용하기 때문에 DB이관으로 데이터가 삭제되면 몆년치가 다 돌아가는 장애가 날수 있음
2. 메타데이터에 의존하기때문에 jobName을 개발자가 실수 혹은 리팩토링으로 바꾸게되면 장애 발생함

## 결론
1. 메타데이터를 이용시 문제가 많음
   - jobName과 DB이관시 메타데이터 역시 이관해야함을 명시해야함
   - 장애상황대비해 오늘부터 몆일전까지 체크할것인지 조회 대상을 한정해야함
   - 메타데이터에 인덱스가 없고, 생성이 잦기 때문에 따로 인덱스를 거는것은 비효율적
2. batch_hist 테이블을 직접 만들어 관리하는게 나음
   - 장점
     - 메타데이터에 의존적이지 않으므로 소스의 변경이 장애로 이어지지않음
     - 인덱스 추가가 자유로움
     - 특정 배치데이터만 저장하므로 데이터양이 적음
   - 단점
     - 테이블을 추가해야함
     - 하루에 144건씩 데이터가 추가됨. 1년에 5만건정도 데이터가 쌓임
     - 주기적으로 삭제시 가장 최근 데이터를 삭제하면 장애발생함

# 2.문제해결 - job실행시 10 row를 처리해야할때 1개의 row가 실패하고 9개의 row가 성공하면 어떻게 처리해야하나?
- 트렌젝션의 원자성을 생각하면 모두 롤백되어야함
- 처리건수가 많다면? job 실행시 1억 row를 처리하는데 1건이 실패하면 모두 롤백해야할까?

## 해결법1. chunk 단위로 처리한다.