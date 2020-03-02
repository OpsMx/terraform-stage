package com.opsmx.terraspin.util;

public class TestUtil {

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

		String currentUserDir = System.getProperty("user.home");
		System.out.println("---" + currentUserDir);

		ProcessUtil processutil = new ProcessUtil();
		String gitclonecommand = "git clone https://lalitv92:Viui59skranmnmnntewm92!@github.com/lalitv92/trytest.git";
		String dir = "/home/opsmx/lalit/work/opsmx/extrathing/gittest/test2";

		boolean isgitcloned = processutil.runcommandwithindir(gitclonecommand, "/home/opsmx");
		// boolean isgitcloned = processutil.runcommand(gitclonecommand);
		System.out.println("-----" + processutil.getStatusRootObj().toString());

	}

}
