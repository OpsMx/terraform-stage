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
import org.springframework.stereotype.Component;

import com.opsmx.terraspin.artifact.ArtifactProvider;
import com.opsmx.terraspin.service.TerraService;
import com.opsmx.terraspin.util.ProcessUtil;
import com.opsmx.terraspin.util.TerraAppUtil;
import com.opsmx.terraspin.util.ZipUtil;

@Component
public class DestroyComponent {

	/**
	 * This method is called during Spring's startup.
	 * 
	 * @param event Event raised when an ApplicationContext gets initialized or
	 *              refreshed.
	 */
	private static final Logger log = LoggerFactory.getLogger(DestroyComponent.class);
	private static final String fileSeparator = File.separator;
	private static final String tfstatefilerepobasedir = "/home/terraspin/tfstatefilerepodir";

	public void onApplicationEvent() {

		String currentUserDir = System.getProperty("user.home").toString().trim();
		String tfStateArtifactAccount = System.getenv("tfStateArtifactAccount").toString().trim();
		String spinStateRepo = System.getenv("stateRepo").toString().trim();
		String uuId = System.getenv("uuId").toString().trim();
		String currentComponent = System.getenv("component").toString().trim();

		log.info("System info current user -> " + System.getProperty("user.name") + " & current dir -> "
				+ System.getProperty("user.home"));
		log.info("Given tf state artifact account name -> " + tfStateArtifactAccount);
		log.info("Given state repo -> " + spinStateRepo);
		log.info("Given unique user id -> " + uuId);
		log.info("Given current Component -> " + currentComponent);

		if (StringUtils.isEmpty(tfStateArtifactAccount)) {
			log.error("Please specify artifact account it should'nt be blank or null.");
		}

		TerraAppUtil terraapputil = new TerraAppUtil();
		TerraService terraservice = new TerraService();
		ProcessUtil processutil = new ProcessUtil();
		ZipUtil ziputil = new ZipUtil();
		JSONParser parser = new JSONParser();
		String overrideVariableFilePath = null;

		String opsmxdir = currentUserDir + fileSeparator + ".opsmx";
		File opsmxDirFile = new File(opsmxdir);
		if (!opsmxDirFile.exists())
			opsmxDirFile.mkdir();

		File scriptDirFile = new File(opsmxDirFile.getPath() + fileSeparator + "script");
		if (!scriptDirFile.exists())
			scriptDirFile.mkdir();

		File terraformDestroySource = new File(scriptDirFile.getPath() + fileSeparator + "exeTerraformDestroy.sh");
		terraapputil.overWriteStreamOnFile(terraformDestroySource, getClass().getClassLoader()
				.getResourceAsStream(fileSeparator + "script" + fileSeparator + "exeTerraformDestroy.sh"));

		String configString = terraapputil.getConfig();
		JSONObject configObject = null;
		try {
			configObject = (JSONObject) parser.parse(configString);
		} catch (ParseException pe) {
			log.info("Exception while parsing config object :: " + configString);
			throw new RuntimeException("config Parse error:", pe);
		}
		JSONArray artifactAccounts = (JSONArray) configObject.get("artifactaccounts");
		JSONObject tfStateAcutualArtifactAccount = null;

		for (int i = 0; i < artifactAccounts.size(); i++) {
			tfStateAcutualArtifactAccount = (JSONObject) artifactAccounts.get(i);
			String githubArtifactaccountName = tfStateAcutualArtifactAccount.get("accountname").toString().trim();
			if (StringUtils.equalsIgnoreCase(githubArtifactaccountName, tfStateArtifactAccount))
				break;
		}

		String artifactType = tfStateAcutualArtifactAccount.get("artifacttype").toString().trim();

		String fullPathOfCurrentArtifactProviderImplClass = "com.opsmx.terraspin.artifact." + artifactType + "Provider";

		ArtifactProvider tfStateCurrentArtifactProviderObj = null;

		try {
			tfStateCurrentArtifactProviderObj = (ArtifactProvider) Class.forName(fullPathOfCurrentArtifactProviderImplClass)
					.newInstance();

		} catch (InstantiationException e2) {
			e2.printStackTrace();
		} catch (IllegalAccessException e2) {
			e2.printStackTrace();
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}

		tfStateCurrentArtifactProviderObj.envSetup(tfStateAcutualArtifactAccount);

		String spinStateRepoName = tfStateCurrentArtifactProviderObj.getArtifactSourceReopName(spinStateRepo);
		String staterepoDirPath = tfstatefilerepobasedir + fileSeparator + spinStateRepoName;

		boolean isStateRepoCloned = tfStateCurrentArtifactProviderObj.pullStateArtifactSource(tfstatefilerepobasedir,
				spinStateRepoName, spinStateRepo, uuId, "destroy", tfStateAcutualArtifactAccount);
		if (isStateRepoCloned) {

			String zipfilesrc = staterepoDirPath + fileSeparator + uuId.trim() + ".zip";
			String extrapipelineidsrc = currentUserDir + fileSeparator + "extra" + fileSeparator
					+ "pipelineId-spinPipeId";

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

			terraservice.destroyStart(extrapipelineidsrc, "", overrideVariableFilePath);

			JSONObject destroystatusobj = terraservice.destroyStatus("");
			log.info("current destroystatusobj status :: " + destroystatusobj);
			String destroystatusstr = (String) destroystatusobj.get("status");

			if (StringUtils.equalsIgnoreCase("SUCCESS", destroystatusstr)) {

				tfStateCurrentArtifactProviderObj.pushStateArtifactSource(currentUserDir, spinStateRepoName, staterepoDirPath,
						uuId);

			} else {
				log.info("----- error while executing spinterra destroy ------");
			}

		} else {
			log.info("error during pulling state artifact source");
			System.exit(101);
		}
	}

}