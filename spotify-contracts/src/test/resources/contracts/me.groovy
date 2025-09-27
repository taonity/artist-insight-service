package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return user"

    request {
        url "/v1/me"
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body (
            id: "3126nx54y24ryqyza3qxcchi4wry",
            display_name: "TestUser",
            images: [
                [
                    url: "https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228",
                    height: "300",
                    width: "300"
                ]
            ]
        )
    }
}