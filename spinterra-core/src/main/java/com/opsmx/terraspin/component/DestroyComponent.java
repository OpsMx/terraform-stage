/*
 * Copyright 2019 OpsMX, Inc.
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

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

		String currentUserDir = System.getProperty("user.home");
		String spinArtifactAccount = System.getenv("artifactAccount");
		String spinPlan = System.getenv("plan");
		String tfVariableOverrideFileRepo = System.getenv("variableOverrideFileRepo");
		String spinStateRepo = System.getenv("stateRepo");
		String uuId = System.getenv("uuId");
		String currentComponent = System.getenv("component");

		log.info("System info current user -> " + System.getProperty("user.name") + " & current dir -> "
				+ System.getProperty("user.home"));
		log.info("Given terraform module path -> " + spinPlan);
		log.info("Given artifact account name -> " + spinArtifactAccount);
		log.info("Given override file path -> " + tfVariableOverrideFileRepo);
		log.info("Given state repo -> " + spinStateRepo);
		log.info("Given unique user id -> " + uuId);
		log.info("Given current Component -> " + currentComponent);

		if (StringUtils.isEmpty(spinArtifactAccount)) {
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
		JSONObject artifactAccount = null;

		for (int i = 0; i < artifactAccounts.size(); i++) {
			artifactAccount = (JSONObject) artifactAccounts.get(i);
			String githubArtifactaccountName = (String) artifactAccount.get("accountname");
			if (StringUtils.equalsIgnoreCase(githubArtifactaccountName.trim(), spinArtifactAccount.trim()))
				break;
		}

		String artifactType = (String) artifactAccount.get("artifacttype");

		String fullPathOfCurrentArtifactProviderImplClass = "com.opsmx.terraspin.component." + artifactType.trim()
				+ "Provider";

		ArtifactProvider currentArtifactProviderObj = null;

		try {
			currentArtifactProviderObj = (ArtifactProvider) Class.forName(fullPathOfCurrentArtifactProviderImplClass)
					.newInstance();

		} catch (InstantiationException e2) {
			e2.printStackTrace();
		} catch (IllegalAccessException e2) {
			e2.printStackTrace();
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}

		currentArtifactProviderObj.envSetup(artifactAccount);

		String spinStateRepoName = currentArtifactProviderObj.getArtifactSourceReopName(spinStateRepo);
		// String staterepoDirPath = currentUserDir + "/" + spinStateRepoName;
		String staterepoDirPath = tfstatefilerepobasedir + fileSeparator + spinStateRepoName;

		// String tfVariableOverrideFileRepoName = new String();
		// String tfVariableOverrideFileName = new String();

		/*
		 * String gitUser = (String) artifactAccount.get("username"); String gittoken =
		 * (String) artifactAccount.get("token"); String gitPass = (String)
		 * artifactAccount.get("password");
		 * 
		 * if (!StringUtils.isEmpty(tfVariableOverrideFileRepo)) {
		 * tfVariableOverrideFileRepoName =
		 * tfVariableOverrideFileRepo.trim().split(".git")[0];
		 * tfVariableOverrideFileName =
		 * tfVariableOverrideFileRepo.trim().split("//")[1]; }
		 * 
		 * String checkTfFileStateRepoPresentCommand =
		 * "curl -u GITUSER:GITPASS https://api.github.com/GITUSER/REPONAME"; String
		 * tfFileStateRepoGitCloneCommand =
		 * "git clone https://GITUSER:GITPASS@github.com/GITUSER/REPONAME";
		 * 
		 * String checkTfVariableOverrideFileRepoPresentCommand =
		 * "curl -u GITUSER:GITPASS https://api.github.com/GITUSER/REPONAME"; String
		 * tfVariableOverrideFileGitCloneCommand =
		 * "git clone https://GITUSER:GITPASS@github.com/GITUSER/REPONAME";
		 * 
		 * if (StringUtils.isNoneEmpty(gitPass)) { checkTfFileStateRepoPresentCommand =
		 * checkTfFileStateRepoPresentCommand.replaceAll("GITUSER", gitUser)
		 * .replaceAll("GITPASS", gitPass).replaceAll("REPONAME", spinStateRepo);
		 * tfFileStateRepoGitCloneCommand =
		 * tfFileStateRepoGitCloneCommand.replaceAll("GITUSER", gitUser)
		 * .replaceAll("GITPASS", gitPass).replaceAll("REPONAME", spinStateRepo);
		 * 
		 * checkTfVariableOverrideFileRepoPresentCommand =
		 * checkTfVariableOverrideFileRepoPresentCommand .replaceAll("GITUSER",
		 * gitUser).replaceAll("GITPASS", gitPass) .replaceAll("REPONAME",
		 * tfVariableOverrideFileRepoName); tfVariableOverrideFileGitCloneCommand =
		 * tfVariableOverrideFileGitCloneCommand.replaceAll("GITUSER", gitUser)
		 * .replaceAll("GITPASS", gitPass).replaceAll("REPONAME",
		 * tfVariableOverrideFileRepoName);
		 * 
		 * } else { checkTfFileStateRepoPresentCommand =
		 * checkTfFileStateRepoPresentCommand.replaceAll("GITUSER", gitUser)
		 * .replaceAll("GITPASS", gittoken).replaceAll("REPONAME", spinStateRepo);
		 * tfFileStateRepoGitCloneCommand =
		 * tfFileStateRepoGitCloneCommand.replaceAll("GITUSER", gitUser)
		 * .replaceAll("GITPASS", gittoken).replaceAll("REPONAME", spinStateRepo);
		 * 
		 * checkTfVariableOverrideFileRepoPresentCommand =
		 * checkTfVariableOverrideFileRepoPresentCommand .replaceAll("GITUSER",
		 * gitUser).replaceAll("GITPASS", gittoken) .replaceAll("REPONAME",
		 * tfVariableOverrideFileRepoName); tfVariableOverrideFileGitCloneCommand =
		 * tfVariableOverrideFileGitCloneCommand.replaceAll("GITUSER", gitUser)
		 * .replaceAll("GITPASS", gittoken).replaceAll("REPONAME",
		 * tfVariableOverrideFileRepoName); }
		 */

		/*
		 * boolean isOverrideVariableFileRepoPresent = processutil
		 * .runcommand(checkTfVariableOverrideFileRepoPresentCommand);
		 * log.info("checking is variable overide file repo present :: " +
		 * isOverrideVariableFileRepoPresent);
		 * 
		 * if (isOverrideVariableFileRepoPresent &&
		 * !StringUtils.isEmpty(tfVariableOverrideFileRepo)) {
		 * 
		 * String overrideVariableFiledestination = "/home/terraspin/extra"; boolean
		 * isOverrideVariableRepoGitcloned = processutil
		 * .runcommandwithindir(tfVariableOverrideFileGitCloneCommand,
		 * overrideVariableFiledestination);
		 * log.info("is overide variable file git repo cloned :: " +
		 * isOverrideVariableRepoGitcloned);
		 * 
		 * if (isOverrideVariableRepoGitcloned) { overrideVariableFilePath =
		 * overrideVariableFiledestination + "/" + tfVariableOverrideFileRepoName + "/"
		 * + tfVariableOverrideFileName;
		 * 
		 * } else { log.info("error : " + processutil.getStatusRootObj()); } } else {
		 * log.info("error : " + processutil.getStatusRootObj()); }
		 */

		/*
		 * String overrideVariableFiledestination = "/home/terraspin/extra"; boolean
		 * isOverrideVariableRepoGitcloned =
		 * currentArtifactProviderObj.cloneOverrideFile(tfVariableOverrideFileRepoName);
		 * 
		 * 
		 * if (isOverrideVariableRepoGitcloned) { overrideVariableFilePath =
		 * overrideVariableFiledestination + "/" + tfVariableOverrideFileRepoName + "/"
		 * + tfVariableOverrideFileName;
		 * 
		 * } else {
		 * log.info("error in cloning override variable file from artifact source"); }
		 */

		/*
		 * boolean isrepopresent =
		 * processutil.runcommand(checkTfFileStateRepoPresentCommand);
		 * log.info("isrepopresent -- " + isrepopresent); if (isrepopresent &&
		 * !StringUtils.isEmpty(spinStateRepo)) { boolean isgitcloned =
		 * processutil.runcommandwithindir(tfFileStateRepoGitCloneCommand,
		 * currentUserDir); log.info("isgitcloned -- " + isgitcloned);
		 */
		boolean isStateRepoGitcloned = currentArtifactProviderObj.pullStateArtifactSource(tfstatefilerepobasedir,
				spinStateRepoName, spinStateRepo);
		if (isStateRepoGitcloned) {

			/*
			 * String source1 = "/home/terraspin/" + spinStateRepoName + "/.git"; File
			 * srcDir1 = new File(source1); String destination1 = "/home/terraspin/extra";
			 * File destDir1 = new File(destination1);
			 * 
			 * try { FileUtils.copyDirectoryToDirectory(srcDir1, destDir1); } catch
			 * (IOException e) { e.printStackTrace(); }
			 */

			// String zipfilesrc = "/home/terraspin/" + spinStateRepoName +
			// "/pipelineId-spinPipeId.zip";
			String zipfilesrc = staterepoDirPath + "/" + uuId.trim() + ".zip";

			String extrapipelineidsrc = "/home/terraspin/extra/pipelineId-spinPipeId";
			File extrapipelineidsrcdir = new File(extrapipelineidsrc);
			if (!extrapipelineidsrcdir.exists())
				extrapipelineidsrcdir.mkdir();

			try {
				ziputil.unzip(zipfilesrc, extrapipelineidsrc);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			boolean ischangemod = processutil.runcommand("chmod 777 -R ~/extra");
			log.info("changing mod of file status :: " + ischangemod);

			if (StringUtils.isEmpty(tfVariableOverrideFileRepo)) {
				terraservice.destroyStart(extrapipelineidsrc, "", null);
			} else {
				terraservice.destroyStart(extrapipelineidsrc, "", overrideVariableFilePath);
			}

			JSONObject destroystatusobj = terraservice.destroyStatus("");
			log.info("current destroystatusobj status :: " + destroystatusobj);
			String destroystatusstr = (String) destroystatusobj.get("status");

			if (StringUtils.equalsIgnoreCase("SUCCESS", destroystatusstr)) {

				currentArtifactProviderObj.pushStateArtifactSource(currentUserDir, spinStateRepoName, staterepoDirPath,
						uuId);

				/*
				 * File staterepoDir = new File(staterepoDirPath);
				 * 
				 * try { FileUtils.cleanDirectory(staterepoDir); } catch (IOException e) { //
				 * TODO Auto-generated catch block e.printStackTrace(); }
				 * 
				 * String source2 =
				 * "/home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId";
				 * File srcDir2 = new File(source2); String destination2 = staterepoDirPath;
				 * File destDir2 = new File(destination2);
				 * 
				 * try { FileUtils.copyDirectoryToDirectory(srcDir2, destDir2);
				 * System.out.println("done!!"); } catch (IOException e) { e.printStackTrace();
				 * }
				 * 
				 * String source3 = "/home/terraspin/extra/.git"; File srcDir3 = new
				 * File(source3); String destination3 = staterepoDirPath; File destDir3 = new
				 * File(destination3);
				 * 
				 * try { FileUtils.copyDirectoryToDirectory(srcDir3, destDir3); } catch
				 * (IOException e) { e.printStackTrace(); }
				 * 
				 * 
				 * //String zippath = staterepoDirPath + "/pipelineId-spinPipeId.zip"; String
				 * zippath = staterepoDirPath + "/" + uuId.trim() + ".zip"; String srczippath =
				 * staterepoDirPath + "/pipelineId-spinPipeId"; try {
				 * ziputil.zipDirectory(srczippath, zippath); } catch (IOException e1) { // TODO
				 * Auto-generated catch block e1.printStackTrace(); }
				 * 
				 * 
				 * try { FileUtils.deleteDirectory(new File(srczippath)); } catch (IOException
				 * e) { e.printStackTrace(); }
				 * 
				 * ////////////////////
				 * 
				 * String gitconfigusernamecommand = "git config --global user.name \"OpsMx\"";
				 * boolean isgitconfigusernamecommandsuccess = processutil
				 * .runcommandwithindir(gitconfigusernamecommand, staterepoDirPath);
				 * log.info("isgitconfigusernamecommandsuccess : " +
				 * isgitconfigusernamecommandsuccess);
				 * 
				 * String gitconfiguseremailcommand =
				 * "git config --global user.email \"Team@OpsMx.com\""; boolean
				 * isconfiguseremailcommandsuccess =
				 * processutil.runcommandwithindir(gitconfiguseremailcommand, staterepoDirPath);
				 * log.info("isconfiguseremailcommandsuccess : " +
				 * isconfiguseremailcommandsuccess);
				 * 
				 * String gitaddcommand = "git add ."; boolean isgitaddcommandsuccess =
				 * processutil.runcommandwithindir(gitaddcommand, staterepoDirPath);
				 * 
				 * if (isgitaddcommandsuccess) { String gitcommitcommand =
				 * "git commit -m \"adding spinterra apply state\""; // String gitcommitcommand
				 * = "git commit"; boolean isgitcommitcommandsuccess =
				 * processutil.runcommandwithindir(gitcommitcommand, staterepoDirPath);
				 * 
				 * if (isgitcommitcommandsuccess) { String gitpushcommand =
				 * "git push -u origin master"; boolean isgitpushcommandsuccess =
				 * processutil.runcommandwithindir(gitpushcommand, staterepoDirPath);
				 * 
				 * if (isgitpushcommandsuccess) { log.info("gitpushcommand got success : "); }
				 * else { log.info("isgitpushcommandnotsuccess : "); log.info("error : " +
				 * processutil.getStatusRootObj()); } } else {
				 * log.info("isgitcommitcommandnotsuccess : "); log.info("error : " +
				 * processutil.getStatusRootObj()); } } else {
				 * log.info("isgitaddcommandnotsuccess : "); log.info("error : " +
				 * processutil.getStatusRootObj()); }
				 */

			} else {
				log.info("----- error while executing spinterra destroy ------");
			}

		} else {
			log.info("error during pulling state artifact source");
		}

		/*
		 * } else { log.
		 * info("on github account repo is not present from where will pulling terraform plan state"
		 * ); log.info("error : " + processutil.getStatusRootObj()); }
		 */

	}

}