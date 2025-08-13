package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return configuration information about OpenID Connect Identity Provider (IdP)"

    request {
        url "/.well-known/openid-configuration"
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }

        // see https://accounts.spotify.com/.well-known/openid-configuration
        body (
            issuer                               : "http://localhost:8100",
            authorization_endpoint               : "http://localhost:8100/oauth2/v2/auth",
            token_endpoint                       : "http://localhost:8100/api/token",
            userinfo_endpoint                    : "http://localhost:8100/v1/me",
            response_types_supported             : ["code", "none"],
            subject_types_supported              : ["pairwise"],
            jwks_uri                             : "https://accounts.spotify.com/oidc/certs/v1",
        )
    }
}