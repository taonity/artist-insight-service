package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should grant authorization code on user authorization request"

    request {
        urlPath("/oauth2/v2/auth") {
            queryParameters {
                parameter("response_type", "code")
                parameter("client_id", "1234e12345678fafbd90fdce7540e12e")
                parameter("scope", "user-follow-read user-read-private")
                parameter("redirect_uri", "http://localhost:9016/login/oauth2/code/spotify-artist-insight-service")
//                parameter("state", "ObcfjFZqredmQena-wKM-afjKGuCHpZ-vDNersT-XPc%3D")
            }
        }
        method GET()

    }

    def code = "AQDumADm_Lo9NowrjxjTNmSxDHAjBJyAM5SjAqgDJFctaMRqsE7Ygwh7TN4IYfJzg8Q_QAcC_EX9WVXqdoSn0OpV837TodeHH05nbnVZh6Oe701XYy8DVgaNGULoS8x-1543zAW4pfUz4t2XrN8qUaim_23IRwQzAba-a6LdlHYFPrG-E-mBy4m3j4-FQv6toBjnMwqvjwoPFSEw7cQOMeK4MWx0DWizTXgyn8j5wAQzTGtqf76BTHOBb_naWQVKBPT3otyzvhNQflFQEDCn"

    response {
        status SEE_OTHER()
        headers {
            header("Location", "${fromRequest().query("redirect_uri")}?code=${code}&state=${fromRequest().query("state")}")
        }
    }
}