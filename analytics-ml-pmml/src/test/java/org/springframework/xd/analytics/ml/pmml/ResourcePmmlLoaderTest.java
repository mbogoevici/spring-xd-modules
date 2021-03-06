/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.xd.analytics.ml.pmml;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.dmg.pmml.PMML;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Thomas Darimont
 */
public class ResourcePmmlLoaderTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	PmmlLoader loader = new ResourcePmmlLoader();

	/**
	 * @see XD-1420
	 */
	@Test
	public void testLoadPmml() throws Exception {

		PMML pmml = loader.loadPmml("classpath:analytics/pmml/multiple-models.pmml.xml");

		assertThat(pmml, is(notNullValue()));
		assertThat(pmml.getModels(),hasSize(2));
		assertThat(pmml.getModels().get(0).getModelName(),is("KMeans_Model1"));
	}

	/**
	 * @see XD-1420
	 */
	@Test
	public void testLoadPmmlShouldThrowExceptionForUnknownPmmlLocation() throws Exception {

		expectedException.expect(RuntimeException.class);
		expectedException.expectMessage("FileNotFound");
		expectedException.expectMessage("UNKNOWN.pmml.xml");

		loader.loadPmml("classpath:analytics/pmml/UNKNOWN.pmml.xml");
	}

	/**
	 * @see XD-1420
	 */
	@Test
	public void testLoadPmmlShouldNotAllowNullModelLocation() throws Exception {

		expectedException.expect(RuntimeException.class);
		expectedException.expectMessage("modelLocation");

		loader.loadPmml(null);
	}
}
