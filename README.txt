[ Resilience4j ]
	|-> 서비스 간의 통신에서 발생할 수 있는 장애에 대응하는 메커니즘
	|	|	|-> ex) 무응답 / 지연 / 실패
	|	|		|-> 실패 : 요청했는데 응답이 안 옴 => health check으로 파악가능
	|	|		|-> 지연 : 요청했는데 늦게 응답이 옴 => 파악하기 어려움
	|	|
	|	|-> 장애가 발생 시, 해당 서비스에 Circuit을 Open하여 일시적으로 연결 차단
	|	|	|-> 전체 시스템에 영향 최소화
	|	|-> 일정 시간 이후에 다시 시도 or 다른 대체 서비스 호출
	|
	|-> 모듈
		|-> 1) CircuitBreaker
		|	|-> [1] Closed 상태
		|	|		|-> 평상 시 상태, 정상적으로 요청 수행
		|	|-> [2] Open 상태
		|	|		|-> 호출 후, 특정 비율 이상 장애 or 지연 발생 시
		|	|-> [3] Half-Open 상태
		|			|-> Open 상태에서 일정 시간 / 일정 요청 횟수 지난 후, 
		|				Closed과 Open 중 어떤 상태로 전환할지
		|
		|-> 2) fallback
		|	|-> 장애 발생해서 Circuit을 Open했을 때, 대비책으로 동작할 메소드
		|
		|-> 3) retry	
			|-> Circuit을 Open한 상태에서 일정 시간 이후에 실패한 요청을 재시도

1) [ Service01 ]
	|-> dependency
	|	|-> Resilience4j 추가
	|	|	|-> 프젝 우클릭 >> Spring >> Add Starters에서 추가하면 됨
	|	|-> <groupId>io.github.resilience4j</groupId>
    	|	|    <artifactId>resilience4j-circuitbreaker</artifactId>
 	|	|-> <groupId>io.github.resilience4j</groupId>
	|	|    <artifactId>resilience4j-timelimiter</artifactId>
    	|	|-> <groupId>org.springframework.boot</groupId>
  	|	     <artifactId>spring-boot-starter-aop</artifactId>
	|			|-> Resilience4J의 Annotation(@...) 기능들은 내부적으로 AOP를 사용하여 메서드 실행을 가로채고 특정 작업을 자동으로 처리
	|
	|-> index.html
	|	|-> <a href="/tiger/1000">1초 - 성공</a><br/>
	|	     <a href="/tiger/5000">5초 - 실패</a>
	|
	|-> application.yml
	|	|-> timelimiter  --> 시간 관련 설정
	|	|	|-> 3초 이내에 응답이 오지 않으면 실패로 판단
	|	|-> circuitbreaker  -->  서킷브레이커 관련 설정
	|		|-> 실패율이 50% 이상이면 Open상태로 전환
	|		|-> 최근 요청 5개의 실패율을 계산
	|		|-> 시간 기준이 아닌 요청 수(5)를 기준으로 실패율 계산
	|		|-> Open상태일 때, 10초 기다린 후 자동으로 Half-open상태로 전환
	|		|-> Half-Open상태일 때, 3개의 요청의 결과에 따라 Open할지 Closed할지 결정
	|
	|-> Controller
		|-> @CircuitBreaker(
		|	name = "service01_CB",
		|	fallbackMethod = "service01_FB")
		|-> @TimeLimiter(name = "service01_CB")
		|-> CompletableFuture<String> 사용
		|	|-> 비동기 처리해야함
		|		|-> Open조건을 만족했을 때는 
		|			요청을 성공했어도 실패로 표시되어야하는데, 
		|			비동기 처리를 하지 않으면 계속 성공이라고 표시됨
		|-> fallback 함수

2) [Service02 ]
	|-> Controller


*** http://localhost:8081로 접속
	|-> 요청에 대한 응답이 3초(timeout-duration) 이상 걸리면 fallback함

					|   CircuitBreaker 상태 	|   결과		|
	---------------------------	|--------------------------	|-------------------	|
	1)   "1초" 버튼 		|   Closed			|   성공		|
	2)   "5초" 버튼 	  	|   Closed			|   fallback		|
	3)   "1초" 버튼 	   	|   Closed			|   성공		|
	4)   "5초" 버튼 	   	|   Closed			|   fallback		|
	5)   "5초" 버튼 	   	|   Open			|   fallback		| ---> 5개(slidingWindowSize)의 요청이 누적되었고
					|				|			|		50%(failureRateThreshold) 이상의 실패율이기 때문에
					|				|			|		Open상태가 되었음
	---------------------------	|--------------------------	|-------------------	|
	6)   "1초" or "5초" 버튼	|   Open			|   fallback !!!! 	| ---> Open상태이기 때문에 
					|				|			|		10초(waitDurationInOpenState)동안은 뭘 누르든 fallback
	---------------------------	|--------------------------	|-------------------	|
	      10초 후 ...		|   Half-Open		|			| ---> 이후에 누적된 3회(permittedNumberOfCallsInHalfOpenState)의 
														요청의 결과에 따라 Open할지 Closed할지 결정



