import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.data.mapper.toDomain
import com.asensiodev.feature.searchmovies.impl.data.model.SearchMoviesResponseApiModel
import com.asensiodev.santoro.core.data.model.MovieApiModel
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SearchMoviesResponseApiModelMapperTest {
    @Test
    fun `GIVEN SearchMoviesResponseApiModel WHEN toDomain THEN returns expected list of Movies`() {
        val apiModel =
            SearchMoviesResponseApiModel(
                results =
                    listOf(
                        createMovieApiModel(
                            id = 1,
                            title = "Inception",
                            posterPath = "/inception.jpg",
                            overview = "A thief who steals corporate secrets through dream-sharing technology.",
                            releaseDate = "2010-07-16",
                            popularity = 8.3,
                        ),
                        createMovieApiModel(
                            id = 2,
                            title = "The Dark Knight",
                            posterPath = "/dark_knight.jpg",
                            overview = "Batman faces the Joker in Gotham City.",
                            releaseDate = "2008-07-18",
                            popularity = 9.0,
                        ),
                    ),
                page = 1,
                totalPages = 10,
                totalResults = 100,
            )

        val expectedMovies =
            listOf(
                createMovie(
                    id = 1,
                    title = "Inception",
                    posterPath = "/inception.jpg",
                    overview = "A thief who steals corporate secrets through dream-sharing technology.",
                    releaseDate = "2010-07-16",
                    popularity = 8.3,
                ),
                createMovie(
                    id = 2,
                    title = "The Dark Knight",
                    posterPath = "/dark_knight.jpg",
                    overview = "Batman faces the Joker in Gotham City.",
                    releaseDate = "2008-07-18",
                    popularity = 9.0,
                ),
            )

        val result = apiModel.toDomain()

        result shouldBeEqualTo expectedMovies
    }

    private fun createMovieApiModel(
        id: Int,
        title: String,
        posterPath: String,
        overview: String,
        releaseDate: String,
        popularity: Double,
    ): MovieApiModel =
        MovieApiModel(
            id = id,
            title = title,
            posterPath = posterPath,
            backdropPath = null,
            overview = overview,
            releaseDate = releaseDate,
            popularity = popularity,
            voteAverage = 8.8,
            voteCount = 32000,
            genres = listOf(),
            productionCountries = listOf(),
            credits = null,
            runtime = null,
        )

    private fun createMovie(
        id: Int,
        title: String,
        posterPath: String,
        overview: String,
        releaseDate: String,
        popularity: Double,
    ): Movie =
        Movie(
            id = id,
            title = title,
            posterPath = posterPath,
            backdropPath = null,
            overview = overview,
            releaseDate = releaseDate,
            popularity = popularity,
            voteAverage = 8.8,
            voteCount = 32000,
            genres = listOf(),
            productionCountries = listOf(),
            cast = emptyList(),
            isWatched = false,
            isInWatchlist = false,
        )
}
