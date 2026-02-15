package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return followed artists"

    request {
        method GET()
        urlPath("/v1/me/following") {
            queryParameters {
                parameter("type", "artist")
                parameter("limit", "50")
            }
        }
    }


    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
            artists: [
                items: [
                    [
                        id: "artist-with-genre",
                        name: "ArtistWithGenre",
                        genres: ["metal", "rock"],
                        href: "https://api.spotify.com/v1/artists/artist-with-genre",
                        images: [],
                        external_urls: [spotify: "https://open.spotify.com/artist/artist-with-genre"],
                        followers: [total: 1000],
                        popularity: 50
                    ],
                    [
                        id: "artist-without-genre",
                        name: "ArtistWithoutGenre",
                        genres: [],
                        href: "https://api.spotify.com/v1/artists/artist-without-genre",
                        images: [],
                        external_urls: [spotify: "https://open.spotify.com/artist/artist-without-genre"],
                        followers: [total: 500],
                        popularity: 30
                    ],
                    [
                        id: "21bOoXa6JISSaqYu2oYbWy",
                        name: "By The Spirits",
                        genres: [],
                        href: "https://api.spotify.com/v1/artists/artist-with-genre",
                        images: [],
                        external_urls: [spotify: "https://open.spotify.com/artist/21bOoXa6JISSaqYu2oYbWy"],
                        followers: [total: 2000],
                        popularity: 40
                    ],
                    [
                        id: "6A0sYtzqMUPBpHzVvEgOhA",
                        name: "PATHS",
                        genres: [],
                        href: "https://api.spotify.com/v1/artists/artist-with-genre",
                        images: [],
                        external_urls: [spotify: "https://open.spotify.com/artist/6A0sYtzqMUPBpHzVvEgOhA"],
                        followers: [total: 1500],
                        popularity: 35
                    ],
                    [
                        id: "4uFZsG1vXrPcvnZ4iSQyrx",
                        name: "C418",
                        genres: [],
                        href: "https://api.spotify.com/v1/artists/artist-with-genre",
                        images: [],
                        external_urls: [spotify: "https://open.spotify.com/artist/4uFZsG1vXrPcvnZ4iSQyrx"],
                        followers: [total: 100000],
                        popularity: 75
                    ]
                ],
                next: null
            ]
        )
    }
}
