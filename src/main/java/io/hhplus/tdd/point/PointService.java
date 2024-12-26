package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {
    UserPoint findByUserId(long id);
    List<PointHistory> findUserPointsById(long id);
    UserPoint chargeUserPoint(long id, long amount);

    UserPoint useUserPoint(long id, long amount);
}
