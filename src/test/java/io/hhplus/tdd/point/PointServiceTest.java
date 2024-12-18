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
    import static org.mockito.ArgumentMatchers.*;
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
        @DisplayName("포인트 충전 성공")
        public void 포인트충전_성공(){
            // given: 테스트에 필요한 데이터 준비
            long userId = 1L;
            long point = 100;

            // 초기 사용자 포인트 상태: 기존 포인트가 100L
            UserPoint initialUserPoint = new UserPoint(userId, 100L, System.currentTimeMillis());
            // 충전 후 사용자 포인트 상태: 기존 100L에 100L가 추가된 상태
            UserPoint updatedUserPoint = new UserPoint(userId, 200L, System.currentTimeMillis());
            // 포인트 충전 기록: 충전 이력 테이블에 저장될 정보
            PointHistory expectedHistory = new PointHistory(1, userId, point, TransactionType.CHARGE, System.currentTimeMillis());

            // when: 메서드 실행 전에 필요한 Mock 객체의 행동 정의
            when(userPointTable.selectById(eq(userId))).thenReturn(initialUserPoint);
            when(userPointTable.insertOrUpdate(eq(userId), eq(point))).thenReturn(updatedUserPoint);
            when(pointHistoryTable.insert(eq(userId), eq(point), eq(TransactionType.CHARGE), anyLong())).thenReturn(expectedHistory);
            // when: 실제 테스트 대상 메서드 실행
            UserPoint successUser = pointService.chargeUserPoint(userId, point);
            // then: 결과 검증 - 반환된 UserPoint 객체가 예상과 일치하는지 확인
            assertNotNull(successUser);
            assertEquals(successUser.id(), userId);
            assertEquals(successUser.point(), 200);

            // then: 메서드 호출 검증 - Mock 객체의 메서드가 예상대로 호출되었는지 확인
            verify(userPointTable).selectById(eq(userId));
            // 사용자 포인트 조회 메서드가 정상적으로 호출되었는지 검증
            verify(userPointTable).insertOrUpdate(eq(userId), eq(point));
            // 포인트 업데이트 메서드가 정상적으로 호출되었는지 검증
            verify(pointHistoryTable).insert(eq(userId), eq(point), eq(TransactionType.CHARGE), anyLong());
            // 포인트 이력 기록 메서드가 정상적으로 호출되었는지 검증

        }

        @Test
        @DisplayName("포인트 충전 금액이 0원이거나 보다 작을 경우 예외")
        public void 포인트충전_실패_금액0원미만(){
            //예외 테스트

            // given: 테스트에 필요한 입력값 설정
            long userId = 1L; //일치하지 않는 사용자 값
            long point = 0; //충전 금액이 0원 (잘못된 입력)

            // userPointTable.selectById() 호출 시 초기 사용자 포인트 반환하도록 Mock 설정
            UserPoint initialUserPoint = new UserPoint(userId, 100L, System.currentTimeMillis());
            // when & then: 충전 금액이 0원 이하일 경우 예외 발생 검증
            when(userPointTable.selectById(userId)).thenReturn(initialUserPoint);
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> pointService.chargeUserPoint(userId, point)
            );
            //예외 메세지 검증
            assertEquals("충전금액으 0보다 커야합니다.", exception.getMessage());
        }


        @Test
        @DisplayName("포인트 충전시 최대잔고를 초과했을 경우 예외")
        public void 포인트전트_실패_최대잔고초과(){
            // 예외 테스트: 충전 시 포인트가 최대 잔액을 초과하는 경우 예외 발생 여부를 검증
            // given: 테스트에 필요한 입력값 및 초기 상태 설정
            long userId = 1L; //일치하지 않는 사용자 값
            long point = 10;
            long MAX_AMOUNT = 10000L;
            // 사용자의 초기 포인트 상태를 설정 (이미 최대 잔액인 10000L)
            UserPoint initialUserPoint = new UserPoint(userId, 10000L, System.currentTimeMillis());
            // Stubbing: userPointTable.selectById()가 초기 사용자 포인트 반환
            when(userPointTable.selectById(userId)).thenReturn(initialUserPoint);
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> pointService.chargeUserPoint(userId, point)
            );
            //예외 메세지 검증
            assertEquals("잔고가 초과 되었습니다 포인트는 "+ MAX_AMOUNT +"을 초과할 수 없습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("사용자 정상 조회")
        public void 사용자_조회(){
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
        @DisplayName("사용자 조회 실패_잘못된 사용자 ID")
        public void 사용자_조회_실패_잘못된ID() {
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
        public void 사용자_포인트_내역조회(){
            // given: 테스트에 필요한 입력값과 초기 상태 설정
            long userId = 1L; //일치하지 않는 사용자 값
            long point = 10;
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
        public void 포인트_히스토리_조회_결과없음() {
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
        public void 포인트_히스토리_조회_잘못된_사용자ID() {
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
