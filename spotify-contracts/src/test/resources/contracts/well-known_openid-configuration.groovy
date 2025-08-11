package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return user"

    request {
        url "/.well-known/openid-configuration"
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body (
            issuer                               : "http://localhost:8100",
            authorization_endpoint               : "http://localhost:8100/authorize",
            token_endpoint                       : "http://localhost:8100/api/token",
            userinfo_endpoint                    : "http://localhost:8100/v1/me",
            jwks_uri                             : "http://localhost:8100/.well-known/jwks.json",
            response_types_supported             : ["code"],
            subject_types_supported              : ["public"],
            id_token_signing_alg_values_supported: ["RS256"],
            scopes_supported                     : [
                    "user-read-private",
                    "user-read-email"
            ]
        )
    }
}