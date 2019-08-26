/**
 * Copyright gradecak.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradecak.alfresco.mvc.webscript;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

import org.springframework.util.Assert;

/**
 * this is a copy of the srpingframework
 * {@link org.springframework.mock.web.DelegatingServletOutputStream}
 */
public class DelegatingServletOutputStream extends ServletOutputStream {

	private final OutputStream targetStream;

	public DelegatingServletOutputStream(OutputStream targetStream) {
		Assert.notNull(targetStream, "Target OutputStream must not be null");
		this.targetStream = targetStream;
	}

	public final OutputStream getTargetStream() {
		return this.targetStream;
	}

	@Override
	public void write(int b) throws IOException {
		this.targetStream.write(b);
	}

	@Override
	public void flush() throws IOException {
		super.flush();
		this.targetStream.flush();
	}

	@Override
	public void close() throws IOException {
		super.close();
		this.targetStream.close();
	}

	// @Override
	// public boolean isReady() {
	// return true;
	// }
	//
	// @Override
	// public void setWriteListener(WriteListener writeListener) {
	// throw new UnsupportedOperationException();
	// }

}
