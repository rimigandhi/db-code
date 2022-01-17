package com.db.awmd.challenge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;

@Service
public class EmailNotificationService implements NotificationService {

	Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

	@Override
	public void notifyAboutTransfer(Account account, String transferDescription) {
		// THIS METHOD SHOULD NOT BE CHANGED - ASSUME YOUR COLLEAGUE WILL IMPLEMENT IT
		log.info("Sending notification to owner of {}: {}", account.getAccountId(), transferDescription);
	}

}
