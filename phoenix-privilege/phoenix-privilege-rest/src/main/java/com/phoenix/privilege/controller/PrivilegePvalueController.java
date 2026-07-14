package com.phoenix.privilege.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.phoenix.privilege.dto.PrivilegePvalueDTO;
import com.phoenix.privilege.dto.query.PrivilegePvalueQuery;
import com.phoenix.privilege.entity.PrivilegePvalue;
import com.phoenix.privilege.entity.PrivilegeUser;
import com.phoenix.privilege.service.IPrivilegePvalueService;
import com.phoenix.privilege.vo.PrivilegePvalueVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.phoenix.privilege.constant.CommonConstant.LOGIN_USER_INFO;

@Slf4j
@RestController
@RequestMapping("/api/privilege/pvalue")
@RequiredArgsConstructor
public class PrivilegePvalueController {

	private final IPrivilegePvalueService privilegePvalueService;

	@PostMapping("/page")
	public ReturnVo<Page<PrivilegePvalueVO>> page(@RequestBody PrivilegePvalueQuery query) {
		QueryWrapper qw = QueryWrapper.create()
			.like(PrivilegePvalue::getName, query.getName(), StrUtil.isNotBlank(query.getName()))
			.orderBy(PrivilegePvalue::getPosition, true);
		Page<PrivilegePvalue> entityPage = privilegePvalueService.page(new Page<>(query.getPage(), query.getSize()), qw);
		Page<PrivilegePvalueVO> voPage = new Page<>(entityPage.getPageNumber(), entityPage.getPageSize(),
				entityPage.getTotalRow());
		voPage.setRecords(entityPage.getRecords()
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegePvalueVO.class))
			.toList());
		return ReturnVo.ok(voPage);
	}

	@GetMapping("/{id}")
	public ReturnVo<PrivilegePvalueVO> getById(@PathVariable String id) {
		return ReturnVo.ok(BeanUtil.copyProperties(privilegePvalueService.getById(id), PrivilegePvalueVO.class));
	}

	@GetMapping("/system")
	public ReturnVo<List<PrivilegePvalueVO>> getBySystemId() {
		List<PrivilegePvalueVO> list = privilegePvalueService.list()
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegePvalueVO.class))
			.toList();
		return ReturnVo.ok(list);
	}

	@PostMapping
	public ReturnVo<Boolean> save(@RequestBody PrivilegePvalueDTO dto) {
		PrivilegeUser user = getCurrentUser();
		if (user == null) {
			return ReturnVo.fail("用户未登录");
		}
		PrivilegePvalue entity = dto.toEntity();
		entity.setCreateBy(user.getCode());
		return ReturnVo.ok(privilegePvalueService.save(entity));
	}

	@PutMapping
	public ReturnVo<Boolean> update(@RequestBody PrivilegePvalueDTO dto) {
		return ReturnVo.ok(privilegePvalueService.updateById(dto.toEntity()));
	}

	@DeleteMapping("/{id}")
	public ReturnVo<Boolean> delete(@PathVariable String id) {
		return ReturnVo.ok(privilegePvalueService.removeById(id));
	}

	private PrivilegeUser getCurrentUser() {
		Object value = StpUtil.getSession().get(LOGIN_USER_INFO);
		if (value instanceof PrivilegeUser pUser) {
			return pUser;
		}
		ObjectMapper mapper = new ObjectMapper();
		if (value instanceof Map<?, ?> map) {
			return mapper.convertValue(map, PrivilegeUser.class);
		}
		if (value instanceof String str) {
			try {
				return mapper.readValue(str, PrivilegeUser.class);
			} catch (Exception e) {
				log.warn("Failed to parse PrivilegeUser from session string", e);
			}
		}
		return null;
	}
}
