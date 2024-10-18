package pack.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@RestController
@RequestMapping("/tiger")    // GatewayServer > application.yml > predicates
public class Service01Controller {
	
	@Autowired
	RestTemplate rt;
	
	@GetMapping("")
	public String f1() {
		System.out.println("서비스01 - f1()");
		
		String result = rt.getForObject(
				"http://Service02/lion",    // Service02
				String.class
		);
		return "서비스01  >>  서비스02/lion : " + result;
	}
	
	// -------------------------------------------------------------------------

	@GetMapping("/{num}")
	@CircuitBreaker(
		name = "service01_CB",
		fallbackMethod = "service01_FB")
	@TimeLimiter(name = "service01_CB")
	public CompletableFuture<String> f2(@PathVariable Integer num) {
		CompletableFuture<String> result = CompletableFuture.supplyAsync(
			() -> {
				return rt.getForObject(
		        		"http://Service02/lion/" + num, 
		        		String.class
		        );	
			}
		);
		return result;
	}
	
	@SuppressWarnings("unused")    // 해당 메서드가 사용되지 않는 것으로 인식되는 경고를 방지
	private CompletableFuture<String> service01_FB(Integer num, Throwable t) {
		System.out.println("fallback!!");
		return CompletableFuture.supplyAsync(
			() -> {
				return "fallback!!";
			}
		);
	}
	
}
