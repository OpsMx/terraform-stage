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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.opsmx.terraspin.artifact.ArtifactProvider;
import com.opsmx.terraspin.service.TerraService;
import com.opsmx.terraspin.util.TerraAppUtil;

@Component
public class PlanComponent {

	private static final String fileSeparator = File.separator;
	JSONParser parser = new JSONParser();
	TerraAppUtil terraapputil = new TerraAppUtil();
	TerraService terraservice = new TerraService();

	String spinApplicationName = "spinApp";
	String spinPipelineName = "spinPipe";
	String spinpiPelineId = "spinPipeId";
	String applicationName = "applicationName-" + spinApplicationName;
	String pipelineName = "pipelineName-" + spinPipelineName;
	String pipelineId = "pipelineId-" + spinpiPelineId;

	@SuppressWarnings("unchecked")
	public String onTerraspinPlan(String payload, String baseURL) {

		File currentTerraformInfraCodeDir = terraapputil.createDirForPipelineId(applicationName, pipelineName,
				pipelineId);

		try {
			FileUtils.cleanDirectory(currentTerraformInfraCodeDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

		writePlanStatus(currentTerraformInfraCodeDir, "RUNNING");

		PlanComponentThread PCThread = new PlanComponentThread(payload, currentTerraformInfraCodeDir);
		Thread thread1 = new Thread(PCThread);
		thread1.start();

		String statusPollURL = baseURL + "/api/v1/terraform/planStatus";
		JSONObject outRootObj = new JSONObject();
		outRootObj.put("status", "RUNNING");
		outRootObj.put("statusurl", statusPollURL);
		return outRootObj.toJSONString();
	}

	@SuppressWarnings("unchecked")
	void writePlanStatus(File currentTerraformInfraCodeDir, String statusstr) {

		String statusFilePath = currentTerraformInfraCodeDir + "/planStatus";
		File statusFile = new File(statusFilePath);

		if (statusFile.length() == 0) {
			JSONObject status = new JSONObject();
			status.put("status", statusstr);

			InputStream statusInputStream = new ByteArrayInputStream(
					status.toString().getBytes(StandardCharsets.UTF_8));
			terraapputil.overWriteStreamOnFile(statusFile, statusInputStream);
		} else {
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj = (JSONObject) parser.parse(new FileReader(statusFilePath));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}

			jsonObj.put("status", statusstr);
			InputStream statusInputStream = new ByteArrayInputStream(
					jsonObj.toString().getBytes(StandardCharsets.UTF_8));
			terraapputil.overWriteStreamOnFile(statusFile, statusInputStream);
		}
	}

	JSONObject getPlanStatus(File currentTerraformInfraCodeDir) {

		String statusFilePath = currentTerraformInfraCodeDir + "/planStatus";
		File statusFile = new File(statusFilePath);

		if (statusFile.length() == 0) {
			return null;
		} else {
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj = (JSONObject) parser.parse(new FileReader(statusFilePath));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return jsonObj;
		}

	}

	class PlanComponentThread implements Runnable {

		private final Logger log = LoggerFactory.getLogger(PlanComponentThread.class);
		public String payload;
		public File currentTerraformInfraCodeDir;

		public PlanComponentThread(String payload, File currentTerraformInfraCodeDir) {
			this.payload = payload;
			this.currentTerraformInfraCodeDir = currentTerraformInfraCodeDir;
		}

		@Override
		public void run() {

			JSONObject payloadJsonObject = new JSONObject();

			try {
				payloadJsonObject = (JSONObject) parser.parse(payload);
			} catch (Exception e) {
				log.info(":: Exception while parsing payload ::" + payload);
				throw new RuntimeException("Payload parse error ", e);
			}

			String tfScriptArtifactAccount = payloadJsonObject.get("tfScriptArtifactAccount").toString().trim();
			String tfStateArtifactAccount = payloadJsonObject.get("tfStateArtifactAccount").toString().trim();
			String spinPlan = payloadJsonObject.get("plan").toString().trim();
			String tfVariableOverrideFileRepo = payloadJsonObject.get("variableOverrideFileRepo").toString().trim();
			String spinStateRepo = payloadJsonObject.get("stateRepo").toString().trim();
			String uuId = payloadJsonObject.get("uuId").toString().trim();

			log.info("System info current user -> " + System.getProperty("user.name") + " & current dir -> "
					+ System.getProperty("user.home"));
			log.info("Given terraform module path -> " + spinPlan);
			log.info("Given tf script artifact account name -> " + tfScriptArtifactAccount);
			log.info("Given tf state artifact account name -> " + tfStateArtifactAccount);
			log.info("Given override file path -> " + tfVariableOverrideFileRepo);
			log.info("Given state repo -> " + spinStateRepo);
			log.info("Given unique user id -> " + uuId);
			log.info("Given current Component ->  Plan");

			if (StringUtils.isEmpty(tfScriptArtifactAccount)) {
				log.error("Please specify artifact account it should'nt be blank or null.");
			}

			if (StringUtils.isEmpty(spinPlan)) {
				log.error("Please specify terrafom plan it should'nt be blank or null.");
			}

			String currentUserDir = System.getProperty("user.home");
			String overridefilerepobasedir = currentUserDir + fileSeparator + "overridefilerepodir";
			String tfstatefilerepobasedir = currentUserDir + fileSeparator + "tfstatefilerepodir";

			File overridefilerepobasedirsrc = new File(overridefilerepobasedir);
			if (!overridefilerepobasedirsrc.exists())
				overridefilerepobasedirsrc.mkdirs();

			File tfstatefilerepobasedirsrc = new File(tfstatefilerepobasedir);
			if (!tfstatefilerepobasedirsrc.exists())
				tfstatefilerepobasedirsrc.mkdirs();

			String configString = terraapputil.getConfig();
			JSONObject configObject = null;
			try {
				configObject = (JSONObject) parser.parse(configString);
			} catch (ParseException pe) {
				log.info("Exception while parsing application config object :: " + configString);
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

			String tfScriptArtifactType = tfScriptAcutualArtifactAccount.get("artifacttype").toString().trim();

			String tfScriptFullPathOfCurrentArtifactProviderImplClass = "com.opsmx.terraspin.artifact." + tfScriptArtifactType
					+ "Provider";

			ArtifactProvider tfScriptCurrentArtifactProviderObj = null;

			try {
				tfScriptCurrentArtifactProviderObj = (ArtifactProvider) Class
						.forName(tfScriptFullPathOfCurrentArtifactProviderImplClass).newInstance();

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

			File terraformInitSource = new File(scriptDirFile.getPath() + fileSeparator + "exeTerraformInit.sh");
			terraapputil.overWriteStreamOnFile(terraformInitSource, getClass().getClassLoader()
					.getResourceAsStream(fileSeparator + "script" + fileSeparator + "exeTerraformInit.sh"));

			File terraformPlanSource = new File(scriptDirFile.getPath() + fileSeparator + "exeTerraformPlan.sh");
			terraapputil.overWriteStreamOnFile(terraformPlanSource, getClass().getClassLoader()
					.getResourceAsStream(fileSeparator + "script" + fileSeparator + "exeTerraformPlan.sh"));

			if (!StringUtils.isEmpty(tfVariableOverrideFileRepo)) {
				tfVariableOverrideFileRepoName = tfScriptCurrentArtifactProviderObj
						.getArtifactSourceReopName(tfVariableOverrideFileRepo);
				tfVariableOverrideFileName = tfScriptCurrentArtifactProviderObj
						.getOverrideFileNameWithPath(tfVariableOverrideFileRepo);
			}

			if (!tfScriptAcutualArtifactAccount.isEmpty()) {

				if (StringUtils.isEmpty(tfVariableOverrideFileRepo)) {
					log.info("Terraform plan start without override file ");
					terraservice.planStart(tfScriptAcutualArtifactAccount, null, spinPlan, tfScriptArtifactAccount, tfScriptArtifactType);
				} else {
					log.info("Terraform plan start with override file ");

					String tfVariableOverrideFileReopNameWithUsername = tfScriptCurrentArtifactProviderObj
							.getArtifactSourceReopNameWithUsername(tfVariableOverrideFileRepo);

					boolean isOverrideVariableRepoCloned = tfScriptCurrentArtifactProviderObj
							.cloneOverrideFile(overridefilerepobasedir, tfVariableOverrideFileReopNameWithUsername, tfScriptAcutualArtifactAccount);
					if (isOverrideVariableRepoCloned) {
						String overrideVariableFilePath = overridefilerepobasedir + fileSeparator
								+ tfVariableOverrideFileRepoName + fileSeparator + tfVariableOverrideFileName;
						terraservice.planStart(tfScriptAcutualArtifactAccount, overrideVariableFilePath, spinPlan, tfScriptArtifactAccount,
								tfScriptArtifactType);

					} else {
						log.info("error in cloning override variable file from artifact source");
					}
				}

				JSONObject planstatusobj = getPlanStatus(currentTerraformInfraCodeDir);
				log.info("----- current plan status :: " + planstatusobj);
				String planstatusstr = (String) planstatusobj.get("planstatus");

				if (StringUtils.equalsIgnoreCase("SUCCESS", planstatusstr)) {
					boolean isStateRepoCloned = tfStateCurrentArtifactProviderObj.pullStateArtifactSource(
							tfstatefilerepobasedir, spinStateRepoName, spinStateRepo, uuId, "plan", tfStateAcutualArtifactAccount);

					if (isStateRepoCloned) {
						tfStateCurrentArtifactProviderObj.pushStateArtifactSource(currentUserDir, spinStateRepoName,
								staterepoDirPath, uuId);

					} else {
						log.info("error during pulling state artifact source");
						writePlanStatus(currentTerraformInfraCodeDir, "TERMINAL");
					}

					writePlanStatus(currentTerraformInfraCodeDir, "SUCCESS");

				} else {
					log.info("error during execution of terraform init and plan");
					writePlanStatus(currentTerraformInfraCodeDir, "TERMINAL");
				}

			} else {

				log.error("Please specify artifact account it should'nt be blank or null.");
			}

		}
	}

}