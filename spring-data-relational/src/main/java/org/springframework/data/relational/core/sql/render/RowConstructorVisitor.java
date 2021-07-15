/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.data.relational.core.sql.render;

import org.springframework.data.relational.core.sql.RowConstructor;
import org.springframework.data.relational.core.sql.Visitable;

/**
 * {@link PartRenderer} for {@link RowConstructor}.
 * 
 * @author Yunyoung LEE
 * @since 2.3
 */
class RowConstructorVisitor extends DelegatingVisitor implements PartRenderer {

	private final StringBuilder renderedPart = new StringBuilder();
	private final ExpressionVisitor expressionVisitor;
	private Visitable rootSegment;

	public RowConstructorVisitor(RenderContext context) {

		this.expressionVisitor = new ExpressionVisitor(context);
	}

	@Override
	public Delegation doEnter(Visitable segment) {

		if (rootSegment == null) {
			rootSegment = segment;
			renderedPart.append('(');
			return Delegation.retain();
		}

		if (renderedPart.length() > 1) {
			renderedPart.append(", ");
		}
		return Delegation.delegateTo(expressionVisitor);
	}

	@Override
	public Delegation doLeave(Visitable segment) {

		if (rootSegment == segment) {
			renderedPart.append(')');

			return Delegation.leave();
		}

		renderedPart.append(expressionVisitor.getRenderedPart());
		return Delegation.retain();
	}

	@Override
	public CharSequence getRenderedPart() {
		return renderedPart;
	}
}
