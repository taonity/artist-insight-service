package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should oauth2 authorize"

    request {
        url "/authorize"
        method GET()
    }

    response {
        status OK()
        headers {
            header("Location", "http://localhost:8100/login/oauth2/code/spotify-artist-insight-service?code=AQDumADm_Lo9NowrjxjTNmSxDHAjBJyAM5SjAqgDJFctaMRqsE7Ygwh7TN4IYfJzg8Q_QAcC_EX9WVXqdoSn0OpV837TodeHH05nbnVZh6Oe701XYy8DVgaNGULoS8x-1543zAW4pfUz4t2XrN8qUaim_23IRwQzAba-a6LdlHYFPrG-E-mBy4m3j4-FQv6toBjnMwqvjwoPFSEw7cQOMeK4MWx0DWizTXgyn8j5wAQzTGtqf76BTHOBb_naWQVKBPT3otyzvhNQflFQEDCn&state=KETSK-BFPxQmb6IqDEZnlpnYKkKFnRi67iipoQ7eZCY%3D")
        }
    }
}