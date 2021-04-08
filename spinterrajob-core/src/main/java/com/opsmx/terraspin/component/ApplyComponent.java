/*
 * Copyright OpsMx, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opsmx.terraspin.component;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.opsmx.terraspin.artifact.ArtifactProvider;
import com.opsmx.terraspin.service.TerraService;
import com.opsmx.terraspin.util.ProcessUtil;
import com.opsmx.terraspin.util.TerraAppUtil;
import com.opsmx.terraspin.util.ZipUtil;

@Component
public class ApplyComponent {

	/**
	 * This method is called during Spring's startup.
	 * 
	 * @param event Event raised when an ApplicationContext gets initialized or
	 *              refreshed.
	 */


	private static final Logger log = LoggerFactory.getLogger(ApplyComponent.class);
	private static final String fileSeparator = File.separator;
	private static final String overridefilerepobasedir = "/home/terraspin/overridefilerepodir";
	private static final String tfstatefilerepobasedir = "/home/terraspin/tfstatefilerepodir";

	public void onApplicationEvent() {

		String currentUserDir = System.getProperty("user.home").toString().trim();
		String tfScriptArtifactAccount = System.getenv("tfScriptArtifactAccount").toString().trim();
		String tfStateArtifactAccount = System.getenv("tfStateArtifactAccount").toString().trim();
		String tfVariableOverrideFileRepo = System.getenv("variableOverrideFileRepo").toString().trim();
		String spinStateRepo = System.getenv("stateRepo").toString().trim();
		String uuId = System.getenv("uuId").toString().trim();
		String currentComponent = System.getenv("component").toString().trim();

		log.info("System info current user -> " + System.getProperty("user.name") + " & current dir -> "
				+ System.getProperty("user.home"));
		log.info("Given tf script artifact account name -> " + tfScriptArtifactAccount);
		log.info("Given tf state artifact account name -> " + tfStateArtifactAccount);
		log.info("Given override file path -> " + tfVariableOverrideFileRepo);
		log.info("Given state repo -> " + spinStateRepo);
		log.info("Given unique user id -> " + uuId);
		log.info("Given current Component -> " + currentComponent);

		if (StringUtils.isEmpty(tfScriptArtifactAccount)) {
			log.error("Please specify artifact account it should'nt be blank or null.");
		}

		TerraAppUtil terraapputil = new TerraAppUtil();
		TerraService terraservice = new TerraService();
		ProcessUtil processutil = new ProcessUtil();
		ZipUtil ziputil = new ZipUtil();
		JSONParser parser = new JSONParser();
		String overrideVariableFilePath = null;

		String configString = terraapputil.getConfig();
		JSONObject configObject = null;
		try {
			configObject = (JSONObject) parser.parse(configString);
		} catch (ParseException pe) {
			log.info("Exception while parsing config object :: " + configString);
			throw new RuntimeException("config Parse error:", pe);
		}

		JSONArray artifactAccounts = (JSONArray) configObject.get("artifactaccounts");
		JSONObject tfScriptAcutualArtifactAccount = null;

		for (int i = 0; i < artifactAccounts.size(); i++) {
			tfScriptAcutualArtifactAccount = (JSONObject) artifactAccounts.get(i);
			String artifactaccountName = tfScriptAcutualArtifactAccount.get("accountname").toString().trim();
			if (StringUtils.equalsIgnoreCase(artifactaccountName, tfScriptArtifactAccount))
				break;
		}

		String tfScriptArtifactType = (String) tfScriptAcutualArtifactAccount.get("artifacttype");

		String tfScriptFullPathOfCurrentArtifactProviderImplClass = "com.opsmx.terraspin.artifact." + tfScriptArtifactType.trim()
				+ "Provider";

		ArtifactProvider tfScriptCurrentArtifactProviderObj = null;

		try {
			tfScriptCurrentArtifactProviderObj = (ArtifactProvider) Class.forName(tfScriptFullPathOfCurrentArtifactProviderImplClass)
					.newInstance();

		} catch (InstantiationException e2) {
			e2.printStackTrace();
		} catch (IllegalAccessException e2) {
			e2.printStackTrace();
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}

		JSONObject tfStateAcutualArtifactAccount = null;

		for (int i = 0; i < artifactAccounts.size(); i++) {
			tfStateAcutualArtifactAccount = (JSONObject) artifactAccounts.get(i);
			String artifactaccountName = tfStateAcutualArtifactAccount.get("accountname").toString().trim();
			if (StringUtils.equalsIgnoreCase(artifactaccountName, tfStateArtifactAccount))
				break;
		}
		
		String tfStateArtifactType = tfStateAcutualArtifactAccount.get("artifacttype").toString().trim();
		
		String tfStateFullPathOfCurrentArtifactProviderImplClass = "com.opsmx.terraspin.artifact." + tfStateArtifactType	+ "Provider";

		ArtifactProvider tfStateCurrentArtifactProviderObj = null;

		try {
			tfStateCurrentArtifactProviderObj = (ArtifactProvider) Class.forName(tfStateFullPathOfCurrentArtifactProviderImplClass)
					.newInstance();

		} catch (InstantiationException e2) {
			e2.printStackTrace();
		} catch (IllegalAccessException e2) {
			e2.printStackTrace();
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}

		tfScriptCurrentArtifactProviderObj.envSetup(tfScriptAcutualArtifactAccount);
		tfStateCurrentArtifactProviderObj.envSetup(tfStateAcutualArtifactAccount);
		String spinStateRepoName = tfStateCurrentArtifactProviderObj.getArtifactSourceReopName(spinStateRepo);
		String staterepoDirPath = tfstatefilerepobasedir + fileSeparator + spinStateRepoName;
		String tfVariableOverrideFileRepoName = new String();
		String tfVariableOverrideFileName = new String();

		String opsmxdir = currentUserDir + fileSeparator + ".opsmx";
		File opsmxDirFile = new File(opsmxdir);
		if (!opsmxDirFile.exists())
			opsmxDirFile.mkdir();

		File scriptDirFile = new File(opsmxDirFile.getPath() + fileSeparator + "script");
		if (!scriptDirFile.exists())
			scriptDirFile.mkdir();

		File terraformApplySource = new File(scriptDirFile.getPath() + fileSeparator + "exeTerraformApply.sh");
		terraapputil.overWriteStreamOnFile(terraformApplySource, getClass().getClassLoader()
				.getResourceAsStream(fileSeparator + "script" + fileSeparator + "exeTerraformApply.sh"));

		File terraformOutputSource = new File(scriptDirFile.getPath() + fileSeparator + "exeTerraformOutput.sh");
		terraapputil.overWriteStreamOnFile(terraformOutputSource, getClass().getClassLoader()
				.getResourceAsStream(fileSeparator + "script" + fileSeparator + "exeTerraformOutput.sh"));

		File terraformGitOutputSource = new File(
				scriptDirFile.getPath() + fileSeparator + "exeTerraformModuleOutput.sh");
		terraapputil.overWriteStreamOnFile(terraformGitOutputSource, getClass().getClassLoader()
				.getResourceAsStream(fileSeparator + "script" + fileSeparator + "exeTerraformModuleOutput.sh"));

		if (!StringUtils.isEmpty(tfVariableOverrideFileRepo)) {
			tfVariableOverrideFileRepoName = tfScriptCurrentArtifactProviderObj
					.getArtifactSourceReopName(tfVariableOverrideFileRepo);
			tfVariableOverrideFileName = tfScriptCurrentArtifactProviderObj
					.getOverrideFileNameWithPath(tfVariableOverrideFileRepo);

			String tfVariableOverrideFileReopNameWithUsername = tfScriptCurrentArtifactProviderObj
					.getArtifactSourceReopNameWithUsername(tfVariableOverrideFileRepo);
			
			boolean isOverrideVariableRepoCloned = tfScriptCurrentArtifactProviderObj
					.cloneOverrideFile(overridefilerepobasedir, tfVariableOverrideFileReopNameWithUsername, tfScriptAcutualArtifactAccount);

			if (isOverrideVariableRepoCloned) {
				overrideVariableFilePath = overridefilerepobasedir + fileSeparator + tfVariableOverrideFileRepoName
						+ fileSeparator + tfVariableOverrideFileName;
			} else {
				log.info("error in cloning override variable file from artifact source");
			}

		}

		boolean isStateRepoCloned = tfStateCurrentArtifactProviderObj.pullStateArtifactSource(tfstatefilerepobasedir,
				spinStateRepoName, spinStateRepo, uuId, "apply", tfStateAcutualArtifactAccount);

		if (isStateRepoCloned) {

			String zipfilesrc = staterepoDirPath + "/" + uuId.trim() + ".zip";
			String extrapipelineidsrc = currentUserDir + fileSeparator + "extra" + fileSeparator + "pipelineId-spinPipeId";
			File extrapipelineidsrcdir = new File(extrapipelineidsrc);
			if (!extrapipelineidsrcdir.exists()) {
				extrapipelineidsrcdir.mkdirs();
			} else {
				try {
					FileUtils.forceDelete(extrapipelineidsrcdir);
				} catch (IOException e) {
					e.printStackTrace();
				}
				extrapipelineidsrcdir.mkdirs();
			}

			try {
				ziputil.unzip(zipfilesrc, extrapipelineidsrc);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			boolean ischangemod = processutil.runcommand("chmod 777 -R ~/extra");
			log.info("changing mod of file status :: " + ischangemod);
			log.info("changing mod of file current file  is ~/extra. is file mod change " + ischangemod);

			if (StringUtils.isEmpty(tfVariableOverrideFileRepo)) {
				terraservice.applyStart(extrapipelineidsrc, "", null);
			} else {
				terraservice.applyStart(extrapipelineidsrc, "", overrideVariableFilePath);
			}

			JSONObject applystatusobj = terraservice.applyStatus("");
			log.info("Terraform apply status obj :: " + applystatusobj);
			String applystatusstr = (String) applystatusobj.get("status");

			if (StringUtils.equalsIgnoreCase("SUCCESS", applystatusstr)) {

				tfStateCurrentArtifactProviderObj.pushStateArtifactSource(currentUserDir, spinStateRepoName, staterepoDirPath,
						uuId);
			
			} else {
				log.info("----- error while executing spinterra apply ------");
			}

		} else {
			log.info("error during pulling state artifact source");
			System.exit(101);
		}
	}

}