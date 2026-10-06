package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import beans.common.Gender;
import dao.common.GenderDAO;
import dao.common.GenderDAOImpl;
import exception.BusinessException;
import utils.DbTransaction;
import utils.TransactionCallback;

public class GenderServiceImpl implements GenderService {
	private final GenderDAO genderDAO;

	private static final String ERR_GENDER_FETCH_ALL = "Không thể tải danh sách giới tính lúc này.";
	private static final String ERR_GENDER_FETCH_ONE = "Không thể tải thông tin chi tiết của giới tính yêu cầu.";

	public GenderServiceImpl() {
		this(new GenderDAOImpl());
	}

	public GenderServiceImpl(GenderDAO genderDAO) {
		this.genderDAO = genderDAO;
	}

	@Override
	public List<Gender> getAllActiveGenders() throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<List<Gender>>() {
				@Override
				public List<Gender> doInTransaction(Connection conn) throws SQLException {
					return genderDAO.findAllActive(conn);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException(ERR_GENDER_FETCH_ALL);
		}
	}

	@Override
	public Optional<Gender> findByCode(String code) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Optional<Gender>>() {
				@Override
				public Optional<Gender> doInTransaction(Connection conn) throws SQLException {
					return genderDAO.findByCode(conn, code);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException(ERR_GENDER_FETCH_ONE);
		}
	}
}
