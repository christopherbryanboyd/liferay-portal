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

package com.liferay.layout.page.template.headless.delivery.dto.v1_0;

import com.liferay.fragment.contributor.FragmentCollectionContributorTracker;
import com.liferay.fragment.renderer.FragmentRendererTracker;
import com.liferay.fragment.util.configuration.FragmentEntryConfigurationParser;
import com.liferay.headless.delivery.dto.v1_0.ColumnDefinition;
import com.liferay.headless.delivery.dto.v1_0.FragmentImage;
import com.liferay.headless.delivery.dto.v1_0.Layout;
import com.liferay.headless.delivery.dto.v1_0.MasterPage;
import com.liferay.headless.delivery.dto.v1_0.PageDefinition;
import com.liferay.headless.delivery.dto.v1_0.PageElement;
import com.liferay.headless.delivery.dto.v1_0.RowDefinition;
import com.liferay.headless.delivery.dto.v1_0.SectionDefinition;
import com.liferay.headless.delivery.dto.v1_0.Settings;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalServiceUtil;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalServiceUtil;
import com.liferay.layout.util.structure.ColumnLayoutStructureItem;
import com.liferay.layout.util.structure.ContainerLayoutStructureItem;
import com.liferay.layout.util.structure.DropZoneLayoutStructureItem;
import com.liferay.layout.util.structure.FragmentLayoutStructureItem;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.layout.util.structure.RootLayoutStructureItem;
import com.liferay.layout.util.structure.RowLayoutStructureItem;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ColorScheme;
import com.liferay.portal.kernel.model.Theme;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Rubén Pulido
 */
public class PageDefinitionConverterUtil {

	public static PageDefinition toPageDefinition(
		FragmentCollectionContributorTracker
			fragmentCollectionContributorTracker,
		FragmentEntryConfigurationParser fragmentEntryConfigurationParser,
		FragmentRendererTracker fragmentRendererTracker,
		com.liferay.portal.kernel.model.Layout layout) {

		return toPageDefinition(
			fragmentCollectionContributorTracker,
			fragmentEntryConfigurationParser, fragmentRendererTracker, layout,
			true, true);
	}

	public static PageDefinition toPageDefinition(
		FragmentCollectionContributorTracker
			fragmentCollectionContributorTracker,
		FragmentEntryConfigurationParser fragmentEntryConfigurationParser,
		FragmentRendererTracker fragmentRendererTracker,
		com.liferay.portal.kernel.model.Layout layout,
		boolean saveInlineContent, boolean saveMappingConfiguration) {

		return new PageDefinition() {
			{
				pageElement = _toPageElement(
					fragmentCollectionContributorTracker,
					fragmentEntryConfigurationParser, fragmentRendererTracker,
					layout, saveInlineContent, saveMappingConfiguration);
				settings = _toSettings(layout);
			}
		};
	}

	public static PageElement toPageElement(
		FragmentCollectionContributorTracker
			fragmentCollectionContributorTracker,
		FragmentEntryConfigurationParser fragmentEntryConfigurationParser,
		FragmentRendererTracker fragmentRendererTracker,
		LayoutStructure layoutStructure,
		LayoutStructureItem layoutStructureItem, boolean saveInlineContent,
		boolean saveMappingConfiguration) {

		List<PageElement> pageElements = new ArrayList<>();

		List<String> childrenItemIds = layoutStructureItem.getChildrenItemIds();

		for (String childItemId : childrenItemIds) {
			LayoutStructureItem childLayoutStructureItem =
				layoutStructure.getLayoutStructureItem(childItemId);

			List<String> grandChildrenItemIds =
				childLayoutStructureItem.getChildrenItemIds();

			if (grandChildrenItemIds.isEmpty()) {
				pageElements.add(
					_toPageElement(
						fragmentCollectionContributorTracker,
						fragmentEntryConfigurationParser,
						fragmentRendererTracker, childLayoutStructureItem,
						saveInlineContent, saveMappingConfiguration));
			}
			else {
				pageElements.add(
					toPageElement(
						fragmentCollectionContributorTracker,
						fragmentEntryConfigurationParser,
						fragmentRendererTracker, layoutStructure,
						childLayoutStructureItem, saveInlineContent,
						saveMappingConfiguration));
			}
		}

		PageElement pageElement = _toPageElement(
			fragmentCollectionContributorTracker,
			fragmentEntryConfigurationParser, fragmentRendererTracker,
			layoutStructureItem, saveInlineContent, saveMappingConfiguration);

		if (!pageElements.isEmpty()) {
			pageElement.setPageElements(
				pageElements.toArray(new PageElement[0]));
		}

		return pageElement;
	}

