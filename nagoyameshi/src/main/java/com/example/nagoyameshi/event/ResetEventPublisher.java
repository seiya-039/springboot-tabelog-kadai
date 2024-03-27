package com.example.nagoyameshi.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;

@Component
public class ResetEventPublisher {
	private final ApplicationEventPublisher applicationEventPublisher;

	public ResetEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void publishResetEvent(User user, String requestUrl) {
		applicationEventPublisher.publishEvent(new ResetEvent(this, user, requestUrl));
	}
}