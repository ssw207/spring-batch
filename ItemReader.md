# ItemReader로 API조회 결과 가져오는법
## 목적
- API의 URL, 파라미터 정보를 jobParameter로 ItemReader에 전달
- ItemReader는 전달받은 파라미터 정보중 조회기간(분)으로 API의 URL파라미터를 동적생성후 API를 조회후 결과를 리턴
- 조회건수가 100건이 넘어가면 처리 실패상태를 리턴하고 Step을
- 종료함