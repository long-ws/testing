package service;

import java.util.List;
import java.util.Optional;

import beans.common.Gender;
import exception.BusinessException;

public interface GenderService {
	List<Gender> getAllActiveGenders() throws BusinessException;

	Optional<Gender> findByCode(String code) throws BusinessException;
}
