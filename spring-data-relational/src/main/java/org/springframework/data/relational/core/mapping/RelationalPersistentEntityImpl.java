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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.data.util.Lazy;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.StringUtils;

/**
 * Meta data a repository might need for implementing persistence operations for instances of type {@code T}
 *
 * @author Jens Schauder
 * @author Greg Turnquist
 * @author Bastian Wilhelm
 * @author Yunyoung LEE
 */
class RelationalPersistentEntityImpl<T> extends BasicPersistentEntity<T, RelationalPersistentProperty>
		implements RelationalPersistentEntity<T> {

	private final NamingStrategy namingStrategy;
	private final Lazy<Optional<SqlIdentifier>> tableName;
	private boolean forceQuote = true;
	private final List<RelationalPersistentProperty> idProperties = new ArrayList<>();

	/**
	 * Creates a new {@link RelationalPersistentEntityImpl} for the given {@link TypeInformation}.
	 *
	 * @param information must not be {@literal null}.
	 */
	RelationalPersistentEntityImpl(TypeInformation<T> information, NamingStrategy namingStrategy) {

		super(information);

		this.namingStrategy = namingStrategy;
		this.tableName = Lazy.of(() -> Optional.ofNullable( //
				findAnnotation(Table.class)) //
				.map(Table::value) //
				.filter(StringUtils::hasText) //
				.map(this::createSqlIdentifier) //
		);
	}

	private SqlIdentifier createSqlIdentifier(String name) {
		return isForceQuote() ? SqlIdentifier.quoted(name) : SqlIdentifier.unquoted(name);
	}

	private SqlIdentifier createDerivedSqlIdentifier(String name) {
		return new DerivedSqlIdentifier(name, isForceQuote());
	}

	public boolean isForceQuote() {
		return forceQuote;
	}

	public void setForceQuote(boolean forceQuote) {
		this.forceQuote = forceQuote;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.relational.mapping.model.RelationalPersistentEntity#getTableName()
	 */
	@Override
	public SqlIdentifier getTableName() {
		return tableName.get().orElseGet(() -> {

			String schema = namingStrategy.getSchema();
			SqlIdentifier tableName = createDerivedSqlIdentifier(namingStrategy.getTableName(getType()));

			return StringUtils.hasText(schema) ? SqlIdentifier.from(createDerivedSqlIdentifier(schema), tableName)
					: tableName;
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.relational.core.mapping.model.RelationalPersistentEntity#getIdColumn()
	 */
	@Override
	@Deprecated
	public SqlIdentifier getIdColumn() {
		return getRequiredIdProperty().getColumnName();
	}

	@Override
	public void addPersistentProperty(RelationalPersistentProperty property) {
		super.addPersistentProperty(property);

		if (property.isIdProperty()) {
			idProperties.add(property);
		}
	}

	@Override
	@Deprecated
	public RelationalPersistentProperty getIdProperty() {
		return idProperties.stream().findFirst().orElse(null);
	}

	@Override
	public List<RelationalPersistentProperty> getIdProperties() {
		return Collections.unmodifiableList(idProperties);
	}

	@Override
	public boolean hasIdProperty() {
		return !idProperties.isEmpty();
	}

	@Override
	public boolean isIdProperty(PersistentProperty<?> property) {
		return idProperties.contains(property);
	}

	@Override
	protected RelationalPersistentProperty returnPropertyIfBetterIdPropertyCandidateOrNull(
			RelationalPersistentProperty property) {
		// always return null for composite id handling
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("RelationalPersistentEntityImpl<%s>", getType());
	}
}
