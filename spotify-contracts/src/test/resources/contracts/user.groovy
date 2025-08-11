package contracts

import org.springframework.cloud.contract.spec.Contract

// NOT USED
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
            id: "12345",
            displayName: "John Doe",
            images: [
                [
                    url: "https://example.com/image.jpg"
                ]
            ]
        )
    }
}