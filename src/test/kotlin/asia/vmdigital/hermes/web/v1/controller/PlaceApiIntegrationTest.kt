package asia.vmdigital.hermes.web.v1.controller

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.`when`
import com.jayway.restassured.RestAssured.given
import org.apache.http.HttpStatus
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.junit.After
import com.jayway.restassured.module.jsv.JsonSchemaValidator
import org.hamcrest.CoreMatchers.`is`
import org.springframework.test.annotation.DirtiesContext

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PlaceApiIntegrationTest {

    @Value("\${local.server.port}")
    var port: Int = 0

    @Before
    fun setUp() {
        RestAssured.port = port
    }

    @After
    fun cleanUp() {
    }

    @Test
    fun testPing() {
        `when`().get("/ping").then().statusCode(HttpStatus.SC_OK)
    }

    @Test
    fun testSavePlace() {
        val requestJson = """
            {
	            "user_id": "place-user-01",
                "saved_id": "1e285b15-ec02-4870-80f8-b1fc4f8196q2",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 100.4534,
	            "lat": 14.0525
            }
        """.trimIndent()
        val placeId = given().contentType("application/json").body(requestJson)
                .`when`().post("saved/v1/user/hermes-int-test01/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .and().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("json-schema/placeResponseSchema.json"))
                .and().body("user_id", equalTo("hermes-int-test01"))
                .and().body("saved_id", equalTo("1e285b15-ec02-4870-80f8-b1fc4f8196q2"))
                .and().body("categories", hasItem("restaurant"))
                .extract().path<String>("id")

        `when`().get("saved/v1/user/hermes-int-test01/saved/$placeId")
                .then().statusCode(HttpStatus.SC_OK).and().body("id", equalTo(placeId))
                .and().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("json-schema/placeResponseSchema.json"))
                .and().body("user_id", equalTo("hermes-int-test01"))
                .and().body("saved_id", equalTo("1e285b15-ec02-4870-80f8-b1fc4f8196q2"))
                .and().body("categories", hasItem("restaurant"))

    }

    @Test
    fun testGetNoneExistPlace() {
        `when`().get("saved/v1/user/hermes-int-test01/saved/no-t-Real--Id")
                .then().statusCode(HttpStatus.SC_NOT_FOUND).and()
    }

    @Test
    fun testSavePlaceAndGetById() {
        val requestJson = """
            {
	            "user_id": "hermes-int-test01",
                "saved_id": "1e285b15-ec02-4870-80f8-b1fc4f8196q2",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 100.4534,
	            "lat": 14.0525
            }
        """.trimIndent()
        val placeId = given().contentType("application/json").body(requestJson)
                .`when`().post("saved/v1/user/hermes-int-test01/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .extract().path<String>("id")

        `when`().get("saved/v1/user/hermes-int-test01/saved/$placeId")
                .then().statusCode(HttpStatus.SC_OK).and().body("id", equalTo(placeId))
                .and().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("json-schema/placeResponseSchema.json"))
                .and().body("user_id", equalTo("hermes-int-test01"))
                .and().body("saved_id", equalTo("1e285b15-ec02-4870-80f8-b1fc4f8196q2"))
                .and().body("categories", hasItem("restaurant"))
    }

    @Test
    fun testSavePlaceAndGetAndQueryPlace() {
        val requestJsonPlace1 = """
            {
	            "user_id": "hermes-int-test99",
                "saved_id": "place-1",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 111.4534,
	            "lat": 34.0525
            }
        """.trimIndent()

        val requestJsonPlace2 = """
            {
	            "user_id": "hermes-int-test99",
                "saved_id": "place-2",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 100.4534,
	            "lat": 14.0525
            }
        """.trimIndent()

        val requestJsonAnotherUser = """
            {
	            "user_id": "another-user",
                "saved_id": "place-2",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 100.4534,
	            "lat": 14.0525
            }
        """.trimIndent()

        given().contentType("application/json").body(requestJsonPlace1)
                .`when`().post("saved/v1/user/hermes-int-test99/saved")
                .then().statusCode(HttpStatus.SC_OK)

        given().contentType("application/json").body(requestJsonPlace2)
                .`when`().post("saved/v1/user/hermes-int-test99/saved")
                .then().statusCode(HttpStatus.SC_OK)

        given().contentType("application/json").body(requestJsonAnotherUser)
                .`when`().post("saved/v1/user/another-user/saved")
                .then().statusCode(HttpStatus.SC_OK)

        given().queryParam("userId", "hermes-int-test99").
        `when`().get("saved/v1/user/hermes-int-test99/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .and().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("json-schema/placeQueryResponse.json"))
                .and().body("result.size()", `is`(2))
                .and().body("result[0].user_id", equalTo("hermes-int-test99"))
                .and().body("result[1].user_id", equalTo("hermes-int-test99"))

    }


    @Test
    fun testDeletePlace() {
        val requestJson = """
            {
	            "user_id": "hermes",
                "saved_id": "1e285b15-ec02-4870-80f8",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 111.4534,
	            "lat": 16.0525
            }
        """.trimIndent()
        val placeId = given().contentType("application/json").body(requestJson)
                .`when`().post("saved/v1/user/hermes/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .extract().path<String>("id")

        `when`().get("saved/v1/user/hermes/saved/$placeId")
                .then().statusCode(HttpStatus.SC_OK).and().body("id", equalTo(placeId))

        `when`().delete("saved/v1/user/hermes/saved/$placeId")
                .then().statusCode(HttpStatus.SC_OK)

        `when`().get("saved/v1/user/hermes/saved/$placeId")
                .then().statusCode(HttpStatus.SC_NOT_FOUND)
    }
}