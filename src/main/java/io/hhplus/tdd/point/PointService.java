package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {
    UserPoint findByUserId(long id);
    List<PointHistory> findUserPointsById(long id);
}