	private static PageElement _toPageElement(
		FragmentCollectionContributorTracker
			fragmentCollectionContributorTracker,
		FragmentEntryConfigurationParser fragmentEntryConfigurationParser,
		FragmentRendererTracker fragmentRendererTracker,
		com.liferay.portal.kernel.model.Layout layout,
		boolean saveInlineContent, boolean saveMappingConfiguration) {

		LayoutPageTemplateStructure layoutPageTemplateStructure =
			LayoutPageTemplateStructureLocalServiceUtil.
				fetchLayoutPageTemplateStructure(
					layout.getGroupId(),
					PortalUtil.getClassNameId(
						com.liferay.portal.kernel.model.Layout.class),
					layout.getPlid());

		LayoutStructure layoutStructure = LayoutStructure.of(
			layoutPageTemplateStructure.getData(0L));

		LayoutStructureItem mainLayoutStructureItem =
			layoutStructure.getMainLayoutStructureItem();

		List<PageElement> mainPageElements = new ArrayList<>();

		for (String childItemId :
				mainLayoutStructureItem.getChildrenItemIds()) {

			mainPageElements.add(
				toPageElement(
					fragmentCollectionContributorTracker,
					fragmentEntryConfigurationParser, fragmentRendererTracker,
					layoutStructure,
					layoutStructure.getLayoutStructureItem(childItemId),
					saveInlineContent, saveMappingConfiguration));
		}

		PageElement pageElement = _toPageElement(
			fragmentCollectionContributorTracker,
			fragmentEntryConfigurationParser, fragmentRendererTracker,
			mainLayoutStructureItem, saveInlineContent,
			saveMappingConfiguration);

		if (!mainPageElements.isEmpty()) {
			pageElement.setPageElements(
				mainPageElements.toArray(new PageElement[0]));
		}

		return pageElement;
	}

