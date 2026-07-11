package com.phoenix.privilege.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.phoenix.privilege.dto.PrivilegeLoginLogDTO;
import com.phoenix.privilege.entity.PrivilegeLoginLog;
import com.phoenix.privilege.service.IPrivilegeLoginLogService;
import com.phoenix.privilege.vo.PrivilegeLoginLogVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/privilege/login-log")
@RequiredArgsConstructor
public class PrivilegeLoginLogController {

	private final IPrivilegeLoginLogService privilegeLoginLogService;

	@GetMapping("/page")
	public ReturnVo<Page<PrivilegeLoginLogVO>> page(@RequestParam(defaultValue = "1") long page,
			@RequestParam(defaultValue = "10") long size, PrivilegeLoginLogDTO dto) {
		QueryWrapper qw = QueryWrapper.create()
			.eq(PrivilegeLoginLog::getOperationId, dto.getOperationId(), dto.getOperationId() != null)
			.like(PrivilegeLoginLog::getOperationPerson, dto.getOperationPerson(),
					StrUtil.isNotBlank(dto.getOperationPerson()))
			.orderBy(PrivilegeLoginLog::getCreateTime, false);
		Page<PrivilegeLoginLog> entityPage = privilegeLoginLogService.page(new Page<>(page, size), qw);
		Page<PrivilegeLoginLogVO> voPage = new Page<>(entityPage.getPageNumber(), entityPage.getPageSize(),
				entityPage.getTotalRow());
		voPage.setRecords(entityPage.getRecords()
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeLoginLogVO.class))
			.toList());
		return ReturnVo.ok(voPage);
	}

	@GetMapping("/{id}")
	public ReturnVo<PrivilegeLoginLogVO> getById(@PathVariable Long id) {
		return ReturnVo.ok(BeanUtil.copyProperties(privilegeLoginLogService.getById(id), PrivilegeLoginLogVO.class));
	}

	@PostMapping
	public ReturnVo<Boolean> save(@RequestBody PrivilegeLoginLogDTO dto) {
		return ReturnVo.ok(privilegeLoginLogService.save(dto.toEntity()));
	}

	@DeleteMapping("/{id}")
	public ReturnVo<Boolean> delete(@PathVariable Long id) {
		return ReturnVo.ok(privilegeLoginLogService.removeById(id));
	}

}
