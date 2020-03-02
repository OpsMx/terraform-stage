package com.opsmx.terraspin.component;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opsmx.terraspin.util.ProcessUtil;
import com.opsmx.terraspin.util.TerraAppUtil;
import com.opsmx.terraspin.util.ZipUtil;

public class GithubProvider extends ArtifactProvider {

	private static final Logger log = LoggerFactory.getLogger(PlanComponent.class);
	private static final String fileSeparator = File.separator;
	private static final String currentComponent = System.getenv("component");
	private static final ProcessUtil processutil = new ProcessUtil();
	private static final TerraAppUtil terraapputil = new TerraAppUtil();
	private static final ZipUtil ziputil = new ZipUtil();

	@Override
	void envSetup(JSONObject artifactAccount) {

		log.info("started env setup of github");
		String githubEnvUnhydratedCredentailsStr = "https://USER:PASS@github.com";
		String user = (String) artifactAccount.get("username");
		String pass = (String) artifactAccount.get("password");
		String githubEnvHydratedCredentailsStr = githubEnvUnhydratedCredentailsStr.replaceAll("USER", user)
				.replaceAll("PASS", pass);
		String currentUserDir = System.getProperty("user.home");

		File gitCredentailFileSource = new File(currentUserDir + fileSeparator + ".git-credentials");
		terraapputil.overWriteStreamOnFile(gitCredentailFileSource,
				IOUtils.toInputStream(githubEnvHydratedCredentailsStr));

		boolean ischangemod = processutil.runcommand("chmod 777 -R " + gitCredentailFileSource.getAbsolutePath());
		log.info("changing mod of file current file is " + gitCredentailFileSource.getAbsolutePath()
				+ " is file mod change " + ischangemod + " and process obj status " + processutil.getStatusRootObj());

		/*
		 * String githttppostBuffercommand =
		 * "git config --global http.postBuffer 1048576000"; boolean
		 * isgithttppostBuffercommandsuccess =
		 * processutil.runcommand(githttppostBuffercommand);
		 * log.info("isgithttppostBuffer command got success :: " +
		 * isgithttppostBuffercommandsuccess);
		 * 
		 * String githttpsslVerifycommand = "git config --global http.sslVerify false";
		 * boolean isgitgithttpsslVerifycommandsuccess =
		 * processutil.runcommand(githttpsslVerifycommand);
		 * log.info("isgithttpsslVerify command got success :: " +
		 * isgitgithttpsslVerifycommandsuccess);
		 *
		 *
		 * git config --global core.compression 0
		 */

		String gitconfigusernamecommand = "git config --global user.name \"lalit\"";
		boolean isgitconfigusernamecommandsuccess = processutil.runcommand(gitconfigusernamecommand);
		log.info("isgitconfigusername command got success " + isgitconfigusernamecommandsuccess
				+ " and process obj status " + processutil.getStatusRootObj());
		// log.info("error :: " + processutil.getStatusRootObj());

		String gitconfiguseremailcommand = "git config --global user.email \"lalit@opsmx.io\"";
		boolean isconfiguseremailcommandsuccess = processutil.runcommand(gitconfiguseremailcommand);
		log.info("isconfiguseremailcommand got success :: " + isconfiguseremailcommandsuccess
				+ " and process obj status " + processutil.getStatusRootObj());
		// log.info("error :: " + processutil.getStatusRootObj());

		String gitcredentialHelpercommand = "git config --global credential.helper store";
		boolean isgitcredentialHelpercommandsuccess = processutil.runcommand(gitcredentialHelpercommand);
		log.info("isgitcredentialHelpercommandsuccess got success " + isgitcredentialHelpercommandsuccess
				+ " and process obj status " + processutil.getStatusRootObj());

		log.info("finish env setup of github");
	}

	@Override
	String getArtifactSourceReopName(String terraSpinStateRepoPath) {
		String spinStateRepoNameWithUserName = terraSpinStateRepoPath.trim().split(".git")[0];
		String spinStateRepoName = spinStateRepoNameWithUserName.trim().split("/")[1];
		return spinStateRepoName;
	}

	String getArtifactSourceReopNameWithUsername(String terraSpinStateRepoPath) {
		String spinStateRepoNameWithUserName = terraSpinStateRepoPath.trim().split(".git")[0];
		return spinStateRepoNameWithUserName;
	}

	@Override
	String getOverrideFileNameWithPath(String tfVariableOverrideFileRepo) {
		String VariableOverrideFilePath = tfVariableOverrideFileRepo.trim().split("//")[1];
		return VariableOverrideFilePath;
	}

