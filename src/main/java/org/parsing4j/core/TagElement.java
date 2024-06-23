package org.parsing4j.core;

/*
 * @author Florent Guille
 * */
public abstract class TagElement {

	public int getAsInt() throws TagElementTypeMismatch {
		throw new TagElementTypeMismatch(Integer.class.getSimpleName(), this.getClass().getSimpleName());
	}

	public String getAsString() throws TagElementTypeMismatch {
		throw new TagElementTypeMismatch(String.class.getSimpleName(), this.getClass().getSimpleName());
	}

	public TagElementObject getAsObject() throws TagElementTypeMismatch {
		throw new TagElementTypeMismatch(TagElementObject.class.getSimpleName(), this.getClass().getSimpleName());
	}

	public TagElementArray getAsArray() throws TagElementTypeMismatch {
		throw new TagElementTypeMismatch(TagElementArray.class.getSimpleName(), this.getClass().getSimpleName());
	}

	public boolean getAsBoolean() throws TagElementTypeMismatch {
		throw new TagElementTypeMismatch(Boolean.class.getSimpleName(), this.getClass().getSimpleName());
	}

	@SuppressWarnings("serial")
	public static class TagElementTypeMismatch extends Exception {

		private String expectedType, foundType;

		public TagElementTypeMismatch(String expectedType, String foundType) {
			this.expectedType = expectedType;
			this.foundType = foundType;
		}

		public String getMessage() {
			return "Cannot convert %s to %s".formatted(foundType, expectedType);
		}

	}

}
