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

AUI.add(
	'liferay-dynamic-select',
	A => {
		var sortByValue = function(a, b) {
			var pos = a.indexOf('">');

			var nameA = a.substring(pos);

			pos = b.indexOf('">');

			var nameB = b.substring(pos);

			var retVal = 0;

			if (nameA < nameB) {
				retVal = -1;
			}
			else if (nameA > nameB) {
				retVal = 1;
			}

			return retVal;
		};

		/**
		 * OPTIONS
		 *
		 * Required
		 * array {array}: An array of options.
		 * array[i].select {string}: An id of a select box.
		 * array[i].selectId {string}: A JSON object field name for an option value.
		 * array[i].selectDesc {string}: A JSON object field name for an option description.
		 * array[i].selectVal {string}: The value that is displayed in an option field.
		 *
		 * Optional
		 * array[i].selectDisableOnEmpty {boolean}: Whether or not to disable the select field when selectData returns no results
		 *
		 * Callbacks
		 * array[i].selectData {function}: Returns a JSON array to populate the next select box.
		 */

		var DynamicSelect = function(array) {
			var instance = this;

			instance.array = array;

			array.forEach((item, index) => {
				var id = item.select;
				var select = A.one('#' + id);
				var selectData = item.selectData;

				if (select) {
					select.attr('data-componentType', 'dynamic_select');

					var prevSelectVal = null;

					if (index > 0) {
						prevSelectVal = array[index - 1].selectVal;
					}

					selectData(list => {
						instance._updateSelect(index, list);
					}, prevSelectVal);

					if (!select.attr('name')) {
						select.attr('name', id);
					}

					select.on('change', () => {
						instance._callSelectData(index);
					});
				}
			});
		};

		DynamicSelect.prototype = {
			_callSelectData(i) {
				var instance = this;

				var array = instance.array;

				if (i + 1 < array.length) {
					var curSelect = A.one('#' + array[i].select);
					var nextSelectData = array[i + 1].selectData;

					nextSelectData(list => {
						instance._updateSelect(i + 1, list);
					}, curSelect && curSelect.val());
				}
			},

			_updateSelect(i, list) {
				var instance = this;

				var options = instance.array[i];

				var select = A.one('#' + options.select);
				var selectDesc = options.selectDesc;
				var selectDisableOnEmpty = options.selectDisableOnEmpty;
				var selectId = options.selectId;
				var selectNullable = options.selectNullable !== false;
				var selectSort = options.selectSort;

				var selectVal = A.Array(options.selectVal);

				var selectOptions = [];

				if (selectNullable) {
					selectOptions.push('<option selected value="0"></option>');
				}

				list.forEach(item => {
					var key = item[selectId];
					var value = item[selectDesc];

					var selected = '';

					if (selectVal.indexOf(key) > -1) {
						selected = 'selected="selected"';
					}

					selectOptions.push(
						'<option ' +
							selected +
							' value="' +
							key +
							'">' +
							value +
							'</option>'
					);
				});

				if (selectSort) {
					selectOptions = selectOptions.sort(sortByValue);
				}

				selectOptions = selectOptions.join('');

				if (select) {
					select.empty().append(selectOptions);

					if (selectDisableOnEmpty) {
						Liferay.Util.toggleDisabled(select, !list.length);
					}
				}
			},
		};

		Liferay.DynamicSelect = DynamicSelect;
	},
	'',
	{
		requires: ['aui-base'],
	}
);
