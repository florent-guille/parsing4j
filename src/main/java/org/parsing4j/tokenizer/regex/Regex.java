package org.parsing4j.tokenizer.regex;

import java.util.List;

public abstract class Regex {

	public abstract List<Regex> getChildren();

	public abstract String getPrettyRepr();

}
