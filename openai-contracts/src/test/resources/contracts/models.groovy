package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return models"

    request {
        url "/v1/models"
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            body(
                object: "list",
                data: [
                    [
                        id      : "model-id-0",
                        object  : "model",
                        created : 1686935002,
                        owned_by: "organization-owner"
                    ],
                    [
                        id      : "model-id-1",
                        object  : "model",
                        created : 1686935002,
                        owned_by: "organization-owner"
                    ],
                    [
                        id      : "model-id-2",
                        object  : "model",
                        created : 1686935002,
                        owned_by: "openai"
                    ]
                ]
            )
        }
    }
}