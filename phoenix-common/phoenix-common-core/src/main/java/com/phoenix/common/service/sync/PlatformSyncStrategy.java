package com.phoenix.common.service.sync;

public interface PlatformSyncStrategy {

	String syncAll();

	String syncDepartments();

	String syncUsers();

	String syncSubDepartments(String deptId);

	String syncUsersByDept(String deptId);

	String syncUser(String thirdPartyId);

}
