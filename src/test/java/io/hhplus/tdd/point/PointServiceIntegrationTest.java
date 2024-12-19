package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointServiceIntegrationTest {
    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("기존_포인트100포인트를_가진사용자가100포인트를_충전하면_잔금_포인트가_200포인트여야한다")
    public void 기존_포인트100포인트를_가진사용자가100포인트를_충전하면_잔금_포인트가_200포인트여야한다() {
        // given: 초기 데이터 설정
        long userId = 1L;
        long chargeAmount = 100L;
        userPointTable.insertOrUpdate(userId, 100L); // 초기 포인트 설정

        // when
        UserPoint result = pointService.chargeUserPoint(userId, chargeAmount);

        // then
        assertThat(result.point()).isEqualTo(200L);
    }

    @Test
    @DisplayName("잔금100포인트를가진_특정사용자가_50포인트를_사용했을경우_잔금50포인트를반환해야한다")
    public void 잔금100포인트를가진_특정사용자가_50포인트를_사용했을경우_잔금50포인트를반환해야한다() {
        // given: 초기 데이터 설정
        long userId = 1L;
        long initialPoint = 100L;
        long useAmount = 50L;

        userPointTable.insertOrUpdate(userId, initialPoint);

        // when: 포인트 사용 서비스 호출
        UserPoint updatedUserPoint = pointService.useUserPoint(userId, useAmount);

        // then: 반환된 UserPoint 검증
        assertThat(updatedUserPoint).isNotNull();
        assertThat(updatedUserPoint.point()).isEqualTo(initialPoint - useAmount);
    }

}
