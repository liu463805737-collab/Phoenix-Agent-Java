package com.phoenix.privilege.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.phoenix.privilege.dto.PrivilegeCompanyDTO;
import com.phoenix.privilege.entity.PrivilegeCompany;
import com.phoenix.privilege.service.IPrivilegeCompanyService;
import com.phoenix.privilege.vo.PrivilegeCompanyVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;

@RestController
@RequestMapping("/api/privilege/company")
@RequiredArgsConstructor
public class PrivilegeCompanyController {

	private final IPrivilegeCompanyService privilegeCompanyService;

	@GetMapping("/page")
	public ReturnVo<Page<PrivilegeCompanyVO>> page(@RequestParam(defaultValue = "1") long page,
			@RequestParam(defaultValue = "10") long size, PrivilegeCompanyDTO dto) {
		QueryWrapper qw = QueryWrapper.create()
			.like(PrivilegeCompany::getCname, dto.getCname(), StrUtil.isNotBlank(dto.getCname()))
			.like(PrivilegeCompany::getShortName, dto.getShortName(), StrUtil.isNotBlank(dto.getShortName()))
			.eq(PrivilegeCompany::getCode, dto.getCode(), StrUtil.isNotBlank(dto.getCode()))
			.eq(PrivilegeCompany::getStatus, dto.getStatus(), dto.getStatus() != null)
			.orderBy(PrivilegeCompany::getCreateTime, false);
		Page<PrivilegeCompany> entityPage = privilegeCompanyService.page(new Page<>(page, size), qw);
		Page<PrivilegeCompanyVO> voPage = new Page<>(entityPage.getPageNumber(), entityPage.getPageSize(),
				entityPage.getTotalRow());
		voPage.setRecords(entityPage.getRecords()
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeCompanyVO.class))
				.sorted(Comparator.comparing(PrivilegeCompanyVO::getSort))
			.toList());
		return ReturnVo.ok(voPage);
	}

	@GetMapping("/{id}")
	public ReturnVo<PrivilegeCompanyVO> getById(@PathVariable Long id) {
		return ReturnVo.ok(BeanUtil.copyProperties(privilegeCompanyService.getById(id), PrivilegeCompanyVO.class));
	}

	@GetMapping("/code/{code}")
	public ReturnVo<PrivilegeCompanyVO> getByCode(@PathVariable String code) {
		return ReturnVo.ok(BeanUtil.copyProperties(privilegeCompanyService.getByCode(code), PrivilegeCompanyVO.class));
	}

	@PostMapping
	public ReturnVo<Boolean> save(@RequestBody PrivilegeCompanyDTO dto) {
		return ReturnVo.ok(privilegeCompanyService.save(dto.toEntity()));
	}

	@PutMapping
	public ReturnVo<Boolean> update(@RequestBody PrivilegeCompanyDTO dto) {
		dto.setCreateBy(StpUtil.getLoginId().toString());
		return ReturnVo.ok(privilegeCompanyService.updateById(dto.toEntity()));
	}

	@DeleteMapping("/{id}")
	public ReturnVo<String> delete(@PathVariable Long id) {
		return privilegeCompanyService.deleteCompanyById(id.toString());
	}

}
