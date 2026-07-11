package com.phoenix.privilege.service;

import com.mybatisflex.core.service.IService;
import com.phoenix.privilege.entity.PrivilegeDictionary;

import java.util.List;

public interface IPrivilegeDictionaryService extends IService<PrivilegeDictionary> {

	List<PrivilegeDictionary> getBySystemSn(String systemSn);

	List<PrivilegeDictionary> getByPcode(String pcode);

}
