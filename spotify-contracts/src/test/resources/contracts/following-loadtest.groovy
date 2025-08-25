package contracts

import org.springframework.cloud.contract.spec.Contract

// Configuration variables
def totalFollowings = 500 // Set how many followings to return
def withGenreRatio = 0.3 // Set ratio of followings with genres (e.g., 0.3 means 30% with genres)

def withGenreCount = (totalFollowings * withGenreRatio).toInteger()
def withoutGenreCount = totalFollowings - withGenreCount

def items = []

// Generate artists with genres
(1..withGenreCount).each { idx ->
    items << [
        id: "artist-with-genre-$idx",
        name: "ArtistWithGenre$idx",
        genres: ["genreA", "genreB"],
        href: "https://api.spotify.com/v1/artists/artist-with-genre-$idx",
        images: []
    ]
}

// Generate artists without genres
(1..withoutGenreCount).each { idx ->
    items << [
        id: "artist-without-genre-$idx",
        name: "ArtistWithoutGenre$idx",
        genres: [],
        href: "https://api.spotify.com/v1/artists/artist-without-genre-$idx",
        images: []
    ]
}

Contract.make {
    description "should return followed artists (load test, configurable count and genre ratio)"

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
                items: items,
                next: null
            ]
        )
    }
}
