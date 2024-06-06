package org.parsing4j.tokenizer.regex;

import org.parsing4j.core.IntIdentifiable;
import org.parsing4j.tokenizer.regex.structure.RegexRangeTree;

public class RegexTokenizerNode extends IntIdentifiable{
	
	private RegexRangeTree tree;
	
	public RegexTokenizerNode(int id) {
		super(id);
		this.tree = new RegexRangeTree();
	}
	
	public RegexRangeTree getTree() {
		return tree;
	}

}
