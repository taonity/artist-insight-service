package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should exchange authorization code for access token"

    request {
        url "/api/token"
        method POST()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            body(
                    access_token: "BQDumADm_Lo9NowrjxjTNmSxDHAjBJyAM5SjAqgDJFctaMRqsE7Ygwh7TN4IYfJzg8Q_QAcC_EX9WVXqdoSn0OpV837TodeHH05nbnVZh6Oe701XYy8DVgaNGULoS8x-1543zAW4pfUz4t2XrN8qUaim_23IRwQzAba-a6LdlHYFPrG-E-mBy4m3j4-FQv6toBjnMwqvjwoPFSEw7cQOMeK4MWx0DWizTXgyn8j5wAQzTGtqf76BTHOBb_naWQVKBPT3otyzvhNQflFQEDCn",
                    token_type: "Bearer",
                    scope: "user-read-private user-read-email",
                    expires_in: 3600,
                    refresh_token: "AQDumADm_Lo9NowrjljTNmSxDHAjBJyAM5SjAqgDJFctaMRqsE7Ygwh7TN4IYfJzg8Q_QAcC_EX9WVXqdoSn0OpV837TodeHH05nbnVZh6Oe701XYy8DVgaNGULoS8x-1543zAW4pfUz4t2XrN8qUaim_23IRwQzAba-a6LdlHYFPrG-E-mBy4m3j4-FQv6toBjnMwqvjwoPFSEw7cQOMeK4MWx0DWizTXgyn8j5wAQzTGtqf76BTHOBb_naWQVKBPT3otyzvhNQflFQEDCn"
            )
        }
    }
}