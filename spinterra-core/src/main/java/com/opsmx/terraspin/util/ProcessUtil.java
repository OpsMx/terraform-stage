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

package com.opsmx.terraspin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opsmx.terraspin.component.ApplicationStartup;

public class ProcessUtil {

	private static final Logger log = LoggerFactory.getLogger(ProcessUtil.class);
	JSONObject statusRootObj = new JSONObject();
	
	@SuppressWarnings("unchecked")
	public boolean runcommand(String command) {
		
		Process exec;
		try {
			exec = Runtime.getRuntime().exec(
					new String[] { "/bin/sh", "-c", command });
			
			exec.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
			String line = "";
			String tempLine = "";
			while ((tempLine = reader.readLine()) != null) {
				line = line + tempLine.trim() + System.lineSeparator();
			}

			BufferedReader reader2 = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
			String line2 = "";
			String tempLine2 = "";
			while ((tempLine2 = reader2.readLine()) != null) {
				line2 = line2 + tempLine2.trim() + System.lineSeparator();
			}

			reader.close();
			reader2.close();
			
			
			if (exec.exitValue() == 0) {
				statusRootObj.put("status", "SUCCESS");
				statusRootObj.put("output", line);
				setStatusRootObj(statusRootObj);
				return true;
			} else {
				statusRootObj.put("status", "TERMINAL");
				statusRootObj.put("output", line2);
				setStatusRootObj(statusRootObj);
				return false;
			}

		} catch (IOException | InterruptedException e) {
			 log.info("Error in running command "+ e.getMessage());
			 throw new RuntimeException("terraform apply execution error",e);
		}
	}
	
	
@SuppressWarnings("unchecked")
public boolean runcommandwithindir(String command, String dir) {
		
		Process exec;
		try {
			exec = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", command }, null, new File(dir) );
			exec.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
			String line = "";
			String tempLine = "";
			while ((tempLine = reader.readLine()) != null) {
				line = line + tempLine.trim() + System.lineSeparator();
			}

			BufferedReader reader2 = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
			String line2 = "";
			String tempLine2 = "";
			while ((tempLine2 = reader2.readLine()) != null) {
				line2 = line2 + tempLine2.trim() + System.lineSeparator();
			}

			reader.close();
			reader2.close();
			
			
			if (exec.exitValue() == 0) {
				statusRootObj.put("status", "SUCCESS");
				statusRootObj.put("output", line);
				setStatusRootObj(statusRootObj);
				return true;
			} else {
				statusRootObj.put("status", "TERMINAL");
				statusRootObj.put("output", line2);
				setStatusRootObj(statusRootObj);
				return false;
			}

		} catch (IOException | InterruptedException e) {
			log.info("Error in running command "+ e.getMessage());
		    throw new RuntimeException("terraform apply execution error",e);
		}
	}
	
	public JSONObject getStatusRootObj() {
		return statusRootObj;
	}
	public void setStatusRootObj(JSONObject statusRootObj) {
		this.statusRootObj = statusRootObj;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
