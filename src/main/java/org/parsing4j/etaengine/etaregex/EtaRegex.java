package org.parsing4j.etaengine.etaregex;

import java.util.List;

public abstract class EtaRegex {

	public abstract List<EtaRegex> getChildren();

	public abstract String getPrettyRepr();

	@Override
	public String toString() {
		return getPrettyRepr();
	}
}
