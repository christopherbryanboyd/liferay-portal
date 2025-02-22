/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.jenkins.results.parser;

import java.io.File;
import java.io.IOException;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * @author Kenji Heigel
 */
public class NotificationUtil {

	public static void sendEmail(
		String body, String senderName, String subject,
		String recipientEmailAddress) {

		String hostName = JenkinsResultsParserUtil.getHostName(null);

		sendEmail(
			JenkinsResultsParserUtil.combine(senderName, "@", hostName),
			senderName, recipientEmailAddress, subject, body);
	}

	public static void sendEmail(
		String senderEmailAddress, String senderName,
		String recipientEmailAddress, String subject, String body) {

		sendEmail(
			senderEmailAddress, senderName, recipientEmailAddress, subject,
			body, null);
	}

	public static void sendEmail(
		String senderEmailAddress, String senderName,
		String recipientEmailAddress, String subject, String body,
		String attachmentFileName) {

		Properties sessionProperties = System.getProperties();

		sessionProperties.put("mail.smtp.auth", "true");
		sessionProperties.put("mail.smtp.port", 587);
		sessionProperties.put("mail.smtp.starttls.enable", "true");
		sessionProperties.put("mail.transport.protocol", "smtp");

		Session session = Session.getDefaultInstance(sessionProperties);

		MimeMessage mimeMessage = new MimeMessage(session);

		try {
			mimeMessage.setFrom(
				new InternetAddress(senderEmailAddress, senderName));
			mimeMessage.setRecipients(
				Message.RecipientType.TO, recipientEmailAddress);
			mimeMessage.setSubject(subject);

			Multipart multipart = new MimeMultipart();

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setContent(body, "text/plain");

			multipart.addBodyPart(messageBodyPart);

			if ((attachmentFileName != null) &&
				!attachmentFileName.equals("")) {

				BodyPart attachmentBodyPart = new MimeBodyPart();

				DataSource source = new FileDataSource(attachmentFileName);

				attachmentBodyPart.setDataHandler(new DataHandler(source));

				File attachmentFile = new File(attachmentFileName);

				attachmentBodyPart.setFileName(attachmentFile.getName());

				multipart.addBodyPart(attachmentBodyPart);
			}

			mimeMessage.setContent(multipart);

			mimeMessage.saveChanges();

			Transport transport = session.getTransport();

			Properties buildProperties =
				JenkinsResultsParserUtil.getBuildProperties();

			transport.connect(
				buildProperties.getProperty("email.smtp.server"),
				buildProperties.getProperty("email.smtp.username"),
				buildProperties.getProperty("email.smtp.password"));

			transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());

			System.out.println("Email sent to: " + recipientEmailAddress);

			transport.close();
		}
		catch (IOException | MessagingException exception) {
			System.out.println("Unable to send email.");
			System.out.println(exception.getMessage());

			exception.printStackTrace();
		}
	}

	static {
		Thread thread = Thread.currentThread();

		thread.setContextClassLoader(Message.class.getClassLoader());
	}

}