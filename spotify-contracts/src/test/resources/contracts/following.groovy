package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return followed artists"

    request {
        method GET()
        url "/v1/me/following?type=artist"
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
                    ]
                ],
                next: null
            ]
        )
    }
}
