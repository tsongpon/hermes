package asia.vmdigital.hermes.web.v1.controller

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.`when`
import org.apache.http.HttpStatus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PlaceControllerIntegrationTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Value("\${local.server.port}")   // 6
    var port: Int = 0

    @Before
    fun setUp() {
        RestAssured.port = port
    }

    @Test
    fun testPing() {
        `when`().get("/ping").then().statusCode(HttpStatus.SC_OK)
    }
}