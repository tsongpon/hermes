# Hermes
Place and user's favorite place API.

Responsible for:

 - store user's favorite places.
 - store place's detail, name, location, category.
 - answer question "What are the places that my friends went to"

![enter image description here](https://bitbucket.org/vmdigital/hermes/raw/master/diagram/VMD%20Architecture.png)

**Requirements**

 - kotlin
 - mongodb
 - gradle

 **Set Environment variable**

    export USERSERVICE_URL=http://localhost:8080
    export MONGO_HOST=127.0.0.1

 **Build service**

 Run this command to build and test hermes

    gradle build

 **Run service**

 Run this command to run hanuman

    ./gradlew bootRun

  or

    docker-compose up

 **API(s)**

 save user's place

    POST: http://localhost:9002/saved/v1/user/{userId}/saved

payload

    {
        "user_id": "user-01",
        "saved_id": "1e285b15-ec02-4870-80f8-b1fc4f8196q2",
        "type": "Google",
        "categories": ["restaurant"],
        "lon": 100.4534,
        "lat": 14.0525
    }

 get user's place

    GET: http://localhost:9002/saved/v1/user/{userId}/saved

 response

    {
        "result": [
            {
                "id": "dd39cf2a-44b4-45dc-b8da-20183a53577c",
                "user_id": "123",
                "saved_id": "1e285b15-ec02-4870-80f8-b1fc4f8196q2",
                "type": "Google",
                "categories": [
                    "restaurant"
                ],
                "create_at": 1524559438867,
                "update_at": 1524559438867,
                "lon": 100.4534,
                "lat": 14.0525
            }
        ],
        "next": null,
        "previous": null,
        "first": "/saved/v1/user/123/saved?page=1&size=30",
        "last": "/saved/v1/user/123/saved?page=1&size=30"
    }