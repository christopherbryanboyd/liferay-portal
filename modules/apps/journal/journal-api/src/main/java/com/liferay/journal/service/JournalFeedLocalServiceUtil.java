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

package com.liferay.journal.service;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Provides the local service utility for JournalFeed. This utility wraps
 * <code>com.liferay.journal.service.impl.JournalFeedLocalServiceImpl</code> and
 * is an access point for service operations in application layer code running
 * on the local server. Methods of this service will not have security checks
 * based on the propagated JAAS credentials because this service can only be
 * accessed from within the same VM.
 *
 * @author Brian Wing Shun Chan
 * @see JournalFeedLocalService
 * @generated
 */
public class JournalFeedLocalServiceUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Add custom service methods to <code>com.liferay.journal.service.impl.JournalFeedLocalServiceImpl</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static com.liferay.journal.model.JournalFeed addFeed(
			long userId, long groupId, String feedId, boolean autoFeedId,
			String name, String description, String ddmStructureKey,
			String ddmTemplateKey, String ddmRendererTemplateKey, int delta,
			String orderByCol, String orderByType,
			String targetLayoutFriendlyUrl, String targetPortletId,
			String contentField, String feedFormat, double feedVersion,
			com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().addFeed(
			userId, groupId, feedId, autoFeedId, name, description,
			ddmStructureKey, ddmTemplateKey, ddmRendererTemplateKey, delta,
			orderByCol, orderByType, targetLayoutFriendlyUrl, targetPortletId,
			contentField, feedFormat, feedVersion, serviceContext);
	}

	public static void addFeedResources(
			com.liferay.journal.model.JournalFeed feed,
			boolean addGroupPermissions, boolean addGuestPermissions)
		throws com.liferay.portal.kernel.exception.PortalException {

		getService().addFeedResources(
			feed, addGroupPermissions, addGuestPermissions);
	}

	public static void addFeedResources(
			com.liferay.journal.model.JournalFeed feed,
			com.liferay.portal.kernel.service.permission.ModelPermissions
				modelPermissions)
		throws com.liferay.portal.kernel.exception.PortalException {

		getService().addFeedResources(feed, modelPermissions);
	}

	public static void addFeedResources(
			long feedId, boolean addGroupPermissions,
			boolean addGuestPermissions)
		throws com.liferay.portal.kernel.exception.PortalException {

		getService().addFeedResources(
			feedId, addGroupPermissions, addGuestPermissions);
	}

	/**
	 * Adds the journal feed to the database. Also notifies the appropriate model listeners.
	 *
	 * @param journalFeed the journal feed
	 * @return the journal feed that was added
	 */
	public static com.liferay.journal.model.JournalFeed addJournalFeed(
		com.liferay.journal.model.JournalFeed journalFeed) {

		return getService().addJournalFeed(journalFeed);
	}

	/**
	 * Creates a new journal feed with the primary key. Does not add the journal feed to the database.
	 *
	 * @param id the primary key for the new journal feed
	 * @return the new journal feed
	 */
	public static com.liferay.journal.model.JournalFeed createJournalFeed(
		long id) {

		return getService().createJournalFeed(id);
	}

	/**
	 * @throws PortalException
	 */
	public static com.liferay.portal.kernel.model.PersistedModel
			createPersistedModel(java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().createPersistedModel(primaryKeyObj);
	}

	public static void deleteFeed(com.liferay.journal.model.JournalFeed feed)
		throws com.liferay.portal.kernel.exception.PortalException {

		getService().deleteFeed(feed);
	}

	public static void deleteFeed(long feedId)
		throws com.liferay.portal.kernel.exception.PortalException {

		getService().deleteFeed(feedId);
	}

	public static void deleteFeed(long groupId, String feedId)
		throws com.liferay.portal.kernel.exception.PortalException {

		getService().deleteFeed(groupId, feedId);
	}

	/**
	 * Deletes the journal feed from the database. Also notifies the appropriate model listeners.
	 *
	 * @param journalFeed the journal feed
	 * @return the journal feed that was removed
	 */
	public static com.liferay.journal.model.JournalFeed deleteJournalFeed(
		com.liferay.journal.model.JournalFeed journalFeed) {

		return getService().deleteJournalFeed(journalFeed);
	}

	/**
	 * Deletes the journal feed with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param id the primary key of the journal feed
	 * @return the journal feed that was removed
	 * @throws PortalException if a journal feed with the primary key could not be found
	 */
	public static com.liferay.journal.model.JournalFeed deleteJournalFeed(
			long id)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().deleteJournalFeed(id);
	}

	/**
	 * @throws PortalException
	 */
	public static com.liferay.portal.kernel.model.PersistedModel
			deletePersistedModel(
				com.liferay.portal.kernel.model.PersistedModel persistedModel)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().deletePersistedModel(persistedModel);
	}

	public static com.liferay.portal.kernel.dao.orm.DynamicQuery
		dynamicQuery() {

		return getService().dynamicQuery();
	}

	/**
	 * Performs a dynamic query on the database and returns the matching rows.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the matching rows
	 */
	public static <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {

		return getService().dynamicQuery(dynamicQuery);
	}

	/**
	 * Performs a dynamic query on the database and returns a range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.journal.model.impl.JournalFeedModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @return the range of matching rows
	 */
	public static <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end) {

		return getService().dynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * Performs a dynamic query on the database and returns an ordered range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.journal.model.impl.JournalFeedModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching rows
	 */
	public static <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end,
		com.liferay.portal.kernel.util.OrderByComparator<T> orderByComparator) {

		return getService().dynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the number of rows matching the dynamic query
	 */
	public static long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {

		return getService().dynamicQueryCount(dynamicQuery);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @param projection the projection to apply to the query
	 * @return the number of rows matching the dynamic query
	 */
	public static long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery,
		com.liferay.portal.kernel.dao.orm.Projection projection) {

		return getService().dynamicQueryCount(dynamicQuery, projection);
	}

	public static com.liferay.journal.model.JournalFeed fetchFeed(
		long groupId, String feedId) {

		return getService().fetchFeed(groupId, feedId);
	}

	public static com.liferay.journal.model.JournalFeed fetchJournalFeed(
		long id) {

		return getService().fetchJournalFeed(id);
	}

	/**
	 * Returns the journal feed matching the UUID and group.
	 *
	 * @param uuid the journal feed's UUID
	 * @param groupId the primary key of the group
	 * @return the matching journal feed, or <code>null</code> if a matching journal feed could not be found
	 */
	public static com.liferay.journal.model.JournalFeed
		fetchJournalFeedByUuidAndGroupId(String uuid, long groupId) {

		return getService().fetchJournalFeedByUuidAndGroupId(uuid, groupId);
	}

	public static com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return getService().getActionableDynamicQuery();
	}

	public static com.liferay.portal.kernel.dao.orm.ExportActionableDynamicQuery
		getExportActionableDynamicQuery(
			com.liferay.exportimport.kernel.lar.PortletDataContext
				portletDataContext) {

		return getService().getExportActionableDynamicQuery(portletDataContext);
	}

	public static com.liferay.journal.model.JournalFeed getFeed(long feedId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().getFeed(feedId);
	}

	public static com.liferay.journal.model.JournalFeed getFeed(
			long groupId, String feedId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().getFeed(groupId, feedId);
	}

	public static java.util.List<com.liferay.journal.model.JournalFeed>
		getFeeds() {

		return getService().getFeeds();
	}

	public static java.util.List<com.liferay.journal.model.JournalFeed>
		getFeeds(long groupId) {

		return getService().getFeeds(groupId);
	}

	public static java.util.List<com.liferay.journal.model.JournalFeed>
		getFeeds(long groupId, int start, int end) {

		return getService().getFeeds(groupId, start, end);
	}

	public static int getFeedsCount(long groupId) {
		return getService().getFeedsCount(groupId);
	}

	public static
		com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
			getIndexableActionableDynamicQuery() {

		return getService().getIndexableActionableDynamicQuery();
	}

	/**
	 * Returns the journal feed with the primary key.
	 *
	 * @param id the primary key of the journal feed
	 * @return the journal feed
	 * @throws PortalException if a journal feed with the primary key could not be found
	 */
	public static com.liferay.journal.model.JournalFeed getJournalFeed(long id)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().getJournalFeed(id);
	}

	/**
	 * Returns the journal feed matching the UUID and group.
	 *
	 * @param uuid the journal feed's UUID
	 * @param groupId the primary key of the group
	 * @return the matching journal feed
	 * @throws PortalException if a matching journal feed could not be found
	 */
	public static com.liferay.journal.model.JournalFeed
			getJournalFeedByUuidAndGroupId(String uuid, long groupId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().getJournalFeedByUuidAndGroupId(uuid, groupId);
	}

	/**
	 * Returns a range of all the journal feeds.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.journal.model.impl.JournalFeedModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of journal feeds
	 * @param end the upper bound of the range of journal feeds (not inclusive)
	 * @return the range of journal feeds
	 */
	public static java.util.List<com.liferay.journal.model.JournalFeed>
		getJournalFeeds(int start, int end) {

		return getService().getJournalFeeds(start, end);
	}

	/**
	 * Returns all the journal feeds matching the UUID and company.
	 *
	 * @param uuid the UUID of the journal feeds
	 * @param companyId the primary key of the company
	 * @return the matching journal feeds, or an empty list if no matches were found
	 */
	public static java.util.List<com.liferay.journal.model.JournalFeed>
		getJournalFeedsByUuidAndCompanyId(String uuid, long companyId) {

		return getService().getJournalFeedsByUuidAndCompanyId(uuid, companyId);
	}

	/**
	 * Returns a range of journal feeds matching the UUID and company.
	 *
	 * @param uuid the UUID of the journal feeds
	 * @param companyId the primary key of the company
	 * @param start the lower bound of the range of journal feeds
	 * @param end the upper bound of the range of journal feeds (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the range of matching journal feeds, or an empty list if no matches were found
	 */
	public static java.util.List<com.liferay.journal.model.JournalFeed>
		getJournalFeedsByUuidAndCompanyId(
			String uuid, long companyId, int start, int end,
			com.liferay.portal.kernel.util.OrderByComparator
				<com.liferay.journal.model.JournalFeed> orderByComparator) {

		return getService().getJournalFeedsByUuidAndCompanyId(
			uuid, companyId, start, end, orderByComparator);
	}

	/**
	 * Returns the number of journal feeds.
	 *
	 * @return the number of journal feeds
	 */
	public static int getJournalFeedsCount() {
		return getService().getJournalFeedsCount();
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	public static String getOSGiServiceIdentifier() {
		return getService().getOSGiServiceIdentifier();
	}

	/**
	 * @throws PortalException
	 */
	public static com.liferay.portal.kernel.model.PersistedModel
			getPersistedModel(java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().getPersistedModel(primaryKeyObj);
	}

	public static java.util.List<com.liferay.journal.model.JournalFeed> search(
		long companyId, long groupId, String keywords, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<com.liferay.journal.model.JournalFeed> obc) {

		return getService().search(
			companyId, groupId, keywords, start, end, obc);
	}

	public static java.util.List<com.liferay.journal.model.JournalFeed> search(
		long companyId, long groupId, String feedId, String name,
		String description, boolean andOperator, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<com.liferay.journal.model.JournalFeed> obc) {

		return getService().search(
			companyId, groupId, feedId, name, description, andOperator, start,
			end, obc);
	}

	public static int searchCount(
		long companyId, long groupId, String keywords) {

		return getService().searchCount(companyId, groupId, keywords);
	}

	public static int searchCount(
		long companyId, long groupId, String feedId, String name,
		String description, boolean andOperator) {

		return getService().searchCount(
			companyId, groupId, feedId, name, description, andOperator);
	}

	public static com.liferay.journal.model.JournalFeed updateFeed(
			long groupId, String feedId, String name, String description,
			String ddmStructureKey, String ddmTemplateKey,
			String ddmRendererTemplateKey, int delta, String orderByCol,
			String orderByType, String targetLayoutFriendlyUrl,
			String targetPortletId, String contentField, String feedFormat,
			double feedVersion,
			com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().updateFeed(
			groupId, feedId, name, description, ddmStructureKey, ddmTemplateKey,
			ddmRendererTemplateKey, delta, orderByCol, orderByType,
			targetLayoutFriendlyUrl, targetPortletId, contentField, feedFormat,
			feedVersion, serviceContext);
	}

	/**
	 * Updates the journal feed in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * @param journalFeed the journal feed
	 * @return the journal feed that was updated
	 */
	public static com.liferay.journal.model.JournalFeed updateJournalFeed(
		com.liferay.journal.model.JournalFeed journalFeed) {

		return getService().updateJournalFeed(journalFeed);
	}

	public static JournalFeedLocalService getService() {
		return _serviceTracker.getService();
	}

	private static ServiceTracker
		<JournalFeedLocalService, JournalFeedLocalService> _serviceTracker;

	static {
		Bundle bundle = FrameworkUtil.getBundle(JournalFeedLocalService.class);

		ServiceTracker<JournalFeedLocalService, JournalFeedLocalService>
			serviceTracker =
				new ServiceTracker
					<JournalFeedLocalService, JournalFeedLocalService>(
						bundle.getBundleContext(),
						JournalFeedLocalService.class, null);

		serviceTracker.open();

		_serviceTracker = serviceTracker;
	}

}