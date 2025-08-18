package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should exchange authorization code for access token"

    request {
        url "/api/token"
        method POST()
//        headers {
//            header('Content-Type', 'application/x-www-form-urlencoded;charset=UTF-8')
//            header('Accept', 'application/json;charset=UTF-8')
//            header('Authorization', 'Basic MTIzNGUxMjM0NTY3OGZhZmJkOTBmZGNlNzU0MGUxMmU6MTJiMTIzYjdjMmVhNGNmMjhhYmM4M2YzZTEyMzQ1ZGU=')
//        }
//        body(
//            code: $(consumer(regex('[A-Za-z0-9]+')), producer('validCode')),
//            redirect_uri: $(consumer(regex('https://.+')), producer('https://example.com/callback')),
//            grant_type: $(consumer('authorization_code'), producer('authorization_code'))
//        )
//        body(
//                grant_type: 'authorization_code',
//                code: "AQDumADm_Lo9NowrjxjTNmSxDHAjBJyAM5SjAqgDJFctaMRqsE7Ygwh7TN4IYfJzg8Q_QAcC_EX9WVXqdoSn0OpV837TodeHH05nbnVZh6Oe701XYy8DVgaNGULoS8x-1543zAW4pfUz4t2XrN8qUaim_23IRwQzAba-a6LdlHYFPrG-E-mBy4m3j4-FQv6toBjnMwqvjwoPFSEw7cQOMeK4MWx0DWizTXgyn8j5wAQzTGtqf76BTHOBb_naWQVKBPT3otyzvhNQflFQEDCn",
//                redirect_uri: "http://localhost:9016/login/oauth2/code/spotify-artist-insight-service", no :9016
//        )
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