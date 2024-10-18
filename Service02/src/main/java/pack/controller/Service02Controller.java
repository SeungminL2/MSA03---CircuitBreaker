package pack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lion")    // GatewayServer > application.yml > predicates
public class Service02Controller {
	
	@GetMapping("")
	public String f1() {
		System.out.println("서비스02 - f1()");
		return "라이언";
	}
	
	// --------------------------------------------
	
	@GetMapping("/{num}")
	public String f2(@PathVariable Integer num) {
		System.out.println("서비스02 - f2()");
		System.out.println("   |-> num : " + num);
		
		try {
			Thread.sleep(num);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "요청한 서비스 성공적으로 시작";
	}
}
