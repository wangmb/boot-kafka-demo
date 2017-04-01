package org.mb.kafka.producer.service;

import java.util.Date;
import java.util.concurrent.Future;

import org.mb.kafka.config.producer.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class TestSenderService {
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	private Integer count = 0;
	
	@Autowired
	private Sender sender;
	
	@Async//("threadPoolTaskExecutor")
	public Future<String> doTaskSend(Integer loopCount,String topic){
		long start = System.nanoTime();
		
		for(int i=0;i<loopCount;i++){
			count++;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sender.send(topic, "{‘business_type’：‘023’，‘send_date’：‘"+new Date().toString()+"’，‘msg_count’：‘"+count+"’,'date':'"+System.nanoTime()+"'}");
		}
		
		long end = System.nanoTime();
		
		LOG.info("任务耗时：[{}ns]",end-start);
		
		return new AsyncResult<String>("发送完成，发送消息条数："+loopCount+"发送TOPIC:"+topic);
	}
}
