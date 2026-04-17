package com.mediroster;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * 应用上下文与基础接口冒烟测试。
 *
 * @author tongguo.li
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MediRosterApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Spring 上下文加载成功即可
    }

    @Test
    void healthReturnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value("UP"));
    }
}
