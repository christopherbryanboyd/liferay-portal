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

package com.liferay.portal.resiliency.spi.agent;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.io.BigEndianCodec;
import com.liferay.portal.kernel.io.Deserializer;
import com.liferay.portal.kernel.io.Serializer;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.nio.intraband.RegistrationReference;
import com.liferay.portal.kernel.nio.intraband.mailbox.MailboxException;
import com.liferay.portal.kernel.nio.intraband.mailbox.MailboxUtil;
import com.liferay.portal.kernel.resiliency.spi.agent.annotation.Direction;
import com.liferay.portal.kernel.resiliency.spi.agent.annotation.DistributedRegistry;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.servlet.ServletContextClassLoaderPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.ThreadLocalDistributor;
import com.liferay.portal.kernel.util.ThreadLocalDistributorRegistry;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Shuyang Zhou
 */
public class SPIAgentSerializable implements Serializable {

	public static Map<String, Serializable> extractDistributedRequestAttributes(
		HttpServletRequest httpServletRequest, Direction direction) {

		Map<String, Serializable> distributedRequestAttributes =
			new HashMap<>();

		Enumeration<String> enumeration =
			httpServletRequest.getAttributeNames();

		while (enumeration.hasMoreElements()) {
			String name = enumeration.nextElement();

			if (DistributedRegistry.isDistributed(name, direction)) {
				Object value = httpServletRequest.getAttribute(name);

				if (value instanceof Serializable) {
					distributedRequestAttributes.put(name, (Serializable)value);
				}
				else if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Nonserializable distributed request attribute ",
							"name ", name, " with value ", value));
				}
			}
			else if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"Nondistributed request attribute name ", name,
						" with direction ", direction, " and value ",
						httpServletRequest.getAttribute(name)));
			}
		}

		return distributedRequestAttributes;
	}

	public static Map<String, List<String>> extractRequestHeaders(
		HttpServletRequest httpServletRequest) {

		Map<String, List<String>> headers = new HashMap<>();

		Enumeration<String> nameEnumeration =
			httpServletRequest.getHeaderNames();

		while (nameEnumeration.hasMoreElements()) {
			String headerName = nameEnumeration.nextElement();

			// Remove Accept-Encoding header, to prevent content modification

			if (StringUtil.equalsIgnoreCase(
					HttpHeaders.ACCEPT_ENCODING, headerName)) {

				continue;
			}

			// Directly passing around cookie

			if (StringUtil.equalsIgnoreCase(HttpHeaders.COOKIE, headerName)) {
				continue;
			}

			Enumeration<String> valueEnumeration =
				httpServletRequest.getHeaders(headerName);

			if (valueEnumeration != null) {
				List<String> values = new ArrayList<>();

				while (valueEnumeration.hasMoreElements()) {
					values.add(valueEnumeration.nextElement());
				}

				if (values.isEmpty()) {
					values = Collections.emptyList();
				}

				headers.put(StringUtil.toLowerCase(headerName), values);
			}
		}

		if (headers.isEmpty()) {
			headers = Collections.emptyMap();
		}

		return headers;
	}

	public static Map<String, Serializable> extractSessionAttributes(
		HttpServletRequest httpServletRequest) {

		Portlet portlet = (Portlet)httpServletRequest.getAttribute(
			WebKeys.SPI_AGENT_PORTLET);

		String portletSessionAttributesKey =
			WebKeys.PORTLET_SESSION_ATTRIBUTES.concat(portlet.getContextName());

		Map<String, Serializable> sessionAttributes = new HashMap<>();

		HttpSession session = httpServletRequest.getSession();

		Enumeration<String> enumeration = session.getAttributeNames();

		while (enumeration.hasMoreElements()) {
			String name = enumeration.nextElement();

			if (name.startsWith(WebKeys.PORTLET_SESSION_ATTRIBUTES) &&
				!name.equals(portletSessionAttributesKey)) {

				continue;
			}

			Object value = session.getAttribute(name);

			if (value instanceof Serializable) {
				sessionAttributes.put(name, (Serializable)value);
			}
			else if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Nonserializable session attribute name ", name,
						" with value ", value));
			}
		}

		HttpSession portletSession =
			(HttpSession)httpServletRequest.getAttribute(
				WebKeys.PORTLET_SESSION);

		if (portletSession != null) {
			httpServletRequest.removeAttribute(WebKeys.PORTLET_SESSION);

			HashMap<String, Serializable> portletSessionAttributes =
				new HashMap<>();

			enumeration = portletSession.getAttributeNames();

			while (enumeration.hasMoreElements()) {
				String name = enumeration.nextElement();

				Object value = portletSession.getAttribute(name);

				if (value instanceof Serializable) {
					portletSessionAttributes.put(name, (Serializable)value);
				}
				else if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Nonserializable session attribute name ", name,
							" with value ", value));
				}
			}

			sessionAttributes.put(
				portletSessionAttributesKey, portletSessionAttributes);
		}

		return sessionAttributes;
	}

	public static <T extends SPIAgentSerializable> T readFrom(
			InputStream inputStream)
		throws IOException {

		byte[] data = new byte[8];
		int length = 0;

		while (length < 8) {
			int count = inputStream.read(data, length, 8 - length);

			if (count < 0) {
				throw new EOFException();
			}

			length += count;
		}

		long receipt = BigEndianCodec.getLong(data, 0);

		ByteBuffer byteBuffer = MailboxUtil.receiveMail(receipt);

		if (byteBuffer == null) {
			throw new IllegalArgumentException(
				"No mail with receipt " + receipt);
		}

		Deserializer deserializer = new Deserializer(byteBuffer);

		Thread currentThread = Thread.currentThread();

		ClassLoader contextClassLoader = currentThread.getContextClassLoader();

		try {
			String servletContextName = deserializer.readString();

			ClassLoader classLoader =
				ServletContextClassLoaderPool.getClassLoader(
					servletContextName);

			if (classLoader != null) {
				currentThread.setContextClassLoader(classLoader);
			}

			T t = deserializer.readObject();

			t.servletContextName = servletContextName;

			return t;
		}
		catch (ClassNotFoundException classNotFoundException) {
			throw new IOException(classNotFoundException);
		}
		finally {
			currentThread.setContextClassLoader(contextClassLoader);
		}
	}

	public SPIAgentSerializable(String servletContextName) {
		this.servletContextName = servletContextName;
	}

	public void writeTo(
			RegistrationReference registrationReference,
			OutputStream outputStream)
		throws IOException {

		Serializer serializer = new Serializer();

		serializer.writeString(servletContextName);
		serializer.writeObject(this);

		try {
			byte[] data = new byte[8];

			ByteBuffer byteBuffer = serializer.toByteBuffer();

			long receipt = MailboxUtil.sendMail(
				registrationReference, byteBuffer);

			BigEndianCodec.putLong(data, 0, receipt);

			outputStream.write(data);

			outputStream.flush();
		}
		catch (MailboxException mailboxException) {
			throw new IOException(mailboxException);
		}
	}

	protected void captureThreadLocals() {
		threadLocalDistributors =
			ThreadLocalDistributorRegistry.getThreadLocalDistributors();

		for (ThreadLocalDistributor threadLocalDistributor :
				threadLocalDistributors) {

			threadLocalDistributor.capture();
		}
	}

	protected void restoreThreadLocals() {
		for (ThreadLocalDistributor threadLocalDistributor :
				threadLocalDistributors) {

			threadLocalDistributor.restore();
		}
	}

	protected transient String servletContextName;
	protected ThreadLocalDistributor[] threadLocalDistributors;

	private static final Log _log = LogFactoryUtil.getLog(
		SPIAgentSerializable.class);

}