/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toedter.spring.hateoas.jsonapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toedter.spring.hateoas.jsonapi.support.Director;
import com.toedter.spring.hateoas.jsonapi.support.Movie;
import org.junit.jupiter.api.*;
import org.springframework.hateoas.*;

import java.util.ArrayList;
import java.util.List;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiModelBuilder Test")
public class JsonApiModelBuilderTest extends AbstractJsonApiTest {
    private ObjectMapper mapper;

    @BeforeEach
    void setUpModule() {
        JsonApiMediaTypeConfiguration configuration = new JsonApiMediaTypeConfiguration();
        mapper = new ObjectMapper();
        configuration.configureObjectMapper(mapper);
    }

    @Test
    void should_build_empty_model() throws Exception {
        final RepresentationModel<?> jsonApiModel = jsonApiModel().build();
        final String emptyDoc = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(emptyDoc, "emptyDoc.json");
    }

    @Test
    void should_build_empty_entity_model() throws Exception {
        final RepresentationModel<?> jsonApiModel = jsonApiModel().entity(new Object()).build();
        final String emptyDoc = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(emptyDoc, "emptyDoc.json");
    }

    @Test
    void should_build_single_movie_model() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        final RepresentationModel<?> jsonApiModel = jsonApiModel().entity(movie).build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void should_build_single_movie_entity_model() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        final RepresentationModel<?> jsonApiModel = jsonApiModel().entity(EntityModel.of(movie)).build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void should_build_single_movie_model_with_relationship() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        Director director = new Director("1", "George Lucas");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel().entity(movie)
                        .relationship("directors", EntityModel.of(director))
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithRelationship.json");
    }

    @Test
    void should_build_single_movie_model_with_many_relationships() throws Exception {
        Movie movie = new Movie("4", "The Matrix");
        Movie relatedMovie = new Movie("2", "The Matrix 2");
        Director director1 = new Director("1", "Lana Wachowski");
        Director director2 = new Director("2", "Lilly Wachowski");

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel().entity(movie)
                        .relationship("directors", director1)
                        .relationship("directors", EntityModel.of(director2))
                        .relationship("relatedMovies", EntityModel.of(relatedMovie))
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithManyRelationships.json");
    }

    @Test
    void should_build_single_movie_model_with_many_relationships_and_included() throws Exception {
        Movie movie = new Movie("1", "The Matrix");
        Movie relatedMovie = new Movie("2", "The Matrix 2");
        Director director1 = new Director("1", "Lana Wachowski");
        Director director2 = new Director("2", "Lilly Wachowski");

        Movie movie2 = new Movie("2", "Star Wars");

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel().entity(movie)
                        .relationship("directors", director1)
                        .relationship("directors", director2)
                        .relationship("relatedMovies", EntityModel.of(relatedMovie))
                        .included(director1)
                        .included(director2)
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithManyRelationshipsAndIncluded.json");
    }

    @Test
    void should_build_paged_movie_model_with_many_relationships_and_included() throws Exception {
        Movie movie = new Movie("1", "The Matrix");
        Movie relatedMovie = new Movie("2", "The Matrix 2");
        Director director1 = new Director("1", "Lana Wachowski");
        final EntityModel<Director> director1EntityModel = EntityModel.of(director1);
        Director director2 = new Director("2", "Lilly Wachowski");
        final EntityModel<Director> director2EntityModel = EntityModel.of(director2);

        final RepresentationModel<?> jsonApiModel1 =
                jsonApiModel()
                        .entity(movie)
                        .relationship("directors", director1EntityModel)
                        .relationship("directors", director2EntityModel)
                        .relationship("relatedMovies", EntityModel.of(relatedMovie))
                        .build();

        Movie movie2 = new Movie("3", "Star Wars");
        Director director3 = new Director("3", "George Lucas");
        final EntityModel<Director> director3EntityModel = EntityModel.of(director3);

        final RepresentationModel<?> jsonApiModel2 =
                jsonApiModel()
                        .entity(movie2)
                        .relationship("directors", director3EntityModel)
                        .build();

        List<RepresentationModel<?>> movies = new ArrayList<>();
        movies.add(jsonApiModel1);
        movies.add(jsonApiModel2);

        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(2, 1, 2, 2);
        Link nextLink = Link.of("http://localhost/movies?page[number]=2&page[size]=2").withRel(IanaLinkRelations.NEXT);
        final PagedModel<RepresentationModel<?>> pagedModel = PagedModel.of(movies, pageMetadata);

        RepresentationModel<?> pagedJasonApiModel =
                jsonApiModel()
                        .entity(pagedModel)
                        .included(director1EntityModel)
                        .included(director2EntityModel)
                        .link(nextLink)
                        .build();

        final String pagedModelJson = mapper.writeValueAsString(pagedJasonApiModel);
        compareWithFile(pagedModelJson, "moviesPagedJsonApiModelWithIncluded.json");
    }
}
