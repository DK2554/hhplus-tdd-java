package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

@Service
public class PointServiceImpl implements PointService{

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

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
}
