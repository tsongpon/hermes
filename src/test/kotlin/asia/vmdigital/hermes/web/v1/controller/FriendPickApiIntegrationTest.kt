package asia.vmdigital.hermes.web.v1.controller

import com.github.kristofa.test.http.Method
import com.github.kristofa.test.http.MockHttpServer
import com.github.kristofa.test.http.SimpleHttpResponseProvider
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.`when`
import com.jayway.restassured.module.jsv.JsonSchemaValidator
import org.apache.http.HttpStatus
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.lang.Thread.sleep

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FriendPickApiIntegrationTest {

    @Value("\${local.server.port}")
    var port: Int = 0

    var responseProvider: SimpleHttpResponseProvider? = null
    var server: MockHttpServer? = null

    @Before
    fun setUp() {
        RestAssured.port = port
        responseProvider = SimpleHttpResponseProvider()
        server = MockHttpServer(8180, responseProvider)
        server!!.start()
    }

    @After
    fun cleanUp() {
        server!!.stop()
    }

    @Test
    fun testGetFriendPickOneFriendSave() {
        val contractOwnerResponseUser02 = """
            {
                "result": [
                    {
                        "id": "user01",
                        "friendly_id": "user1",
                        "friendly_id_updated": false,
                        "profile_name": "user01",
                        "profile_photo": "bphoto",
                        "email": "user1@vmd.asia",
                        "mood": "",
                        "mood_expired_at": null,
                        "onboarded": false,
                        "require_verification": true,
                        "email_verified": false,
                        "language": "en-US"
                    }
                ]
            }
            """.trimIndent()

        val userInfoResponseUser02 = """
            {
                "id": "user02",
                "friendly_id": "user02",
                "friendly_id_updated": false,
                "profile_name": "user02",
                "profile_photo": "bphoto",
                "email": "user02@vmd.asia",
                "mood": "",
                "mood_expired_at": null,
                "onboarded": false,
                "require_verification": true,
                "email_verified": false,
                "language": "en-US"
            }
            """.trimIndent()
        responseProvider!!.expect(Method.GET, "/accounts/v1/users/user02/contactowners")
                .respondWith(200,
                        "application/json", contractOwnerResponseUser02)

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/user02")
                .respondWith(200,
                        "application/json", userInfoResponseUser02)

        val requestJson = """
            {
	            "user_id": "user02",
                "place_id": "user02-place-01",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 100.4534,
	            "lat": 14.0525
            }
        """.trimIndent()

        RestAssured.given().contentType("application/json").body(requestJson)
                .`when`().post("saved/v1/user/user02/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .extract().path<String>("id")

        sleep(500) //populate FriendPick work in separate thread, need to wait for process to finish

        RestAssured.`when`().get("friendpicks/v1/user/user01/friendpicks")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .and().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                        "json-schema/friendpickResponseSchema.json"))
                .and().body("result[0].user_id", equalTo("user01"))
                .and().body("result[0].place_id", equalTo("user02-place-01"))
                .and().body("result[0].type", equalTo("Google"))
                .and().body("result[0].friends[0].friend_id", equalTo("user02"))
    }

    @Test
    fun testDelete() {
        val contractOwnerResponseUser02D = """
            {
                "result": [
                    {
                        "id": "user01d",
                        "friendly_id": "user1D",
                        "friendly_id_updated": false,
                        "profile_name": "user01D",
                        "profile_photo": "bphoto",
                        "email": "user1D@vmd.asia",
                        "mood": "",
                        "mood_expired_at": null,
                        "onboarded": false,
                        "require_verification": true,
                        "email_verified": false,
                        "language": "en-US"
                    }
                ]
            }
            """.trimIndent()

        val userInfoResponseUser02d = """
            {
                "id": "user02d",
                "friendly_id": "user02D",
                "friendly_id_updated": false,
                "profile_name": "user02D",
                "profile_photo": "bphoto",
                "email": "user02D@vmd.asia",
                "mood": "",
                "mood_expired_at": null,
                "onboarded": false,
                "require_verification": true,
                "email_verified": false,
                "language": "en-US"
            }
            """.trimIndent()
        responseProvider!!.expect(Method.GET, "/accounts/v1/users/user02d/contactowners")
                .respondWith(200,
                        "application/json", contractOwnerResponseUser02D)

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/user02d")
                .respondWith(200,
                        "application/json", userInfoResponseUser02d)

        val requestJson = """
            {
	            "user_id": "user02d",
                "place_id": "user02-place-01d",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 10.4534,
	            "lat": 1.0525
            }
        """.trimIndent()

        RestAssured.given().contentType("application/json").body(requestJson)
                .`when`().post("saved/v1/user/user02d/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .extract().path<String>("id")

        sleep(500) //populate FriendPick work in separate thread, need to wait for process to finish

        val id = RestAssured.`when`().get("friendpicks/v1/user/user01d/friendpicks")
                .then().statusCode(HttpStatus.SC_OK).log().body().extract().path<String>("result[0].id")

        `when`().delete("friendpicks/v1/user/user01d/friendpicks/$id").then().statusCode(HttpStatus.SC_OK)

        `when`().get("friendpicks/v1/user/user01d/friendpicks")
                .then().statusCode(HttpStatus.SC_OK).and().body("result.size()", CoreMatchers.`is`(0))
    }

    @Test
    fun testGetFriendPickMultipleFriendSave() {
        val contractOwnerResponseUserFp02 = """
            {
                "result": [
                    {
                        "id": "userFp01",
                        "friendly_id": "userFp01",
                        "friendly_id_updated": false,
                        "profile_name": "user01",
                        "profile_photo": "userFp01Photo",
                        "email": "userFp01@vmd.asia",
                        "mood": "",
                        "mood_expired_at": null,
                        "onboarded": false,
                        "require_verification": true,
                        "email_verified": false,
                        "language": "en-US"
                    }
                ]
            }
            """.trimIndent()
        val userInfoResponseUserFp02 = """
            {
                "id": "userFp02",
                "friendly_id": "user02",
                "friendly_id_updated": false,
                "profile_name": "user02",
                "profile_photo": "bphotoFp02",
                "email": "userFp02@vmd.asia",
                "mood": "",
                "mood_expired_at": null,
                "onboarded": false,
                "require_verification": true,
                "email_verified": false,
                "language": "en-US"
            }
            """.trimIndent()

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/userFp02/contactowners")
                .respondWith(200,
                        "application/json", contractOwnerResponseUserFp02)

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/userFp02")
                .respondWith(200,
                        "application/json", userInfoResponseUserFp02)

        val contractOwnerResponseUserFp03 = """
            {
                "result": [
                    {
                        "id": "userFp01",
                        "friendly_id": "userFp01",
                        "friendly_id_updated": false,
                        "profile_name": "user01",
                        "profile_photo": "userFp01Photo",
                        "email": "userFp01@vmd.asia",
                        "mood": "",
                        "mood_expired_at": null,
                        "onboarded": false,
                        "require_verification": true,
                        "email_verified": false,
                        "language": "en-US"
                    }
                ]
            }
            """.trimIndent()
        val userInfoResponseUserFp03 = """
            {
                "id": "userFp03",
                "friendly_id": "user03",
                "friendly_id_updated": false,
                "profile_name": "user03",
                "profile_photo": "bphotoFp03",
                "email": "userFp03@vmd.asia",
                "mood": "",
                "mood_expired_at": null,
                "onboarded": false,
                "require_verification": true,
                "email_verified": false,
                "language": "en-US"
            }
            """.trimIndent()

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/userFp03/contactowners")
                .respondWith(200,
                        "application/json", contractOwnerResponseUserFp03)

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/userFp03")
                .respondWith(200,
                        "application/json", userInfoResponseUserFp03)

        val requestJsonUserFp02 = """
            {
	            "user_id": "userFp02",
                "place_id": "fp02-a-cool-place",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 101.4534,
	            "lat": 13.0525
            }
        """.trimIndent()

        val requestJsonUserFp03 = """
            {
	            "user_id": "userFp02",
                "place_id": "fp02-a-cool-place",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 101.4534,
	            "lat": 13.0525
            }
        """.trimIndent()

        RestAssured.given().contentType("application/json").body(requestJsonUserFp02)
                .`when`().post("saved/v1/user/userFp02/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .extract().path<String>("id")

        sleep(500) //populate FriendPick work in separate thread, need to wait for process to finish

        RestAssured.given().contentType("application/json").body(requestJsonUserFp03)
                .`when`().post("saved/v1/user/userFp03/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .extract().path<String>("id")

        sleep(500) //populate FriendPick work in separate thread, need to wait for process to finish

        RestAssured.`when`().get("friendpicks/v1/user/userFp01/friendpicks")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .and().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                        "json-schema/friendpickResponseSchema.json"))
                .and().body("result[0].user_id", equalTo("userFp01"))
                .and().body("result[0].place_id", equalTo("fp02-a-cool-place"))
                .and().body("result[0].type", equalTo("Google"))
                .and().body("result[0].friends.size()", CoreMatchers.`is`(2))
                .and().body("result[0].friends[0].friend_id", equalTo("userFp03")) //save after come first
                .and().body("result[0].friends[1].friend_id", equalTo("userFp02")) //save fist come after
    }

    @Test
    fun testUnsaveWitOnePick() {
        val contractOwnerResponseUser02 = """
            {
                "result": [
                    {
                        "id": "user01",
                        "friendly_id": "user1",
                        "friendly_id_updated": false,
                        "profile_name": "user01",
                        "profile_photo": "bphoto",
                        "email": "user1@vmd.asia",
                        "mood": "",
                        "mood_expired_at": null,
                        "onboarded": false,
                        "require_verification": true,
                        "email_verified": false,
                        "language": "en-US"
                    }
                ]
            }
            """.trimIndent()

        val userInfoResponseUser02 = """
            {
                "id": "user02",
                "friendly_id": "user02",
                "friendly_id_updated": false,
                "profile_name": "user02",
                "profile_photo": "bphoto",
                "email": "user02@vmd.asia",
                "mood": "",
                "mood_expired_at": null,
                "onboarded": false,
                "require_verification": true,
                "email_verified": false,
                "language": "en-US"
            }
            """.trimIndent()
        responseProvider!!.expect(Method.GET, "/accounts/v1/users/user02/contactowners")
                .respondWith(200,
                        "application/json", contractOwnerResponseUser02)

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/user02")
                .respondWith(200,
                        "application/json", userInfoResponseUser02)

        val requestJson = """
            {
	            "user_id": "user02",
                "place_id": "user02-place-01",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 100.4534,
	            "lat": 14.0525
            }
        """.trimIndent()

        val placeSavedId = RestAssured.given().contentType("application/json").body(requestJson)
                .`when`().post("saved/v1/user/user02/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .extract().path<String>("id")

        sleep(500) //populate FriendPick work in separate thread, need to wait for process to finish

        `when`().delete("saved/v1/user/user02/saved/$placeSavedId").then().statusCode(HttpStatus.SC_OK)

        RestAssured.`when`().get("friendpicks/v1/user/user01/friendpicks")
                .then().statusCode(HttpStatus.SC_OK)
                .and().body("result.size()", CoreMatchers.`is`(0))
    }

    @Test
    fun testUnsaveWithMultiplePick() {
        val contractOwnerResponseUserFp02 = """
            {
                "result": [
                    {
                        "id": "userFp01",
                        "friendly_id": "userFp01",
                        "friendly_id_updated": false,
                        "profile_name": "user01",
                        "profile_photo": "userFp01Photo",
                        "email": "userFp01@vmd.asia",
                        "mood": "",
                        "mood_expired_at": null,
                        "onboarded": false,
                        "require_verification": true,
                        "email_verified": false,
                        "language": "en-US"
                    }
                ]
            }
            """.trimIndent()
        val userInfoResponseUserFp02 = """
            {
                "id": "userFp02",
                "friendly_id": "user02",
                "friendly_id_updated": false,
                "profile_name": "user02",
                "profile_photo": "bphotoFp02",
                "email": "userFp02@vmd.asia",
                "mood": "",
                "mood_expired_at": null,
                "onboarded": false,
                "require_verification": true,
                "email_verified": false,
                "language": "en-US"
            }
            """.trimIndent()

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/userFp02/contactowners")
                .respondWith(200,
                        "application/json", contractOwnerResponseUserFp02)

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/userFp02")
                .respondWith(200,
                        "application/json", userInfoResponseUserFp02)

        val contractOwnerResponseUserFp03 = """
            {
                "result": [
                    {
                        "id": "userFp01",
                        "friendly_id": "userFp01",
                        "friendly_id_updated": false,
                        "profile_name": "user01",
                        "profile_photo": "userFp01Photo",
                        "email": "userFp01@vmd.asia",
                        "mood": "",
                        "mood_expired_at": null,
                        "onboarded": false,
                        "require_verification": true,
                        "email_verified": false,
                        "language": "en-US"
                    }
                ]
            }
            """.trimIndent()
        val userInfoResponseUserFp03 = """
            {
                "id": "userFp03",
                "friendly_id": "user03",
                "friendly_id_updated": false,
                "profile_name": "user03",
                "profile_photo": "bphotoFp03",
                "email": "userFp03@vmd.asia",
                "mood": "",
                "mood_expired_at": null,
                "onboarded": false,
                "require_verification": true,
                "email_verified": false,
                "language": "en-US"
            }
            """.trimIndent()

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/userFp03/contactowners")
                .respondWith(200,
                        "application/json", contractOwnerResponseUserFp03)

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/userFp03")
                .respondWith(200,
                        "application/json", userInfoResponseUserFp03)

        val requestJsonUserFp02 = """
            {
	            "user_id": "userFp02",
                "place_id": "fp02-a-cool-place",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 101.4534,
	            "lat": 13.0525
            }
        """.trimIndent()

        val requestJsonUserFp03 = """
            {
	            "user_id": "userFp02",
                "place_id": "fp02-a-cool-place",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 101.4534,
	            "lat": 13.0525
            }
        """.trimIndent()

        val userFp02SavedId = RestAssured.given().contentType("application/json").body(requestJsonUserFp02)
                .`when`().post("saved/v1/user/userFp02/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .extract().path<String>("id")

        sleep(500) //populate FriendPick work in separate thread, need to wait for process to finish

        RestAssured.given().contentType("application/json").body(requestJsonUserFp03)
                .`when`().post("saved/v1/user/userFp03/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .extract().path<String>("id")

        sleep(500) //populate FriendPick work in separate thread, need to wait for process to finish

        `when`().delete("saved/v1/user/user02/saved/$userFp02SavedId").then().statusCode(HttpStatus.SC_OK)

        RestAssured.`when`().get("friendpicks/v1/user/userFp01/friendpicks")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .and().body("result.size()", CoreMatchers.`is`(1))
                .and().body("result[0].user_id", equalTo("userFp01"))
                .and().body("result[0].friends[0].friend_id", equalTo("userFp03"))
    }

    @Test
    fun testChangeReOrderWhenUserPlaceSaveUpdate() {
        val contractOwnerResponseUserFp02 = """
            {
                "result": [
                    {
                        "id": "userFp01",
                        "friendly_id": "userFp01",
                        "friendly_id_updated": false,
                        "profile_name": "user01",
                        "profile_photo": "userFp01Photo",
                        "email": "userFp01@vmd.asia",
                        "mood": "",
                        "mood_expired_at": null,
                        "onboarded": false,
                        "require_verification": true,
                        "email_verified": false,
                        "language": "en-US"
                    }
                ]
            }
            """.trimIndent()
        val userInfoResponseUserFp02 = """
            {
                "id": "userFp02",
                "friendly_id": "user02",
                "friendly_id_updated": false,
                "profile_name": "user02",
                "profile_photo": "bphotoFp02",
                "email": "userFp02@vmd.asia",
                "mood": "",
                "mood_expired_at": null,
                "onboarded": false,
                "require_verification": true,
                "email_verified": false,
                "language": "en-US"
            }
            """.trimIndent()

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/userFp02/contactowners")
                .respondWith(200,
                        "application/json", contractOwnerResponseUserFp02)

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/userFp02")
                .respondWith(200,
                        "application/json", userInfoResponseUserFp02)

        val contractOwnerResponseUserFp03 = """
            {
                "result": [
                    {
                        "id": "userFp01",
                        "friendly_id": "userFp01",
                        "friendly_id_updated": false,
                        "profile_name": "user01",
                        "profile_photo": "userFp01Photo",
                        "email": "userFp01@vmd.asia",
                        "mood": "",
                        "mood_expired_at": null,
                        "onboarded": false,
                        "require_verification": true,
                        "email_verified": false,
                        "language": "en-US"
                    }
                ]
            }
            """.trimIndent()
        val userInfoResponseUserFp03 = """
            {
                "id": "userFp03",
                "friendly_id": "user03",
                "friendly_id_updated": false,
                "profile_name": "user03",
                "profile_photo": "bphotoFp03",
                "email": "userFp03@vmd.asia",
                "mood": "",
                "mood_expired_at": null,
                "onboarded": false,
                "require_verification": true,
                "email_verified": false,
                "language": "en-US"
            }
            """.trimIndent()

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/userFp03/contactowners")
                .respondWith(200,
                        "application/json", contractOwnerResponseUserFp03)

        responseProvider!!.expect(Method.GET, "/accounts/v1/users/userFp03")
                .respondWith(200,
                        "application/json", userInfoResponseUserFp03)

        val requestJsonUserFp02 = """
            {
	            "user_id": "userFp02",
                "place_id": "fp02-a-cool-place",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 101.4534,
	            "lat": 13.0525
            }
        """.trimIndent()

        val requestJsonUserFp03 = """
            {
	            "user_id": "userFp02",
                "place_id": "fp02-a-cool-place",
	            "type": "Google",
	            "categories": ["restaurant"],
	            "lon": 101.4534,
	            "lat": 13.0525
            }
        """.trimIndent()

        RestAssured.given().contentType("application/json").body(requestJsonUserFp02)
                .`when`().post("saved/v1/user/userFp02/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .extract().path<String>("id")

        sleep(500) //populate FriendPick work in separate thread, need to wait for process to finish

        RestAssured.given().contentType("application/json").body(requestJsonUserFp03)
                .`when`().post("saved/v1/user/userFp03/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .extract().path<String>("id")

        sleep(500) //populate FriendPick work in separate thread, need to wait for process to finish

        RestAssured.`when`().get("friendpicks/v1/user/userFp01/friendpicks")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .and().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                        "json-schema/friendpickResponseSchema.json"))
                .and().body("result[0].user_id", equalTo("userFp01"))
                .and().body("result[0].place_id", equalTo("fp02-a-cool-place"))
                .and().body("result[0].type", equalTo("Google"))
                .and().body("result[0].friends.size()", CoreMatchers.`is`(2))
                .and().body("result[0].friends[0].friend_id", equalTo("userFp03")) //save after come first
                .and().body("result[0].friends[1].friend_id", equalTo("userFp02")) //save fist come after

        // save again to get update time stamp
        RestAssured.given().contentType("application/json").body(requestJsonUserFp02)
                .`when`().post("saved/v1/user/userFp02/saved")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .extract().path<String>("id")

        sleep(500) //populate FriendPick work in separate thread, need to wait for process to finish

        RestAssured.`when`().get("friendpicks/v1/user/userFp01/friendpicks")
                .then().statusCode(HttpStatus.SC_OK).log().body()
                .and().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                        "json-schema/friendpickResponseSchema.json"))
                .and().body("result[0].user_id", equalTo("userFp01"))
                .and().body("result[0].place_id", equalTo("fp02-a-cool-place"))
                .and().body("result[0].type", equalTo("Google"))
                .and().body("result[0].friends.size()", CoreMatchers.`is`(2))
                .and().body("result[0].friends[0].friend_id", equalTo("userFp02")) //save after come first
                .and().body("result[0].friends[1].friend_id", equalTo("userFp03")) //save fist come after
    }
}