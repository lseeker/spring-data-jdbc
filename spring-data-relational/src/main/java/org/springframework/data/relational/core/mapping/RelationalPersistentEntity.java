/*
 * Copyright 2017-2021 the original author or authors.
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
package org.springframework.data.relational.core.mapping;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.mapping.AssociationHandler;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.model.MutablePersistentEntity;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.util.Assert;

/**
 * A {@link org.springframework.data.mapping.PersistentEntity} interface with additional methods for JDBC/RDBMS related
 * metadata.
 *
 * @author Jens Schauder
 * @author Oliver Gierke
 * @author Yunyoung LEE
 */
public interface RelationalPersistentEntity<T> extends MutablePersistentEntity<T, RelationalPersistentProperty> {

	/**
	 * Returns the name of the table backing the given entity.
	 *
	 * @return the table name.
	 */
	SqlIdentifier getTableName();

	/**
	 * Returns the column representing the identifier.
	 *
	 * @return will never be {@literal null}.
	 * @deprecated since 2.3, use {@link #getIdColumns()} to support composite id.
	 */
	@Deprecated
	SqlIdentifier getIdColumn();

	/**
	 * Returns list of columns representing identifiers.
	 *
	 * @return will never be empty.
	 * @since 2.3
	 */
	default List<SqlIdentifier> getIdColumns() {
		return getRequiredIdProperties().stream().map(RelationalPersistentProperty::getColumnName)
				.collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated since 2.3, use {@link #getIdProperties()} to support composite id.
	 */
	@Override
	@Deprecated
	RelationalPersistentProperty getIdProperty();

	/**
	 * Returns list of id properties of the {@link RelationalPersistentEntity}. Can be empty in case this is an entity
	 * completely handled by a custom conversion.
	 *
	 * @return list of id properties of the {@link RelationalPersistentEntity}.
	 * @since 2.3
	 */
	default List<RelationalPersistentProperty> getIdProperties() {
		return Collections.singletonList(getIdProperty());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated since 2.3, use {@link #getRequiredIdProperties()} to support composite id.
	 */
	@Override
	@Deprecated
	default RelationalPersistentProperty getRequiredIdProperty() {
		return getRequiredIdProperties().get(0);
	}

	/**
	 * Returns list of id properties of the {@link RelationalPersistentEntity}.
	 * 
	 * @return list of id properties of the {@link RelationalPersistentEntity}.
	 * @since 2.3
	 */
	default List<RelationalPersistentProperty> getRequiredIdProperties() {
		List<RelationalPersistentProperty> properties = getIdProperties();

		if (!properties.isEmpty()) {
			return properties;
		}

		throw new IllegalStateException(String.format("Required identifier properties not found for %s!", getType()));
	}

	// copy from from data core 2.5 for A1 release
	default void doWithAll(PropertyHandler<RelationalPersistentProperty> handler) {

		Assert.notNull(handler, "PropertyHandler must not be null!");

		doWithProperties(handler);
		doWithAssociations((AssociationHandler<RelationalPersistentProperty>) association -> handler
				.doWithPersistentProperty(association.getInverse()));
	}

}
