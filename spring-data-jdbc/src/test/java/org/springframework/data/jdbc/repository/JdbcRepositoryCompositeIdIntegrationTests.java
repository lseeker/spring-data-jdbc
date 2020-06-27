/*
 * Copyright 2017-2020 the original author or authors.
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
package org.springframework.data.jdbc.repository;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import lombok.Data;
import lombok.Value;

import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jdbc.repository.support.JdbcRepositoryFactory;
import org.springframework.data.jdbc.testing.TestConfiguration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests repository that have composite id entity. Contains basic tests in JdbcRepositoryIntegrationTests.
 * 
 * @author Yunyoung LEE
 */
@Transactional
public class JdbcRepositoryCompositeIdIntegrationTests {

	@Configuration
	@Import(TestConfiguration.class)
	static class Config {

		@Autowired JdbcRepositoryFactory factory;

		@Bean
		Class<?> testClass() {
			return JdbcRepositoryCompositeIdIntegrationTests.class;
		}

		@Bean
		DummyEntityRepository dummyEntityRepository() {
			return factory.getRepository(DummyEntityRepository.class);
		}

	}

	@ClassRule public static final SpringClassRule classRule = new SpringClassRule();
	@Rule public SpringMethodRule methodRule = new SpringMethodRule();

	@Autowired DummyEntityRepository repository;

	@Test // DATAJDBC-352
	public void savesAsEntity() {

		repository.save(createDummyEntity("id1", "id2"));
		assertThat(repository.count()).isEqualTo(1);
	}

	@Test // DATAJDBC-352
	public void saveAndLoadAnEntity() {

		DummyEntity entity = repository.save(createDummyEntity("id1", "id2"));

		assertThat(repository.findById(entity.getId())).hasValueSatisfying(it -> {

			assertThat(it.getId1()).isEqualTo(entity.getId1());
			assertThat(it.getId2()).isEqualTo(entity.getId2());
			assertThat(it.getName()).isEqualTo(entity.getName());
		});
	}

	@Test // DATAJDBC-352
	public void savesManyEntities() {

		DummyEntity entity = createDummyEntity("id1", "id2");
		DummyEntity other = createDummyEntity("id1", "id3");

		repository.saveAll(asList(entity, other));

		assertThat(repository.findAll()) //
				.extracting(DummyEntity::getId2) //
				.containsExactlyInAnyOrder(entity.getId2(), other.getId2());
	}

	@Test // DATAJDBC-352
	public void existsReturnsTrueIffEntityExists() {

		DummyEntity entity = repository.save(createDummyEntity("id1", "id2"));

		assertThat(repository.existsById(entity.getId())).isTrue();
		assertThat(repository.existsById(DummyEntityKey.of("id1", "id3"))).isFalse();
	}

	@Test // DATAJDBC-352
	public void findAllFindsAllEntities() {

		DummyEntity entity = repository.save(createDummyEntity("id1", "id2"));
		DummyEntity other = repository.save(createDummyEntity("id1", "id3"));

		Iterable<DummyEntity> all = repository.findAll();

		assertThat(all)//
				.extracting(DummyEntity::getId2)//
				.containsExactlyInAnyOrder(entity.getId2(), other.getId2());
	}

	@Test // DATAJDBC-352
	public void findAllFindsAllSpecifiedEntities() {

		DummyEntity entity = repository.save(createDummyEntity("id1", "id2"));
		DummyEntity other = repository.save(createDummyEntity("id1", "id3"));

		assertThat(repository.findAllById(asList(entity.getId(), other.getId())))//
				.extracting(DummyEntity::getId)//
				.containsExactlyInAnyOrder(entity.getId(), other.getId());
	}

	@Test // DATAJDBC-352
	public void countsEntities() {

		repository.save(createDummyEntity("id1", "id2"));
		repository.save(createDummyEntity("id1", "id3"));
		repository.save(createDummyEntity("id2", "id2"));

		assertThat(repository.count()).isEqualTo(3L);
	}

	@Test // DATAJDBC-352
	public void deleteById() {

		DummyEntity one = repository.save(createDummyEntity("id1", "id2"));
		DummyEntity two = repository.save(createDummyEntity("id1", "id3"));
		DummyEntity three = repository.save(createDummyEntity("id2", "id2"));

		repository.deleteById(two.getId());

		assertThat(repository.findAll()) //
				.extracting(DummyEntity::getId) //
				.containsExactlyInAnyOrder(one.getId(), three.getId());
	}

