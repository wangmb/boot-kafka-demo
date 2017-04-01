package org.mb.kafka.producer.controller;

import java.util.HashMap;
import java.util.Map;

import org.mb.kafka.producer.service.TestSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/producer")
public class TestProducerController {
	@Autowired
	private TestSenderService testSenderService;

	@RequestMapping("test/{loop_count}/{topic}")
	public Map<String, String> test(@PathVariable(name="loop_count",required=true)Integer loopCount,
			@PathVariable(name="topic",required=true)String topic){
		Map<String, String> resData = new HashMap<String, String>();
		resData.put("code", "200");
		resData.put("msg", "调用成功！");
		
		testSenderService.doTaskSend(loopCount, topic);
		
		return resData;
	}
}
