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

package com.opsmx.terraspin.util;

import java.io.File;

import org.apache.commons.io.IOUtils;

public class TestUtil {
	private static final TerraAppUtil terraapputil = new TerraAppUtil();

	void timec() {
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestUtil tu = new TestUtil();
		/*
		 * String MYREGION = "abregion"; String MYACCESSKEY = "abaccess" ; String
		 * MYSECRETKEY = "absecret"; String providerConfig = "provider \"aws\"" +
		 * "{ region = \"" + MYREGION + "\"" + "\n access_key = \"" + MYACCESSKEY + "\""
		 * + "\n secret_key = \"" + MYSECRETKEY + "\"  }";
		 * 
		 * System.out.println("hey :" + providerConfig);
		 */

		//String currentUserDir = System.getProperty("user.home");
		//System.out.println("---" + currentUserDir);

		//ProcessUtil processutil = new ProcessUtil();
		//String gitclonecommand = "git clone https://lalitv92:Viui59skranmnmnntewm92!@github.com/lalitv92/trytest.git";
		//String dir = "/home/opsmx/lalit/work/opsmx/extrathing/gittest/test2";

		//boolean isgitcloned = processutil.runcommandwithindir(gitclonecommand, "/home/opsmx");
		// boolean isgitcloned = processutil.runcommand(gitclonecommand);
		//System.out.println("-----" + processutil.getStatusRootObj().toString());
		
		
		String credHelper = "[credential] \n        helper = store";
		System.out.println(credHelper);

		File gitCredentailFileSource = new File("/home/opsmx/lalit/work/opsmx/extra/.gitconfig");
		terraapputil.overWriteStreamOnFile(gitCredentailFileSource,
				IOUtils.toInputStream(credHelper));
		
		String gitsourceurl = "scm/cdwor/terraform-planmodules.git//aws/eks?ref=feature/staging-platform";



	}

}