	private static PageElement _toPageElement(
		FragmentCollectionContributorTracker
			fragmentCollectionContributorTracker,
		FragmentEntryConfigurationParser fragmentEntryConfigurationParser,
		FragmentRendererTracker fragmentRendererTracker,
		LayoutStructureItem layoutStructureItem, boolean saveInlineContent,
		boolean saveMappingConfiguration) {

		if (layoutStructureItem instanceof ColumnLayoutStructureItem) {
			ColumnLayoutStructureItem columnLayoutStructureItem =
				(ColumnLayoutStructureItem)layoutStructureItem;

			return new PageElement() {
				{
					definition = new ColumnDefinition() {
						{
							size = columnLayoutStructureItem.getSize();
						}
					};
					type = PageElement.Type.COLUMN;
				}
			};
		}

		if (layoutStructureItem instanceof ContainerLayoutStructureItem) {
			ContainerLayoutStructureItem containerLayoutStructureItem =
				(ContainerLayoutStructureItem)layoutStructureItem;

			return new PageElement() {
				{
					definition = new SectionDefinition() {
						{
							backgroundColorCssClass = GetterUtil.getString(
								containerLayoutStructureItem.
									getBackgroundColorCssClass(),
								null);
							layout = new Layout() {
								{
									paddingBottom =
										containerLayoutStructureItem.
											getPaddingBottom();
									paddingHorizontal =
										containerLayoutStructureItem.
											getPaddingHorizontal();
									paddingTop =
										containerLayoutStructureItem.
											getPaddingTop();

									setContainerType(
										() -> {
											String containerType =
												containerLayoutStructureItem.
													getContainerType();

											if (Validator.isNull(
													containerType)) {

												return null;
											}

											return ContainerType.create(
												StringUtil.upperCaseFirstLetter(
													containerType));
										});
								}
							};

							setBackgroundImage(
								() -> {
									JSONObject backgroundImageJSONObject =
										containerLayoutStructureItem.
											getBackgroundImageJSONObject();

									if ((backgroundImageJSONObject == null) ||
										!backgroundImageJSONObject.has("url")) {

										return null;
									}

									return new FragmentImage() {
										{
											url = backgroundImageJSONObject.get(
												"url");
										}
									};
								});
						}
					};
					type = PageElement.Type.SECTION;
				}
			};
		}

		if (layoutStructureItem instanceof DropZoneLayoutStructureItem) {
			return new PageElement() {
				{
					type = PageElement.Type.DROP_ZONE;
				}
			};
		}

		if (layoutStructureItem instanceof FragmentLayoutStructureItem) {
			FragmentLayoutStructureItem fragmentLayoutStructureItem =
				(FragmentLayoutStructureItem)layoutStructureItem;

			return new PageElement() {
				{
					definition =
						FragmentInstanceDefinitionConverterUtil.
							toFragmentInstanceDefinition(
								fragmentCollectionContributorTracker,
								fragmentEntryConfigurationParser,
								fragmentLayoutStructureItem,
								fragmentRendererTracker, saveInlineContent,
								saveMappingConfiguration);
					type = PageElement.Type.FRAGMENT;
				}
			};
		}

		if (layoutStructureItem instanceof RootLayoutStructureItem) {
			return new PageElement() {
				{
					type = PageElement.Type.ROOT;
				}
			};
		}

		if (layoutStructureItem instanceof RowLayoutStructureItem) {
			RowLayoutStructureItem rowLayoutStructureItem =
				(RowLayoutStructureItem)layoutStructureItem;

			return new PageElement() {
				{
					definition = new RowDefinition() {
						{
							gutters = rowLayoutStructureItem.isGutters();
							numberOfColumns =
								rowLayoutStructureItem.getNumberOfColumns();
						}
					};
					type = PageElement.Type.ROW;
				}
			};
		}

		return null;
	}

	private static Settings _toSettings(
		com.liferay.portal.kernel.model.Layout layout) {

		UnicodeProperties unicodeProperties =
			layout.getTypeSettingsProperties();

		return new Settings() {
			{
				setColorSchemeName(
					() -> {
						ColorScheme colorScheme = null;

						try {
							colorScheme = layout.getColorScheme();
						}
						catch (PortalException portalException) {
							if (_log.isWarnEnabled()) {
								_log.warn(portalException, portalException);
							}
						}

						if (colorScheme == null) {
							return null;
						}

						return colorScheme.getName();
					});
				setCss(
					() -> {
						if (Validator.isNull(layout.getCss())) {
							return null;
						}

						return layout.getCss();
					});
				setJavascript(
					() -> {
						for (Map.Entry<String, String> entry :
								unicodeProperties.entrySet()) {

							String key = entry.getKey();

							if (key.equals("javascript")) {
								return entry.getValue();
							}
						}

						return null;
					});
				setMasterPage(
					() -> {
						LayoutPageTemplateEntry layoutPageTemplateEntry =
							LayoutPageTemplateEntryLocalServiceUtil.
								fetchLayoutPageTemplateEntryByPlid(
									layout.getMasterLayoutPlid());

						if (layoutPageTemplateEntry == null) {
							return null;
						}

						return new MasterPage() {
							{
								name = layoutPageTemplateEntry.getName();
							}
						};
					});
				setThemeName(
					() -> {
						Theme theme = layout.getTheme();

						if (theme == null) {
							return null;
						}

						return theme.getName();
					});
				setThemeSettings(
					() -> {
						UnicodeProperties themeSettingsUnicodeProperties =
							new UnicodeProperties();

						for (Map.Entry<String, String> entry :
								unicodeProperties.entrySet()) {

							String key = entry.getKey();

							if (key.startsWith("lfr-theme:")) {
								themeSettingsUnicodeProperties.setProperty(
									key, entry.getValue());
							}
						}

						if (themeSettingsUnicodeProperties.isEmpty()) {
							return null;
						}

						return themeSettingsUnicodeProperties;
					});
			}
		};
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PageDefinitionConverterUtil.class);

}