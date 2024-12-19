package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointServiceImpl implements PointService{

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private final long MIN_AMOUNT = 0L;

    private final long MAX_AMOUNT = 10000L;

    public PointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }
    @Override
    public UserPoint findByUserId(long id) {

        if(id < 0){
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }

        return userPointTable.selectById(id);
    }

    @Override
    public List<PointHistory> findUserPointsById(long id) {

        if(id < 0){
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }

        return pointHistoryTable.selectAllByUserId(id);
    }

    @Override
    public UserPoint chargeUserPoint(long userId, long amount) {

        UserPoint userPoint = userPointTable.selectById(userId);

        if(amount <= MIN_AMOUNT){
            throw new IllegalArgumentException("충전금액으 0보다 커야합니다.");
        }

        long updateAmount = userPoint.point() + amount;
        // 최대 잔고 초과 여부 확인
        if (updateAmount > MAX_AMOUNT) {
            throw new IllegalArgumentException("잔고가 초과 되었습니다 포인트는 " + MAX_AMOUNT + "을 초과할 수 없습니다.");
        }

        UserPoint updateUser =  userPointTable.insertOrUpdate(userId, updateAmount);

        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return updateUser;
    }

    @Override
    public UserPoint useUserPoint(long userId, long amount) {
        UserPoint userPoint = userPointTable.selectById(userId);

        if(amount <= MIN_AMOUNT) {
            throw new IllegalArgumentException("사용금액은 0보다 커야합니다.");
        }

        long updateAmount = userPoint.point() - amount;
        // 최대 잔고 초과 여부 확인

        if(updateAmount < 0){
            throw new IllegalArgumentException("잔고에 금액이 부족합니다");
        }

        UserPoint updateUser =  userPointTable.insertOrUpdate(userId, updateAmount);

        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

        return updateUser;
    }

}
