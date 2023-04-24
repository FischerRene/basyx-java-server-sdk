/*******************************************************************************
 * Copyright (C) 2023 the Eclipse BaSyx Authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 ******************************************************************************/

package org.eclipse.digitaltwin.basyx.submodelrepository.http;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.digitaltwin.basyx.http.serialization.BaSyxHttpTestUtils;
import org.eclipse.digitaltwin.basyx.submodelservice.DummySubmodelFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Tests the Submodel specific parts of the SubmodelRepository HTTP/REST API
 * 
 * @author schnicke, fischer
 *
 */
public class TestSubmodelRepositorySubmodelHTTP {
	private ConfigurableApplicationContext appContext;

	@Before
	public void startAASRepo() throws Exception {
		appContext = new SpringApplication(DummySubmodelRepositoryComponent.class).run(new String[] {});
	}

	@After
	public void shutdownAASRepo() {
		appContext.close();
	}

	@Test
	public void getAllSubmodelsPreconfigured() throws IOException, ParseException {
		String submodelsJSON = getAllSubmodelsJSON();
		String expectedSubmodelsJSON = getAllSubmodelJSON();
		BaSyxHttpTestUtils.assertSameJSONContent(expectedSubmodelsJSON, submodelsJSON);
	}

	@Test
	public void getSpecificSubmodel() throws ParseException, IOException {
		String submodelJSON = requestSpecificSubmodelJSON(DummySubmodelFactory.createTechnicalDataSubmodel().getId());
		String expectedSubmodelJSON = getSingleSubmodelJSON();

		BaSyxHttpTestUtils.assertSameJSONContent(expectedSubmodelJSON, submodelJSON);
	}

	@Test
	public void getSpecificSubmodelMetadata() throws ParseException, IOException {
		String contentParameter = "metadata";
		String expectedSubmodelJSON = getSingleSubmodelMetadataJSON();

		submodelContentFlagTest(contentParameter, expectedSubmodelJSON);
	}

	@Test
	public void getSpecificSubmodelValue() throws ParseException, IOException {
		String contentParameter = "value";
		String expectedSubmodelJSON = getSingleSubmodelValueJSON();

		submodelContentFlagTest(contentParameter, expectedSubmodelJSON);
	}

	@Test
	public void getSpecificSubmodelNonExisting() throws IOException {
		CloseableHttpResponse response = requestSubmodel("nonExisting");

		assertEquals(HttpStatus.NOT_FOUND.value(), response.getCode());
	}

	@Test
	public void updateExistingSubmodel() throws IOException, ParseException {
		String id = "7A7104BDAB57E184";
		String expectedSubmodelJSON = getUpdatedSubmodelJSON();

		CloseableHttpResponse creationResponse = putSubmodel(id, expectedSubmodelJSON);

		assertEquals(HttpStatus.NO_CONTENT.value(), creationResponse.getCode());

		String submodelJSON = requestSpecificSubmodelJSON(id);
		BaSyxHttpTestUtils.assertSameJSONContent(expectedSubmodelJSON, submodelJSON);
	}

	@Test
	public void updateNonExistingSubmodel() throws IOException {
		String id = "nonExisting";
		String expectedSubmodelJSON = getUpdatedSubmodelJSON();

		CloseableHttpResponse updateResponse = putSubmodel(id, expectedSubmodelJSON);

		assertEquals(HttpStatus.NOT_FOUND.value(), updateResponse.getCode());
	}

	@Test
	public void createSubmodelNewId() throws IOException, ParseException {
		String submodelJSON = getNewSubmodelJSON();
		CloseableHttpResponse creationResponse = createSubmodel(submodelJSON);

		assertSubmodelCreationReponse(submodelJSON, creationResponse);

		String requestedSubmodel = requestSpecificSubmodelJSON("newSubmodel");
		BaSyxHttpTestUtils.assertSameJSONContent(submodelJSON, requestedSubmodel);
	}

	@Test
	public void createSubmodelCollidingId() throws IOException {
		String submodelJSON = getSingleSubmodelJSON();
		CloseableHttpResponse creationResponse = createSubmodel(submodelJSON);

		assertEquals(HttpStatus.CONFLICT.value(), creationResponse.getCode());
	}

	@Test
	public void deleteSubmodel() throws IOException {
		String existingSubmodelId = DummySubmodelFactory.createTechnicalDataSubmodel().getId();

		CloseableHttpResponse deletionResponse = deleteSubmodelById(existingSubmodelId);
		assertEquals(HttpStatus.NO_CONTENT.value(), deletionResponse.getCode());

		CloseableHttpResponse getResponse = requestSubmodel(existingSubmodelId);
		assertEquals(HttpStatus.NOT_FOUND.value(), getResponse.getCode());
	}

