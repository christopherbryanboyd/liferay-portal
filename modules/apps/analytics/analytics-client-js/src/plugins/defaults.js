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

import blogs from './blogs';
import custom from './custom';
import documents from './documents';
import forms from './forms';
import resolution from './resolution';
import scrolling from './scrolling';
import timing from './timing';
import webContents from './web-contents';

export {blogs, documents, forms, resolution, scrolling, timing, webContents};
export default [
	// Resolution should come first, because it chages the context

	resolution,

	blogs,
	custom,
	documents,
	forms,
	scrolling,
	timing,
	webContents,
];
