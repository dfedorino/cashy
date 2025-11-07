package com.dfedorino.cashy.jdbc.repository.operation;

import com.dfedorino.cashy.domain.model.operation.OperationEntity;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import com.dfedorino.cashy.domain.repository.operation.OperationRepository;
import com.dfedorino.cashy.jdbc.util.KeyHolderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcOperationRepository implements OperationRepository {

    public static final String USER_ID = "userId";
    public static final String CATEGORY_ID = "categoryId";
    public static final String AMOUNT = "amount";

    public static final String INSERT_OPERATION =
            "INSERT INTO \"operation\"(user_id, category_id, amount) "
                    + "VALUES (:" + USER_ID + ", :" + CATEGORY_ID + ", :" + AMOUNT + ")";

    private final JdbcClient jdbcClient;

    @Override
    public OperationEntity createOperation(OperationEntity operation) {
        var keyHolder = new GeneratedKeyHolder();

        try {
            jdbcClient.sql(INSERT_OPERATION)
                    .param(USER_ID, operation.getUserId())
                    .param(CATEGORY_ID, operation.getCategoryId())
                    .param(AMOUNT, operation.getAmount())
                    .update(keyHolder);
        } catch (Exception e) {
            log.error(">> Failed to create operation for userId: {}, categoryId: {}, amount: {}",
                      operation.getUserId(),
                      operation.getCategoryId(),
                      operation.getAmount());
            log.error(">> ", e);
            throw new RepositoryException(e);
        }

        operation.setId(KeyHolderUtil.getId(keyHolder));
        operation.setCreatedAt(KeyHolderUtil.getCreatedAt(keyHolder));

        return operation;
    }
}

