    package io.hhplus.tdd.point;

    import io.hhplus.tdd.database.PointHistoryTable;
    import io.hhplus.tdd.database.UserPointTable;
    import org.junit.jupiter.api.DisplayName;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.extension.ExtendWith;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.junit.jupiter.MockitoExtension;

    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.List;

    import static org.junit.jupiter.api.Assertions.*;
    import static org.mockito.ArgumentMatchers.anyLong;
    import static org.mockito.Mockito.*;

    @ExtendWith(MockitoExtension.class)
    public class PointServiceTest {
        @Mock
        private UserPointTable userPointTable;
        @Mock
        private PointHistoryTable pointHistoryTable;
        @InjectMocks
        private PointServiceImpl pointService;

        @Test
        @DisplayName("사용자가1L인_사용자를_조회하면_해당사용자의_UserPoint가_반환된다")
        public void 사용자가1L인_사용자를_조회하면_해당사용자의_UserPoint가_반환된다(){
            // given: 테스트에 필요한 입력값과 초기 상태 설정
            long userId = 1L; //일치하지 않는 사용자 값
            long point = 10;

            // 초기 사용자 정보: ID와 보유 포인트를 포함한 UserPoint 객체 생성
            UserPoint initialUserPoint = new UserPoint(userId, point, System.currentTimeMillis());
            // userPointTable.selectById(userId)가 초기 사용자 정보를 반환하도록 설정
            when(userPointTable.selectById(userId)).thenReturn(initialUserPoint);

            UserPoint successUser = pointService.findByUserId(userId);

            // then: 반환된 결과 검증
            assertNotNull(successUser);
            assertEquals(successUser.id(), userId);
            assertEquals(successUser.point(), 10);
        }

        @Test
        @DisplayName("잘못된_사용자를_조회할경우_IllegalArgumentException_예외가_발생하고_메세지는_유효하지않은_사용자ID입니다_를반환해야한다")
        public void 잘못된_사용자를_조회할경우_IllegalArgumentException_예외가_발생하고_메세지는_유효하지않은_사용자ID입니다_를반환해야한다() {
            // given: 잘못된 사용자 ID 설정
            long userId = -1L; // 유효하지 않은 사용자 ID

            // when & then: 잘못된 ID로 메서드를 호출 시 예외 발생 검증
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> pointService.findByUserId(userId)
            );

            // 예외 메시지 검증
            assertEquals("유효하지 않은 사용자 ID입니다.", exception.getMessage());

            // selectById 메서드가 호출되지 않았는지 검증
            verify(userPointTable, never()).selectById(anyLong());
        }

        @Test
        @DisplayName("사용자의 포인트 내역을 조회한다.")
        public void 특정사용자의_충전포인트_내역을_조회한다(){
            // given: 테스트에 필요한 입력값과 초기 상태 설정
            long userId = 1L; //일치하지 않는 사용자 값

            // 포인트 히스토리 목록 설정
            List<PointHistory> pointList = new ArrayList<>();
            pointList.add(new PointHistory(1, userId, 100L, TransactionType.CHARGE, System.currentTimeMillis()));
            pointList.add(new PointHistory(2, userId, 50L, TransactionType.USE, System.currentTimeMillis()));
            // pointHistoryTable.selectAllByUserId(userId)가 포인트 히스토리 목록을 반환하도록 설정
            when(pointHistoryTable.selectAllByUserId(eq(userId))).thenReturn(pointList);
            // when: 포인트 히스토리 조회 메서드 실행
            List<PointHistory> result = pointService.findUserPointsById(userId);

            // then: 반환된 결과 검증
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(100L, result.get(0).amount());
            assertEquals(TransactionType.CHARGE, result.get(0).type());

        }

        @Test
        @DisplayName("포인트 히스토리 조회 조회 결과 없음")
        public void 특정사용자의포인트내역을조회했을때내역이없으면빈조회결과를반환한다() {
            // given: 존재하지 않는 사용자 ID
            long userId = 999L;

            // Stubbing: selectAllByUserId()가 빈 리스트를 반환
            when(pointHistoryTable.selectAllByUserId(eq(userId))).thenReturn(Collections.emptyList());

            // when: 히스토리 조회 메서드 실행
            List<PointHistory> result = pointService.findUserPointsById(userId);

            // then: 결과 검증
            assertNotNull(result);
            assertTrue(result.isEmpty());

            // selectAllByUserId 메서드 호출 검증
            verify(pointHistoryTable).selectAllByUserId(eq(userId));
        }

        @Test
        @DisplayName("포인트 히스토리 조회 실패_잘못된 사용자 ID")
        public void 포인트내역_조회시_잘못된_사용자음수사용자ID인경우_예외를_발생해야하며_메세지는_유효하지않은_사용자ID입니다를_반환한다() {
            // given: 잘못된 사용자 ID
            long userId = -1L;

            // when & then: 잘못된 입력으로 예외 발생 검증
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> pointService.findUserPointsById(userId)
            );

            // 예외 메시지 검증
            assertEquals("유효하지 않은 사용자 ID입니다.", exception.getMessage());

            // pointHistoryTable 메서드가 호출되지 않았는지 검증
            verify(pointHistoryTable, never()).selectAllByUserId(anyLong());
        }

    }