	@Override
	boolean cloneOverrideFile(String cloneDir, String tfVariableOverrideFileReopNameWithUsername) {
		log.info("cloneOverrideFile repo name with user name -> " + tfVariableOverrideFileReopNameWithUsername);
		String githubOverrideFileRepoCloneCommand = "git clone https://github.com/REPONAME";
		githubOverrideFileRepoCloneCommand = githubOverrideFileRepoCloneCommand.replaceAll("REPONAME",
				tfVariableOverrideFileReopNameWithUsername);
		boolean isOverrideVariableRepoGitcloned = processutil.runcommandwithindir(githubOverrideFileRepoCloneCommand,
				cloneDir);

		if (isOverrideVariableRepoGitcloned) {
			log.info("is override variable file git repo cloned " + isOverrideVariableRepoGitcloned
					+ " and process obj status " + processutil.getStatusRootObj());
			return isOverrideVariableRepoGitcloned;
		} else {
			log.info("error in cloning override variable file" + " and process obj status "
					+ processutil.getStatusRootObj());
			return isOverrideVariableRepoGitcloned;
		}
	}

	@Override
	void pushStateArtifactSource(String currentUserDir, String spinStateRepoName, String staterepoDirPath,
			String uuId) {

		String source2 = "/home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId";
		File srcDir2 = new File(source2);
		String destination2 = staterepoDirPath;
		File destDir2 = new File(destination2);

		try {
			FileUtils.copyDirectoryToDirectory(srcDir2, destDir2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String zippath = staterepoDirPath + "/" + uuId.trim() + ".zip";
		String srczippath = staterepoDirPath + "/pipelineId-spinPipeId";

		File zippathfile = new File(zippath);
		zippathfile.delete();

		try {
			ziputil.zipDirectory(srczippath, zippath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			FileUtils.deleteDirectory(new File(srczippath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * String gitlfsinstallcommand = "git lfs install"; boolean
		 * isgitlfsinstallcommandsuccess =
		 * processutil.runcommandwithindir(gitlfsinstallcommand, staterepoDirPath);
		 * log.info("isgitlfsinstallcommandsuccess got success :: " +
		 * isgitlfsinstallcommandsuccess + " and process obj status " +
		 * processutil.getStatusRootObj());
		 * 
		 * //String gitlfsHelpercommand = "git lfs track '" + staterepoDirPath +
		 * "/.terraform/plugins/'"; String gitlfsHelpercommand =
		 * "git lfs track '*.zip'"; boolean isgitlfsHelpercommandsuccess =
		 * processutil.runcommandwithindir(gitlfsHelpercommand, staterepoDirPath);
		 * log.info("isgitlfsHelpercommandsuccess got success :: " +
		 * isgitlfsHelpercommandsuccess + " and process obj status " +
		 * processutil.getStatusRootObj());
		 */

		String gitaddcommand = "git add .";
		boolean isgitaddcommandsuccess = processutil.runcommandwithindir(gitaddcommand, staterepoDirPath);
		log.info("is git add command got success :: " + isgitaddcommandsuccess);

		if (isgitaddcommandsuccess) {
			String gitcommitcommand = "git commit -m \"adding spinterra " + currentComponent + " state\"";
			boolean isgitcommitcommandsuccess = processutil.runcommandwithindir(gitcommitcommand, staterepoDirPath);
			log.info("isgitcommit command got success " + isgitcommitcommandsuccess + " and process obj status "
					+ processutil.getStatusRootObj());

			if (isgitcommitcommandsuccess) {
				String gitpushcommand = "git push -u origin master";
				boolean isgitpushcommandsuccess = processutil.runcommandwithindir(gitpushcommand, staterepoDirPath);

				if (isgitpushcommandsuccess) {
					log.info("gitpushcommand got success " + isgitpushcommandsuccess + " and process obj status "
							+ processutil.getStatusRootObj());
				} else {
					log.info("error : " + processutil.getStatusRootObj());
				}
			} else {
				log.info("error : " + processutil.getStatusRootObj());
			}
		} else {
			log.info("error : " + processutil.getStatusRootObj());
		}
	}

	@Override
	boolean pullStateArtifactSource(String cloneDir, String spinStateRepoName, String spinStateRepoNameWithUserName) {

		log.info("Repo name -> " + spinStateRepoName + " repo name with user name -> " + spinStateRepoNameWithUserName);
		log.info("cloning dir path " + cloneDir);
		String githubtfStateRepoCloneCommand = "git clone https://github.com/REPONAME";
		githubtfStateRepoCloneCommand = githubtfStateRepoCloneCommand.replaceAll("REPONAME",
				spinStateRepoNameWithUserName);
		boolean isStateRepoGitcloned = processutil.runcommandwithindir(githubtfStateRepoCloneCommand, cloneDir);
		log.info("is current repo : " + spinStateRepoNameWithUserName + ". is cloned sucessfully : "
				+ isStateRepoGitcloned + " and process obj status " + processutil.getStatusRootObj());

		if (isStateRepoGitcloned) {

			String changefilecommand = "chmod 777 -R " + cloneDir + fileSeparator + spinStateRepoName;
			boolean ischangemod = processutil.runcommand(changefilecommand);
			log.info("changing file mod command " + changefilecommand + " is command got success " + ischangemod
					+ " and process obj status " + processutil.getStatusRootObj());
			log.info("sucessfully cloned current repo " + spinStateRepoNameWithUserName);
			return isStateRepoGitcloned;
		} else {
			log.info("error in clone this repo " + spinStateRepoNameWithUserName + " process error obj"
					+ processutil.getStatusRootObj());
			return isStateRepoGitcloned;
		}
	}

}
