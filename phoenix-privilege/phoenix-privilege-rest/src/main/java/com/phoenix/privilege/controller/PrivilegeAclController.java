package com.phoenix.privilege.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.phoenix.privilege.dto.PrivilegeAclDTO;
import com.phoenix.privilege.entity.PrivilegeAcl;
import com.phoenix.privilege.service.IPrivilegeAclService;
import com.phoenix.privilege.vo.PrivilegeAclVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/privilege/acl")
@RequiredArgsConstructor
public class PrivilegeAclController {

	private final IPrivilegeAclService privilegeAclService;

	@GetMapping("/page")
	public ReturnVo<Page<PrivilegeAclVO>> page(@RequestParam(defaultValue = "1") long page,
			@RequestParam(defaultValue = "10") long size, PrivilegeAclDTO dto) {
		QueryWrapper qw = QueryWrapper.create()
			.eq(PrivilegeAcl::getReleaseId, dto.getReleaseId(), StrUtil.isNotBlank(dto.getReleaseId()))
			.eq(PrivilegeAcl::getReleaseSn, dto.getReleaseSn(), StrUtil.isNotBlank(dto.getReleaseSn()))
			.eq(PrivilegeAcl::getModuleId, dto.getModuleId(), StrUtil.isNotBlank(dto.getModuleId()))
			.eq(PrivilegeAcl::getSystemSn, dto.getSystemSn(), StrUtil.isNotBlank(dto.getSystemSn()))
			.orderBy(PrivilegeAcl::getCreateTime, false);
		Page<PrivilegeAcl> entityPage = privilegeAclService.page(new Page<>(page, size), qw);
		Page<PrivilegeAclVO> voPage = new Page<>(entityPage.getPageNumber(), entityPage.getPageSize(),
				entityPage.getTotalRow());
		voPage.setRecords(
				entityPage.getRecords().stream().map(e -> BeanUtil.copyProperties(e, PrivilegeAclVO.class)).toList());
		return ReturnVo.ok(voPage);
	}

	@GetMapping("/{id}")
	public ReturnVo<PrivilegeAclVO> getById(@PathVariable String id) {
		return ReturnVo.ok(BeanUtil.copyProperties(privilegeAclService.getById(id), PrivilegeAclVO.class));
	}

	@GetMapping("/release/{releaseId}")
	public ReturnVo<List<PrivilegeAclVO>> getByReleaseId(@PathVariable String releaseId) {
		List<PrivilegeAclVO> list = privilegeAclService.getByReleaseId(releaseId)
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeAclVO.class))
			.toList();
		return ReturnVo.ok(list);
	}

	@GetMapping("/release/module/{releaseId}/{moduleId}")
	public ReturnVo<PrivilegeAclVO> getByReleaseIdAndModuleId(@PathVariable String releaseId,
			@PathVariable String moduleId) {
		return ReturnVo.ok(BeanUtil.copyProperties(privilegeAclService.getByReleaseIdAndModuleId(releaseId, moduleId),
				PrivilegeAclVO.class));
	}

	@PutMapping
	public ReturnVo<Boolean> update(@RequestBody PrivilegeAclDTO dto) {
		PrivilegeAcl entity = dto.toEntity();
		PrivilegeAcl existing = privilegeAclService.getByReleaseIdAndModuleId(entity.getReleaseId(),
				entity.getModuleId());
		if (existing != null) {
			entity.setId(existing.getId());
			return ReturnVo.ok(privilegeAclService.updateById(entity));
		}
		return ReturnVo.ok(privilegeAclService.save(entity));
	}

	@DeleteMapping("/{id}")
	public ReturnVo<Boolean> delete(@PathVariable String id) {
		return ReturnVo.ok(privilegeAclService.deleteAcl(id));
	}

	@PostMapping("/saveAll/{releaseId}/{checkStatus}")
	public ReturnVo<Void> saveAllAcl(@PathVariable String releaseId, @PathVariable(required = false) Boolean checkStatus) {
		privilegeAclService.saveAllAcl(releaseId,checkStatus);
		return ReturnVo.ok();
	}

	@PostMapping("/saveModule")
	public ReturnVo<Void> saveModuleAcl(@RequestBody PrivilegeAclDTO dto) {
		privilegeAclService.saveModuleAcl(dto);
		return ReturnVo.ok();
	}
}
