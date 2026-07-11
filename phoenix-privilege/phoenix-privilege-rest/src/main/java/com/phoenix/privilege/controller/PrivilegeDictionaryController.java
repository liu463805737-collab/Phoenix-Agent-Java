package com.phoenix.privilege.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.phoenix.privilege.dto.PrivilegeDictionaryDTO;
import com.phoenix.privilege.entity.PrivilegeDictionary;
import com.phoenix.privilege.service.IPrivilegeDictionaryService;
import com.phoenix.privilege.vo.PrivilegeDictionaryVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/privilege/dictionary")
@RequiredArgsConstructor
public class PrivilegeDictionaryController {

	private final IPrivilegeDictionaryService privilegeDictionaryService;

	@GetMapping("/page")
	public ReturnVo<Page<PrivilegeDictionaryVO>> page(@RequestParam(defaultValue = "1") long page,
			@RequestParam(defaultValue = "10") long size, PrivilegeDictionaryDTO dto) {
		QueryWrapper qw = QueryWrapper.create()
			.eq(PrivilegeDictionary::getSn, dto.getSn(), StrUtil.isNotBlank(dto.getSn()))
			.eq(PrivilegeDictionary::getSystemSn, dto.getSystemSn(), StrUtil.isNotBlank(dto.getSystemSn()))
			.like(PrivilegeDictionary::getName, dto.getName(), StrUtil.isNotBlank(dto.getName()))
			.orderBy(PrivilegeDictionary::getOrderNo, true);
		Page<PrivilegeDictionary> entityPage = privilegeDictionaryService.page(new Page<>(page, size), qw);
		Page<PrivilegeDictionaryVO> voPage = new Page<>(entityPage.getPageNumber(), entityPage.getPageSize(),
				entityPage.getTotalRow());
		voPage.setRecords(entityPage.getRecords()
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeDictionaryVO.class))
			.toList());
		return ReturnVo.ok(voPage);
	}

	@GetMapping("/{id}")
	public ReturnVo<PrivilegeDictionaryVO> getById(@PathVariable Long id) {
		return ReturnVo
			.ok(BeanUtil.copyProperties(privilegeDictionaryService.getById(id), PrivilegeDictionaryVO.class));
	}

	@GetMapping("/system/{systemSn}")
	public ReturnVo<List<PrivilegeDictionaryVO>> getBySystemSn(@PathVariable String systemSn) {
		List<PrivilegeDictionaryVO> list = privilegeDictionaryService.getBySystemSn(systemSn)
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeDictionaryVO.class))
			.toList();
		return ReturnVo.ok(list);
	}

	@GetMapping("/pcode/{pcode}")
	public ReturnVo<List<PrivilegeDictionaryVO>> getByPcode(@PathVariable String pcode) {
		List<PrivilegeDictionaryVO> list = privilegeDictionaryService.getByPcode(pcode)
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeDictionaryVO.class))
			.toList();
		return ReturnVo.ok(list);
	}

	@PostMapping
	public ReturnVo<Boolean> save(@RequestBody PrivilegeDictionaryDTO dto) {
		return ReturnVo.ok(privilegeDictionaryService.save(dto.toEntity()));
	}

	@PutMapping
	public ReturnVo<Boolean> update(@RequestBody PrivilegeDictionaryDTO dto) {
		return ReturnVo.ok(privilegeDictionaryService.updateById(dto.toEntity()));
	}

	@DeleteMapping("/{id}")
	public ReturnVo<Boolean> delete(@PathVariable Long id) {
		return ReturnVo.ok(privilegeDictionaryService.removeById(id));
	}

}
