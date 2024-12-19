package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;



    @Test
    @DisplayName("chargeUserPoint - 동시성 문제 해결 테스트")
    public void 사용자가100포인트충전을연속적으로요청했을경우정상적으로5500포인트가충전되어야한다() throws InterruptedException {
        long userId = 1L;
        long initialPoint = 500L;
        long chargeAmount = 100L;
        int threadCount = 50;

        userPointTable.insertOrUpdate(userId, initialPoint);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(userId, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long expectedTotal = initialPoint + (chargeAmount * threadCount);
        long actualTotal = userPointTable.selectById(userId).point();

        System.out.println("최종 포인트: " + actualTotal + ", 예상 포인트: " + expectedTotal);
        assertEquals(expectedTotal, actualTotal, "동시성 문제로 인해 최종 포인트 값이 예상과 다릅니다.");
    }

    @Test
    @DisplayName("useUserPoint - 동시성 문제 해결 테스트")
    public void 사용자가100포인트사용을연속적으로요청했을경우정상적으로잔금에서포인트가차감되어야한다() throws InterruptedException {
        long userId = 1L;
        long initialPoint = 5000L;
        long chargeAmount = 100L;
        int threadCount = 50;

        userPointTable.insertOrUpdate(userId, initialPoint);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.useUserPoint(userId, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long expectedTotal = initialPoint - (chargeAmount * threadCount);
        long actualTotal = userPointTable.selectById(userId).point();

        System.out.println("최종 포인트: " + actualTotal + ", 예상 포인트: " + expectedTotal);
        assertEquals(expectedTotal, actualTotal, "동시성 문제로 인해 최종 포인트 값이 예상과 다릅니다.");
    }

    @Test
    @DisplayName("여러 사용자가 충전과 사용을 동시에 요청하는 테스트")
    public void 여러사용자가동시에충전과사용을동시에요청했을경우에정상적으로처리되어야한다() throws InterruptedException {
        int userCount = 50; // 사용자 수
        long initialPoint = 5000L; // 각 사용자의 초기 포인트
        ExecutorService executorService = Executors.newFixedThreadPool(20); // 20개의 쓰레드 풀 생성
        CountDownLatch latch = new CountDownLatch(userCount * 2); // 사용자 수 x 2 (충전 + 사용 요청)

        // 사용자 초기 데이터 설정
        Map<Long, Long> userPointsTracker = new ConcurrentHashMap<>(); // 사용자별 최종 포인트 계산용
        for (int i = 1; i <= userCount; i++) {
            long userId = i;
            userPointTable.insertOrUpdate(userId, initialPoint); // 초기 포인트 설정
            userPointsTracker.put(userId, initialPoint); // 사용자별 초기 포인트 기록
        }

        Random random = new Random(); // 랜덤 충전/사용 금액 생성기

        for (int i = 1; i <= userCount; i++) {
            long userId = i;

            // 충전 요청 쓰레드
            executorService.submit(() -> {
                try {
                    long chargeAmount = 50 + random.nextInt(100); // 50 ~ 149 랜덤 충전 금액
                    pointService.chargeUserPoint(userId, chargeAmount);
                    userPointsTracker.compute(userId, (id, current) -> current + chargeAmount); // 트래킹 업데이트
                } finally {
                    latch.countDown(); // 작업 완료
                }
            });

            // 사용 요청 쓰레드
            executorService.submit(() -> {
                try {
                    long useAmount = 10 + random.nextInt(50); // 10 ~ 59 랜덤 사용 금액
                    pointService.useUserPoint(userId, useAmount);
                    userPointsTracker.compute(userId, (id, current) -> current - useAmount); // 트래킹 업데이트
                } finally {
                    latch.countDown(); // 작업 완료
                }
            });
        }

        latch.await(); // 모든 쓰레드 작업 완료 대기
        executorService.shutdown();

        // 각 사용자의 최종 포인트 값 검증
        for (int i = 1; i <= userCount; i++) {
            long userId = i;
            long expectedTotal = userPointsTracker.get(userId); // 동적으로 계산된 예상 포인트
            long actualTotal = userPointTable.selectById(userId).point(); // 실제 포인트
            System.out.println("사용자 " + i + " - 최종 포인트: " + actualTotal + ", 예상 포인트: " + expectedTotal);
            assertEquals(expectedTotal, actualTotal, "사용자 " + i + "의 최종 포인트가 예상과 다릅니다.");
        }
    }

}
