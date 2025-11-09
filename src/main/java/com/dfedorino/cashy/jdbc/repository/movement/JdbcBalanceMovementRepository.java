package com.dfedorino.cashy.jdbc.repository.movement;

import com.dfedorino.cashy.domain.model.movement.BalanceMovementEntity;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import com.dfedorino.cashy.domain.repository.movement.BalanceMovementRepository;
import com.dfedorino.cashy.jdbc.util.KeyHolderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcBalanceMovementRepository implements BalanceMovementRepository {

    public static final String USER_ID = "userId";
    public static final String OPERATION_ID = "operationId";
    public static final String ACCOUNT_BALANCE_ID = "accountBalanceId";
    public static final String CATEGORY_BALANCE_ID = "categoryBalanceId";
    public static final String DIRECTION_TYPE_ID = "directionTypeId";
    public static final String AMOUNT = "amount";

    public static final String INSERT_MOVEMENT =
            "INSERT INTO balance_movement("
                    + "user_id, "
                    + "operation_id, "
                    + "account_balance_id, "
                    + "category_balance_id, "
                    + "direction_type_id, "
                    + "amount) "
                    + "VALUES ("
                    + ":" + USER_ID + ", "
                    + ":" + OPERATION_ID + ", "
                    + ":" + ACCOUNT_BALANCE_ID + ", "
                    + ":" + CATEGORY_BALANCE_ID + ", "
                    + ":" + DIRECTION_TYPE_ID + ", "
                    + ":" + AMOUNT + ")";

    private final JdbcClient jdbcClient;

    @Override
    public BalanceMovementEntity createMovement(BalanceMovementEntity movement) {
        var keyHolder = new GeneratedKeyHolder();

        try {
            jdbcClient.sql(INSERT_MOVEMENT)
                    .param(USER_ID, movement.getUserId())
                    .param(OPERATION_ID, movement.getOperationId())
                    .param(ACCOUNT_BALANCE_ID, movement.getAccountBalanceId())
                    .param(CATEGORY_BALANCE_ID, movement.getCategoryBalanceId())
                    .param(DIRECTION_TYPE_ID, movement.getDirectionTypeId())
                    .param(AMOUNT, movement.getAmount())
                    .update(keyHolder);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }

        movement.setId(KeyHolderUtil.getId(keyHolder));
        movement.setCreatedAt(KeyHolderUtil.getCreatedAt(keyHolder));

        return movement;
    }
}

