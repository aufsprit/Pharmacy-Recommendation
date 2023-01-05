package com.example.project.api.service

import com.example.project.AbstractIntegrationContainerBaseTest
import com.example.project.api.dto.DocumentDto
import com.example.project.api.dto.KakaoApiResponseDto
import com.example.project.api.dto.MetaDto
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper

class KakaoAddressSearchServiceRetryTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private KakaoAddressSearchService kakaoAddressSearchService

    @SpringBean
    private KakaoUriBuilderService kakaoUriBuilderService = Mock()
    /*
    * Spock 에서는 @SpringBean 을 사용
    * Mockito 에서는 @MockBean 을 사용
    * Spring 컨테이너 내에 있는 Bean 을 Mocking
    */
    private MockWebServer mockWebServer;
    private ObjectMapper mapper = new ObjectMapper()
    private String inputAddress = "서울 성북구 종암로 10길"

    def setup() {
        mockWebServer = new MockWebServer()
        mockWebServer.start()
        println mockWebServer.port
        println mockWebServer.url("/")
    }

    def cleanup() {
        mockWebServer.shutdown()
    }

    def "requestAddressSearch retry success"() {
        given:
        def metaDto = new MetaDto(1)
        def documentDto = DocumentDto.builder()
                .addressName(inputAddress)
                .build()
        def expectedResponse = new KakaoApiResponseDto(metaDto, Arrays.asList(documentDto))
        def uri = mockWebServer.url("/").uri()

        when:
        mockWebServer.enqueue(new MockResponse().setResponseCode(504)) // 504 error
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse))) // 정상

        def kakaoApiResult = kakaoAddressSearchService.requestAddressSearch(inputAddress)
        def takeRequest = mockWebServer.takeRequest()

        then:
        // 두 번 호출되었는지, 각 테스트 조건에 맞는지를 확인
        2 * kakaoUriBuilderService.buildUriByAddressSearch(inputAddress) >> uri
        takeRequest.getMethod() == "GET"
        kakaoApiResult.getDocumentList().size() == 1
        kakaoApiResult.getMetaDto().totalCount == 1
        kakaoApiResult.getDocumentList().get(0).getAddressName() == inputAddress
    }

    def "requestAddressSearch retry fail "() {
        given:
        def uri = mockWebServer.url("/").uri()

        when:
        mockWebServer.enqueue(new MockResponse().setResponseCode(504))
        mockWebServer.enqueue(new MockResponse().setResponseCode(504))

        def result = kakaoAddressSearchService.requestAddressSearch(inputAddress)

        then:
        // 두 번 모두 에러이므로 null 처리
        2 * kakaoUriBuilderService.buildUriByAddressSearch(inputAddress) >> uri
        result == null
    }
}