	@Test // DATAJDBC-352
	public void deleteByEntity() {

		DummyEntity one = repository.save(createDummyEntity("id1", "id2"));
		DummyEntity two = repository.save(createDummyEntity("id1", "id3"));
		DummyEntity three = repository.save(createDummyEntity("id2", "id2"));

		repository.delete(one);

		assertThat(repository.findAll()) //
				.extracting(DummyEntity::getId) //
				.containsExactlyInAnyOrder(two.getId(), three.getId());
	}

	@Test // DATAJDBC-352
	public void deleteByList() {

		DummyEntity one = repository.save(createDummyEntity("id1", "id2"));
		DummyEntity two = repository.save(createDummyEntity("id1", "id3"));
		DummyEntity three = repository.save(createDummyEntity("id2", "id2"));

		repository.deleteAll(asList(one, three));

		assertThat(repository.findAll()) //
				.extracting(DummyEntity::getId) //
				.containsExactlyInAnyOrder(two.getId());
	}

	@Test // DATAJDBC-352
	public void deleteAll() {

		repository.save(createDummyEntity("id1", "id2"));
		repository.save(createDummyEntity("id1", "id3"));
		repository.save(createDummyEntity("id2", "id2"));

		assertThat(repository.findAll()).isNotEmpty();

		repository.deleteAll();

		assertThat(repository.findAll()).isEmpty();
	}

	@Test // DATAJDBC-352
	public void update() {

		DummyEntity entity = repository.save(createDummyEntity("id1", "id2"));

		entity.setName("something else");
		entity.setNew(false);
		DummyEntity saved = repository.save(entity);

		assertThat(repository.findById(entity.getId())).hasValueSatisfying(it -> {
			assertThat(it.getName()).isEqualTo(saved.getName());
		});
	}

	@Test // DATAJDBC-352
	public void updateMany() {

		DummyEntity entity = repository.save(createDummyEntity("id1", "id2"));
		DummyEntity other = repository.save(createDummyEntity("id1", "id3"));

		entity.setName("something else");
		entity.setNew(false);
		other.setName("others Name");
		other.setNew(false);

		repository.saveAll(asList(entity, other));

		assertThat(repository.findAll()) //
				.extracting(DummyEntity::getName) //
				.containsExactlyInAnyOrder(entity.getName(), other.getName());
	}

	@Test // DATAJDBC-352
	public void findByIdReturnsEmptyWhenNoneFound() {

		// NOT saving anything, so DB is empty

		assertThat(repository.findById(DummyEntityKey.of("id1", "id2"))).isEmpty();
	}

	@Test // DATAJDBC-352
	public void existsWorksAsExpected() {

		DummyEntity dummy = repository.save(createDummyEntity("id1", "id2"));

		assertSoftly(softly -> {

			softly.assertThat(repository.existsByName(dummy.getName())) //
					.describedAs("Positive") //
					.isTrue();
			softly.assertThat(repository.existsByName("not an existing name")) //
					.describedAs("Positive") //
					.isFalse();
		});
	}

	@Test // DATAJDBC-352
	public void countByQueryDerivation() {

		DummyEntity one = createDummyEntity("id1", "id2");
		DummyEntity two = createDummyEntity("id2", "id3");
		two.name = "other";
		DummyEntity three = createDummyEntity("id2", "id2");

		repository.saveAll(asList(one, two, three));

		assertThat(repository.countByName(one.getName())).isEqualTo(2);
	}

	@Test // DATAJDBC-352
	public void selectByQueryDerivation() {

		DummyEntity one = createDummyEntity("id1", "id2");
		DummyEntity two = createDummyEntity("id2", "id3");
		two.name = "other";
		DummyEntity three = createDummyEntity("id2", "id2");

		repository.saveAll(asList(one, two, three));

		assertThat(repository.findAllByName(one.getName()).size()).isEqualTo(2);
	}

	private static DummyEntity createDummyEntity(String id1, String id2) {

		DummyEntity entity = new DummyEntity(id1, id2, "name");
		entity.setNew(true);

		return entity;
	}

	interface DummyEntityRepository extends CrudRepository<DummyEntity, DummyEntityKey> {

		boolean existsByName(String name);

		int countByName(String name);

		List<DummyEntity> findAllByName(String name);
	}

	@Data
	static class DummyEntity implements Persistable<DummyEntityKey> {
		@Id String id1;
		@Id String id2;

		String name;

		@Transient boolean isNew;

		public DummyEntity(String id1, String id2, String name) {
			this.id1 = id1;
			this.id2 = id2;
			this.name = name;
		}

		@Override
		public DummyEntityKey getId() {
			return DummyEntityKey.of(id1, id2);
		}
	}

	@Value(staticConstructor = "of")
	static class DummyEntityKey {
		String id1;
		String id2;
	}
}
