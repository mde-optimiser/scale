/*
 * generated by Xtext 2.18.0
 */
package uk.ac.kcl.inf.mdeoptimiser.languages.parser.antlr;

import com.google.inject.Inject;
import org.eclipse.xtext.parser.antlr.AbstractAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import uk.ac.kcl.inf.mdeoptimiser.languages.parser.antlr.internal.InternalScaleParser;
import uk.ac.kcl.inf.mdeoptimiser.languages.services.ScaleGrammarAccess;

public class ScaleParser extends AbstractAntlrParser {

	@Inject
	private ScaleGrammarAccess grammarAccess;

	@Override
	protected void setInitialHiddenTokens(XtextTokenStream tokenStream) {
		tokenStream.setInitialHiddenTokens("RULE_WS", "RULE_ML_COMMENT", "RULE_SL_COMMENT");
	}
	

	@Override
	protected InternalScaleParser createParser(XtextTokenStream stream) {
		return new InternalScaleParser(stream, getGrammarAccess());
	}

	@Override 
	protected String getDefaultRuleName() {
		return "Scale";
	}

	public ScaleGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}

	public void setGrammarAccess(ScaleGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
}