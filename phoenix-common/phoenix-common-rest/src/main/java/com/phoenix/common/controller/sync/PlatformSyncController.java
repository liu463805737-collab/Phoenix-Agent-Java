package com.phoenix.common.controller.sync;

import com.phoenix.common.service.sync.PlatformSyncFactory;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform/sync")
@RequiredArgsConstructor
public class PlatformSyncController {

	private final PlatformSyncFactory syncFactory;

	/** 全量同步所有部门及人员 */
	@PostMapping("/all")
	public ReturnVo<String> syncAll() {
		return ReturnVo.ok(syncFactory.getStrategy().syncAll());
	}

	/** 同步所有部门数据 */
	@PostMapping("/departments")
	public ReturnVo<String> syncDepartments() {
		return ReturnVo.ok(syncFactory.getStrategy().syncDepartments());
	}

	/** 同步所有人员数据 */
	@PostMapping("/users")
	public ReturnVo<String> syncUsers() {
		return ReturnVo.ok(syncFactory.getStrategy().syncUsers());
	}

	/** 同步指定部门的下级部门 */
	@PostMapping("/depts/{deptId}")
	public ReturnVo<String> syncSubDepartments(@PathVariable String deptId) {
		return ReturnVo.ok(syncFactory.getStrategy().syncSubDepartments(deptId));
	}

	/** 同步指定部门下的人员 */
	@PostMapping("/depts/users/{deptId}")
	public ReturnVo<String> syncUsersByDept(@PathVariable String deptId) {
		return ReturnVo.ok(syncFactory.getStrategy().syncUsersByDept(deptId));
	}

	/** 根据三方用户ID同步单个人员信息 */
	@PostMapping("/users/{userId}")
	public ReturnVo<String> syncUser(@PathVariable String userId) {
		return ReturnVo.ok(syncFactory.getStrategy().syncUser(userId));
	}

}
