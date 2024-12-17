package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {
    UserPoint chargeUserPoint(long userId, long amount);

    UserPoint findByUserId(long id);

    List<PointHistory> findUserPointsById(long id);
}
