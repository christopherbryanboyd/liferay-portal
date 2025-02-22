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

package com.liferay.depot.internal.security.permission.wrapper;

import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.wrapper.PermissionCheckerWrapper;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserGroupRoleLocalService;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.permission.PermissionCacheUtil;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Cristina González
 */
public class DepotPermissionCheckerWrapper extends PermissionCheckerWrapper {

	public DepotPermissionCheckerWrapper(
		PermissionChecker permissionChecker,
		DepotEntryLocalService depotEntryLocalService,
		ModelResourcePermission<DepotEntry> depotEntryModelResourcePermission,
		GroupLocalService groupLocalService, RoleLocalService roleLocalService,
		UserGroupRoleLocalService userGroupRoleLocalService) {

		super(permissionChecker);

		_depotEntryLocalService = depotEntryLocalService;
		_depotEntryModelResourcePermission = depotEntryModelResourcePermission;
		_groupLocalService = groupLocalService;
		_roleLocalService = roleLocalService;
		_userGroupRoleLocalService = userGroupRoleLocalService;
	}

	@Override
	public boolean hasPermission(
		Group group, String name, long primKey, String actionId) {

		if (super.hasPermission(group, name, primKey, actionId)) {
			return true;
		}

		return _hasPermission(name, primKey, actionId);
	}

	@Override
	public boolean hasPermission(
		long groupId, String name, long primKey, String actionId) {

		if (super.hasPermission(groupId, name, primKey, actionId)) {
			return true;
		}

		return _hasPermission(name, primKey, actionId);
	}

	@Override
	public boolean isGroupAdmin(long groupId) {
		if (!isSignedIn()) {
			return false;
		}

		if (super.isGroupAdmin(groupId)) {
			return true;
		}

		if (groupId <= 0) {
			return false;
		}

		try {
			return _getOrAddToPermissionCache(
				_groupLocalService.fetchGroup(groupId), this::_isGroupAdmin,
				DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR);
		}
		catch (Exception exception) {
			_log.error(exception, exception);

			return false;
		}
	}

	@Override
	public boolean isGroupMember(long groupId) {
		if (!isSignedIn()) {
			return false;
		}

		if (groupId <= 0) {
			return false;
		}

		try {
			return _isGroupMember(_groupLocalService.getGroup(groupId));
		}
		catch (Exception exception) {
			_log.error(exception, exception);

			return false;
		}
	}

	@Override
	public boolean isGroupOwner(long groupId) {
		if (!isSignedIn()) {
			return false;
		}

		if (super.isGroupOwner(groupId)) {
			return true;
		}

		if (groupId <= 0) {
			return false;
		}

		try {
			return _getOrAddToPermissionCache(
				_groupLocalService.getGroup(groupId), this::_isGroupOwner,
				DepotRolesConstants.ASSET_LIBRARY_OWNER);
		}
		catch (Exception exception) {
			_log.error(exception, exception);

			return false;
		}
	}

	private boolean _getOrAddToPermissionCache(
			Group group,
			UnsafeFunction<Group, Boolean, Exception> unsafeFunction,
			String roleName)
		throws Exception {

		if (group == null) {
			return false;
		}

		Boolean value = PermissionCacheUtil.getUserPrimaryKeyRole(
			getUserId(), group.getGroupId(), roleName);

		try {
			if (value == null) {
				value = unsafeFunction.apply(group);

				PermissionCacheUtil.putUserPrimaryKeyRole(
					getUserId(), group.getGroupId(), roleName, value);
			}
		}
		catch (Exception exception) {
			PermissionCacheUtil.removeUserPrimaryKeyRole(
				getUserId(), group.getGroupId(), roleName);

			throw exception;
		}

		return value;
	}

	private boolean _hasPermission(String name, long groupId, String actionId) {
		if (StringUtil.equals(name, Group.class.getName())) {
			try {
				Group group = _groupLocalService.getGroup(groupId);

				if (group.getType() == GroupConstants.TYPE_DEPOT) {
					if (_isGroupAdmin(group)) {
						return true;
					}

					return _depotEntryModelResourcePermission.contains(
						this, group.getClassPK(), actionId);
				}
			}
			catch (Exception exception) {
				_log.error(exception, exception);
			}
		}

		return false;
	}

	private boolean _isGroupAdmin(Group group) throws PortalException {
		if (Objects.equals(group.getType(), GroupConstants.TYPE_DEPOT)) {
			if (_userGroupRoleLocalService.hasUserGroupRole(
					getUserId(), group.getGroupId(),
					DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR, true) ||
				_userGroupRoleLocalService.hasUserGroupRole(
					getUserId(), group.getGroupId(),
					DepotRolesConstants.ASSET_LIBRARY_OWNER, true)) {

				return true;
			}

			Group parentGroup = group;

			while (!parentGroup.isRoot()) {
				parentGroup = parentGroup.getParentGroup();

				if (super.hasPermission(
						parentGroup, Group.class.getName(),
						String.valueOf(parentGroup.getGroupId()),
						ActionKeys.MANAGE_SUBGROUPS)) {

					return true;
				}
			}
		}

		return false;
	}

	private boolean _isGroupMember(Group group) throws Exception {
		long[] roleIds = getRoleIds(getUserId(), group.getGroupId());

		Role role = _roleLocalService.getRole(
			group.getCompanyId(), DepotRolesConstants.ASSET_LIBRARY_MEMBER);

		if (Arrays.binarySearch(roleIds, role.getRoleId()) >= 0) {
			return true;
		}

		return super.isGroupMember(group.getGroupId());
	}

	private boolean _isGroupOwner(Group group) throws PortalException {
		if (Objects.equals(group.getType(), GroupConstants.TYPE_DEPOT) &&
			_userGroupRoleLocalService.hasUserGroupRole(
				getUserId(), group.getGroupId(),
				DepotRolesConstants.ASSET_LIBRARY_OWNER, true)) {

			return true;
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DepotPermissionCheckerWrapper.class);

	private final DepotEntryLocalService _depotEntryLocalService;
	private final ModelResourcePermission<DepotEntry>
		_depotEntryModelResourcePermission;
	private final GroupLocalService _groupLocalService;
	private final RoleLocalService _roleLocalService;
	private final UserGroupRoleLocalService _userGroupRoleLocalService;

}