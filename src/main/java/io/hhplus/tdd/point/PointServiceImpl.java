package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointServiceImpl implements PointService{
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    //최대잔고
    private static final long MAX_AMOUNT = 10000L;
    //최소금액
    private static final long MIN_AMOUNT = 0L;

    public PointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    @Override
    public UserPoint chargeUserPoint(long userId, long amount) {

        UserPoint userPoint = userPointTable.selectById(userId);

        if(userPoint == null){
            throw new IllegalArgumentException("유저가 존재하지 않습니다.");
        }

        if(amount <= MIN_AMOUNT){
            throw new IllegalArgumentException("충전금액으 0보다 커야합니다.");
        }

        long updateAmount = userPoint.point() + amount;
        // 최대 잔고 초과 여부 확인
        if (updateAmount > MAX_AMOUNT) {
            throw new IllegalArgumentException("잔고가 초과 되었습니다 포인트는 " + MAX_AMOUNT + "을 초과할 수 없습니다.");
        }

        UserPoint updateUser =  userPointTable.insertOrUpdate(userId, amount);

        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return updateUser;
    }

}
