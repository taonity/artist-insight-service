package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return artists by IDs"

    request {
        method GET()
        urlPath("/v1/artists") {
            queryParameters {
                parameter("ids", "artist-with-genre,4uFZsG1vXrPcvnZ4iSQyrx,6A0sYtzqMUPBpHzVvEgOhA,21bOoXa6JISSaqYu2oYbWy,artist-without-genre")
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
                    id: "4uFZsG1vXrPcvnZ4iSQyrx",
                    name: "C418",
                    genres: [],
                    href: "https://api.spotify.com/v1/artists/4uFZsG1vXrPcvnZ4iSQyrx",
                    images: [],
                    external_urls: [spotify: "https://open.spotify.com/artist/4uFZsG1vXrPcvnZ4iSQyrx"],
                    followers: [total: 100000],
                    popularity: 75
                ],
                [
                    id: "6A0sYtzqMUPBpHzVvEgOhA",
                    name: "PATHS",
                    genres: [],
                    href: "https://api.spotify.com/v1/artists/6A0sYtzqMUPBpHzVvEgOhA",
                    images: [],
                    external_urls: [spotify: "https://open.spotify.com/artist/6A0sYtzqMUPBpHzVvEgOhA"],
                    followers: [total: 1500],
                    popularity: 35
                ],
                [
                    id: "21bOoXa6JISSaqYu2oYbWy",
                    name: "By The Spirits",
                    genres: [],
                    href: "https://api.spotify.com/v1/artists/21bOoXa6JISSaqYu2oYbWy",
                    images: [],
                    external_urls: [spotify: "https://open.spotify.com/artist/21bOoXa6JISSaqYu2oYbWy"],
                    followers: [total: 2000],
                    popularity: 40
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
                ]
            ]
        )
    }
}