	@Test
	public void deleteNonExistingSubmodel() throws IOException {
		CloseableHttpResponse deletionResponse = deleteSubmodelById("nonExisting");

		assertEquals(HttpStatus.NOT_FOUND.value(), deletionResponse.getCode());
	}

	private void assertSubmodelCreationReponse(String submodelJSON, CloseableHttpResponse creationResponse) throws IOException, ParseException, JsonProcessingException, JsonMappingException {
		assertEquals(HttpStatus.CREATED.value(), creationResponse.getCode());
		String response = BaSyxHttpTestUtils.getResponseAsString(creationResponse);
		BaSyxHttpTestUtils.assertSameJSONContent(submodelJSON, response);
	}

	private void submodelContentFlagTest(String contentParameter, String expectedSubmodelJSON) throws ParseException, IOException {
		String submodelJSON = requestSubmodel(DummySubmodelFactory.createTechnicalDataSubmodel().getId(), contentParameter);

		BaSyxHttpTestUtils.assertSameJSONContent(expectedSubmodelJSON, submodelJSON);

	}

	private CloseableHttpResponse createSubmodel(String submodelJSON) throws IOException {
		return BaSyxHttpTestUtils.executePostOnServer(BaSyxSubmodelHttpTestUtils.submodelAccessURL, submodelJSON);
	}

	private CloseableHttpResponse deleteSubmodelById(String submodelId) throws IOException {
		return BaSyxHttpTestUtils.executeDeleteOnServer(BaSyxSubmodelHttpTestUtils.submodelAccessURL + "/" + Base64UrlEncodedIdentifier.encodeIdentifier(submodelId));
	}

	private CloseableHttpResponse putSubmodel(String submodelId, String submodelJSON) throws IOException {
		return BaSyxHttpTestUtils.executePutOnURL(BaSyxSubmodelHttpTestUtils.getSpecificSubmodelAccessPath(submodelId), submodelJSON);
	}

	private String requestSpecificSubmodelJSON(String submodelId) throws IOException, ParseException {
		CloseableHttpResponse response = requestSubmodel(submodelId);

		return BaSyxHttpTestUtils.getResponseAsString(response);
	}

	private CloseableHttpResponse requestSubmodel(String submodelId) throws IOException {
		return BaSyxHttpTestUtils.executeGetOnURL(BaSyxSubmodelHttpTestUtils.getSpecificSubmodelAccessPath(submodelId));
	}

	private String requestSubmodel(String submodelId, String contentParameter) throws IOException, ParseException {
		CloseableHttpResponse response = requestSubmodelWithContentParameter(submodelId, contentParameter);

		return BaSyxHttpTestUtils.getResponseAsString(response);
	}

	private CloseableHttpResponse requestSubmodelWithContentParameter(String submodelId, String contentParameter) throws IOException {
		String submodelPathWithContentParameter = BaSyxSubmodelHttpTestUtils.getSpecificSubmodelAccessPath(submodelId, contentParameter);
		return BaSyxHttpTestUtils.executeGetOnURL(submodelPathWithContentParameter);
	}

	private String getAllSubmodelsJSON() throws IOException, ParseException {
		CloseableHttpResponse response = BaSyxHttpTestUtils.executeGetOnURL(BaSyxSubmodelHttpTestUtils.submodelAccessURL);

		return BaSyxHttpTestUtils.getResponseAsString(response);
	}

	private String getUpdatedSubmodelJSON() throws IOException {
		return BaSyxHttpTestUtils.readJSONStringFromFile("classpath:SingleSubmodelUpdate.json");
	}

	private String getNewSubmodelJSON() throws IOException {
		return BaSyxHttpTestUtils.readJSONStringFromFile("classpath:SingleSubmodelNew.json");
	}

	private String getSingleSubmodelJSON() throws IOException {
		return BaSyxHttpTestUtils.readJSONStringFromFile("classpath:SingleSubmodel.json");
	}

	private String getSingleSubmodelMetadataJSON() throws IOException {
		return BaSyxHttpTestUtils.readJSONStringFromFile("classpath:SingleSubmodelMetadata.json");
	}

	private String getSingleSubmodelValueJSON() throws IOException {
		return BaSyxHttpTestUtils.readJSONStringFromFile("classpath:SingleSubmodelValue.json");
	}

	private String getAllSubmodelJSON() throws IOException {
		return BaSyxHttpTestUtils.readJSONStringFromFile("classpath:MultipleSubmodels.json");
	}

}
