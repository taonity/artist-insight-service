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
                        images: []
                    ],
                    [
                        id: "artist-without-genre",
                        name: "ArtistWithoutGenre",
                        genres: [],
                        href: "https://api.spotify.com/v1/artists/artist-without-genre",
                        images: []
                    ],
                    [
                        id: "21bOoXa6JISSaqYu2oYbWy",
                        name: "By The Spirits",
                        genres: [],
                        href: "https://api.spotify.com/v1/artists/artist-with-genre",
                        images: []
                    ],
                    [
                        id: "6A0sYtzqMUPBpHzVvEgOhA",
                        name: "PATHS",
                        genres: [],
                        href: "https://api.spotify.com/v1/artists/artist-with-genre",
                        images: []
                    ],
                    [
                        id: "4uFZsG1vXrPcvnZ4iSQyrx",
                        name: "C418",
                        genres: [],
                        href: "https://api.spotify.com/v1/artists/artist-with-genre",
                        images: []
                    ]
                ],
                next: null
            ]
        )
    }
}
