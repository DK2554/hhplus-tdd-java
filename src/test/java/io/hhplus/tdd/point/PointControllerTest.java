package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PointControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Test
    @DisplayName("특정유저포인트조회시정상적으로url호출시정상응답을반환한다")
    public void 특정유저포인트조회시정상적으로url호출시정상응답을반환한다() throws Exception {
        long id = 1L;
        UserPoint mockUserPoint = new UserPoint(id, 100, System.currentTimeMillis());

        // Mock 설정
        when(pointService.findByUserId(id)).thenReturn(mockUserPoint);

        mockMvc.perform(get("/point/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.point").value(100));
    }
    @Test
    @DisplayName("포인트사용시정상적으로url호출시정상응답을반환한다")
    public void 포인트사용시정상적으로url호출시정상응답을반환한다() throws Exception {
        long id = 1L;
        int usePoints = 50;
        UserPoint updatedUserPoint = new UserPoint(id, 50, System.currentTimeMillis());

        // Mock 설정
        when(pointService.useUserPoint(id, usePoints)).thenReturn(updatedUserPoint);
        String requestBody = String.valueOf(usePoints);
        mockMvc.perform(patch("/point/{id}/use",id)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))// 요청 본문
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.point").value(50));
    }

    // 포인트 충전 테스트
    @Test
    @DisplayName("포인트충전시정상적으로url호출시정상응답을반환한다")
    public void 포인트충전시정상적으로url호출시정상응답을반환한다() throws Exception {
        long id = 1L;
        long chargePoint = 30;
        UserPoint updatedUserPoint = new UserPoint(id, 130, System.currentTimeMillis());
        String requestBody = String.valueOf(chargePoint);
        // Mock 설정
        when(pointService.chargeUserPoint(id, chargePoint)).thenReturn(updatedUserPoint);

        mockMvc.perform(patch("/point/{id}/charge",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))// 요청 본문)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.point").value(130));
    }

    // 포인트 내역 조회 테스트
    @Test
    @DisplayName("포인트충전시정상적으로url호출시정상응답을반환한다")
    public void 포인트내역조회시정상적으로url호출시정상응답을반환한다() throws Exception {
        long id = 1L;
        List<PointHistory> history = List.of(
                new PointHistory(1,id,-50, TransactionType.USE , System.currentTimeMillis()),
                new PointHistory(2,id,100, TransactionType.CHARGE,  System.currentTimeMillis())
        );

        // Mock 설정
        when(pointService.findUserPointsById(id)).thenReturn(history);

        mockMvc.perform(get("/point/{id}/histories", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].type").value(TransactionType.USE.name()))
                .andExpect(jsonPath("$[0].amount").value(-50))
                .andExpect(jsonPath("$[1].type").value(TransactionType.CHARGE.name()))
                .andExpect(jsonPath("$[1].amount").value(100));
    }
}
