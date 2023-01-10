package com.example.project.direction.controller

import com.example.project.direction.dto.OutputDto
import com.example.project.pharmacy.service.PharmacyRecommendationService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class FormControllerTest extends Specification {

    private MockMvc mockMvc
    private PharmacyRecommendationService pharmacyRecommendationService = Mock()
    private List<OutputDto> outputDtoList

    def setup() {
        // FormController MockMvc 객체로 만든다.
        mockMvc = MockMvcBuilders.standaloneSetup(new FormController(pharmacyRecommendationService))
                .build()

        outputDtoList = new ArrayList<>()
        outputDtoList.addAll(
                OutputDto.builder()
                        .pharmacyName("pharmacy1")
                        .build(),
                OutputDto.builder()
                        .pharmacyName("pharmacy2")
                        .build()
        )
    }

    def "GET /"() {
        expect:
        // FormController 의 "/" URI 를 GET 방식으로 호출
        mockMvc.perform(get("/"))
                .andExpect(handler().handlerType(FormController.class))
                .andExpect(handler().methodName("main"))
                .andExpect(status().isOk()) // 200 상태인지 체크
                .andExpect(view().name("main")) // view 의 이름이 main 인지 체크
                .andDo(log())
    }

    def "POST /search"() {
        given:
        String inputAddress = "서울 성북구 종암동"

        when:
        def resultActions = mockMvc.perform(post("/search")
                .param("address", inputAddress))

        then:
        1 * pharmacyRecommendationService.recommendPharmacyList(argument -> {
            assert argument == inputAddress // mock 객체의 argument 검증(argument 가 inputAddress 와 동일한지)
        }) >> outputDtoList // 해당 메소드(recommendPharmacyList)가 호출되면 응답값(outputDtoList)을 리턴

        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("output"))
                .andExpect(model().attributeExists("outputFormList")) // model 에 outputFormList 라는 key 가 존재하는지 검증
                .andExpect(model().attribute("outputFormList", outputDtoList)) // outputFormList 에 해당하는 데이터가 outputDtoList 객체인지 검증
                .andDo(print())
    }
}
