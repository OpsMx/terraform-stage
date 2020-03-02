package com.opsmx.terraspin.component;

import org.json.simple.JSONObject;

public abstract class ArtifactProvider {
	
	abstract void envSetup(JSONObject artifactAccount);

	abstract String getArtifactSourceReopName (String artifactSourceReopPath);
	
	abstract String getArtifactSourceReopNameWithUsername (String artifactSourceReopPath);
	
	abstract String getOverrideFileNameWithPath (String tfVariableOverrideFileRepo);
	
	abstract boolean cloneOverrideFile (String cloneDir, String tfVariableOverrideFileRepoName);
	
	abstract void pushStateArtifactSource(String currentUserDir, String spinStateRepoName, String staterepoDirPath, String uuId);
	
	abstract boolean pullStateArtifactSource(String cloneDir, String spinStateRepoName, String spinStateRepoNameWithUserName);

}
