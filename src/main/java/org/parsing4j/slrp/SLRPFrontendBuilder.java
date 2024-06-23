package org.parsing4j.slrp;

import java.io.InputStream;
import java.util.List;

import org.parsing4j.core.Utils.Triplet;
import org.parsing4j.etaengine.parser.EtaParser;
import org.parsing4j.etaengine.parser.EtaParserBuilder;
import org.parsing4j.etaengine.parser.EtaParserBuilder.EtaParserBuilderEntry;
import org.parsing4j.frontend.ParserFrontend;
import org.parsing4j.tokenizer.regex.RegexTokenizer;
import org.parsing4j.tokenizer.regex.RegexTokenizerBuilder;
import org.parsing4j.tokenizer.regex.RegexTokenizerBuilder.RegexTokenizerBuilderEntry;

/*
 * @author Florent Guille
 * */
public class SLRPFrontendBuilder {

	private EtaParserBuilder parserBuilder;
	private RegexTokenizerBuilder tokenizerBuilder;

	public SLRPFrontendBuilder() {
		this.parserBuilder = new EtaParserBuilder();
		this.tokenizerBuilder = new RegexTokenizerBuilder();
	}

	public EtaParserBuilder getParserBuilder() {
		return parserBuilder;
	}

	public RegexTokenizerBuilder getTokenizerBuilder() {
		return tokenizerBuilder;
	}

	public ParserFrontend<RegexTokenizer, EtaParser> build(
			Triplet<String, List<EtaParserBuilderEntry>, List<RegexTokenizerBuilderEntry>> data) throws Exception {
		for (EtaParserBuilderEntry entry : data.middle) {
			parserBuilder.addEntry(entry);
		}

		EtaParser parser = parserBuilder.build(data.left);

		for (RegexTokenizerBuilderEntry entry : data.right) {
			tokenizerBuilder.addEntry(entry);
		}

		RegexTokenizer tokenizer = tokenizerBuilder.build(parser::getTerminal);

		return new ParserFrontend<>(tokenizer, parser);
	}

	public ParserFrontend<RegexTokenizer, EtaParser> build(InputStream stream) throws Exception {
		return build(SLRPParser.parseSLRP(stream));
	}
}
