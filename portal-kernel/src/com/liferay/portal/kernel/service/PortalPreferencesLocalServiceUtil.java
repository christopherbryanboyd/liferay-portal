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

package com.liferay.portal.kernel.service;

import com.liferay.portal.kernel.bean.PortalBeanLocatorUtil;

/**
 * Provides the local service utility for PortalPreferences. This utility wraps
 * <code>com.liferay.portal.service.impl.PortalPreferencesLocalServiceImpl</code> and
 * is an access point for service operations in application layer code running
 * on the local server. Methods of this service will not have security checks
 * based on the propagated JAAS credentials because this service can only be
 * accessed from within the same VM.
 *
 * @author Brian Wing Shun Chan
 * @see PortalPreferencesLocalService
 * @generated
 */
public class PortalPreferencesLocalServiceUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Add custom service methods to <code>com.liferay.portal.service.impl.PortalPreferencesLocalServiceImpl</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static com.liferay.portal.kernel.model.PortalPreferences
		addPortalPreferences(
			long ownerId, int ownerType, String defaultPreferences) {

		return getService().addPortalPreferences(
			ownerId, ownerType, defaultPreferences);
	}

	/**
	 * Adds the portal preferences to the database. Also notifies the appropriate model listeners.
	 *
	 * @param portalPreferences the portal preferences
	 * @return the portal preferences that was added
	 */
	public static com.liferay.portal.kernel.model.PortalPreferences
		addPortalPreferences(
			com.liferay.portal.kernel.model.PortalPreferences
				portalPreferences) {

		return getService().addPortalPreferences(portalPreferences);
	}

	/**
	 * @throws PortalException
	 */
	public static com.liferay.portal.kernel.model.PersistedModel
			createPersistedModel(java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().createPersistedModel(primaryKeyObj);
	}

	/**
	 * Creates a new portal preferences with the primary key. Does not add the portal preferences to the database.
	 *
	 * @param portalPreferencesId the primary key for the new portal preferences
	 * @return the new portal preferences
	 */
	public static com.liferay.portal.kernel.model.PortalPreferences
		createPortalPreferences(long portalPreferencesId) {

		return getService().createPortalPreferences(portalPreferencesId);
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

	/**
	 * Deletes the portal preferences with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param portalPreferencesId the primary key of the portal preferences
	 * @return the portal preferences that was removed
	 * @throws PortalException if a portal preferences with the primary key could not be found
	 */
	public static com.liferay.portal.kernel.model.PortalPreferences
			deletePortalPreferences(long portalPreferencesId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().deletePortalPreferences(portalPreferencesId);
	}

	/**
	 * Deletes the portal preferences from the database. Also notifies the appropriate model listeners.
	 *
	 * @param portalPreferences the portal preferences
	 * @return the portal preferences that was removed
	 */
	public static com.liferay.portal.kernel.model.PortalPreferences
		deletePortalPreferences(
			com.liferay.portal.kernel.model.PortalPreferences
				portalPreferences) {

		return getService().deletePortalPreferences(portalPreferences);
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
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.model.impl.PortalPreferencesModelImpl</code>.
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
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.model.impl.PortalPreferencesModelImpl</code>.
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

	public static com.liferay.portal.kernel.model.PortalPreferences
		fetchPortalPreferences(long portalPreferencesId) {

		return getService().fetchPortalPreferences(portalPreferencesId);
	}

	public static com.liferay.portal.kernel.model.PortalPreferences
		fetchPortalPreferences(long ownerId, int ownerType) {

		return getService().fetchPortalPreferences(ownerId, ownerType);
	}

	public static com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return getService().getActionableDynamicQuery();
	}

	public static
		com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
			getIndexableActionableDynamicQuery() {

		return getService().getIndexableActionableDynamicQuery();
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

	/**
	 * Returns the portal preferences with the primary key.
	 *
	 * @param portalPreferencesId the primary key of the portal preferences
	 * @return the portal preferences
	 * @throws PortalException if a portal preferences with the primary key could not be found
	 */
	public static com.liferay.portal.kernel.model.PortalPreferences
			getPortalPreferences(long portalPreferencesId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().getPortalPreferences(portalPreferencesId);
	}

	/**
	 * Returns a range of all the portal preferenceses.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.model.impl.PortalPreferencesModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of portal preferenceses
	 * @param end the upper bound of the range of portal preferenceses (not inclusive)
	 * @return the range of portal preferenceses
	 */
	public static java.util.List
		<com.liferay.portal.kernel.model.PortalPreferences>
			getPortalPreferenceses(int start, int end) {

		return getService().getPortalPreferenceses(start, end);
	}

	/**
	 * Returns the number of portal preferenceses.
	 *
	 * @return the number of portal preferenceses
	 */
	public static int getPortalPreferencesesCount() {
		return getService().getPortalPreferencesesCount();
	}

	public static javax.portlet.PortletPreferences getPreferences(
		long ownerId, int ownerType) {

		return getService().getPreferences(ownerId, ownerType);
	}

	public static javax.portlet.PortletPreferences getPreferences(
		long ownerId, int ownerType, String defaultPreferences) {

		return getService().getPreferences(
			ownerId, ownerType, defaultPreferences);
	}

	/**
	 * Updates the portal preferences in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * @param portalPreferences the portal preferences
	 * @return the portal preferences that was updated
	 */
	public static com.liferay.portal.kernel.model.PortalPreferences
		updatePortalPreferences(
			com.liferay.portal.kernel.model.PortalPreferences
				portalPreferences) {

		return getService().updatePortalPreferences(portalPreferences);
	}

	public static com.liferay.portal.kernel.model.PortalPreferences
		updatePreferences(
			long ownerId, int ownerType,
			com.liferay.portal.kernel.portlet.PortalPreferences
				portalPreferences) {

		return getService().updatePreferences(
			ownerId, ownerType, portalPreferences);
	}

	public static com.liferay.portal.kernel.model.PortalPreferences
		updatePreferences(long ownerId, int ownerType, String xml) {

		return getService().updatePreferences(ownerId, ownerType, xml);
	}

	public static PortalPreferencesLocalService getService() {
		if (_service == null) {
			_service =
				(PortalPreferencesLocalService)PortalBeanLocatorUtil.locate(
					PortalPreferencesLocalService.class.getName());
		}

		return _service;
	}

	private static PortalPreferencesLocalService _service;

}