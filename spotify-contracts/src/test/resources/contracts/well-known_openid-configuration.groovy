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
        // TODO: make the base url configurable somehow, required for the run in container
        body (
            issuer                               : "http://127.0.0.1:8100",
            authorization_endpoint               : "http://127.0.0.1:8100/oauth2/v2/auth",
            token_endpoint                       : "http://127.0.0.1:8100/api/token",
            userinfo_endpoint                    : "http://127.0.0.1:8100/v1/me",
            response_types_supported             : ["code", "none"],
            subject_types_supported              : ["pairwise"],
            jwks_uri                             : "https://accounts.spotify.com/oidc/certs/v1",
        )
    }
}