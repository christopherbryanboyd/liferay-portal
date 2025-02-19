<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
long categoryId = ParamUtil.getLong(request, "categoryId");

AssetCategory category = AssetCategoryLocalServiceUtil.fetchCategory(categoryId);

int[] categoryPropertiesIndexes = null;

List<AssetCategoryProperty> categoryProperties = Collections.emptyList();

String categoryPropertiesIndexesParam = ParamUtil.getString(request, "categoryPropertiesIndexes");

if (Validator.isNotNull(categoryPropertiesIndexesParam)) {
	categoryProperties = new ArrayList<AssetCategoryProperty>();

	categoryPropertiesIndexes = StringUtil.split(categoryPropertiesIndexesParam, 0);

	for (int categoryPropertiesIndex : categoryPropertiesIndexes) {
		AssetCategoryProperty assetCategoryProperty = AssetCategoryPropertyLocalServiceUtil.createAssetCategoryProperty(0);

		categoryProperties.add(assetCategoryProperty);
	}
}
else {
	if (category != null) {
		categoryProperties = AssetCategoryPropertyServiceUtil.getCategoryProperties(category.getCategoryId());

		categoryPropertiesIndexes = new int[categoryProperties.size()];

		for (int i = 0; i < categoryProperties.size(); i++) {
			categoryPropertiesIndexes[i] = i;
		}
	}

	if (categoryProperties.isEmpty()) {
		categoryProperties = new ArrayList<AssetCategoryProperty>();

		AssetCategoryProperty assetCategoryProperty = AssetCategoryPropertyLocalServiceUtil.createAssetCategoryProperty(0);

		categoryProperties.add(assetCategoryProperty);

		categoryPropertiesIndexes = new int[] {0};
	}

	if (categoryPropertiesIndexes == null) {
		categoryPropertiesIndexes = new int[0];
	}
}

String redirect = ParamUtil.getString(request, "redirect", assetCategoriesDisplayContext.getEditCategoryRedirect());

long vocabularyId = ParamUtil.getLong(request, "vocabularyId");
%>

<portlet:actionURL name="editProperties" var="editPropertiesURL">
	<portlet:param name="mvcPath" value="/edit_category.jsp" />
	<portlet:param name="vocabularyId" value="<%= String.valueOf(vocabularyId) %>" />
</portlet:actionURL>

<liferay-frontend:edit-form
	action="<%= editPropertiesURL %>"
	name="fm"
>
	<aui:input name="redirect" type="hidden" value="<%= redirect %>" />
	<aui:input name="categoryId" type="hidden" value="<%= categoryId %>" />

	<liferay-frontend:edit-form-body>
		<liferay-ui:error exception="<%= CategoryPropertyKeyException.class %>" message="please-enter-a-valid-property-key" />
		<liferay-ui:error exception="<%= CategoryPropertyValueException.class %>" message="please-enter-a-valid-property-value" />
		<liferay-ui:error exception="<%= DuplicateCategoryPropertyException.class %>" message="please-enter-a-unique-property-key" />

		<liferay-frontend:fieldset-group>
			<liferay-frontend:fieldset>
				<div id="<portlet:namespace />categoryPropertiesId">
					<p class="text-muted">
						<liferay-ui:message key="properties-are-a-way-to-add-more-detailed-information-to-a-specific-category" />
					</p>

					<%
					for (int i = 0; i < categoryPropertiesIndexes.length; i++) {
						int categoryPropertiesIndex = categoryPropertiesIndexes[i];

						AssetCategoryProperty categoryProperty = categoryProperties.get(i);
					%>

						<aui:model-context bean="<%= categoryProperty %>" model="<%= AssetCategoryProperty.class %>" />

						<div class="lfr-form-row lfr-form-row-inline">
							<div class="row-fields">
								<aui:input fieldParam='<%= "key" + categoryPropertiesIndex %>' id='<%= "key" + categoryPropertiesIndex %>' name="key" />

								<aui:input fieldParam='<%= "value" + categoryPropertiesIndex %>' id='<%= "value" + categoryPropertiesIndex %>' name="value" />
							</div>
						</div>

					<%
					}
					%>

				</div>

				<aui:input name="categoryPropertiesIndexes" type="hidden" value="<%= StringUtil.merge(categoryPropertiesIndexes) %>" />
			</liferay-frontend:fieldset>
		</liferay-frontend:fieldset-group>
	</liferay-frontend:edit-form-body>

	<liferay-frontend:edit-form-footer>
		<aui:button type="submit" />

		<aui:button href="<%= redirect %>" type="cancel" />
	</liferay-frontend:edit-form-footer>
</liferay-frontend:edit-form>

<aui:script use="liferay-auto-fields">
	var autoFields = new Liferay.AutoFields({
		contentBox: '#<portlet:namespace />categoryPropertiesId',
		fieldIndexes: '<portlet:namespace />categoryPropertiesIndexes',
		namespace: '<portlet:namespace />',
	}).render();

	var categoryPropertiesTrigger = A.one(
		'#<portlet:namespace />categoryPropertiesId'
	);

	if (categoryPropertiesTrigger) {
		categoryPropertiesTrigger.setData('autoFieldsInstance', autoFields);
	}
</aui:script>